package com.vsaas.record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("record_file")
public class RecordFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long channelId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String filePath;
    private Long fileSize;
    private LocalDateTime createdAt;
}
