package com.vsaas.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.user.entity.SysRole;
import com.vsaas.user.entity.SysRoleMenu;
import com.vsaas.user.mapper.SysRoleMapper;
import com.vsaas.user.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @GetMapping("/list")
    public Result<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = requireTenant();
        Page<SysRole> p = new Page<>(page, size);
        return Result.success(roleMapper.selectPage(p,
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getTenantId, tenantId)
                        .orderByDesc(SysRole::getId)));
    }

    @PostMapping
    public Result<SysRole> create(@RequestBody SysRole role) {
        Long tenantId = requireTenant();
        role.setTenantId(tenantId);
        role.setStatus(1);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return Result.success(role);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        Long tenantId = requireTenant();
        SysRole exist = roleMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "觙色不存在");
        }
        role.setId(id);
        role.setTenantId(tenantId);
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = requireTenant();
        SysRole exist = roleMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "觙色不存在");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return Result.success(list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/menus")
    public Result<Void> setRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        Long tenantId = requireTenant();
        SysRole exist = roleMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "觙色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        return Result.success();
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BusinessException(401, "租户信息缺失");
        return tenantId;
    }
}
