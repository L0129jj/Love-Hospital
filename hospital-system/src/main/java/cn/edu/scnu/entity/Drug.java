package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("drug")
public class Drug {
    @TableId("drug_id")
    private Long id;

    @TableField("drug_name")
    private String name;

    private String specification;

    private String unit;

    private BigDecimal price;

    private Integer stock;

    private String description;
}
