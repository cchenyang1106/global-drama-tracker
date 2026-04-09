package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 剧集分集信息实体。
 *
 * @author drama-tracker
 */
@Data
@TableName("episode")
public class Episode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 剧集ID
     */
    private Long dramaId;

    /**
     * 季数（电视剧有多季时使用）
     */
    private Integer season;

    /**
     * 集数
     */
    private Integer episodeNumber;

    /**
     * 分集标题
     */
    private String title;

    /**
     * 分集简介
     */
    private String description;

    /**
     * 播出日期
     */
    private LocalDate airDate;

    /**
     * 时长（分钟）
     */
    private Integer duration;

    /**
     * 是否已播出
     */
    private Boolean aired;

    /**
     * 剧照URL
     */
    private String stillUrl;

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
