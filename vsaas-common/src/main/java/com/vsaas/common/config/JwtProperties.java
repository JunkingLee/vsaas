package com.vsaas.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vsaas.jwt")
public class JwtProperties {
    private String secret = "vsaas-jwt-secret-key-2026-please-change-it-to-very-long-string-at-least-32-bytes";
    private long expireMs = 7 * 24 * 60 * 60 * 1000L;
    private String key = "user-key";
}
