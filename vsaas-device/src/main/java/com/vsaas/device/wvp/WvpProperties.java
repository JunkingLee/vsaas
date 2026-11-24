package com.vsaas.device.wvp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vsaas.wvp")
public class WvpProperties {
    private boolean enabled = false;
    private String baseUrl = "http://127.0.0.1:18080";
    private String accessToken = "";
}
