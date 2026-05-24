package cn.edu.scnu.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleDTO {
    private Long doctorId;
    private LocalDate scheduleDate;
    private String shiftType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long deptId;
}
