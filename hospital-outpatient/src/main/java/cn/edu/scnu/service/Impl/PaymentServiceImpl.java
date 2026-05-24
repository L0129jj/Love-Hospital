package cn.edu.scnu.service.Impl;

import cn.edu.scnu.dto.PaymentDTO;
import cn.edu.scnu.entity.Payment;
import cn.edu.scnu.entity.Prescription;
import cn.edu.scnu.entity.Registration;
import cn.edu.scnu.enums.RegistrationStatus;
import cn.edu.scnu.exception.BusinessException;
import cn.edu.scnu.mapper.PaymentMapper;
import cn.edu.scnu.mapper.PrescriptionMapper;
import cn.edu.scnu.mapper.RegMapper;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.PaymentService;
import cn.edu.scnu.vo.PaymentVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final String STOCK_LOCK_PREFIX = "prescription:stock_lock:";

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private RegMapper regMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentVO> pay(PaymentDTO dto) {

        // 1. 校验处方存在
        Prescription presc = prescriptionMapper.selectById(dto.getPrescriptionId());
        if (presc == null) {
            throw new BusinessException(404, "处方不存在 (prescriptionId=" + dto.getPrescriptionId() + ")");
        }
        if ("已缴费".equals(presc.getStatus()) || "已取药".equals(presc.getStatus())) {
            throw new BusinessException(400, "该处方已缴费，请勿重复缴费");
        }

        // 2. 检查 Redis 锁定是否仍有效（未超时）
        String lockKey = STOCK_LOCK_PREFIX + presc.getId();
        String lockValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue == null) {
            // 库存锁定已超时，库存已被恢复，处方失效
            throw new BusinessException(400, "处方库存锁定已超时（超过30分钟未缴费），请重新就诊开方");
        }

        // 3. 保存缴费记录
        Payment payment = new Payment();
        BeanUtils.copyProperties(dto, payment);
        save(payment);
        log.info("缴费成功：prescriptionId={}, patientId={}, amount={}",
                dto.getPrescriptionId(), dto.getPatientId(), dto.getAmount());

        // 3.5 真实扣减 Redis 中的就诊卡余额
        String balanceKey = "patient:balance:" + dto.getPatientId();
        String balanceStr = redisTemplate.opsForValue().get(balanceKey);
        java.math.BigDecimal curBalance = balanceStr == null ? java.math.BigDecimal.valueOf(1000.00) : new java.math.BigDecimal(balanceStr);
        java.math.BigDecimal nextBalance = curBalance.subtract(dto.getAmount());
        redisTemplate.opsForValue().set(balanceKey, nextBalance.toString());
        log.info("患者就诊卡发生门诊扣款：patientId={}, amount={}, 扣后余额={}",
                dto.getPatientId(), dto.getAmount(), nextBalance);

        // 4. 更新处方状态 → 已缴费
        presc.setStatus("已缴费");
        prescriptionMapper.updateById(presc);

        // 5. 更新挂号状态 → 已完成
        Registration reg = regMapper.selectById(presc.getRegId());
        if (reg != null) {
            reg.setStatus(RegistrationStatus.COMPLETED.getDisplayName());
            regMapper.updateById(reg);
        }

        // 6. 删除 Redis 库存锁定 key（表示正式确认扣减，不再恢复库存）
        redisTemplate.delete(lockKey);
        log.info("库存锁定已确认（缴费成功），删除锁定 key: {}", lockKey);

        // 7. 返回 VO
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return Result.success(vo);
    }
}
