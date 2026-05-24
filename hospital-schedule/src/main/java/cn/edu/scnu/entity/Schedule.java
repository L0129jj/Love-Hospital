package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("schedule")
public class Schedule {
    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private String shiftType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long deptId;
}
