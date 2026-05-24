package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hospitalization_archive")
public class Archive {
    @TableId(value = "archive_id", type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long deptId;
    private Long wardId;
    private Long bedId;
    private Long attendingDoctorId;
    private LocalDateTime admissionTime;
    private LocalDateTime dischargeTime;
    private BigDecimal prepaidAmount;
    private BigDecimal balance;
    private String status;
}
