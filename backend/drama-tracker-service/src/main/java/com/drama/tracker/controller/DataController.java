package com.drama.tracker.controller;

import com.drama.tracker.common.result.Result;
import com.drama.tracker.service.DataInitService;
import com.drama.tracker.service.RankingService;
import com.drama.tracker.service.TmdbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据管理控制器。
 * 提供数据抓取、初始化、排行榜刷新等管理接口。
 *
 * @author drama-tracker
 */
@Slf4j
@Tag(name = "数据管理", description = "数据抓取、初始化、排行榜管理接口")
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final TmdbService tmdbService;
    private final DataInitService dataInitService;
    private final RankingService rankingService;

    /**
     * 从 TMDB 抓取全量数据（异步执行）。
     *
     * @return 提交结果
     */
    @Operation(summary = "从 TMDB 抓取全量数据")
    @PostMapping("/fetch/tmdb")
    public Result<Map<String, Object>> fetchFromTmdb() {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = tmdbService.fetchAllData();
                log.info("TMDB全量抓取完成: {}", result);
                // 抓取完后自动刷新排行榜
                rankingService.refreshRankings();
                log.info("排行榜已自动刷新");
            } catch (Exception e) {
                log.error("TMDB全量抓取失败", e);
            }
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "submitted");
        result.put("message", "全量抓取任务已提交，后台执行中，请稍后刷新页面查看数据");
        return Result.success(result);
    }

    /**
     * 从 TMDB 按地区抓取数据（异步执行）。
     *
     * @param region 地区代码 (CN/JP/KR/US/UK/EU)
     * @return 提交结果
     */
    @Operation(summary = "按地区抓取 TMDB 数据")
    @PostMapping("/fetch/tmdb/{region}")
    public Result<Map<String, Object>> fetchByRegion(
            @Parameter(description = "地区代码") @PathVariable String region) {
        String upperRegion = region.toUpperCase();
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = tmdbService.fetchByRegion(upperRegion);
                log.info("TMDB地区[{}]抓取完成: {}", upperRegion, result);
                rankingService.refreshRankings();
            } catch (Exception e) {
                log.error("TMDB地区[{}]抓取失败", upperRegion, e);
            }
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "submitted");
        result.put("region", upperRegion);
        result.put("message", "地区[" + upperRegion + "]抓取任务已提交，后台执行中，请稍后刷新页面查看数据");
        return Result.success(result);
    }

    /**
     * 初始化种子数据（内置精选数据，不需要 API Key）。
     *
     * @return 初始化结果
     */
    @Operation(summary = "初始化种子数据（内置精选数据）")
    @PostMapping("/init/seed")
    public Result<Map<String, Object>> initSeedData() {
        Map<String, Object> result = dataInitService.initSeedData();
        // 种子数据初始化后自动刷新排行榜，确保排行榜页面有数据
        try {
            rankingService.refreshRankings();
            result.put("rankingRefreshed", true);
        } catch (Exception e) {
            result.put("rankingRefreshed", false);
            result.put("rankingError", e.getMessage());
        }
        return Result.success(result);
    }

    /**
     * 刷新排行榜数据。
     *
     * @return 刷新结果
     */
    @Operation(summary = "刷新排行榜数据")
    @PostMapping("/refresh/ranking")
    public Result<String> refreshRankings() {
        rankingService.refreshRankings();
        return Result.success("排行榜已刷新");
    }
}
