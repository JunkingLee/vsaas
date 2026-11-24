package com.vsaas.device.wvp;

import com.vsaas.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wvp")
@RequiredArgsConstructor
public class WvpController {

    private final WvpClient wvpClient;

    @GetMapping("/enabled")
    public Result<Boolean> enabled() {
        return Result.success(wvpClient.isEnabled());
    }

    @GetMapping("/devices")
    public Result<List<Map<String, Object>>> devices() {
        return Result.success(wvpClient.listDevices());
    }

    @GetMapping("/devices/{deviceId}/channels")
    public Result<List<Map<String, Object>>> channels(@PathVariable String deviceId) {
        return Result.success(wvpClient.listChannels(deviceId));
    }

    @PostMapping("/play/start")
    public Result<Map<String, Object>> playStart(@RequestParam String deviceId,
                                                 @RequestParam String channelId) {
        return Result.success(wvpClient.startPlay(deviceId, channelId));
    }

    @PostMapping("/play/stop")
    public Result<Void> playStop(@RequestParam String deviceId,
                                 @RequestParam String channelId) {
        wvpClient.stopPlay(deviceId, channelId);
        return Result.success();
    }
}
