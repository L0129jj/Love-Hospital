package cn.edu.scnu.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RevenueStatsVO {
    private BigDecimal totalRevenue;
    private String startDate;
    private String endDate;
}
