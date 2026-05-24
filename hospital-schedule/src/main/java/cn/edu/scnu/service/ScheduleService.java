package cn.edu.scnu.service;

import cn.edu.scnu.dto.ScheduleDTO;
import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.entity.Schedule;
import cn.edu.scnu.vo.ScheduleVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.time.LocalDate;

public interface ScheduleService extends IService<Schedule> {

    Result<Void> add(@Valid ScheduleDTO dto);
}
