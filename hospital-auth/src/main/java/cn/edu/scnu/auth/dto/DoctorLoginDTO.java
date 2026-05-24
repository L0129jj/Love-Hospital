package cn.edu.scnu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorLoginDTO {
    @NotBlank(message = "工号不能为空")
    private String workNo;

    @NotBlank(message = "密码不能为空")
    private String password;
}
