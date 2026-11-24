package com.vsaas.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.common.core.BusinessException;
import com.vsaas.device.entity.DeviceChannel;
import com.vsaas.device.entity.DeviceInfo;
import com.vsaas.device.mapper.DeviceChannelMapper;
import com.vsaas.device.mapper.DeviceInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceChannelMapper channelMapper;
    private final JdbcTemplate jdbcTemplate;

    public void checkCanAddDevice(Long tenantId) {
        checkNotInArrears(tenantId);
        int maxChannels = resolveMaxChannels(tenantId);
        long current = channelMapper.selectCount(
                new LambdaQueryWrapper<DeviceChannel>().eq(DeviceChannel::getTenantId, tenantId));
        long devices = deviceInfoMapper.selectCount(
                new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getTenantId, tenantId));
        if (current >= maxChannels || devices >= maxChannels) {
            throw new BusinessException(403, "已达租户最大路数配额: " + maxChannels);
        }
    }

    public void checkCanAddChannel(Long tenantId) {
        checkNotInArrears(tenantId);
        int maxChannels = resolveMaxChannels(tenantId);
        long current = channelMapper.selectCount(
                new LambdaQueryWrapper<DeviceChannel>().eq(DeviceChannel::getTenantId, tenantId));
        if (current >= maxChannels) {
            throw new BusinessException(403, "已达租户最大路数配额: " + maxChannels);
        }
    }

    private int resolveMaxChannels(Long tenantId) {
        try {
            Integer max = jdbcTemplate.query(
                    """
                    SELECT p.max_channels FROM billing_tenant_package tp
                    JOIN billing_package p ON p.id = tp.package_id
                    WHERE tp.tenant_id = ? AND tp.status = 1
                    ORDER BY tp.id DESC LIMIT 1
                    """,
                    rs -> rs.next() ? rs.getInt(1) : null,
                    tenantId
            );
            if (max != null && max > 0) return max;
        } catch (Exception ignored) {
        }
        return 100;
    }

    private void checkNotInArrears(Long tenantId) {
        try {
            Integer cnt = jdbcTemplate.query(
                    """
                    SELECT COUNT(*) FROM billing_bill
                    WHERE tenant_id = ? AND status = 0
                      AND created_at < NOW() - INTERVAL '15 days'
                    """,
                    rs -> rs.next() ? rs.getInt(1) : 0,
                    tenantId
            );
            if (cnt != null && cnt > 0) {
                throw new BusinessException(402, "账户存在逾期未付账单，请先结清后再新增设备/通道");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) {
        }
    }
}
