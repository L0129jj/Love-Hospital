package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ward")
public class Ward {
    @TableId(value = "ward_id", type = IdType.AUTO)
    private Long id;
    private String wardNo;
    private String location;
    private BigDecimal feeStandard;
    private Long deptId;
}
