package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private Long patientId;
    private BigDecimal amount;
    private LocalDateTime payTime;
    private String payMethod;
}
