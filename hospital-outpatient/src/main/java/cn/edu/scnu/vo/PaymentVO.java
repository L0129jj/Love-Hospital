package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Long id;
    private Long prescriptionId;
    private Long patientId;
    private BigDecimal amount;
    private LocalDateTime payTime;
    private String payMethod;
}
