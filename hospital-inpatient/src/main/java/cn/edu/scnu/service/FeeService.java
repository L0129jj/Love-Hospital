package cn.edu.scnu.service;

import cn.edu.scnu.entity.Fee;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FeeService extends IService<Fee> {

    /**
     * 根据住院记录（巡诊）自动计算当日费用并扣减余额。
     * 费用构成：床位费 + 诊疗费 + 药品费
     *
     * @param recordId 住院记录 ID
     */
    void calculateDailyFee(Long recordId);
}
