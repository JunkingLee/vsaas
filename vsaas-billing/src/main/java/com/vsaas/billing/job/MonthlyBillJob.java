package com.vsaas.billing.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.billing.entity.BillingBill;
import com.vsaas.billing.mapper.BillingBillMapper;
import com.vsaas.billing.service.BillingEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyBillJob {

    private final BillingEngineService billingEngineService;
    private final BillingBillMapper billMapper;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "${vsaas.billing.monthly-cron:0 0 2 1 * ?}")
    @SchedulerLock(name = "generateLastMonthBills", lockAtMostFor = "PT6H", lockAtLeastFor = "PT1H")
    public void generateLastMonthBills() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String billMonth = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("开始自动出账 billMonth={}", billMonth);
        List<Long> tenantIds;
        try {
            tenantIds = jdbcTemplate.queryForList("SELECT id FROM sys_tenant WHERE status = 1", Long.class);
        } catch (Exception e) {
            tenantIds = jdbcTemplate.queryForList("SELECT DISTINCT tenant_id FROM billing_usage", Long.class);
        }
        int ok = 0, skip = 0, fail = 0;
        for (Long tenantId : tenantIds) {
            try {
                long exist = billMapper.selectCount(new LambdaQueryWrapper<BillingBill>()
                        .eq(BillingBill::getTenantId, tenantId)
                        .eq(BillingBill::getBillMonth, billMonth));
                if (exist > 0) { skip++; continue; }
                billingEngineService.generateMonthlyBill(tenantId, billMonth);
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("租户出账失败 tenantId={}", tenantId, e);
            }
        }
        log.info("自动出账完成 ok={} skip={} fail={}", ok, skip, fail);
    }
}
