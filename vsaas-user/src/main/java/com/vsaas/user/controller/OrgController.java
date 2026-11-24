package com.vsaas.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.user.entity.SysOrg;
import com.vsaas.user.mapper.SysOrgMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrgController {

    private final SysOrgMapper orgMapper;

    @GetMapping("/list")
    public Result<List<SysOrg>> list() {
        Long tenantId = requireTenant();
        return Result.success(orgMapper.selectList(
                new LambdaQueryWrapper<SysOrg>()
                        .eq(SysOrg::getTenantId, tenantId)
                        .eq(SysOrg::getStatus, 1)
                        .orderByAsc(SysOrg::getSort)
                        .orderByAsc(SysOrg::getId)));
    }

    @PostMapping
    public Result<SysOrg> create(@RequestBody SysOrg org) {
        Long tenantId = requireTenant();
        org.setTenantId(tenantId);
        if (org.getParentId() == null) org.setParentId(0L);
        org.setStatus(1);
        org.setCreatedAt(LocalDateTime.now());
        orgMapper.insert(org);
        return Result.success(org);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysOrg org) {
        Long tenantId = requireTenant();
        SysOrg exist = orgMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "组织不存在");
        }
        org.setId(id);
        org.setTenantId(tenantId);
        orgMapper.updateById(org);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = requireTenant();
        SysOrg exist = orgMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "组织不存在");
        }
        orgMapper.deleteById(id);
        return Result.success();
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BusinessException(401, "租户信息缺失");
        return tenantId;
    }
}
