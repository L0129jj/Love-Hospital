package cn.edu.scnu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private Long prescriptionId;
    private Long patientId;
    private BigDecimal amount;
    private String payMethod;
}
