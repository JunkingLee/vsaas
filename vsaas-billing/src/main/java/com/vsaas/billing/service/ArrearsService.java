package com.vsaas.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.billing.entity.BillingBill;
import com.vsaas.billing.mapper.BillingBillMapper;
import com.vsaas.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArrearsService {

    private final BillingBillMapper billMapper;

    @Value("${vsaas.billing.overdue-days:15}")
    private int overdueDays;

    @Value("${vsaas.billing.arrears-enforce:true}")
    private boolean arrearsEnforce;

    public Map<String, Object> status(Long tenantId) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(overdueDays);
        List<BillingBill> unpaid = billMapper.selectList(
                new LambdaQueryWrapper<BillingBill>()
                        .eq(BillingBill::getTenantId, tenantId)
                        .eq(BillingBill::getStatus, 0)
                        .lt(BillingBill::getCreatedAt, deadline)
        );
        Map<String, Object> map = new HashMap<>();
        map.put("inArrears", !unpaid.isEmpty());
        map.put("overdueBillCount", unpaid.size());
        map.put("overdueDays", overdueDays);
        map.put("enforce", arrearsEnforce);
        return map;
    }

    public boolean inArrears(Long tenantId) {
        Object v = status(tenantId).get("inArrears");
        return Boolean.TRUE.equals(v);
    }

    public void checkOrThrow(Long tenantId) {
        if (!arrearsEnforce) {
            return;
        }
        if (inArrears(tenantId)) {
            throw new BusinessException(402, "账户存在逾期未付账单，请先结清后再操作");
        }
    }
}
