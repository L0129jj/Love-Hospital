package cn.edu.scnu.vo;

import lombok.Data;

@Data
public class WorkloadStatsVO {
    private Long doctorId;
    private String doctorName;
    private long totalPatients;
    private long totalSchedules;
    private String startDate;
    private String endDate;
}
