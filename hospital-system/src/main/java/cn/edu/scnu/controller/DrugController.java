package cn.edu.scnu.controller;

import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.DrugDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Drug;
import cn.edu.scnu.service.DrugService;
import cn.edu.scnu.vo.DrugVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/drugs")
public class DrugController {

    @Autowired
    private DrugService drugService;

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("drug");
    }

    /**
     * 药品列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<DrugVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<Drug>()
                .like(name != null && !name.isBlank(), Drug::getName, name)
                .orderByAsc(Drug::getId);
        Page<Drug> p = drugService.page(new Page<>(page, size), wrapper);
        List<DrugVO> records = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    /**
     * 药品全量列表（供处方开具时选择药品使用，只返回有库存的药品）
     */
    @GetMapping("/available")
    public Result<List<DrugVO>> available() {
        List<DrugVO> list = drugService.lambdaQuery()
                .gt(Drug::getStock, 0)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 新增药品
     */
    @PostMapping("/insert")
    public Result<DrugVO> insert(@Valid @RequestBody DrugDTO dto) {
        Drug drug = new Drug();
        BeanUtils.copyProperties(dto, drug);
        drugService.save(drug);
        return Result.success(toVO(drug));
    }

    /**
     * 修改药品
     */
    @PutMapping("/update/{id}")
    public Result<DrugVO> update(@PathVariable Long id, @Valid @RequestBody DrugDTO dto) {
        Drug drug = drugService.getById(id);
        if (drug == null) {
            return Result.error(404, "药品不存在");
        }
        BeanUtils.copyProperties(dto, drug);
        drug.setId(id);
        drugService.updateById(drug);
        return Result.success(toVO(drug));
    }

    /**
     * 删除药品
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        drugService.removeById(id);
        return Result.success(null);
    }

    private DrugVO toVO(Drug drug) {
        DrugVO vo = new DrugVO();
        BeanUtils.copyProperties(drug, vo);
        return vo;
    }
}

