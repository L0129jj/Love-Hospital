package cn.edu.scnu.service.Impl;

import cn.edu.scnu.dto.RecordDTO;
import cn.edu.scnu.entity.Archive;
import cn.edu.scnu.entity.Record;
import cn.edu.scnu.mapper.ArchiveMapper;
import cn.edu.scnu.mapper.RecordMapper;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.service.FeeService;
import cn.edu.scnu.service.RecordService;
import cn.edu.scnu.vo.RecordVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    private final RecordMapper recordMapper;
    private final ArchiveMapper archiveMapper;
    private final FeeService feeService;

    public RecordServiceImpl(RecordMapper recordMapper,
                             ArchiveMapper archiveMapper,
                             FeeService feeService) {
        this.recordMapper = recordMapper;
        this.archiveMapper = archiveMapper;
        this.feeService = feeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RecordVO> addRecord(RecordDTO dto) {

        // 1. 校验住院档案是否存在且可录入巡诊
        Archive archive = archiveMapper.selectById(dto.getArchiveId());
        if (archive == null) {
            return Result.error(404, "住院档案不存在");
        }
        if ("余额不足".equals(archive.getStatus())) {
            return Result.error(400, "患者住院账户余额不足，请先办理续缴后再录入巡诊记录");
        }
        if (!"住院中".equals(archive.getStatus())) {
            return Result.error(400, "患者当前状态为「" + archive.getStatus() + "」，不可录入巡诊记录");
        }

        // 2. 保存巡诊记录
        Record record = new Record();
        BeanUtils.copyProperties(dto, record);
        recordMapper.insert(record);
        log.info("新增巡诊记录：recordId={}, archiveId={}, doctorId={}",
                record.getId(), dto.getArchiveId(), dto.getDoctorId());

        // 3. 自动触发当日费用计算与余额扣减
        feeService.calculateDailyFee(record.getId());

        // 4. 返回 VO
        RecordVO vo = new RecordVO();
        BeanUtils.copyProperties(record, vo);
        return Result.success(vo);
    }
}
