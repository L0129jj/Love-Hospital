package cn.edu.scnu.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecordVO {
    private Long id;
    private Long archiveId;
    private Long doctorId;
    private LocalDate recordDate;
    private String symptoms;
    private String treatmentPlan;
    private String prescriptionNote;
}
