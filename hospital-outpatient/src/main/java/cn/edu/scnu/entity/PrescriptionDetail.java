package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("prescription_detail")
public class PrescriptionDetail {
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private Integer quantity;
    private BigDecimal price;
    private String dosage;
    private BigDecimal subtotal;
}
