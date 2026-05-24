package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ArchiveVO {
    private Long id;
    private Long patientId;
    private Long deptId;
    private Long wardId;
    private Long bedId;
    private Long attendingDoctorId;
    private LocalDateTime admissionTime;
    private LocalDateTime dischargeTime;
    private BigDecimal prepaidAmount;
    private BigDecimal balance;
    private String status;
}
