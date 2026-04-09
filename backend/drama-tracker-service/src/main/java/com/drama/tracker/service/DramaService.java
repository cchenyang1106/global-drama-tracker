package com.drama.tracker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drama.tracker.dao.entity.Drama;
import com.drama.tracker.dto.DramaQueryDto;

import java.util.List;

/**
 * 剧集服务接口。
 *
 * @author drama-tracker
 */
public interface DramaService extends IService<Drama> {

    /**
     * 分页查询剧集列表。
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    Page<Drama> listDramas(DramaQueryDto queryDto);

    /**
     * 根据ID获取剧集详情。
     *
     * @param id 剧集ID
     * @return 剧集信息
     */
    Drama getDramaById(Long id);

    /**
     * 获取今日更新的剧集。
     *
     * @param region 地区（可选）
     * @return 剧集列表
     */
    List<Drama> getTodayUpdated(String region);

    /**
     * 搜索剧集。
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @return 分页结果
     */
    Page<Drama> searchDramas(String keyword, Integer page, Integer size);

    /**
     * 更新剧集热度。
     *
     * @param dramaId  剧集ID
     * @param hotScore 热度增量
     */
    void updateHotScore(Long dramaId, Long hotScore);

    /**
     * 更新剧集评分。
     *
     * @param dramaId 剧集ID
     */
    void refreshRating(Long dramaId);
}
