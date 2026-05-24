package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeeVO {
    private Long id;
    private Long archiveId;
    private Long recordId;
    private LocalDate feeDate;
    private String feeType;
    private BigDecimal amount;
    private String description;
}
