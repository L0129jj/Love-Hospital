package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Archive;
import cn.edu.scnu.entity.Drug;
import cn.edu.scnu.entity.Fee;
import cn.edu.scnu.entity.Record;
import cn.edu.scnu.entity.Ward;
import cn.edu.scnu.mapper.ArchiveMapper;
import cn.edu.scnu.mapper.DrugMapper;
import cn.edu.scnu.mapper.FeeMapper;
import cn.edu.scnu.mapper.RecordMapper;
import cn.edu.scnu.mapper.WardMapper;
import cn.edu.scnu.service.FeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FeeServiceImpl extends ServiceImpl<FeeMapper, Fee> implements FeeService {

    private final FeeMapper feeMapper;
    private final RecordMapper recordMapper;
    private final ArchiveMapper archiveMapper;
    private final WardMapper wardMapper;
    private final DrugMapper drugMapper;

    // 默认诊疗费（当无法从治疗方案中解析出具体金额时使用）
    private static final BigDecimal DEFAULT_TREATMENT_FEE = new BigDecimal("50.00");

    public FeeServiceImpl(FeeMapper feeMapper,
                          RecordMapper recordMapper,
                          ArchiveMapper archiveMapper,
                          WardMapper wardMapper,
                          DrugMapper drugMapper) {
        this.feeMapper = feeMapper;
        this.recordMapper = recordMapper;
        this.archiveMapper = archiveMapper;
        this.wardMapper = wardMapper;
        this.drugMapper = drugMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateDailyFee(Long recordId) {

        // 1. 查询住院记录
        Record record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("住院记录不存在，recordId=" + recordId);
        }

        // 2. 查询住院档案
        Archive archive = archiveMapper.selectById(record.getArchiveId());
        if (archive == null) {
            throw new RuntimeException("住院档案不存在，archiveId=" + record.getArchiveId());
        }
        if (!"住院中".equals(archive.getStatus())) {
            throw new RuntimeException("患者已出院，无法计费");
        }

        // 3. 查询病房获取床位费标准
        Ward ward = wardMapper.selectById(archive.getWardId());
        if (ward == null) {
            throw new RuntimeException("病房信息不存在，wardId=" + archive.getWardId());
        }

        // ============================================================
        // 4. 计算当日费用明细
        // ============================================================

        // 4.1 床位费 — 按病房 fee_standard 收取
        BigDecimal bedFee = ward.getFeeStandard();

        // 4.2 诊疗费 — 从 treatment_plan 估算
        BigDecimal treatmentFee = calculateTreatmentFee(record.getTreatmentPlan());

        // 4.3 药品费 — 从 prescription_note 估算
        BigDecimal drugFee = calculateDrugFee(record.getPrescriptionNote());

        // 4.4 当日总费用
        BigDecimal dailyTotal = bedFee.add(treatmentFee).add(drugFee);

        log.info("住院计费：archiveId={}, recordId={}, 床位费={}, 诊疗费={}, 药品费={}, 合计={}",
                archive.getId(), recordId, bedFee, treatmentFee, drugFee, dailyTotal);

        // ============================================================
        // 5. 保存费用明细（三条记录：床位费、诊疗费、药品费）
        // ============================================================

        LocalDate feeDate = record.getRecordDate() != null ? record.getRecordDate() : LocalDate.now();

        saveFee(archive.getId(), recordId, feeDate, "床位费", bedFee,
                "床位费（" + ward.getWardNo() + "）每日 " + ward.getFeeStandard().setScale(2) + " 元");
        saveFee(archive.getId(), recordId, feeDate, "诊疗费", treatmentFee,
                "诊疗费");
        if (drugFee.compareTo(BigDecimal.ZERO) > 0) {
            saveFee(archive.getId(), recordId, feeDate, "药品费", drugFee,
                    "药品费（根据处方估算）");
        }

        // ============================================================
        // 6. 扣减余额 + 余额不足检查
        // ============================================================

        BigDecimal previousBalance = archive.getBalance();
        BigDecimal newBalance = previousBalance.subtract(dailyTotal);
        archive.setBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            archive.setStatus("余额不足");
            log.warn("余额不足：archiveId={}, 原余额={}, 扣费={}, 新余额={}",
                    archive.getId(), previousBalance, dailyTotal, newBalance);
        } else {
            log.info("余额扣减：archiveId={}, {} → {}", archive.getId(), previousBalance, newBalance);
        }

        archiveMapper.updateById(archive);
    }

    // ====================== 私有辅助方法 ======================

    /**
     * 保存一条费用明细到 inpatient_fee 表
     */
    private void saveFee(Long archiveId, Long recordId, LocalDate feeDate,
                         String feeType, BigDecimal amount, String description) {
        Fee fee = new Fee();
        fee.setArchiveId(archiveId);
        fee.setRecordId(recordId);
        fee.setFeeDate(feeDate);
        fee.setFeeType(feeType);
        fee.setAmount(amount);
        fee.setDescription(description);
        feeMapper.insert(fee);
    }

    /**
     * 从诊疗方案文本中估算诊疗费。
     * 支持格式：`诊疗费:100` 或 `治疗:50` 在文本中出现时取指定金额；
     * 无指定时使用默认值 50 元。
     */
    private BigDecimal calculateTreatmentFee(String treatmentPlan) {
        if (treatmentPlan == null || treatmentPlan.isBlank()) {
            return DEFAULT_TREATMENT_FEE;
        }
        // 尝试提取文本中的 "诊疗费:xxx" 或 "治疗费:xxx"
        Pattern pattern = Pattern.compile("(?:诊疗费|治疗费|治疗)\\s*[:：]\\s*(\\d+(?:\\.\\d{1,2})?)");
        Matcher matcher = pattern.matcher(treatmentPlan);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return DEFAULT_TREATMENT_FEE;
    }

    /**
     * 从诊疗处方文本中估算药品费。
     * 支持格式：`药名:数量` — 如 `阿莫西林:2`
     * 通过药品名在 Drug 表中模糊查询单价，计算小计后累加。
     * 若查询不到药品，按默认单价 20 元/单位估算。
     */
    private BigDecimal calculateDrugFee(String prescriptionNote) {
        if (prescriptionNote == null || prescriptionNote.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;

        // 格式: "药品名:数量" 或 "药品名：数量"
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5\\w]+)\\s*[:：]\\s*(\\d+)");
        Matcher matcher = pattern.matcher(prescriptionNote);

        while (matcher.find()) {
            String drugName = matcher.group(1).trim();
            int quantity = Integer.parseInt(matcher.group(2));

            // 尝试按药品名模糊查询
            Drug drug = drugMapper.selectOne(
                    new LambdaQueryWrapper<Drug>()
                            .like(Drug::getName, drugName)
                            .last("LIMIT 1"));

            BigDecimal unitPrice;
            if (drug != null) {
                unitPrice = drug.getPrice();
            } else {
                // 查不到时按默认单价 20 元估算
                unitPrice = new BigDecimal("20.00");
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);
        }

        return total;
    }
}
