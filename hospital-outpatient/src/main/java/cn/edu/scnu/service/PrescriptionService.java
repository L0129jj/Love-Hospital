package cn.edu.scnu.service;

import cn.edu.scnu.dto.PrescriptionDTO;
import cn.edu.scnu.entity.Prescription;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.vo.PrescriptionVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

public interface PrescriptionService extends IService<Prescription> {
    Result<PrescriptionVO> add(@Valid PrescriptionDTO dto);
}
