package com.vsaas.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "rabbitEventPublisher")
public class LocalEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        if (event.getOccurAt() == null) {
            event.setOccurAt(java.time.LocalDateTime.now());
        }
        log.debug("LOCAL_EVENT type={} tenant={} bizId={}", event.getType(), event.getTenantId(), event.getBizId());
        applicationEventPublisher.publishEvent(event);
    }
}
