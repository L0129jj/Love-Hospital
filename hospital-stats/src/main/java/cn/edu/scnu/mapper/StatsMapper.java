package cn.edu.scnu.mapper;

import cn.edu.scnu.vo.RevenueStatsVO;
import cn.edu.scnu.vo.ScheduleStatsVO;
import cn.edu.scnu.vo.TreatmentStatsVO;
import cn.edu.scnu.vo.WorkloadStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StatsMapper {

    @Select("SELECT d.dept_id AS deptId, d.dept_name AS deptName, " +
            "COUNT(s.schedule_id) AS totalSchedules, " +
            "#{startDate} AS startDate, #{endDate} AS endDate " +
            "FROM department d " +
            "LEFT JOIN `schedule` s ON d.dept_id = s.dept_id " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "WHERE d.dept_id = #{deptId} " +
            "GROUP BY d.dept_id, d.dept_name")
    List<ScheduleStatsVO> scheduleByDept(@Param("deptId") Long deptId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);

    @Select("SELECT dr.doctor_id AS doctorId, dr.name AS doctorName, " +
            "COUNT(DISTINCT r.patient_id) AS totalPatients, " +
            "COUNT(DISTINCT s.schedule_id) AS totalSchedules, " +
            "#{startDate} AS startDate, #{endDate} AS endDate " +
            "FROM doctor dr " +
            "LEFT JOIN `schedule` s ON dr.doctor_id = s.doctor_id " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "LEFT JOIN registration r ON dr.doctor_id = r.doctor_id " +
            "AND r.reg_time BETWEEN CONCAT(#{startDate},' 00:00:00') AND CONCAT(#{endDate},' 23:59:59') " +
            "WHERE dr.doctor_id = #{doctorId} " +
            "GROUP BY dr.doctor_id, dr.name")
    List<WorkloadStatsVO> doctorWorkload(@Param("doctorId") Long doctorId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);

    @Select("SELECT p.patient_id AS patientId, p.name AS patientName, " +
            "COUNT(DISTINCT r.reg_id) AS totalVisits, " +
            "COUNT(DISTINCT ha.archive_id) AS totalHospitalizations " +
            "FROM patient p " +
            "LEFT JOIN registration r ON p.patient_id = r.patient_id " +
            "LEFT JOIN hospitalization_archive ha ON p.patient_id = ha.patient_id " +
            "WHERE p.patient_id = #{patientId} " +
            "GROUP BY p.patient_id, p.name")
    TreatmentStatsVO patientTreatment(@Param("patientId") Long patientId);

    @Select("SELECT COALESCE(SUM(py.amount), 0) AS totalRevenue, " +
            "#{startDate} AS startDate, #{endDate} AS endDate " +
            "FROM payment py " +
            "WHERE py.pay_time BETWEEN CONCAT(#{startDate},' 00:00:00') AND CONCAT(#{endDate},' 23:59:59')")
    RevenueStatsVO revenue(@Param("startDate") String startDate,
                            @Param("endDate") String endDate);
}
