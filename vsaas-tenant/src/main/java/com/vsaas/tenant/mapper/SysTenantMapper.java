package com.vsaas.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vsaas.tenant.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
