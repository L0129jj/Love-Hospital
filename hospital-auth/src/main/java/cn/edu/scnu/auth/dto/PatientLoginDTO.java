package cn.edu.scnu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientLoginDTO {
    @NotBlank(message = "手机号或用户名不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
