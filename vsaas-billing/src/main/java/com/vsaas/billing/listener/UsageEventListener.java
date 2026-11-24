package com.vsaas.billing.listener;

import com.vsaas.common.mq.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UsageEventListener {

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        if (event == null || !"usage.reported".equals(event.getType())) {
            return;
        }
        log.info("用量事件聚合 tenant={} payload={}", event.getTenantId(), event.getPayload());
    }
}
