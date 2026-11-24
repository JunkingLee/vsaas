package com.vsaas.billing.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "vsaas.pay.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public Map<String, Object> createPayment(Long tenantId, Long billId, String billNo, BigDecimal amount) {
        String tradeNo = "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Map<String, Object> data = new HashMap<>();
        data.put("provider", "mock");
        data.put("tradeNo", tradeNo);
        data.put("billId", billId);
        data.put("billNo", billNo);
        data.put("amount", amount);
        data.put("payUrl", "/api/billing/pay/mock/confirm?tradeNo=" + tradeNo + "&billId=" + billId);
        data.put("message", "模拟支付");
        return data;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        return "SUCCESS".equalsIgnoreCase(params.getOrDefault("status", "SUCCESS"));
    }
}
