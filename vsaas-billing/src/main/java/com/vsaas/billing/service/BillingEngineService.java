package com.vsaas.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.billing.entity.BillingBill;
import com.vsaas.billing.entity.BillingPackage;
import com.vsaas.billing.entity.BillingUsage;
import com.vsaas.billing.mapper.BillingBillMapper;
import com.vsaas.billing.mapper.BillingPackageMapper;
import com.vsaas.billing.mapper.BillingUsageMapper;
import com.vsaas.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingEngineService {

    private final BillingUsageMapper usageMapper;
    private final BillingBillMapper billMapper;
    private final BillingPackageMapper packageMapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public BillingBill generateMonthlyBill(Long tenantId, String billMonth) {
        long exist = billMapper.selectCount(new LambdaQueryWrapper<BillingBill>()
                .eq(BillingBill::getTenantId, tenantId)
                .eq(BillingBill::getBillMonth, billMonth));
        if (exist > 0) {
            throw new BusinessException(400, "该月账单已存在");
        }

        YearMonth ym = YearMonth.parse(billMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal total = resolvePackagePrice(tenantId);
        if (total == null) {
            total = BigDecimal.ZERO;
            List<BillingUsage> usages = usageMapper.selectList(new LambdaQueryWrapper<BillingUsage>()
                    .eq(BillingUsage::getTenantId, tenantId)
                    .ge(BillingUsage::getUsageTime, start)
                    .le(BillingUsage::getUsageTime, end));
            for (BillingUsage u : usages) {
                if ("channel".equals(u.getUsageType())) {
                    total = total.add(u.getUsageValue().multiply(new BigDecimal("10")));
                } else if ("storage".equals(u.getUsageType())) {
                    total = total.add(u.getUsageValue().multiply(new BigDecimal("0.5")));
                }
            }
        }

        BillingBill bill = new BillingBill();
        bill.setTenantId(tenantId);
        bill.setBillNo("BILL-" + billMonth.replace("-", "") + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        bill.setBillMonth(billMonth);
        bill.setTotalAmount(total);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus(0);
        bill.setStartTime(start);
        bill.setEndTime(end);
        bill.setCreatedAt(LocalDateTime.now());
        billMapper.insert(bill);
        return bill;
    }

    public BigDecimal resolvePackagePrice(Long tenantId) {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT p.price FROM billing_tenant_package tp
                    JOIN billing_package p ON p.id = tp.package_id
                    WHERE tp.tenant_id = ? AND tp.status = 1
                      AND (tp.end_time IS NULL OR tp.end_time > NOW())
                    ORDER BY tp.id DESC LIMIT 1
                    """,
                    rs -> rs.next() ? rs.getBigDecimal(1) : null,
                    tenantId
            );
        } catch (Exception e) {
            return null;
        }
    }

    public Long resolvePackageId(Long tenantId) {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT package_id FROM billing_tenant_package
                    WHERE tenant_id = ? AND status = 1
                      AND (end_time IS NULL OR end_time > NOW())
                    ORDER BY id DESC LIMIT 1
                    """,
                    rs -> rs.next() ? rs.getLong(1) : null,
                    tenantId
            );
        } catch (Exception e) {
            return null;
        }
    }

    public List<BillingPackage> listPackages() {
        return packageMapper.selectList(new LambdaQueryWrapper<BillingPackage>()
                .eq(BillingPackage::getStatus, 1)
                .orderByAsc(BillingPackage::getId));
    }

    public Map<String, Object> previewAmount(Long tenantId, String billMonth) {
        YearMonth ym = YearMonth.parse(billMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal packagePrice = resolvePackagePrice(tenantId);
        Map<String, Object> result = new HashMap<>();
        if (packagePrice != null) {
            result.put("pricingMode", "package");
            result.put("estimatedAmount", packagePrice);
            result.put("packageId", resolvePackageId(tenantId));
            return result;
        }
        List<BillingUsage> usages = usageMapper.selectList(new LambdaQueryWrapper<BillingUsage>()
                .eq(BillingUsage::getTenantId, tenantId)
                .ge(BillingUsage::getUsageTime, start)
                .le(BillingUsage::getUsageTime, end));
        Map<String, BigDecimal> summary = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (BillingUsage u : usages) {
            summary.merge(u.getUsageType(), u.getUsageValue(), BigDecimal::add);
            if ("channel".equals(u.getUsageType())) {
                total = total.add(u.getUsageValue().multiply(new BigDecimal("10")));
            } else if ("storage".equals(u.getUsageType())) {
                total = total.add(u.getUsageValue().multiply(new BigDecimal("0.5")));
            }
        }
        result.put("pricingMode", "usage");
        result.put("summary", summary);
        result.put("estimatedAmount", total);
        return result;
    }
}
