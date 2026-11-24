package com.vsaas.device.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgScopeService {

    private final JdbcTemplate jdbcTemplate;

    public List<Long> visibleDeviceIds(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return Collections.emptyList();
        }
        try {
            List<Long> orgIds = jdbcTemplate.queryForList(
                    "SELECT org_id FROM sys_user_org WHERE user_id = ?", Long.class, userId);
            if (orgIds == null || orgIds.isEmpty()) {
                return null;
            }
            String in = String.join(",", orgIds.stream().map(String::valueOf).toList());
            return jdbcTemplate.queryForList(
                    "SELECT DISTINCT device_id FROM device_org WHERE org_id IN (" + in + ")",
                    Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}
