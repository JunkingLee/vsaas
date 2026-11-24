package com.vsaas.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.device.entity.DeviceChannel;
import com.vsaas.device.entity.DeviceInfo;
import com.vsaas.device.mapper.DeviceChannelMapper;
import com.vsaas.device.mapper.DeviceInfoMapper;
import com.vsaas.device.service.QuotaService;
import com.vsaas.device.service.OrgScopeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceChannelMapper channelMapper;
    private final QuotaService quotaService;
    private final OrgScopeService orgScopeService;

    @GetMapping("/list")
    public Result<Page<DeviceInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = requireTenant();
        Page<DeviceInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DeviceInfo> wrapper = new LambdaQueryWrapper<DeviceInfo>()
                .eq(DeviceInfo::getTenantId, tenantId)
                .orderByDesc(DeviceInfo::getId);
        Long userId = TenantContext.getUserId();
        java.util.List<Long> deviceIds = orgScopeService.visibleDeviceIds(tenantId, userId);
        if (deviceIds != null) {
            if (deviceIds.isEmpty()) {
                return Result.success(new Page<>(page, size));
            }
            wrapper.in(DeviceInfo::getId, deviceIds);
        }
        return Result.success(deviceInfoMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping
    public Result<DeviceInfo> add(@RequestBody DeviceInfo device) {
        Long tenantId = requireTenant();
        quotaService.checkCanAddDevice(tenantId);
        device.setTenantId(tenantId);
        device.setStatus(0);
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        deviceInfoMapper.insert(device);
        return Result.success(device);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeviceInfo device) {
        Long tenantId = requireTenant();
        DeviceInfo exist = deviceInfoMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        device.setId(id);
        device.setTenantId(tenantId);
        device.setUpdatedAt(LocalDateTime.now());
        deviceInfoMapper.updateById(device);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = requireTenant();
        DeviceInfo exist = deviceInfoMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        deviceInfoMapper.deleteById(id);
        channelMapper.delete(new LambdaQueryWrapper<DeviceChannel>()
                .eq(DeviceChannel::getDeviceId, id)
                .eq(DeviceChannel::getTenantId, tenantId));
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DeviceInfo> detail(@PathVariable Long id) {
        Long tenantId = requireTenant();
        DeviceInfo device = deviceInfoMapper.selectById(id);
        if (device == null || !device.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        return Result.success(device);
    }

    @GetMapping("/{id}/with-channels")
    public Result<Map<String, Object>> detailWithChannels(@PathVariable Long id) {
        Long tenantId = requireTenant();
        DeviceInfo device = deviceInfoMapper.selectById(id);
        if (device == null || !device.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        List<DeviceChannel> channels = channelMapper.selectList(
                new LambdaQueryWrapper<DeviceChannel>()
                        .eq(DeviceChannel::getTenantId, tenantId)
                        .eq(DeviceChannel::getDeviceId, id)
                        .orderByAsc(DeviceChannel::getId));
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("device", device);
        data.put("channels", channels);
        return Result.success(data);
    }

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestBody HeartbeatRequest req) {
        if (req.getDeviceCode() == null) {
            throw new BusinessException(400, "deviceCode 不能为空");
        }
        Long tenantId = req.getTenantId() != null ? req.getTenantId() : TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        DeviceInfo device = deviceInfoMapper.selectOne(
                new LambdaQueryWrapper<DeviceInfo>()
                        .eq(DeviceInfo::getTenantId, tenantId)
                        .eq(DeviceInfo::getDeviceCode, req.getDeviceCode())
                        .last("LIMIT 1"));
        if (device == null) {
            throw new BusinessException(404, "设备不存在");
        }
        DeviceInfo update = new DeviceInfo();
        update.setId(device.getId());
        update.setStatus(1);
        update.setLastOnlineAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        deviceInfoMapper.updateById(update);
        if (req.getOnlineChannelCodes() != null && !req.getOnlineChannelCodes().isEmpty()) {
            DeviceChannel offline = new DeviceChannel();
            offline.setStatus(0);
            offline.setUpdatedAt(LocalDateTime.now());
            channelMapper.update(offline, new LambdaQueryWrapper<DeviceChannel>()
                    .eq(DeviceChannel::getDeviceId, device.getId())
                    .eq(DeviceChannel::getTenantId, tenantId));
            for (String code : req.getOnlineChannelCodes()) {
                DeviceChannel chUpdate = new DeviceChannel();
                chUpdate.setStatus(1);
                chUpdate.setUpdatedAt(LocalDateTime.now());
                channelMapper.update(chUpdate, new LambdaQueryWrapper<DeviceChannel>()
                        .eq(DeviceChannel::getDeviceId, device.getId())
                        .eq(DeviceChannel::getChannelCode, code)
                        .eq(DeviceChannel::getTenantId, tenantId));
            }
        }
        return Result.success();
    }

    @GetMapping("/online-stats")
    public Result<OnlineStats> onlineStats() {
        Long tenantId = requireTenant();
        long total = deviceInfoMapper.selectCount(
                new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getTenantId, tenantId));
        long online = deviceInfoMapper.selectCount(
                new LambdaQueryWrapper<DeviceInfo>()
                        .eq(DeviceInfo::getTenantId, tenantId)
                        .eq(DeviceInfo::getStatus, 1));
        OnlineStats stats = new OnlineStats();
        stats.setTotal(total);
        stats.setOnline(online);
        stats.setOffline(total - online);
        return Result.success(stats);
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        return tenantId;
    }

    @Data
    public static class HeartbeatRequest {
        private Long tenantId;
        private String deviceCode;
        private List<String> onlineChannelCodes;
    }

    @Data
    public static class OnlineStats {
        private long total;
        private long online;
        private long offline;
    }
}
