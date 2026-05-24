package cn.edu.scnu.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegVO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long deptId;
    private LocalDateTime regTime;
    private String visitType;
    private String status;
}
