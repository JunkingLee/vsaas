package com.vsaas.record.controller;

import com.vsaas.record.entity.RecordFile;
import com.vsaas.record.mapper.RecordFileMapper;
import com.vsaas.record.service.RecordStorageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.vsaas.common.mq.DomainEvent;
import com.vsaas.common.mq.EventPublisher;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/srs")
@RequiredArgsConstructor
public class SrsHookController {

    private final RecordFileMapper fileMapper;
    private final RecordStorageService storageService;
    private final EventPublisher eventPublisher;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vsaas.alarm.report-url:http://127.0.0.1:8006/api/alarm/report}")
    private String alarmReportUrl;

    @Value("${vsaas.record.upload-to-minio-on-dvr:true}")
    private boolean uploadToMinioOnDvr;

    @PostMapping("/on_publish")
    public Map<String, Object> onPublish(@RequestBody SrsHookEvent event) {
        log.info("SRS on_publish stream={} ip={}", event.getStream(), event.getIp());
        return Map.of("code", 0);
    }

    @PostMapping("/on_unpublish")
    public Map<String, Object> onUnpublish(@RequestBody SrsHookEvent event) {
        log.info("SRS on_unpublish stream={}", event.getStream());
        try {
            ParsedStream ps = parseStream(event.getStream());
            if (ps != null) {
                reportAlarm(ps.tenantId, ps.channelId, "stream_offline", "流中断: " + event.getStream());
            }
        } catch (Exception e) {
            log.warn("断流告警失败: {}", e.getMessage());
        }
        return Map.of("code", 0);
    }

    @PostMapping("/on_dvr")
    public Map<String, Object> onDvr(@RequestBody SrsHookEvent event) {
        log.info("SRS on_dvr stream={} file={}", event.getStream(), event.getFile());
        try {
            ParsedStream ps = parseStream(event.getStream());
            if (ps == null) {
                log.warn("无法解析 stream: {}", event.getStream());
                return Map.of("code", 0);
            }
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusHours(1);
            if (event.getDuration() != null && event.getDuration() > 0) {
                start = end.minusSeconds(event.getDuration().longValue());
            }
            String objectKey = storageService.buildObjectKey(ps.tenantId, ps.channelId, start, 1);
            String storePath = event.getFile() != null ? event.getFile() : objectKey;
            long fileSize = event.getFileSize() != null ? event.getFileSize() : 0L;
            if (uploadToMinioOnDvr && event.getFile() != null) {
                File local = new File(event.getFile());
                if (local.exists() && local.isFile()) {
                    try (FileInputStream in = new FileInputStream(local)) {
                        storageService.ensureBucket();
                        storePath = storageService.uploadStream(objectKey, in, local.length(), "video/mp4");
                        fileSize = local.length();
                        log.info("录制文件已上传 MinIO: {}", storePath);
                    } catch (Exception ex) {
                        log.warn("上传 MinIO 失败: {}", ex.getMessage());
                    }
                }
            }
            RecordFile file = new RecordFile();
            file.setTenantId(ps.tenantId);
            file.setChannelId(ps.channelId);
            file.setStartTime(start);
            file.setEndTime(end);
            file.setFilePath(storePath);
            file.setFileSize(fileSize);
            file.setCreatedAt(LocalDateTime.now());
            fileMapper.insert(file);
            eventPublisher.publish(DomainEvent.builder()
                    .type("record.ready")
                    .tenantId(ps.tenantId)
                    .bizId(String.valueOf(file.getId()))
                    .payload(java.util.Map.of("channelId", ps.channelId, "path", storePath != null ? storePath : ""))
                    .build());
            log.info("录制片段入库 id={} channelId={}", file.getId(), ps.channelId);
        } catch (Exception e) {
            log.error("处理 on_dvr 失败", e);
        }
        return Map.of("code", 0);
    }

    private ParsedStream parseStream(String stream) {
        if (stream == null || stream.isEmpty()) return null;
        String[] parts = stream.split("_");
        try {
            if (parts.length >= 2 && parts[0].matches("\\d+") && parts[1].matches("\\d+")) {
                return new ParsedStream(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
            }
            if (stream.startsWith("gb_") && parts.length >= 4) {
                Long tenantId = Long.parseLong(parts[1]);
                String last = parts[parts.length - 1];
                if (last.matches("\\d+")) {
                    return new ParsedStream(tenantId, Long.parseLong(last));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void reportAlarm(Long tenantId, Long channelId, String type, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-Id", String.valueOf(tenantId));
        headers.set("X-User-Id", "0");
        Map<String, Object> body = new HashMap<>();
        body.put("channelId", channelId);
        body.put("alarmType", type);
        body.put("alarmContent", content);
        body.put("alarmTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
        restTemplate.postForEntity(alarmReportUrl, new HttpEntity<>(body, headers), Map.class);
    }

    private static class ParsedStream {
        final Long tenantId;
        final Long channelId;
        ParsedStream(Long t, Long c) { this.tenantId = t; this.channelId = c; }
    }

    @Data
    public static class SrsHookEvent {
        private String action;
        private String client_id;
        private String ip;
        private String vhost;
        private String app;
        private String stream;
        private String param;
        private String file;
        private Long fileSize;
        private Double duration;
        private String cwd;
    }
}
