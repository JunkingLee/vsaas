package com.vsaas.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component("rabbitEventPublisher")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@ConditionalOnProperty(name = "vsaas.mq.rabbit-enabled", havingValue = "true")
public class RabbitEventPublisher implements EventPublisher {

    public static final String EXCHANGE = "vsaas.events";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(DomainEvent event) {
        if (event.getOccurAt() == null) {
            event.setOccurAt(java.time.LocalDateTime.now());
        }
        String routingKey = event.getType() != null ? event.getType() : "unknown";
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
        log.info("RABBIT_EVENT type={} tenant={}", event.getType(), event.getTenantId());
    }
}
