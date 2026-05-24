package cn.edu.scnu.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("patient")
public class Patient {
    @TableId(value = "patient_id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String gender;
    private String address;
    private String phone;
    private String username;
    private String password;
    private Integer loginStatus;

    @TableField(exist = false)
    private BigDecimal cardBalance = BigDecimal.valueOf(1000.00); // 默认就诊卡余额为1000.00元
}

