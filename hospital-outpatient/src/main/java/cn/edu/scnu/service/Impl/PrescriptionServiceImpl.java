package cn.edu.scnu.service.Impl;

import cn.edu.scnu.dto.PrescriptionDTO;
import cn.edu.scnu.dto.PrescriptionDetailDTO;
import cn.edu.scnu.entity.*;
import cn.edu.scnu.enums.RegistrationStatus;
import cn.edu.scnu.exception.BusinessException;
import cn.edu.scnu.mapper.DrugMapper;
import cn.edu.scnu.mapper.PrescriptionDetailMapper;
import cn.edu.scnu.mapper.PrescriptionMapper;
import cn.edu.scnu.mapper.RegMapper;
import cn.edu.scnu.mapper.DoctorMapper;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.PrescriptionService;
import cn.edu.scnu.vo.PrescriptionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    /** Redis key 前缀：处方库存锁定 */
    private static final String STOCK_LOCK_PREFIX = "prescription:stock_lock:";
    /** 库存锁定超时时间（分钟） */
    private static final long STOCK_LOCK_TIMEOUT_MINUTES = 30;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private PrescriptionDetailMapper detailMapper;

    @Autowired
    private RegMapper regMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<PrescriptionVO> add(PrescriptionDTO dto) {

        // 1. 校验挂号状态
        Registration reg = regMapper.selectById(dto.getRegId());
        if (reg == null) {
            throw new BusinessException(400, "挂号记录不存在");
        }
        if (!reg.getStatus().equals(RegistrationStatus.IN_PROGRESS.getDisplayName())) {
            throw new BusinessException(400, "挂号状态异常，只有处于「就诊中」的患者才能开具处方");
        }

        // 1.2 防止 reg_id 唯一索引冲突 (一条挂号记录仅能对应一张电子处方)
        Long prescCount = prescriptionMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getRegId, dto.getRegId()));
        if (prescCount > 0) {
            throw new BusinessException(400, "该就诊记录已开立过电子处方，请勿重复开具！");
        }

        // 2. 查询医生诊疗费
        Doctor doctor = doctorMapper.selectById(reg.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(400, "医生信息不存在");
        }
        BigDecimal consultFee = doctor.getConsultationFee();

        // 3. 校验库存并计算费用
        BigDecimal drugTotal = BigDecimal.ZERO;
        List<PrescriptionDetail> details = new ArrayList<>();
        List<Drug> drugsToDeduct = new ArrayList<>();

        for (PrescriptionDetailDTO item : dto.getDetails()) {
            Drug drug = drugMapper.selectById(item.getDrugId());
            if (drug == null) {
                throw new BusinessException(400, "药品ID " + item.getDrugId() + " 不存在");
            }
            if (drug.getStock() < item.getQuantity()) {
                throw new BusinessException(400, "药品「" + drug.getName() + "」库存不足");
            }
            BigDecimal subTotal = drug.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            drugTotal = drugTotal.add(subTotal);
            details.add(buildDetails(item, drug, subTotal));

            // 预扣库存
            drug.setStock(drug.getStock() - item.getQuantity());
            drugsToDeduct.add(drug);
        }

        BigDecimal totalFee = consultFee.add(drugTotal);

        // 4. 扣减数据库库存
        for (Drug drug : drugsToDeduct) {
            drugMapper.updateById(drug);
        }

        // 5. 保存处方
        Prescription presc = new Prescription();
        presc.setRegId(dto.getRegId());
        presc.setDoctorId(dto.getDoctorId() != null ? dto.getDoctorId() : reg.getDoctorId());
        presc.setPatientId(dto.getPatientId() != null ? dto.getPatientId() : reg.getPatientId());
        presc.setSymptoms(dto.getSymptoms());
        presc.setDiagnosis(dto.getDiagnosis());
        presc.setConsultationFee(consultFee);
        presc.setTotalFee(totalFee);
        prescriptionMapper.insert(presc);
        log.info("开具处方：regId={}, patientId={}, totalFee={}",
                dto.getRegId(), presc.getPatientId(), totalFee);

        // 6. 保存明细（回写 prescriptionId）
        final Long prescId = presc.getId();
        details.forEach(d -> {
            d.setPrescriptionId(prescId);
            detailMapper.insert(d);
        });

        // 7. 更新挂号状态为"已完成"
        reg.setStatus(RegistrationStatus.COMPLETED.getDisplayName());
        regMapper.updateById(reg);

        // 8. 在 Redis 中记录库存锁定信息，设置超时时间
        //    Key: prescription:stock_lock:{prescriptionId}
        //    Value: prescriptionId（用于超时恢复时定位处方明细）
        String lockKey = STOCK_LOCK_PREFIX + prescId;
        redisTemplate.opsForValue().set(lockKey, String.valueOf(prescId),
                STOCK_LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        log.info("库存已锁定：prescriptionId={}, 超时={}分钟", prescId, STOCK_LOCK_TIMEOUT_MINUTES);

        // 9. 返回 VO
        PrescriptionVO vo = toVo(presc, details);
        return Result.success(vo);
    }

    /**
     * 构建处方明细实体（不含 prescriptionId，由 insert 时回写）
     */
    private PrescriptionDetail buildDetails(PrescriptionDetailDTO item, Drug drug, BigDecimal subtotal) {
        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setDrugId(item.getDrugId());
        detail.setQuantity(item.getQuantity());
        detail.setPrice(drug.getPrice());
        detail.setDosage(item.getDosage());
        detail.setSubtotal(subtotal);
        return detail;
    }

    /**
     * 将 Prescription + 明细列表 转为 PrescriptionVO
     */
    private PrescriptionVO toVo(Prescription presc, List<PrescriptionDetail> details) {
        PrescriptionVO vo = new PrescriptionVO();
        BeanUtils.copyProperties(presc, vo);
        vo.setDetails(details);
        return vo;
    }
}
