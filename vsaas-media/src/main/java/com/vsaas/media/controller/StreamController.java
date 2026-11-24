package com.vsaas.media.controller;

import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.media.service.StreamService;
import com.vsaas.common.security.PlayTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;
    private final PlayTokenService playTokenService;

    @GetMapping("/play/{streamKey}")
    public Result<Map<String, String>> getPlayUrls(@PathVariable String streamKey) {
        requireTenant();
        return Result.success(playTokenService.appendToken(streamService.buildPlayUrls(streamKey), streamKey));
    }

    @GetMapping("/play/channel/{channelId}")
    public Result<Map<String, String>> getPlayUrlsByChannel(@PathVariable Long channelId) {
        Long tenantId = requireTenant();
        String streamKey = tenantId + "_" + channelId;
        Map<String, String> urls = playTokenService.appendToken(streamService.buildPlayUrls(streamKey), streamKey);
        urls.put("streamKey", streamKey);
        return Result.success(urls);
    }

    @GetMapping("/publish/{streamKey}")
    public Result<String> getPublishUrl(@PathVariable String streamKey) {
        requireTenant();
        return Result.success(streamService.buildPublishUrl(streamKey));
    }

    @GetMapping("/publish/channel/{channelId}")
    public Result<Map<String, String>> getPublishByChannel(@PathVariable Long channelId) {
        Long tenantId = requireTenant();
        String streamKey = tenantId + "_" + channelId;
        return Result.success(Map.of(
                "streamKey", streamKey,
                "rtmp", streamService.buildPublishUrl(streamKey)
        ));
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        return tenantId;
    }
}
