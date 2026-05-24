package cn.edu.scnu.controller;

import cn.edu.scnu.result.Result;
import cn.edu.scnu.dto.PaymentDTO;
import jakarta.validation.Valid;
import cn.edu.scnu.entity.Payment;
import cn.edu.scnu.service.PaymentService;
import cn.edu.scnu.vo.PaymentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/outpatient/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 缴费（联动更新处方状态 + 确认库存扣减）
     */
    @PostMapping("")
    public Result<PaymentVO> add(@Valid @RequestBody PaymentDTO dto) {
        return paymentService.pay(dto);
    }

    /**
     * 患者缴费记录
     */
    @GetMapping("/by-patient")
    public Result<List<PaymentVO>> byPatient(@RequestParam Long patientId) {
        List<PaymentVO> list = paymentService.lambdaQuery()
                .eq(Payment::getPatientId, patientId)
                .orderByDesc(Payment::getPayTime)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    private PaymentVO toVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return vo;
    }
}

