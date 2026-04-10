package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排行榜快照实体。
 *
 * @author drama-tracker
 */
@Data
@TableName("ranking")
public class Ranking implements Serializable {

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
     * 榜单类型 (1:日榜 2:周榜 3:月榜 4:总榜)
     */
    private Integer rankType;

    /**
     * 榜单分类 (region:地区榜 genre:类型榜 hot:热度榜 new:新剧榜)
     */
    private String rankCategory;

    /**
     * 分类值（如地区代码或类型）
     */
    private String categoryValue;

    /**
     * 排名
     */
    @TableField("`rank`")
    private Integer rank;

    /**
     * 上期排名
     */
    private Integer previousRank;

    /**
     * 排名变化（正数上升，负数下降）
     */
    private Integer rankChange;

    /**
     * 综合得分
     */
    private BigDecimal score;

    /**
     * 热度值
     */
    private Long hotScore;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 剧集名称（非持久化字段，查询时填充）
     */
    @TableField(exist = false)
    private String dramaTitle;
}
