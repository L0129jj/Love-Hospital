package cn.edu.scnu.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecordDTO {
    private Long archiveId;
    private Long doctorId;
    private LocalDate recordDate;
    private String symptoms;
    private String treatmentPlan;
    private String prescriptionNote;
}
