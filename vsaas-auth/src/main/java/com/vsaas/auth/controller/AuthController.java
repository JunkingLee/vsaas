package com.vsaas.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.auth.entity.SysUser;
import com.vsaas.auth.mapper.SysUserMapper;
import com.vsaas.common.core.BusinessException;
import com.vsaas.common.core.Result;
import com.vsaas.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername())
                        .eq(SysUser::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return Result.success(buildTokenData(user));
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "缺少 Token");
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("user_id").toString());
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                throw new BusinessException(401, "用户不可用");
            }
            return Result.success(buildTokenData(user));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "Token 无效或已过期，请重新登录");
        }
    }

    private Map<String, Object> buildTokenData(SysUser user) {
        String token = jwtUtils.generateToken(user.getId(), user.getTenantId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("tenantId", user.getTenantId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        return data;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
