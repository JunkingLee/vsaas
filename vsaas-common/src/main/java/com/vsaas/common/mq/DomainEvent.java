package com.vsaas.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {
    private String type;
    private Long tenantId;
    private String bizId;
    private Map<String, Object> payload;
    private LocalDateTime occurAt;
}
