package com.vsaas.alarm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.alarm.entity.AlarmRecord;
import com.vsaas.alarm.mapper.AlarmRecordMapper;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.mq.DomainEvent;
import com.vsaas.common.mq.EventPublisher;
import com.vsaas.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmRecordMapper alarmMapper;
    private final EventPublisher eventPublisher;

    @GetMapping("/list")
    public Result<Page<AlarmRecord>> list(
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = requireTenant();
        Page<AlarmRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<AlarmRecord> q = new LambdaQueryWrapper<AlarmRecord>()
                .eq(AlarmRecord::getTenantId, tenantId)
                .eq(channelId != null, AlarmRecord::getChannelId, channelId)
                .eq(status != null, AlarmRecord::getStatus, status)
                .orderByDesc(AlarmRecord::getAlarmTime);
        return Result.success(alarmMapper.selectPage(p, q));
    }

    @PostMapping("/report")
    public Result<Void> report(@RequestBody AlarmRecord alarm) {
        Long tenantId = requireTenant();
        alarm.setTenantId(tenantId);
        if (alarm.getAlarmTime() == null) {
            alarm.setAlarmTime(LocalDateTime.now());
        }
        alarm.setStatus(0);
        alarm.setCreatedAt(LocalDateTime.now());
        alarmMapper.insert(alarm);
        eventPublisher.publish(DomainEvent.builder()
                .type("alarm.created")
                .tenantId(tenantId)
                .bizId(String.valueOf(alarm.getId()))
                .payload(java.util.Map.of(
                        "alarmType", alarm.getAlarmType() != null ? alarm.getAlarmType() : "",
                        "channelId", alarm.getChannelId() != null ? alarm.getChannelId() : 0
                ))
                .build());
        return Result.success();
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id) {
        Long tenantId = requireTenant();
        AlarmRecord exist = alarmMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "告警不存在");
        }
        AlarmRecord update = new AlarmRecord();
        update.setId(id);
        update.setStatus(1);
        alarmMapper.updateById(update);
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
