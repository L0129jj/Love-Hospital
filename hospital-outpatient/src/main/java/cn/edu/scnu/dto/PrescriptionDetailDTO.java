package cn.edu.scnu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrescriptionDetailDTO {
    private Long prescriptionId;
    private Long drugId;
    private Integer quantity;
    private BigDecimal price;
    private String dosage;
    private BigDecimal subtotal;
}
