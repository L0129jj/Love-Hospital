package cn.edu.scnu.controller;

import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.ArchiveDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Archive;
import cn.edu.scnu.service.ArchiveService;
import cn.edu.scnu.vo.ArchiveVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/inpatient/archives")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final cn.edu.scnu.mapper.BedMapper bedMapper;

    public ArchiveController(ArchiveService archiveService, cn.edu.scnu.mapper.BedMapper bedMapper) {
        this.archiveService = archiveService;
        this.bedMapper = bedMapper;
    }

    /**
     * 获取全院空闲（可用）的病床与病房列表
     */
    @GetMapping("/available-beds")
    public Result<List<cn.edu.scnu.entity.Bed>> getAvailableBeds() {
        List<cn.edu.scnu.entity.Bed> beds = bedMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.edu.scnu.entity.Bed>()
                        .eq(cn.edu.scnu.entity.Bed::getStatus, "空闲")
        );
        return Result.success(beds);
    }

    /**
     * 办理入院登记（含所有外键校验）
     */
    @PostMapping("")
    public Result<ArchiveVO> add(@Valid @RequestBody ArchiveDTO dto) {
        return archiveService.addArchive(dto);
    }

    /**
     * 住院档案列表（分页）
     */
    @GetMapping("")
    public Result<PageResult<ArchiveVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<Archive> wrapper = new LambdaQueryWrapper<Archive>()
                .eq(patientId != null, Archive::getPatientId, patientId)
                .eq(status != null, Archive::getStatus, status)
                .orderByDesc(Archive::getAdmissionTime);

        Page<Archive> p = archiveService.page(new Page<>(page, size), wrapper);

        List<ArchiveVO> records = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<ArchiveVO> result = new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
        log.debug("住院档案查询：total={}, page={}, size={}", p.getTotal(), page, size);
        return Result.success(result);
    }

    /**
     * 出院结算（释放床位）
     */
    @PatchMapping("/{id}/discharge")
    public Result<ArchiveVO> discharge(@PathVariable Long id) {
        return archiveService.discharge(id);
    }

    /**
     * 住院费用续缴
     */
    @PatchMapping("/{id}/recharge")
    public Result<ArchiveVO> recharge(@PathVariable Long id,
                                      @RequestParam java.math.BigDecimal amount) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return Result.error(400, "续缴金额必须大于0");
        }
        return archiveService.recharge(id, amount);
    }

    private ArchiveVO toVO(Archive archive) {
        ArchiveVO vo = new ArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        return vo;
    }
}
