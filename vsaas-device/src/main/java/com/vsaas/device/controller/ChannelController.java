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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final DeviceChannelMapper channelMapper;
    private final DeviceInfoMapper deviceInfoMapper;
    private final QuotaService quotaService;

    @GetMapping("/list")
    public Result<Page<DeviceChannel>> list(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = requireTenant();
        Page<DeviceChannel> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceChannel> q = new LambdaQueryWrapper<DeviceChannel>()
                .eq(DeviceChannel::getTenantId, tenantId)
                .eq(deviceId != null, DeviceChannel::getDeviceId, deviceId)
                .orderByDesc(DeviceChannel::getId);
        return Result.success(channelMapper.selectPage(p, q));
    }

    @GetMapping("/by-device/{deviceId}")
    public Result<List<DeviceChannel>> listByDevice(@PathVariable Long deviceId) {
        Long tenantId = requireTenant();
        DeviceInfo device = deviceInfoMapper.selectById(deviceId);
        if (device == null || !device.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        List<DeviceChannel> list = channelMapper.selectList(
                new LambdaQueryWrapper<DeviceChannel>()
                        .eq(DeviceChannel::getTenantId, tenantId)
                        .eq(DeviceChannel::getDeviceId, deviceId)
                        .orderByAsc(DeviceChannel::getId));
        return Result.success(list);
    }

    @PostMapping
    public Result<DeviceChannel> create(@RequestBody DeviceChannel channel) {
        Long tenantId = requireTenant();
        quotaService.checkCanAddChannel(tenantId);
        if (channel.getDeviceId() == null) {
            throw new BusinessException(400, "设备ID不能为空");
        }
        DeviceInfo device = deviceInfoMapper.selectById(channel.getDeviceId());
        if (device == null || !device.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "设备不存在");
        }
        channel.setTenantId(tenantId);
        channel.setStatus(0);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());
        channelMapper.insert(channel);
        return Result.success(channel);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeviceChannel channel) {
        Long tenantId = requireTenant();
        DeviceChannel exist = channelMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "通道不存在");
        }
        channel.setId(id);
        channel.setTenantId(tenantId);
        channel.setUpdatedAt(LocalDateTime.now());
        channelMapper.updateById(channel);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = requireTenant();
        DeviceChannel exist = channelMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "通道不存在");
        }
        channelMapper.deleteById(id);
        return Result.success();
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        return tenantId;
    }
}
