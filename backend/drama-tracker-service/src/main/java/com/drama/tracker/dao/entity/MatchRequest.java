package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("match_request")
public class MatchRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
