package com.vsaas.record.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.record.entity.RecordFile;
import com.vsaas.record.entity.RecordPlan;
import com.vsaas.record.mapper.RecordFileMapper;
import com.vsaas.record.mapper.RecordPlanMapper;
import com.vsaas.record.service.RecordStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class RecordController {

    private final RecordPlanMapper planMapper;
    private final RecordFileMapper fileMapper;
    private final RecordStorageService storageService;

    @GetMapping("/plan/list")
    public Result<List<RecordPlan>> listPlans(@RequestParam(required = false) Long channelId) {
        Long tenantId = requireTenant();
        LambdaQueryWrapper<RecordPlan> q = new LambdaQueryWrapper<RecordPlan>()
                .eq(RecordPlan::getTenantId, tenantId)
                .eq(channelId != null, RecordPlan::getChannelId, channelId)
                .orderByDesc(RecordPlan::getId);
        return Result.success(planMapper.selectList(q));
    }

    @PostMapping("/plan")
    public Result<RecordPlan> createPlan(@RequestBody RecordPlan plan) {
        Long tenantId = requireTenant();
        plan.setTenantId(tenantId);
        plan.setStatus(1);
        plan.setCreatedAt(LocalDateTime.now());
        if (plan.getRetainDays() == null) {
            plan.setRetainDays(7);
        }
        planMapper.insert(plan);
        return Result.success(plan);
    }

    @DeleteMapping("/plan/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        Long tenantId = requireTenant();
        RecordPlan plan = planMapper.selectById(id);
        if (plan == null || !plan.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "计划不存在");
        }
        planMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/file/list")
    public Result<Page<RecordFile>> listFiles(
            @RequestParam Long channelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long tenantId = requireTenant();
        Page<RecordFile> p = new Page<>(page, size);
        LambdaQueryWrapper<RecordFile> q = new LambdaQueryWrapper<RecordFile>()
                .eq(RecordFile::getTenantId, tenantId)
                .eq(RecordFile::getChannelId, channelId)
                .ge(RecordFile::getStartTime, startTime)
                .le(RecordFile::getEndTime, endTime)
                .orderByAsc(RecordFile::getStartTime);
        return Result.success(fileMapper.selectPage(p, q));
    }

    @GetMapping("/file/{id}/play")
    public Result<Map<String, Object>> getPlayUrl(@PathVariable Long id) {
        Long tenantId = requireTenant();
        RecordFile file = fileMapper.selectById(id);
        if (file == null || !file.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "录像文件不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("filePath", file.getFilePath());
        data.put("startTime", file.getStartTime());
        data.put("endTime", file.getEndTime());
        data.put("fileSize", file.getFileSize());
        String playUrl = file.getFilePath();
        try {
            String key = file.getFilePath();
            if (key.contains("/vsaas-record/")) {
                key = key.substring(key.indexOf("/vsaas-record/") + "/vsaas-record/".length());
            }
            if (!key.startsWith("http")) {
                playUrl = storageService.presignGetUrl(key, 3600);
            }
        } catch (Exception ignored) {}
        data.put("playUrl", playUrl);
        return Result.success(data);
    }

    @PostMapping("/file/report")
    public Result<RecordFile> reportFile(@RequestBody RecordFile file) {
        Long tenantId = requireTenant();
        if (file.getChannelId() == null || file.getStartTime() == null || file.getEndTime() == null) {
            throw new BusinessException(400, "channelId/startTime/endTime 不能为空");
        }
        file.setTenantId(tenantId);
        if (file.getFilePath() == null || file.getFilePath().isEmpty()) {
            String key = storageService.buildObjectKey(tenantId, file.getChannelId(), file.getStartTime(), 1);
            file.setFilePath(storageService.buildAccessUrl(key));
        }
        if (file.getFileSize() == null) {
            file.setFileSize(0L);
        }
        file.setCreatedAt(LocalDateTime.now());
        fileMapper.insert(file);
        return Result.success(file);
    }

    @GetMapping("/storage/path")
    public Result<Map<String, String>> storagePath(
            @RequestParam Long channelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(defaultValue = "1") int seq) {
        Long tenantId = requireTenant();
        return Result.success(storageService.buildPathInfo(tenantId, channelId, startTime, seq));
    }

    @GetMapping("/storage/presign-get")
    public Result<String> presignGet(@RequestParam String objectKey,
                                     @RequestParam(defaultValue = "3600") int expireSeconds) {
        requireTenant();
        return Result.success(storageService.presignGetUrl(objectKey, expireSeconds));
    }

    @GetMapping("/storage/presign-put")
    public Result<String> presignPut(@RequestParam String objectKey,
                                     @RequestParam(defaultValue = "3600") int expireSeconds) {
        requireTenant();
        return Result.success(storageService.presignPutUrl(objectKey, expireSeconds));
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户信息缺失");
        }
        return tenantId;
    }
}
