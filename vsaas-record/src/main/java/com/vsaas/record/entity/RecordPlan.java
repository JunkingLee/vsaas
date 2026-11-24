package com.vsaas.record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("record_plan")
public class RecordPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long channelId;
    private String planName;
    private Integer recordType;
    private Integer retainDays;
    private Integer status;
    private LocalDateTime createdAt;
}
