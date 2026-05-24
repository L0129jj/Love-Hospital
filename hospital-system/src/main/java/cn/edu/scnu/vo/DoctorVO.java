package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorVO {
    private Long id;
    private String name;
    private String deptCode;
    private String title;
    private BigDecimal consultationFee;
    private Long deptId;
}

