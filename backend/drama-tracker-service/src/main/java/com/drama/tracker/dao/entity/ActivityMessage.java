package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动留言板（通过审核的成员可互动留言）。
 */
@Data
@TableName("activity_message")
public class ActivityMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long userId;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
