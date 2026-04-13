package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String category;
    private String description;
    private String location;
    private String activityTime;
    private Integer maxPeople;
    private Integer joinedCount;
    private String tags;
    private Integer preferGender;   // 偏好性别 0不限 1男 2女
    private Integer preferAgeMin;
    private Integer preferAgeMax;
    private String preferCity;
    private String preferTags;
    private String images;
    private Integer status;
    private Integer viewCount;
    private Integer teamComplete;   // 0未完成 1已组队完成

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
