package com.vsaas.billing.controller;

import com.vsaas.billing.entity.BillingBill;
import com.vsaas.billing.mapper.BillingBillMapper;
import com.vsaas.billing.pay.PaymentGateway;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/billing/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGateway paymentGateway;
    private final BillingBillMapper billMapper;

    @PostMapping("/create/{billId}")
    public Result<Map<String, Object>> create(@PathVariable Long billId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        BillingBill bill = billMapper.selectById(billId);
        if (bill == null || !bill.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "账单不存在");
        }
        if (bill.getStatus() != null && bill.getStatus() == 1) {
            throw new BusinessException(400, "账单已支付");
        }
        return Result.success(paymentGateway.createPayment(
                tenantId, bill.getId(), bill.getBillNo(), bill.getTotalAmount()));
    }

    @PostMapping("/mock/confirm")
    public Result<Void> mockConfirm(@RequestParam Long billId,
                                    @RequestParam(required = false) String tradeNo) {
        Long tenantId = TenantContext.getTenantId();
        BillingBill bill = billMapper.selectById(billId);
        if (bill == null || (tenantId != null && !bill.getTenantId().equals(tenantId))) {
            throw new BusinessException(404, "账单不存在");
        }
        boolean ok = paymentGateway.handleCallback(Map.of(
                "status", "SUCCESS",
                "tradeNo", tradeNo != null ? tradeNo : "",
                "billId", String.valueOf(billId)
        ));
        if (ok) {
            BillingBill upd = new BillingBill();
            upd.setId(billId);
            upd.setStatus(1);
            upd.setPaidAmount(bill.getTotalAmount());
            billMapper.updateById(upd);
        }
        return Result.success();
    }

    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> params) {
        boolean ok = paymentGateway.handleCallback(params);
        if (ok && params.get("billId") != null) {
            try {
                Long billId = Long.parseLong(params.get("billId"));
                BillingBill bill = billMapper.selectById(billId);
                if (bill != null && (bill.getStatus() == null || bill.getStatus() == 0)) {
                    BillingBill upd = new BillingBill();
                    upd.setId(billId);
                    upd.setStatus(1);
                    upd.setPaidAmount(bill.getTotalAmount());
                    billMapper.updateById(upd);
                }
            } catch (Exception ignored) {
            }
        }
        return ok ? "SUCCESS" : "FAIL";
    }
}
