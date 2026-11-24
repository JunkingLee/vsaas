package com.vsaas.common.mq;

public interface EventPublisher {
    void publish(DomainEvent event);
}
