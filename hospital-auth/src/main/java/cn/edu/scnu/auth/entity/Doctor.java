package cn.edu.scnu.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("doctor")
public class Doctor {
    @TableId(value = "doctor_id", type = IdType.AUTO)
    private Long id;
    private String workNo;
    private String name;
    private String gender;
    private String title;
    private String phone;
    private String password;
    private Long deptId;
    private BigDecimal consultationFee;
    private Integer loginStatus;
}
