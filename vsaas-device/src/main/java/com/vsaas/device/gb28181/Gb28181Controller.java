package com.vsaas.device.gb28181;

import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/gb28181")
@RequiredArgsConstructor
public class Gb28181Controller {

    private static final Map<String, Gb28181Device> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, List<Map<String, Object>>> CATALOG = new ConcurrentHashMap<>();

    @PostMapping("/register")
    public Result<Void> register(@RequestBody Gb28181Device device) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null && device.getTenantId() == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        if (device.getTenantId() == null) {
            device.setTenantId(tenantId);
        }
        device.setStatus(1);
        device.setRegisterTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device.setKeepaliveTime(device.getRegisterTime());
        REGISTRY.put(device.getDeviceId(), device);
        return Result.success();
    }

    @PostMapping("/keepalive")
    public Result<Void> keepalive(@RequestParam String deviceId) {
        Gb28181Device d = REGISTRY.get(deviceId);
        if (d == null) {
            throw new BusinessException(404, "设备未注册");
        }
        d.setKeepaliveTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        d.setStatus(1);
        return Result.success();
    }

    @PostMapping("/catalog")
    public Result<Void> catalog(@RequestParam String deviceId, @RequestBody List<Map<String, Object>> channels) {
        if (!REGISTRY.containsKey(deviceId)) {
            throw new BusinessException(404, "设备未注册");
        }
        CATALOG.put(deviceId, channels);
        return Result.success();
    }

    @GetMapping("/devices")
    public Result<List<Gb28181Device>> listDevices() {
        Long tenantId = TenantContext.getTenantId();
        List<Gb28181Device> list = new ArrayList<>();
        for (Gb28181Device d : REGISTRY.values()) {
            if (tenantId == null || tenantId.equals(d.getTenantId())) {
                list.add(d);
            }
        }
        return Result.success(list);
    }

    @GetMapping("/catalog/{deviceId}")
    public Result<List<Map<String, Object>>> getCatalog(@PathVariable String deviceId) {
        return Result.success(CATALOG.getOrDefault(deviceId, Collections.emptyList()));
    }

    @PostMapping("/invite")
    public Result<Map<String, String>> invite(@RequestBody InviteRequest req) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        Gb28181Device d = REGISTRY.get(req.getDeviceId());
        if (d == null || d.getStatus() != 1) {
            throw new BusinessException(400, "设备不在线或不存在");
        }
        String streamKey = "gb_" + tenantId + "_" + req.getDeviceId() + "_" + req.getChannelId();
        Map<String, String> data = new HashMap<>();
        data.put("streamKey", streamKey);
        data.put("message", "请向 SIP 设备发送 INVITE，并将流推送到 SRS，streamKey=" + streamKey);
        data.put("ssrc", String.valueOf(System.currentTimeMillis() % 100000000));
        return Result.success(data);
    }

    @PostMapping("/bye")
    public Result<Void> bye(@RequestParam String deviceId, @RequestParam String channelId) {
        return Result.success();
    }

    @Data
    public static class InviteRequest {
        private String deviceId;
        private String channelId;
    }
}
