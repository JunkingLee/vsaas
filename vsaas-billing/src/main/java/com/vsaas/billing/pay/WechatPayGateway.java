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
@ConditionalOnProperty(name = "vsaas.pay.provider", havingValue = "wechat")
public class WechatPayGateway implements PaymentGateway {

    @Value("${vsaas.pay.wechat.app-id:}")
    private String appId;
    @Value("${vsaas.pay.wechat.mch-id:}")
    private String mchId;
    @Value("${vsaas.pay.wechat.notify-url:http://127.0.0.1:8007/api/billing/pay/callback}")
    private String notifyUrl;

    @Override
    public Map<String, Object> createPayment(Long tenantId, Long billId, String billNo, BigDecimal amount) {
        String tradeNo = "WX" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        Map<String, Object> data = new HashMap<>();
        data.put("provider", "wechat");
        data.put("tradeNo", tradeNo);
        data.put("billId", billId);
        data.put("billNo", billNo);
        data.put("amount", amount);
        data.put("appId", appId);
        data.put("mchId", mchId);
        data.put("notifyUrl", notifyUrl);
        data.put("codeUrl", "weixin://wxpay/bizpayurl?pr=" + tradeNo);
        data.put("message", "请接入微信官方 SDK");
        return data;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        return "SUCCESS".equalsIgnoreCase(params.getOrDefault("trade_state",
                params.getOrDefault("status", "")));
    }
}
