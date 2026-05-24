package cn.edu.scnu.service;

import cn.edu.scnu.vo.RevenueStatsVO;
import cn.edu.scnu.vo.ScheduleStatsVO;
import cn.edu.scnu.vo.TreatmentStatsVO;
import cn.edu.scnu.vo.WorkloadStatsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface StatsQueryService  {

    List<ScheduleStatsVO> scheduleByDept(Long deptId, String startDate, String endDate);

    List<WorkloadStatsVO> doctorWorkload(Long doctorId, String startDate, String endDate);

    TreatmentStatsVO patientTreatment(Long patientId);

    RevenueStatsVO revenue(String startDate, String endDate);
}
