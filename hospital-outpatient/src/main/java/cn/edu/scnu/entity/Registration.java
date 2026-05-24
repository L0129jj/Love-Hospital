package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {
    @TableId(value = "reg_id", type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long deptId;
    private LocalDateTime regTime;
    private String visitType;
    private String status;
}
