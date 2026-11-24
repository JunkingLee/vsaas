package com.vsaas.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.tenant.TenantContext;
import com.vsaas.user.entity.SysUser;
import com.vsaas.user.entity.SysUserRole;
import com.vsaas.user.mapper.SysUserMapper;
import com.vsaas.user.mapper.SysUserRoleMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username) {
        Long tenantId = requireTenant();
        Page<SysUser> p = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .like(username != null, SysUser::getUsername, username)
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(p, q);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PostMapping
    public Result<SysUser> create(@RequestBody CreateUserRequest req) {
        Long tenantId = requireTenant();
        long cnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getUsername, req.getUsername()));
        if (cnt > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        if (req.getRoleIds() != null) {
            for (Long roleId : req.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        Long tenantId = requireTenant();
        SysUser exist = userMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setId(id);
        user.setTenantId(tenantId);
        user.setPassword(null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long tenantId = requireTenant();
        SysUser exist = userMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "用户不存在");
        }
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<SysUserRole> list = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return Result.success(list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/roles")
    public Result<Void> setUserRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        Long tenantId = requireTenant();
        SysUser exist = userMapper.selectById(id);
        if (exist == null || !exist.getTenantId().equals(tenantId)) {
            throw new BusinessException(404, "用户不存在");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BusinessException(401, "租户信息缺失");
        return tenantId;
    }

    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;
        private List<Long> roleIds;
    }
}
