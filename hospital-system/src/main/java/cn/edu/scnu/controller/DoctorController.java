package cn.edu.scnu.controller;

import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.DoctorDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Doctor;
import cn.edu.scnu.service.DoctorService;
import cn.edu.scnu.vo.DoctorVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("doctor");
    }

    /**
     * 医生分页列表
     */
    @GetMapping("/list")
    public Result<PageResult<DoctorVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long deptId) {

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<Doctor>()
                .eq(deptId != null, Doctor::getDeptId, deptId);

        Page<Doctor> p = doctorService.page(new Page<>(page, size), wrapper);

        List<DoctorVO> records = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        PageResult<DoctorVO> result = new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
        return Result.success(result);
    }

    /**
     * 新增医生
     */
    @PostMapping("/insert")
    public Result<DoctorVO> insert(@Valid @RequestBody DoctorDTO dto) {
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(dto, doctor);
        if (dto.getDeptCode() != null && !dto.getDeptCode().trim().isEmpty()) {
            doctor.setDeptId(Long.valueOf(dto.getDeptCode()));
        }
        doctorService.save(doctor);
        return Result.success(toVO(doctor));
    }

    /**
     * 获取单个医生详情
     */
    @GetMapping("/{id}")
    public Result<DoctorVO> getById(@PathVariable Long id) {
        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
        return Result.success(toVO(doctor));
    }

    /**
     * 修改医生
     */
    @PutMapping("/update/{id}")
    public Result<DoctorVO> update(@PathVariable Long id, @Valid @RequestBody DoctorDTO dto) {
        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
        // 允许只修改传入的部分字段
        if (dto.getName() != null) doctor.setName(dto.getName());
        if (dto.getGender() != null) doctor.setGender(dto.getGender());
        if (dto.getPhone() != null) doctor.setPhone(dto.getPhone());
        if (dto.getTitle() != null) doctor.setTitle(dto.getTitle());
        if (dto.getConsultationFee() != null) doctor.setConsultationFee(dto.getConsultationFee());
        if (dto.getDeptCode() != null && !dto.getDeptCode().trim().isEmpty()) {
            doctor.setDeptId(Long.valueOf(dto.getDeptCode()));
        }
        
        doctor.setId(id);
        doctorService.updateById(doctor);
        return Result.success(toVO(doctor));
    }

    /**
     * 删除医生
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        doctorService.removeById(id);
        return Result.success(null);
    }

    private DoctorVO toVO(Doctor doctor) {
        DoctorVO vo = new DoctorVO();
        BeanUtils.copyProperties(doctor, vo);
        if (doctor.getDeptId() != null) {
            vo.setDeptCode(String.valueOf(doctor.getDeptId()));
        }
        return vo;
    }
}
