package cn.edu.scnu.controller;

import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.StatsQueryService;
import cn.edu.scnu.vo.RevenueStatsVO;
import cn.edu.scnu.vo.ScheduleStatsVO;
import cn.edu.scnu.vo.TreatmentStatsVO;
import cn.edu.scnu.vo.WorkloadStatsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsQueryService statsQueryService;

    public StatsController(StatsQueryService statsQueryService) {
        this.statsQueryService = statsQueryService;
    }

    /**
     * 科室排班统计
     */
    @GetMapping("/schedule-by-dept")
    public Result<List<ScheduleStatsVO>> scheduleByDept(
            @RequestParam Long deptId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statsQueryService.scheduleByDept(deptId, startDate, endDate));
    }

    /**
     * 医生工作量统计
     */
    @GetMapping("/doctor-workload")
    public Result<List<WorkloadStatsVO>> doctorWorkload(
            @RequestParam Long doctorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statsQueryService.doctorWorkload(doctorId, startDate, endDate));
    }

    /**
     * 患者治疗情况统计
     */
    @GetMapping("/patient-treatment")
    public Result<TreatmentStatsVO> patientTreatment(@RequestParam Long patientId) {
        return Result.success(statsQueryService.patientTreatment(patientId));
    }

    /**
     * 收入统计
     */
    @GetMapping("/revenue")
    public Result<RevenueStatsVO> revenue(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statsQueryService.revenue(startDate, endDate));
    }
}
