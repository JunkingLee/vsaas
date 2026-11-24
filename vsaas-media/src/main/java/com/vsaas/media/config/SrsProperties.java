package com.vsaas.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vsaas.srs")
public class SrsProperties {
    private String httpHost = "http://127.0.0.1:8080";
    private String apiHost = "http://127.0.0.1:1985";
    private String app = "live";
    private boolean webrtcEnabled = true;
}
