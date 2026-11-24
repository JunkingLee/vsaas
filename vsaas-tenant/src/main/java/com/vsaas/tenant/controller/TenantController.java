package com.vsaas.tenant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.tenant.entity.SysTenant;
import com.vsaas.tenant.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final SysTenantMapper tenantMapper;

    @GetMapping("/list")
    public Result<Page<SysTenant>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SysTenant> p = new Page<>(page, size);
        return Result.success(tenantMapper.selectPage(p,
                new LambdaQueryWrapper<SysTenant>().orderByDesc(SysTenant::getId)));
    }

    @GetMapping("/{id}")
    public Result<SysTenant> detail(@PathVariable Long id) {
        SysTenant t = tenantMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(404, "租户不存在");
        }
        return Result.success(t);
    }

    @PostMapping
    public Result<SysTenant> create(@RequestBody SysTenant tenant) {
        if (tenant.getTenantCode() == null || tenant.getTenantName() == null) {
            throw new BusinessException(400, "租户编码和名称不能为空");
        }
        long count = tenantMapper.selectCount(
                new LambdaQueryWrapper<SysTenant>().eq(SysTenant::getTenantCode, tenant.getTenantCode()));
        if (count > 0) {
            throw new BusinessException(400, "租户编码已存在");
        }
        tenant.setStatus(1);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.insert(tenant);
        return Result.success(tenant);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysTenant tenant) {
        SysTenant exist = tenantMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "租户不存在");
        }
        tenant.setId(id);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysTenant t = new SysTenant();
        t.setId(id);
        t.setStatus(status);
        t.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(t);
        return Result.success();
    }
}
