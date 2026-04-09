package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 讨论帖实体。
 *
 * @author drama-tracker
 */
@Data
@TableName("discussion")
public class Discussion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联剧集ID（可为空，表示综合讨论）
     */
    private Long dramaId;

    /**
     * 发帖用户ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 讨论类型 (1:剧评 2:吐槽 3:提问 4:资讯 5:杂谈)
     */
    private Integer type;

    /**
     * 标签（多个用逗号分隔）
     */
    private String tags;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 是否精华
     */
    private Boolean featured;

    /**
     * 状态 (0:待审核 1:已发布 2:已关闭 3:已删除)
     */
    private Integer status;

    /**
     * 最后回复时间
     */
    private LocalDateTime lastReplyTime;

    /**
     * 最后回复用户ID
     */
    private Long lastReplyUserId;

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

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
