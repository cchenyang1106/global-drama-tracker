package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_report")
public class UserReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private String targetType;   // user/activity/comment/chat
    private Long targetId;
    private String reason;
    private String detail;
    private Integer status;      // 0待处理 1已处理 2已驳回
    private String adminNote;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
