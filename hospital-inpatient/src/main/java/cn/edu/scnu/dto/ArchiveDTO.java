package cn.edu.scnu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ArchiveDTO {
    private Long patientId;
    private Long deptId;
    private Long wardId;
    private Long bedId;
    private Long attendingDoctorId;
    private BigDecimal prepaidAmount;
}
