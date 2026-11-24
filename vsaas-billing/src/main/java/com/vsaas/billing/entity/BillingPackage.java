package com.vsaas.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("billing_package")
public class BillingPackage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String packageCode;
    private String packageName;
    private BigDecimal price;
    private Integer billingType;
    private Integer maxChannels;
    private Integer maxStorageGb;
    private Integer retainDays;
    private Integer status;
    private LocalDateTime createdAt;
}
