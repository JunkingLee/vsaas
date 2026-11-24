package com.vsaas.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("billing_bill")
public class BillingBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String billNo;
    private String billMonth;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
}
