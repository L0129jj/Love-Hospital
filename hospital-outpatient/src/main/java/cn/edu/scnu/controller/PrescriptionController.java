package cn.edu.scnu.controller;

import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.PrescriptionDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Prescription;
import cn.edu.scnu.service.PrescriptionService;
import cn.edu.scnu.vo.PrescriptionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outpatient/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * 开具处方
     */
    @PostMapping("")
    public Result<PrescriptionVO> add(@Valid @RequestBody PrescriptionDTO dto) {
        return prescriptionService.add(dto);
    }

    /**
     * 查询处方详情
     */
    @GetMapping("/{id}")
    public Result<PrescriptionVO> detail(@PathVariable Long id) {
        Prescription p = prescriptionService.getById(id);
        if (p == null) {
            return Result.error(404, "处方不存在");
        }
        return Result.success(toVO(p));
    }

    private PrescriptionVO toVO(Prescription p) {
        PrescriptionVO vo = new PrescriptionVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }
}
