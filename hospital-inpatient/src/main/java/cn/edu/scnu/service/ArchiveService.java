package cn.edu.scnu.service;

import cn.edu.scnu.dto.ArchiveDTO;
import cn.edu.scnu.entity.Archive;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.vo.ArchiveVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface ArchiveService extends IService<Archive> {

    /**
     * 办理入院登记，校验所有外键关联后再插入。
     */
    Result<ArchiveVO> addArchive(ArchiveDTO dto);

    /**
     * 办理出院结算，释放病床。
     */
    Result<ArchiveVO> discharge(Long archiveId);

    /**
     * 住院费用续缴，余额不足时恢复为住院中状态。
     */
    Result<ArchiveVO> recharge(Long archiveId, BigDecimal amount);
}

