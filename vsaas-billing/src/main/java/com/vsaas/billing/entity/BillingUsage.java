package com.vsaas.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("billing_usage")
public class BillingUsage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String usageType;
    private BigDecimal usageValue;
    private LocalDateTime usageTime;
    private String bizId;
    private LocalDateTime createdAt;
}
