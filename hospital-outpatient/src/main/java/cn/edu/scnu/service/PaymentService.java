package cn.edu.scnu.service;

import cn.edu.scnu.dto.PaymentDTO;
import cn.edu.scnu.entity.Payment;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.vo.PaymentVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PaymentService extends IService<Payment> {

    /**
     * 缴费：保存缴费记录 + 更新处方状态 + 更新挂号状态 + 确认库存扣减（删除Redis锁定）
     */
    Result<PaymentVO> pay(PaymentDTO dto);
}

