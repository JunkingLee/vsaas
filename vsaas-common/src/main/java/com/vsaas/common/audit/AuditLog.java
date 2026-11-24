package com.vsaas.common.audit;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String action;
    private String resource;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;
}
