package com.vsaas.billing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.billing.entity.BillingBill;
import com.vsaas.billing.entity.BillingPackage;
import com.vsaas.billing.entity.BillingUsage;
import com.vsaas.billing.mapper.BillingBillMapper;
import com.vsaas.billing.mapper.BillingUsageMapper;
import com.vsaas.billing.service.BillingEngineService;
import com.vsaas.billing.service.ArrearsService;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.common.mq.DomainEvent;
import com.vsaas.common.mq.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingUsageMapper usageMapper;
    private final BillingBillMapper billMapper;
    private final BillingEngineService billingEngineService;
    private final ArrearsService arrearsService;
    private final EventPublisher eventPublisher;

    @PostMapping("/usage/report")
    public Result<Void> reportUsage(@RequestBody BillingUsage usage) {
        Long tenantId = requireTenant();
        usage.setTenantId(tenantId);
        if (usage.getUsageTime() == null) {
            usage.setUsageTime(LocalDateTime.now());
        }
        usage.setCreatedAt(LocalDateTime.now());
        usageMapper.insert(usage);
        eventPublisher.publish(DomainEvent.builder()
                .type("usage.reported")
                .tenantId(tenantId)
                .bizId(usage.getBizId())
                .payload(java.util.Map.of(
                        "usageType", usage.getUsageType() != null ? usage.getUsageType() : "",
                        "usageValue", usage.getUsageValue() != null ? usage.getUsageValue().toString() : "0"
                ))
                .build());
        return Result.success();
    }

    @GetMapping("/usage/summary")
    public Result<Map<String, Object>> usageSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Long tenantId = requireTenant();
        List<BillingUsage> list = usageMapper.selectList(
                new LambdaQueryWrapper<BillingUsage>()
                        .eq(BillingUsage::getTenantId, tenantId)
                        .ge(BillingUsage::getUsageTime, startTime)
                        .le(BillingUsage::getUsageTime, endTime)
        );
        Map<String, BigDecimal> summary = new HashMap<>();
        for (BillingUsage u : list) {
            summary.merge(u.getUsageType(), u.getUsageValue(), BigDecimal::add);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("detailCount", list.size());
        return Result.success(result);
    }

    @GetMapping("/bill/list")
    public Result<Page<BillingBill>> billList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = requireTenant();
        Page<BillingBill> p = new Page<>(page, size);
        return Result.success(billMapper.selectPage(p,
                new LambdaQueryWrapper<BillingBill>()
                        .eq(BillingBill::getTenantId, tenantId)
                        .orderByDesc(BillingBill::getBillMonth)));
    }

    @GetMapping("/bill/{id}")
    public Result<BillingBill> billDetail(@PathVariable Long id) {
        Long tenantId = requireTenant();
        BillingBill bill = billMapper.selectById(id);
        if (bill == null || !bill.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "账单不存在");
        }
        return Result.success(bill);
    }

    @PostMapping("/bill/generate")
    public Result<BillingBill> generateBill(@RequestParam String billMonth) {
        Long tenantId = requireTenant();
        return Result.success(billingEngineService.generateMonthlyBill(tenantId, billMonth));
    }

    @GetMapping("/package/list")
    public Result<List<BillingPackage>> packageList() {
        return Result.success(billingEngineService.listPackages());
    }

    @GetMapping("/bill/preview")
    public Result<Map<String, Object>> preview(@RequestParam String billMonth) {
        Long tenantId = requireTenant();
        return Result.success(billingEngineService.previewAmount(tenantId, billMonth));
    }

    @PostMapping("/bill/generate/force")
    public Result<BillingBill> forceGenerate(@RequestParam String billMonth) {
        Long tenantId = requireTenant();
        billMapper.delete(new LambdaQueryWrapper<BillingBill>()
                .eq(BillingBill::getTenantId, tenantId)
                .eq(BillingBill::getBillMonth, billMonth));
        return Result.success(billingEngineService.generateMonthlyBill(tenantId, billMonth));
    }

    @GetMapping("/arrears/status")
    public Result<Map<String, Object>> arrearsStatus() {
        Long tenantId = requireTenant();
        return Result.success(arrearsService.status(tenantId));
    }

    @PostMapping("/bill/{id}/pay")
    public Result<Void> payBill(@PathVariable Long id) {
        Long tenantId = requireTenant();
        BillingBill bill = billMapper.selectById(id);
        if (bill == null || !bill.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "账单不存在");
        }
        BillingBill upd = new BillingBill();
        upd.setId(id);
        upd.setStatus(1);
        upd.setPaidAmount(bill.getTotalAmount());
        billMapper.updateById(upd);
        return Result.success();
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        return tenantId;
    }
}
