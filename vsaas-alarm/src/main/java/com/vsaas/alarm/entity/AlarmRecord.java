package com.vsaas.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long channelId;
    private String alarmType;
    private String alarmContent;
    private Integer status;
    private LocalDateTime alarmTime;
    private LocalDateTime handleTime;
    private LocalDateTime createdAt;
}
