package cn.edu.scnu.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorDTO {
    private String name;
    private String deptCode;
    private String workNo;
    private String gender;
    private String title;
    private String phone;
    private BigDecimal consultationFee;
}

