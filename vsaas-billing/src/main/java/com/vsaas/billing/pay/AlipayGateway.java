package com.vsaas.billing.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "vsaas.pay.provider", havingValue = "alipay")
public class AlipayGateway implements PaymentGateway {

    @Value("${vsaas.pay.alipay.app-id:}")
    private String appId;
    @Value("${vsaas.pay.alipay.notify-url:http://127.0.0.1:8007/api/billing/pay/callback}")
    private String notifyUrl;
    @Value("${vsaas.pay.alipay.return-url:}")
    private String returnUrl;

    @Override
    public Map<String, Object> createPayment(Long tenantId, Long billId, String billNo, BigDecimal amount) {
        String tradeNo = "ALI" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        Map<String, Object> data = new HashMap<>();
        data.put("provider", "alipay");
        data.put("tradeNo", tradeNo);
        data.put("billId", billId);
        data.put("billNo", billNo);
        data.put("amount", amount);
        data.put("appId", appId);
        data.put("notifyUrl", notifyUrl);
        data.put("returnUrl", returnUrl);
        data.put("message", "请接入支付宝 SDK");
        return data;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        return "TRADE_SUCCESS".equalsIgnoreCase(params.getOrDefault("trade_status",
                params.getOrDefault("status", "")));
    }
}
