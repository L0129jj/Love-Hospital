package cn.edu.scnu.service.Impl;

import cn.edu.scnu.auth.entity.Patient;
import cn.edu.scnu.auth.mapper.PatientMapper;
import cn.edu.scnu.dto.ArchiveDTO;
import cn.edu.scnu.entity.*;
import cn.edu.scnu.mapper.*;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.ArchiveService;
import cn.edu.scnu.vo.ArchiveVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ArchiveServiceImpl extends ServiceImpl<ArchiveMapper, Archive> implements ArchiveService {

    private final ArchiveMapper archiveMapper;
    private final PatientMapper patientMapper;
    private final DeptMapper deptMapper;
    private final WardMapper wardMapper;
    private final BedMapper bedMapper;
    private final DoctorMapper doctorMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public ArchiveServiceImpl(ArchiveMapper archiveMapper,
                              PatientMapper patientMapper,
                              DeptMapper deptMapper,
                              WardMapper wardMapper,
                              BedMapper bedMapper,
                              DoctorMapper doctorMapper,
                              org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.archiveMapper = archiveMapper;
        this.patientMapper = patientMapper;
        this.deptMapper = deptMapper;
        this.wardMapper = wardMapper;
        this.bedMapper = bedMapper;
        this.doctorMapper = doctorMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArchiveVO> addArchive(ArchiveDTO dto) {
        // 1. 校验患者是否存在
        if (dto.getPatientId() == null || patientMapper.selectById(dto.getPatientId()) == null) {
            return Result.error(400, "患者不存在 (patientId=" + dto.getPatientId() + ")，请先注册患者信息");
        }
        // 2. 校验科室是否存在
        if (dto.getDeptId() == null || deptMapper.selectById(dto.getDeptId()) == null) {
            return Result.error(400, "科室不存在 (deptId=" + dto.getDeptId() + ")");
        }
        // 3. 校验病房是否存在
        if (dto.getWardId() == null || wardMapper.selectById(dto.getWardId()) == null) {
            return Result.error(400, "病房不存在 (wardId=" + dto.getWardId() + ")");
        }
        // 4. 校验病床是否存在且空闲
        Bed bed = null;
        if (dto.getBedId() == null || (bed = bedMapper.selectById(dto.getBedId())) == null) {
            return Result.error(400, "病床不存在 (bedId=" + dto.getBedId() + ")");
        }
        if ("占用".equals(bed.getStatus())) {
            return Result.error(400, "该病床已被占用 (bedId=" + dto.getBedId() + ")，请选择其他空闲病床");
        }
        // 5. 校验主治医生是否存在
        if (dto.getAttendingDoctorId() == null || doctorMapper.selectById(dto.getAttendingDoctorId()) == null) {
            return Result.error(400, "医生不存在 (doctorId=" + dto.getAttendingDoctorId() + ")");
        }

        // 5.5 校验并真实扣减 Redis 中的就诊卡余额
        String balanceKey = "patient:balance:" + dto.getPatientId();
        String balanceStr = redisTemplate.opsForValue().get(balanceKey);
        java.math.BigDecimal curBalance = balanceStr == null ? java.math.BigDecimal.valueOf(1000.00) : new java.math.BigDecimal(balanceStr);
        if (curBalance.compareTo(dto.getPrepaidAmount()) < 0) {
            return Result.error(400, "办理入院登记失败：就诊卡余额不足！当前余额为 " + curBalance + " 元，本次所需缴交押金 " + dto.getPrepaidAmount() + " 元，请先充值！");
        }
        java.math.BigDecimal nextBalance = curBalance.subtract(dto.getPrepaidAmount());
        redisTemplate.opsForValue().set(balanceKey, nextBalance.toString());
        log.info("患者办理入院手续，就诊卡余额已动态扣减：patientId={}, 扣减押金={}, 剩余余额={}",
                dto.getPatientId(), dto.getPrepaidAmount(), nextBalance);

        // 6. 更新病床状态为"占用"
        bed.setStatus("占用");
        bedMapper.updateById(bed);

        // 7. 创建档案
        Archive archive = new Archive();
        BeanUtils.copyProperties(dto, archive);
        archive.setAdmissionTime(LocalDateTime.now());
        archive.setBalance(dto.getPrepaidAmount());
        archive.setStatus("住院中");

        archiveMapper.insert(archive);
        log.info("入院登记成功：archiveId={}, patientId={}, wardId={}, bedId={}",
                archive.getId(), dto.getPatientId(), dto.getWardId(), dto.getBedId());

        // 8. 返回 VO
        ArchiveVO vo = new ArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArchiveVO> discharge(Long archiveId) {
        Archive archive = archiveMapper.selectById(archiveId);
        if (archive == null) {
            return Result.error(404, "住院档案不存在");
        }
        if ("已出院".equals(archive.getStatus())) {
            return Result.error(400, "患者已出院，请勿重复操作");
        }

        // 1. 更新档案状态
        archive.setStatus("已出院");
        archive.setDischargeTime(LocalDateTime.now());
        archiveMapper.updateById(archive);

        // 2. 释放病床
        Bed bed = bedMapper.selectById(archive.getBedId());
        if (bed != null) {
            bed.setStatus("空闲");
            bedMapper.updateById(bed);
            log.info("病床已释放：bedId={}", archive.getBedId());
        }

        log.info("出院结算：archiveId={}, patientId={}", archiveId, archive.getPatientId());
        ArchiveVO vo = new ArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArchiveVO> recharge(Long archiveId, BigDecimal amount) {
        Archive archive = archiveMapper.selectById(archiveId);
        if (archive == null) {
            return Result.error(404, "住院档案不存在");
        }
        if ("已出院".equals(archive.getStatus())) {
            return Result.error(400, "患者已出院，无法续缴");
        }

        // 校验并真实扣减 Redis 中的就诊卡余额
        String balanceKey = "patient:balance:" + archive.getPatientId();
        String balanceStr = redisTemplate.opsForValue().get(balanceKey);
        java.math.BigDecimal curBalance = balanceStr == null ? java.math.BigDecimal.valueOf(1000.00) : new java.math.BigDecimal(balanceStr);
        if (curBalance.compareTo(amount) < 0) {
            return Result.error(400, "住院费用续缴失败：就诊卡余额不足！当前余额为 " + curBalance + " 元，本次所需续缴金额 " + amount + " 元，请先充值就诊卡！");
        }
        java.math.BigDecimal nextBalance = curBalance.subtract(amount);
        redisTemplate.opsForValue().set(balanceKey, nextBalance.toString());
        log.info("患者住院续缴费成功，就诊卡余额已动态扣减：patientId={}, 续缴金额={}, 剩余余额={}",
                archive.getPatientId(), amount, nextBalance);

        // 续缴金额
        archive.setBalance(archive.getBalance().add(amount));
        archive.setPrepaidAmount(archive.getPrepaidAmount().add(amount));

        // 如果之前状态为"余额不足"，恢复为"住院中"
        if ("余额不足".equals(archive.getStatus())) {
            archive.setStatus("住院中");
            log.info("续缴后恢复住院中状态：archiveId={}", archiveId);
        }

        archiveMapper.updateById(archive);
        log.info("住院续缴成功：archiveId={}, 续缴金额={}, 当前余额={}",
                archiveId, amount, archive.getBalance());

        ArchiveVO vo = new ArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        return Result.success(vo);
    }
}

