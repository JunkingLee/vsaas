package com.vsaas.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_org")
public class SysOrg {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long parentId;
    private String orgName;
    private Integer sortOrder;
    private Integer status;
}
