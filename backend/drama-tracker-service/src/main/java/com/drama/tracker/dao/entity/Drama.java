package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 剧集/电影实体。
 *
 * @author drama-tracker
 */
@Data
@TableName("drama")
public class Drama implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 剧集名称
     */
    private String title;

    /**
     * 原名（外语原名）
     */
    private String originalTitle;

    /**
     * 别名（多个用逗号分隔）
     */
    private String aliases;

    /**
     * 地区代码 (CN/JP/KR/US/UK/EU/OT)
     */
    private String region;

    /**
     * 类型 (1:电视剧 2:电影 3:网剧 4:综艺 5:动漫 6:纪录片)
     */
    private Integer type;

    /**
     * 分类标签（多个用逗号分隔，如：剧情,悬疑,犯罪）
     */
    private String genres;

    /**
     * 状态 (0:未播出 1:连载中 2:已完结 3:已停播)
     */
    private Integer status;

    /**
     * 首播日期
     */
    private LocalDate releaseDate;

    /**
     * 完结日期
     */
    private LocalDate endDate;

    /**
     * 总集数
     */
    private Integer totalEpisodes;

    /**
     * 已播集数
     */
    private Integer airedEpisodes;

    /**
     * 单集时长（分钟）
     */
    private Integer episodeDuration;

    /**
     * 导演（多个用逗号分隔）
     */
    private String directors;

    /**
     * 编剧（多个用逗号分隔）
     */
    private String writers;

    /**
     * 主演（多个用逗号分隔）
     */
    private String actors;

    /**
     * 简介
     */
    private String description;

    /**
     * 海报图片URL
     */
    private String posterUrl;

    /**
     * 横版封面URL
     */
    private String coverUrl;

    /**
     * 用户平均评分
     */
    private BigDecimal userRating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * AI 情感分析评分
     */
    private BigDecimal aiRating;

    /**
     * 综合评分
     */
    private BigDecimal totalRating;

    /**
     * 热度值
     */
    private Long hotScore;

    /**
     * 播放/观看平台
     */
    private String platforms;

    /**
     * 官方网站
     */
    private String officialUrl;

    /**
     * 豆瓣ID（用于关联）
     */
    private String doubanId;

    /**
     * IMDB ID（用于关联）
     */
    private String imdbId;

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
