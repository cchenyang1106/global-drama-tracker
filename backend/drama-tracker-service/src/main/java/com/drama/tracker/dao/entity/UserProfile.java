package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String avatarUrl;
    private String realName;
    private Integer age;
    private Integer gender;
    private String city;
    private String occupation;
    private String bio;
    private String hobbies;
    private String tags;        // 个人标签(逗号分隔)
    private String wechat;
    private Integer showWechat; // 0不公开 1公开
    private String photos;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
