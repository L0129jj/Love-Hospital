package cn.edu.scnu.vo;

import lombok.Data;

@Data
public class TreatmentStatsVO {
    private Long patientId;
    private String patientName;
    private long totalVisits;
    private long totalHospitalizations;
}
