package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("inpatient_fee")
public class Fee {
    @TableId(value = "fee_id", type = IdType.AUTO)
    private Long id;
    private Long archiveId;
    private Long recordId;
    private LocalDate feeDate;
    private String feeType;
    private BigDecimal amount;
    private String description;
}
