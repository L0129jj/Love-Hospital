package cn.edu.scnu.vo;

import lombok.Data;

@Data
public class ScheduleStatsVO {
    private Long deptId;
    private String deptName;
    private long totalSchedules;
    private String startDate;
    private String endDate;
}
