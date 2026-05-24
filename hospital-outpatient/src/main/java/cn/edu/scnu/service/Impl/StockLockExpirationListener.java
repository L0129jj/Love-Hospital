package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Drug;
import cn.edu.scnu.entity.PrescriptionDetail;
import cn.edu.scnu.mapper.DrugMapper;
import cn.edu.scnu.mapper.PrescriptionDetailMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis Key 过期事件监听器。
 * 当处方库存锁定 Key 过期时（即超时未缴费），自动恢复被锁定的药品库存。
 */
@Slf4j
@Component
public class StockLockExpirationListener extends KeyExpirationEventMessageListener {

    private static final String STOCK_LOCK_PREFIX = "prescription:stock_lock:";

    @Autowired
    private PrescriptionDetailMapper detailMapper;

    @Autowired
    private DrugMapper drugMapper;

    public StockLockExpirationListener(RedisMessageListenerContainer container) {
        super(container);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (!expiredKey.startsWith(STOCK_LOCK_PREFIX)) {
            return;
        }

        String prescIdStr = expiredKey.substring(STOCK_LOCK_PREFIX.length());
        try {
            Long prescriptionId = Long.valueOf(prescIdStr);
            restoreStock(prescriptionId);
        } catch (NumberFormatException e) {
            log.warn("无法解析处方ID from expired key: {}", expiredKey);
        }
    }

    /**
     * 恢复被锁定的库存：根据处方明细回加药品库存
     */
    private void restoreStock(Long prescriptionId) {
        List<PrescriptionDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<PrescriptionDetail>()
                        .eq(PrescriptionDetail::getPrescriptionId, prescriptionId));

        if (details.isEmpty()) {
            log.warn("处方 {} 无明细记录，跳过库存恢复", prescriptionId);
            return;
        }

        for (PrescriptionDetail detail : details) {
            Drug drug = drugMapper.selectById(detail.getDrugId());
            if (drug != null) {
                drug.setStock(drug.getStock() + detail.getQuantity());
                drugMapper.updateById(drug);
                log.info("库存恢复：药品ID={}, 恢复数量={}, 当前库存={}",
                        drug.getId(), detail.getQuantity(), drug.getStock());
            }
        }
        log.info("处方 {} 超时未缴费，库存已全部恢复", prescriptionId);
    }
}
