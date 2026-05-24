package cn.edu.scnu.service;

import cn.edu.scnu.dto.RecordDTO;
import cn.edu.scnu.entity.Record;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.vo.RecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RecordService extends IService<Record> {

    /**
     * 新增巡诊记录，自动触发当日费用计算与余额检查。
     */
    Result<RecordVO> addRecord(RecordDTO dto);
}
