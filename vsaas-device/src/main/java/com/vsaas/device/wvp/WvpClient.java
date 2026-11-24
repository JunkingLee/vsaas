package com.vsaas.device.wvp;

import com.vsaas.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WvpClient {

    private final WvpProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isEnabled() {
        return props.isEnabled();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listDevices() {
        ensureEnabled();
        String url = props.getBaseUrl() + "/api/device/query/devices?page=1&count=100";
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity(), Map.class);
        Map body = resp.getBody();
        if (body == null) return Collections.emptyList();
        Object data = body.get("data");
        if (data instanceof Map && ((Map<?, ?>) data).get("list") instanceof List) {
            return (List<Map<String, Object>>) ((Map<?, ?>) data).get("list");
        }
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listChannels(String deviceId) {
        ensureEnabled();
        String url = props.getBaseUrl() + "/api/device/query/devices/" + deviceId + "/channels?page=1&count=100";
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity(), Map.class);
        Map body = resp.getBody();
        if (body == null) return Collections.emptyList();
        Object data = body.get("data");
        if (data instanceof Map && ((Map<?, ?>) data).get("list") instanceof List) {
            return (List<Map<String, Object>>) ((Map<?, ?>) data).get("list");
        }
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> startPlay(String deviceId, String channelId) {
        ensureEnabled();
        String url = props.getBaseUrl() + "/api/play/start/" + deviceId + "/" + channelId;
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity(), Map.class);
        Map body = resp.getBody();
        if (body == null) {
            throw new BusinessException(502, "WVP 点播无响应");
        }
        Object data = body.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return body;
    }

    public void stopPlay(String deviceId, String channelId) {
        ensureEnabled();
        String url = props.getBaseUrl() + "/api/play/stop/" + deviceId + "/" + channelId;
        restTemplate.exchange(url, HttpMethod.GET, entity(), Map.class);
    }

    private HttpEntity<Void> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (props.getAccessToken() != null && !props.getAccessToken().isEmpty()) {
            headers.set("access-token", props.getAccessToken());
            headers.setBearerAuth(props.getAccessToken());
        }
        return new HttpEntity<>(headers);
    }

    private void ensureEnabled() {
        if (!props.isEnabled()) {
            throw new BusinessException(400, "未启用 WVP 对接，请配置 vsaas.wvp.enabled=true");
        }
        if (props.getBaseUrl() == null || props.getBaseUrl().isEmpty()) {
            throw new BusinessException(500, "vsaas.wvp.base-url 未配置");
        }
    }
}
