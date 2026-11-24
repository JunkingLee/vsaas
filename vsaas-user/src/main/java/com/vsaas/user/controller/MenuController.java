package com.vsaas.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.common.core.Result;
import com.vsaas.user.entity.SysMenu;
import com.vsaas.user.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuMapper menuMapper;

    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        List<SysMenu> list = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .orderByAsc(SysMenu::getSort));
        return Result.success(list);
    }

    @PostMapping
    public Result<SysMenu> create(@RequestBody SysMenu menu) {
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getStatus() == null) menu.setStatus(1);
        menuMapper.insert(menu);
        return Result.success(menu);
    }
}
