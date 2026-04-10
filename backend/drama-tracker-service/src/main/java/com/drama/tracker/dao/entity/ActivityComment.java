package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_comment")
public class ActivityComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer pinned;
    private Integer hidden;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
