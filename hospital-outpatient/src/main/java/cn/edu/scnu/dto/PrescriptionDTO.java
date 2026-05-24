package cn.edu.scnu.dto;

import cn.edu.scnu.entity.PrescriptionDetail;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PrescriptionDTO {
    private Long regId;
    private Long doctorId;
    private Long patientId;
    private String symptoms;
    private String diagnosis;
    private BigDecimal consultationFee;
    private List<PrescriptionDetailDTO> details;

}
