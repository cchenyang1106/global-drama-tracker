package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户追剧清单实体。
 *
 * @author drama-tracker
 */
@Data
@TableName("user_watchlist")
public class UserWatchlist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 剧集ID
     */
    private Long dramaId;

    /**
     * 状态 (1:想看 2:在看 3:看过 4:弃剧)
     */
    private Integer status;

    /**
     * 观看进度（已看集数）
     */
    private Integer watchedEpisodes;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
