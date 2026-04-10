package com.drama.tracker.controller;

import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.PageView;
import com.drama.tracker.dao.mapper.PageViewMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "数据统计", description = "PV/UV 埋点上报与统计")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final PageViewMapper pageViewMapper;

    /**
     * 前端埋点上报。
     */
    @Operation(summary = "上报页面访问")
    @PostMapping("/report")
    public Result<String> report(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        PageView pv = new PageView();
        pv.setPath((String) body.getOrDefault("path", "/"));
        pv.setPageTitle((String) body.get("pageTitle"));
        pv.setReferrer((String) body.get("referrer"));
        pv.setVisitorId((String) body.getOrDefault("visitorId", "unknown"));
        pv.setIp(getClientIp(request));
        pv.setUserAgent(request.getHeader("User-Agent"));
        if (body.get("screenWidth") != null) {
            pv.setScreenWidth(Integer.parseInt(body.get("screenWidth").toString()));
        }
        if (body.get("screenHeight") != null) {
            pv.setScreenHeight(Integer.parseInt(body.get("screenHeight").toString()));
        }
        pv.setCreateTime(LocalDateTime.now());
        pageViewMapper.insert(pv);
        return Result.success("ok");
    }

    /**
     * 获取统计概览。
     */
    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("today", pageViewMapper.getTodayStats());
        result.put("allTime", pageViewMapper.getAllTimeStats());
        result.put("hourly", pageViewMapper.getHourlyStats());
        return Result.success(result);
    }

    /**
     * 获取每日 PV/UV 趋势。
     */
    @Operation(summary = "获取每日PV/UV趋势")
    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> dailyStats(
            @RequestParam(defaultValue = "30") Integer days) {
        String startDate = LocalDate.now().minusDays(days).toString();
        return Result.success(pageViewMapper.getDailyStats(startDate));
    }

    /**
     * 获取页面访问排行。
     */
    @Operation(summary = "获取页面访问排行")
    @GetMapping("/pages")
    public Result<List<Map<String, Object>>> pageStats(
            @RequestParam(defaultValue = "30") Integer days) {
        String startDate = LocalDate.now().minusDays(days).toString();
        return Result.success(pageViewMapper.getPageStats(startDate));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
