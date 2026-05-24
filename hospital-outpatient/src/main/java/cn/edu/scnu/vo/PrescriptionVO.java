package cn.edu.scnu.vo;

import cn.edu.scnu.entity.PrescriptionDetail;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionVO {
    private Long id;
    private Long regId;
    private Long doctorId;
    private Long patientId;
    private String symptoms;
    private String diagnosis;
    private BigDecimal consultationFee;
    private BigDecimal totalFee;
    private LocalDateTime createTime;
    private String status;
    private List<PrescriptionDetail> details;
}
