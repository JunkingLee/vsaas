package com.vsaas.alarm.listener;

import com.vsaas.alarm.ws.AlarmWsHub;
import com.vsaas.common.mq.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmEventListener {

    private final AlarmWsHub alarmWsHub;

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        if (event == null || !"alarm.created".equals(event.getType())) {
            return;
        }
        log.info("消费告警事件 tenant={} bizId={}", event.getTenantId(), event.getBizId());
        alarmWsHub.pushToTenant(event.getTenantId(), event);
    }
}
