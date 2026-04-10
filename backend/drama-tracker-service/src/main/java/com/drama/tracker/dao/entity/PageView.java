package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("page_view")
public class PageView implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String path;

    private String pageTitle;

    private String referrer;

    private String visitorId;

    private String ip;

    private String userAgent;

    private Integer screenWidth;

    private Integer screenHeight;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
