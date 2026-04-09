package com.drama.tracker.controller;

import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.Ranking;
import com.drama.tracker.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜控制器。
 *
 * @author drama-tracker
 */
@Tag(name = "排行榜", description = "各类排行榜接口")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 获取日榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Operation(summary = "获取日榜")
    @GetMapping("/daily")
    public Result<List<Ranking>> getDailyRanking(
            @Parameter(description = "榜单分类 (region/genre/hot/new)") @RequestParam(defaultValue = "hot") String category,
            @Parameter(description = "分类值（如地区代码）") @RequestParam(required = false) String value,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "50") Integer limit) {
        List<Ranking> rankings = rankingService.getDailyRanking(category, value, limit);
        return Result.success(rankings);
    }

    /**
     * 获取周榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Operation(summary = "获取周榜")
    @GetMapping("/weekly")
    public Result<List<Ranking>> getWeeklyRanking(
            @Parameter(description = "榜单分类") @RequestParam(defaultValue = "hot") String category,
            @Parameter(description = "分类值") @RequestParam(required = false) String value,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "50") Integer limit) {
        List<Ranking> rankings = rankingService.getWeeklyRanking(category, value, limit);
        return Result.success(rankings);
    }

    /**
     * 获取月榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Operation(summary = "获取月榜")
    @GetMapping("/monthly")
    public Result<List<Ranking>> getMonthlyRanking(
            @Parameter(description = "榜单分类") @RequestParam(defaultValue = "hot") String category,
            @Parameter(description = "分类值") @RequestParam(required = false) String value,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "50") Integer limit) {
        List<Ranking> rankings = rankingService.getMonthlyRanking(category, value, limit);
        return Result.success(rankings);
    }

    /**
     * 获取地区排行榜。
     *
     * @param region 地区代码
     * @param type   榜单类型
     * @param limit  数量限制
     * @return 排行榜数据
     */
    @Operation(summary = "获取地区排行榜")
    @GetMapping("/region/{region}")
    public Result<List<Ranking>> getRegionRanking(
            @Parameter(description = "地区代码") @PathVariable String region,
            @Parameter(description = "榜单类型 (1:日榜 2:周榜 3:月榜)") @RequestParam(defaultValue = "1") Integer type,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "50") Integer limit) {
        List<Ranking> rankings = rankingService.getRegionRanking(region, type, limit);
        return Result.success(rankings);
    }

    /**
     * 获取新剧热度榜。
     *
     * @param days  天数范围
     * @param limit 数量限制
     * @return 排行榜数据
     */
    @Operation(summary = "获取新剧热度榜")
    @GetMapping("/new")
    public Result<List<Ranking>> getNewDramaRanking(
            @Parameter(description = "天数范围") @RequestParam(defaultValue = "30") Integer days,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        List<Ranking> rankings = rankingService.getNewDramaRanking(days, limit);
        return Result.success(rankings);
    }
}
