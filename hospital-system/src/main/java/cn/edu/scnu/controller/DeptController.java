package cn.edu.scnu.controller;

import cn.edu.scnu.result.PageResult;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.DeptDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Dept;
import cn.edu.scnu.service.DeptService;
import cn.edu.scnu.vo.DeptVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/depts")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("dept");
    }

    /**
     * 科室列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<DeptVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<Dept>()
                .like(name != null && !name.isBlank(), Dept::getName, name);
        Page<Dept> p = deptService.page(new Page<>(page, size), wrapper);
        List<DeptVO> records = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(new PageResult<>(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    /**
     * 科室全量列表（供下拉选择使用）
     */
    @GetMapping("/all")
    public Result<List<DeptVO>> all() {
        List<DeptVO> list = deptService.list().stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 新增科室
     */
    @PostMapping("/insert")
    public Result<DeptVO> insert(@Valid @RequestBody DeptDTO dto) {
        Dept dept = new Dept();
        BeanUtils.copyProperties(dto, dept);
        deptService.save(dept);
        return Result.success(toVO(dept));
    }

    /**
     * 修改科室
     */
    @PutMapping("/update/{id}")
    public Result<DeptVO> update(@PathVariable Long id, @Valid @RequestBody DeptDTO dto) {
        Dept dept = deptService.getById(id);
        if (dept == null) {
            return Result.error(404, "科室不存在");
        }
        BeanUtils.copyProperties(dto, dept);
        dept.setId(id);
        deptService.updateById(dept);
        return Result.success(toVO(dept));
    }

    /**
     * 删除科室
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.success(null);
    }

    private DeptVO toVO(Dept dept) {
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }
}

