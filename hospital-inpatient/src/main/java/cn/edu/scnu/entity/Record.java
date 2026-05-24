package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("inpatient_record")
public class Record {
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long id;
    private Long archiveId;
    private Long doctorId;
    private LocalDate recordDate;
    private String symptoms;
    private String treatmentPlan;
    private String prescriptionNote;
}
