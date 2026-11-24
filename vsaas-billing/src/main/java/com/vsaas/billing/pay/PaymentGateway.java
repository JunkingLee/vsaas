package com.vsaas.billing.pay;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {
    Map<String, Object> createPayment(Long tenantId, Long billId, String billNo, BigDecimal amount);
    boolean handleCallback(Map<String, String> params);
}
