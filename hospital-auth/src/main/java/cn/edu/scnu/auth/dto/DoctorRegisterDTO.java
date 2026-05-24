package cn.edu.scnu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorRegisterDTO {
    @NotBlank(message = "医生工号不能为空")
    private String workNo;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender; // '男' 或 '女'

    @NotBlank(message = "职称不能为空")
    private String title; // '主任医师', '副主任医师', '主治医师', '住院医师'

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确")
    private String phone;

    @NotNull(message = "所属科室不能为空")
    private Long deptId;

    @NotNull(message = "诊疗费不能为空")
    private BigDecimal consultationFee;
}
