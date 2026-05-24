package cn.edu.scnu.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleVO {
    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private String shiftType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long deptId;
}
