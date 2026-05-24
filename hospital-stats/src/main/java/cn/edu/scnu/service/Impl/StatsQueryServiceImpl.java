package cn.edu.scnu.service.Impl;

import cn.edu.scnu.mapper.StatsMapper;
import cn.edu.scnu.service.StatsQueryService;
import cn.edu.scnu.vo.RevenueStatsVO;
import cn.edu.scnu.vo.ScheduleStatsVO;
import cn.edu.scnu.vo.TreatmentStatsVO;
import cn.edu.scnu.vo.WorkloadStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsQueryServiceImpl implements StatsQueryService {

    @Autowired
    private final StatsMapper statsMapper;

    public StatsQueryServiceImpl(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    @Override
    public List<ScheduleStatsVO> scheduleByDept(Long deptId, String startDate, String endDate) {
        return statsMapper.scheduleByDept(deptId, startDate, endDate);
    }

    @Override
    public List<WorkloadStatsVO> doctorWorkload(Long doctorId, String startDate, String endDate) {
        return statsMapper.doctorWorkload(doctorId, startDate, endDate);
    }

    @Override
    public TreatmentStatsVO patientTreatment(Long patientId) {
        return statsMapper.patientTreatment(patientId);
    }

    @Override
    public RevenueStatsVO revenue(String startDate, String endDate) {
        return statsMapper.revenue(startDate, endDate);
    }
}
