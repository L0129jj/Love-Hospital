package cn.edu.scnu.service.Impl;

import cn.edu.scnu.dto.ScheduleDTO;
import cn.edu.scnu.exception.BusinessException;
import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.entity.Schedule;
import cn.edu.scnu.mapper.ScheduleMapper;
import cn.edu.scnu.service.ScheduleService;
import cn.edu.scnu.vo.ScheduleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    @Autowired
    private ScheduleMapper scheduleMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> add(ScheduleDTO dto) {
        //1.检查排班有无时间冲突
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId,dto.getDoctorId())
                .eq(Schedule::getScheduleDate,dto.getScheduleDate());
        List<Schedule> existing = scheduleMapper.selectList(wrapper);
        boolean conflict = existing.stream().anyMatch(s -> (s.getEndTime() .isAfter( dto.getStartTime())) &&
                s.getStartTime() .isBefore(dto.getEndTime())
        );
        if (conflict) {
            throw new BusinessException(409,"排班时间与已有时间冲突");
        }
        //2.无时间冲突，新建排班
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(dto,schedule);
        save(schedule);
        return Result.success(null);
    }
}
