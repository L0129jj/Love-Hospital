package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {
    @TableId(value = "prescription_id", type = IdType.AUTO)
    private Long id;
    private Long regId;
    private Long doctorId;
    private Long patientId;
    private String symptoms;
    private String diagnosis;
    private BigDecimal consultationFee;
    private BigDecimal totalFee;
    private LocalDateTime createTime;
    private String status;
}
