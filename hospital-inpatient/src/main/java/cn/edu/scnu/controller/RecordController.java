package cn.edu.scnu.controller;

import cn.edu.scnu.dto.RecordDTO;
import cn.edu.scnu.entity.Record;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.RecordService;
import cn.edu.scnu.vo.RecordVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inpatient/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * 新增巡诊记录（自动触发当日费用计算与余额检查）
     */
    @PostMapping("")
    public Result<RecordVO> add(@Valid @RequestBody RecordDTO dto) {
        return recordService.addRecord(dto);
    }

    /**
     * 查询档案的巡诊记录
     */
    @GetMapping("/by-archive")
    public Result<List<RecordVO>> byArchive(@RequestParam Long archiveId) {
        List<RecordVO> list = recordService.lambdaQuery()
                .eq(Record::getArchiveId, archiveId)
                .orderByAsc(Record::getRecordDate)
                .list()
                .stream()
                .map(record -> {
                    RecordVO vo = new RecordVO();
                    org.springframework.beans.BeanUtils.copyProperties(record, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        return Result.success(list);
    }
}
