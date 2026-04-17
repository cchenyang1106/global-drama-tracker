package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体。
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("phone")
    private String username;

    private String password;
    private String nickname;
    private Integer role;
    private Integer status;

    @TableField(value = "last_login_time", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime lastLoginTime;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    @TableField(value = "deleted", insertStrategy = FieldStrategy.NOT_NULL)
    private Integer deleted;
}
