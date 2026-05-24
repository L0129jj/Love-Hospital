package cn.edu.scnu.controller;

import
        cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.RegDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Registration;
import cn.edu.scnu.enums.RegistrationStatus;
import cn.edu.scnu.service.RegService;
import cn.edu.scnu.vo.RegVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/outpatient/registrations")
public class RegController {

    private final RegService regService;

    public RegController(RegService regService) {
        this.regService = regService;
    }

    // ==================== 挂号入口（护士/管理员/患者自助） ====================

    /**
     * 患者挂号
     */
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public Result<RegVO> add(@Valid @RequestBody RegDTO dto) {
        Registration reg = new Registration();
        BeanUtils.copyProperties(dto, reg);
        reg.setStatus(RegistrationStatus.PENDING.getDisplayName());
        regService.save(reg);
        log.info("挂号成功：患者ID={}, 医生ID={}, 挂号ID={}",
                dto.getPatientId(), dto.getDoctorId(), reg.getId());
        return Result.success(toVO(reg));
    }

    // ==================== 医生接诊工作台 ====================

    /**
     * 医生查看自己的待接诊患者（待就诊 + 就诊中）
     * 医生ID 前端从 JWT 解析 userId 后传入
     */
    @GetMapping("/my-patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<List<RegVO>> myPatients(@RequestParam Long doctorId) {
        List<Registration> list = regService.lambdaQuery()
                .eq(Registration::getDoctorId, doctorId)
                .in(Registration::getStatus,
                        RegistrationStatus.PENDING.getDisplayName(),
                        RegistrationStatus.IN_PROGRESS.getDisplayName())
                .orderByAsc(Registration::getRegTime)
                .list();

        List<RegVO> result = list.stream().map(this::toVO).collect(Collectors.toList());
        log.debug("医生 {} 的待接诊患者数: {}", doctorId, result.size());
        return Result.success(result);
    }

    /**
     * 开始接诊（待就诊 → 就诊中）
     */
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Void> startConsultation(@PathVariable Long id) {
        Registration reg = regService.getById(id);
        if (reg == null) {
            return Result.error(404, "挂号记录不存在");
        }
        if (!RegistrationStatus.PENDING.getDisplayName().equals(reg.getStatus())) {
            return Result.error(400, "当前状态无法开始接诊（" + reg.getStatus() + "）");
        }
        reg.setStatus(RegistrationStatus.IN_PROGRESS.getDisplayName());
        regService.updateById(reg);
        log.info("开始接诊：挂号ID={}, 患者ID={}", id, reg.getPatientId());
        return Result.success(null);
    }

    /**
     * 结束接诊（就诊中 → 已完成）
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Void> completeConsultation(@PathVariable Long id) {
        Registration reg = regService.getById(id);
        if (reg == null) {
            return Result.error(404, "挂号记录不存在");
        }
        if (!RegistrationStatus.IN_PROGRESS.getDisplayName().equals(reg.getStatus())) {
            return Result.error(400, "当前状态无法结束接诊（" + reg.getStatus() + "）");
        }
        reg.setStatus(RegistrationStatus.COMPLETED.getDisplayName());
        regService.updateById(reg);
        log.info("结束接诊：挂号ID={}, 患者ID={}", id, reg.getPatientId());
        return Result.success(null);
    }

    // ==================== 通用查询与管理 ====================

    /**
     * 挂号列表（分页，管理员/护士使用）
     */
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<PageResult<RegVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(patientId != null, Registration::getPatientId, patientId)
                .eq(doctorId != null, Registration::getDoctorId, doctorId)
                .eq(status != null, Registration::getStatus, status)
                .orderByDesc(Registration::getRegTime);

        Page<Registration> p = regService.page(new Page<>(page, size), wrapper);

        List<RegVO> records = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<RegVO> result = new PageResult<>(records, p.getTotal(),
                (int) p.getCurrent(), (int) p.getSize());
        log.debug("挂号列表查询：total={}, page={}, size={}", p.getTotal(), page, size);
        return Result.success(result);
    }

    /**
     * 取消挂号
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public Result<Void> cancel(@PathVariable Long id) {
        Registration reg = regService.getById(id);
        if (reg == null) {
            return Result.error(404, "挂号记录不存在");
        }
        reg.setStatus(RegistrationStatus.CANCELLED.getDisplayName());
        regService.updateById(reg);
        log.info("取消挂号：挂号ID={}", id);
        return Result.success(null);
    }

    /**
     * 患者自助查询就诊挂号记录（包含患者本人就诊状态的跟踪）
     */
    @GetMapping("/patient-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public Result<PageResult<RegVO>> patientHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long patientId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getPatientId, patientId)
                .orderByDesc(Registration::getRegTime);
        Page<Registration> p = regService.page(new Page<>(page, size), wrapper);
        List<RegVO> records = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    private RegVO toVO(Registration reg) {
        RegVO vo = new RegVO();
        BeanUtils.copyProperties(reg, vo);
        return vo;
    }
}

