package cn.edu.scnu.dto;

import lombok.Data;

@Data
public class RegDTO {
    private Long patientId;
    private Long doctorId;
    private Long deptId;
    private String visitType;
}
