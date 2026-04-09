package com.drama.tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 剧集查询 DTO。
 *
 * @author drama-tracker
 */
@Data
@Schema(description = "剧集查询参数")
public class DramaQueryDto {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 地区代码
     */
    @Schema(description = "地区代码 (CN/JP/KR/US/UK/EU)", example = "CN")
    private String region;

    /**
     * 类型
     */
    @Schema(description = "类型 (1:电视剧 2:电影 3:网剧 4:综艺 5:动漫 6:纪录片)", example = "1")
    private Integer type;

    /**
     * 状态
     */
    @Schema(description = "状态 (0:未播出 1:连载中 2:已完结)", example = "1")
    private Integer status;

    /**
     * 分类标签
     */
    @Schema(description = "分类标签", example = "悬疑")
    private String genre;

    /**
     * 年份
     */
    @Schema(description = "年份", example = "2024")
    private Integer year;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "hot")
    private String sortBy;

    /**
     * 排序方向
     */
    @Schema(description = "排序方向 (asc/desc)", example = "desc")
    private String sortOrder = "desc";
}
