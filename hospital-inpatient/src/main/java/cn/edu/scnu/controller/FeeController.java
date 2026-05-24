package cn.edu.scnu.controller;

import cn.edu.scnu.result.Result;
import cn.edu.scnu.entity.Fee;
import cn.edu.scnu.service.FeeService;
import cn.edu.scnu.vo.FeeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inpatient/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    /**
     * 查询住院费用
     */
    @GetMapping("/by-archive")
    public Result<List<FeeVO>> byArchive(@RequestParam Long archiveId) {
        List<FeeVO> list = feeService.lambdaQuery()
                .eq(Fee::getArchiveId, archiveId)
                .orderByAsc(Fee::getFeeDate)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    private FeeVO toVO(Fee fee) {
        FeeVO vo = new FeeVO();
        BeanUtils.copyProperties(fee, vo);
        return vo;
    }
}
