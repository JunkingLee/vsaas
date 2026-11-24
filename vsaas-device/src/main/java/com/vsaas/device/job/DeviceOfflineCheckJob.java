package com.vsaas.device.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.device.entity.DeviceChannel;
import com.vsaas.device.entity.DeviceInfo;
import com.vsaas.device.mapper.DeviceChannelMapper;
import com.vsaas.device.mapper.DeviceInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.vsaas.common.mq.DomainEvent;
import com.vsaas.common.mq.EventPublisher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceOfflineCheckJob {

    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceChannelMapper channelMapper;
    private final EventPublisher eventPublisher;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vsaas.device.heartbeat-timeout-seconds:120}")
    private long heartbeatTimeoutSeconds;

    @Value("${vsaas.alarm.report-url:http://127.0.0.1:8006/api/alarm/report}")
    private String alarmReportUrl;

    @Scheduled(fixedDelayString = "${vsaas.device.offline-check-interval-ms:30000}")
    @SchedulerLock(name = "checkOffline", lockAtMostFor = "PT1M", lockAtLeastFor = "PT20S")
    public void checkOffline() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        List<DeviceInfo> onlineDevices = deviceInfoMapper.selectList(
                new LambdaQueryWrapper<DeviceInfo>()
                        .eq(DeviceInfo::getStatus, 1)
                        .and(w -> w.isNull(DeviceInfo::getLastOnlineAt)
                                .or()
                                .lt(DeviceInfo::getLastOnlineAt, threshold))
        );
        if (onlineDevices.isEmpty()) {
            return;
        }
        log.info("心跳超时设备数: {}", onlineDevices.size());
        for (DeviceInfo d : onlineDevices) {
            try {
                DeviceInfo upd = new DeviceInfo();
                upd.setId(d.getId());
                upd.setStatus(0);
                upd.setUpdatedAt(LocalDateTime.now());
                deviceInfoMapper.updateById(upd);

                DeviceChannel chUpd = new DeviceChannel();
                chUpd.setStatus(0);
                chUpd.setUpdatedAt(LocalDateTime.now());
                channelMapper.update(chUpd, new LambdaQueryWrapper<DeviceChannel>()
                        .eq(DeviceChannel::getDeviceId, d.getId()));

                reportOfflineAlarm(d);
                eventPublisher.publish(DomainEvent.builder()
                        .type("device.offline")
                        .tenantId(d.getTenantId())
                        .bizId(String.valueOf(d.getId()))
                        .payload(java.util.Map.of("deviceCode", d.getDeviceCode() != null ? d.getDeviceCode() : ""))
                        .build());
                log.info("设备已自动离线并告警 deviceId={} code={}", d.getId(), d.getDeviceCode());
            } catch (Exception e) {
                log.error("处理离线设备失败 id={}", d.getId(), e);
            }
        }
    }

    private void reportOfflineAlarm(DeviceInfo d) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Tenant-Id", String.valueOf(d.getTenantId()));
            headers.set("X-User-Id", "0");
            Map<String, Object> body = new HashMap<>();
            body.put("channelId", null);
            body.put("alarmType", "device_offline");
            body.put("alarmContent", "设备离线: " + d.getDeviceCode() + " / " + d.getDeviceName());
            body.put("alarmTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
            restTemplate.postForEntity(alarmReportUrl, new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.warn("上报离线告警失败: {}", e.getMessage());
        }
    }
}
