package com.drama.tracker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.PageResult;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.result.ResultCode;
import com.drama.tracker.dao.entity.Drama;
import com.drama.tracker.dto.DramaQueryDto;
import com.drama.tracker.service.DramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 剧集控制器。
 *
 * @author drama-tracker
 */
@Tag(name = "剧集管理", description = "剧集查询、搜索相关接口")
@RestController
@RequestMapping("/api/drama")
@RequiredArgsConstructor
public class DramaController {

    private final DramaService dramaService;

    /**
     * 分页查询剧集列表。
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询剧集列表")
    @GetMapping("/list")
    public Result<PageResult<Drama>> listDramas(DramaQueryDto queryDto) {
        Page<Drama> page = dramaService.listDramas(queryDto);
        PageResult<Drama> pageResult = PageResult.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        return Result.success(pageResult);
    }

    /**
     * 获取剧集详情。
     *
     * @param id 剧集ID
     * @return 剧集信息
     */
    @Operation(summary = "获取剧集详情")
    @GetMapping("/{id}")
    public Result<Drama> getDramaDetail(
            @Parameter(description = "剧集ID") @PathVariable Long id) {
        Drama drama = dramaService.getDramaById(id);
        if (drama == null) {
            return Result.fail(ResultCode.DRAMA_NOT_FOUND);
        }
        return Result.success(drama);
    }

    /**
     * 获取今日更新的剧集。
     *
     * @param region 地区（可选）
     * @return 剧集列表
     */
    @Operation(summary = "获取今日更新")
    @GetMapping("/today")
    public Result<List<Drama>> getTodayUpdated(
            @Parameter(description = "地区代码") @RequestParam(required = false) String region) {
        List<Drama> dramas = dramaService.getTodayUpdated(region);
        return Result.success(dramas);
    }

    /**
     * 搜索剧集。
     *
     * @param keyword  关键词
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Operation(summary = "搜索剧集")
    @GetMapping("/search")
    public Result<PageResult<Drama>> searchDramas(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<Drama> pageResult = dramaService.searchDramas(keyword, page, pageSize);
        return Result.success(PageResult.of(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.getRecords()
        ));
    }

    /**
     * 按地区获取剧集。
     *
     * @param region   地区代码
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Operation(summary = "按地区获取剧集")
    @GetMapping("/region/{region}")
    public Result<PageResult<Drama>> getDramasByRegion(
            @Parameter(description = "地区代码") @PathVariable String region,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        DramaQueryDto queryDto = new DramaQueryDto();
        queryDto.setRegion(region);
        queryDto.setPageNum(page);
        queryDto.setPageSize(pageSize);

        Page<Drama> pageResult = dramaService.listDramas(queryDto);
        return Result.success(PageResult.of(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.getRecords()
        ));
    }
}
