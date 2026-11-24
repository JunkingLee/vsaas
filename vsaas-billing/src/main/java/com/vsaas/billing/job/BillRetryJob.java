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
public class BillRetryJob {

    private final BillingEngineService billingEngineService;
    private final BillingBillMapper billMapper;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "${vsaas.billing.retry-cron:0 30 * * * ?}")
    @SchedulerLock(name = "retryMissingBills", lockAtMostFor = "PT2H", lockAtLeastFor = "PT30M")
    public void retryMissingBills() {
        String billMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Long> tenantIds;
        try {
            tenantIds = jdbcTemplate.queryForList("SELECT id FROM sys_tenant WHERE status = 1", Long.class);
        } catch (Exception e) {
            return;
        }
        int fixed = 0;
        for (Long tenantId : tenantIds) {
            long exist = billMapper.selectCount(new LambdaQueryWrapper<BillingBill>()
                    .eq(BillingBill::getTenantId, tenantId)
                    .eq(BillingBill::getBillMonth, billMonth));
            if (exist > 0) continue;
            try {
                billingEngineService.generateMonthlyBill(tenantId, billMonth);
                fixed++;
                log.info("补偿出账成功 tenantId={} month={}", tenantId, billMonth);
            } catch (Exception ex) {
                log.warn("补偿出账失败 tenantId={}: {}", tenantId, ex.getMessage());
            }
        }
        if (fixed > 0) {
            log.info("本轮补偿出账完成 fixed={}", fixed);
        }
    }
}
