package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DrugVO {
    private Long id;
    private String name;
    private String specification;
    private String unit;
    private BigDecimal price;
    private Integer stock;
    private String description;
}

