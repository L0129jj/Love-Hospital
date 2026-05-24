package cn.edu.scnu.controller;

import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.ScheduleDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Schedule;
import cn.edu.scnu.service.ScheduleService;
import cn.edu.scnu.vo.ScheduleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    @Autowired
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 排班分页列表
     */
    @GetMapping("")
    public Result<PageResult<ScheduleVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) LocalDate date) {
            LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                    .eq(doctorId != null, Schedule::getDoctorId, doctorId)
                    .eq(date != null, Schedule::getScheduleDate, date)
                    .orderByAsc(Schedule::getScheduleDate, Schedule::getStartTime);

            Page<Schedule> p = scheduleService.page(new Page<>(page, size), wrapper);

            List<ScheduleVO> records = p.getRecords().stream()
                    .map(this::toVO)
                    .collect(Collectors.toList());

            PageResult<ScheduleVO> result = new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
            return  Result.success(result);


    }

    /**
     * 新增排班
     */
    @PostMapping("")
    public Result<Void> add(@Valid @RequestBody ScheduleDTO dto) {
        return  scheduleService.add(dto);
    }

    /**
     * 修改排班
     */
    @PutMapping("/{id}")
    public Result<ScheduleVO> update(@PathVariable Long id, @Valid @RequestBody ScheduleDTO dto) {
        Schedule schedule = scheduleService.getById(id);
        if (schedule == null) {
            return Result.error(404, "排班不存在");
        }
        BeanUtils.copyProperties(dto, schedule);
        schedule.setId(id);
        scheduleService.updateById(schedule);
        return Result.success(toVO(schedule));
    }

    /**
     * 删除排班
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.removeById(id);
        return Result.success(null);
    }

    /**
     * 按科室查询排班
     */
    @GetMapping("/by-dept")
    public Result<List<ScheduleVO>> byDept(
            @RequestParam Long deptId,
            @RequestParam(required = false) LocalDate date) {

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDeptId, deptId)
                .eq(date != null, Schedule::getScheduleDate, date)
                .orderByAsc(Schedule::getScheduleDate, Schedule::getStartTime);

        List<ScheduleVO> list = scheduleService.list(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    private ScheduleVO toVO(Schedule schedule) {
        ScheduleVO vo = new ScheduleVO();
        BeanUtils.copyProperties(schedule, vo);
        return vo;
    }
}
