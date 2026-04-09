package com.drama.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drama.tracker.dao.entity.Drama;
import com.drama.tracker.dao.mapper.DramaMapper;
import com.drama.tracker.dto.DramaQueryDto;
import com.drama.tracker.service.DramaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 剧集服务实现类。
 *
 * @author drama-tracker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DramaServiceImpl extends ServiceImpl<DramaMapper, Drama> implements DramaService {

    /**
     * 分页查询剧集列表。
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @Override
    public Page<Drama> listDramas(DramaQueryDto queryDto) {
        Page<Drama> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

        LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(queryDto.getRegion()), Drama::getRegion, queryDto.getRegion())
                .eq(queryDto.getType() != null, Drama::getType, queryDto.getType())
                .eq(queryDto.getStatus() != null, Drama::getStatus, queryDto.getStatus())
                .like(StringUtils.isNotBlank(queryDto.getGenre()), Drama::getGenres, queryDto.getGenre())
                .ge(queryDto.getYear() != null, Drama::getReleaseDate, LocalDate.of(queryDto.getYear(), 1, 1))
                .le(queryDto.getYear() != null, Drama::getReleaseDate, LocalDate.of(queryDto.getYear(), 12, 31))
                .orderByDesc(Drama::getHotScore);

        return this.page(page, wrapper);
    }

    /**
     * 根据ID获取剧集详情。
     *
     * @param id 剧集ID
     * @return 剧集信息
     */
    @Override
    public Drama getDramaById(Long id) {
        Drama drama = this.getById(id);
        if (drama != null) {
            // 增加热度
            this.updateHotScore(id, 1L);
        }
        return drama;
    }

    /**
     * 获取今日更新的剧集。
     *
     * @param region 地区（可选）
     * @return 剧集列表
     */
    @Override
    public List<Drama> getTodayUpdated(String region) {
        LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Drama::getStatus, 1) // 连载中
                .eq(StringUtils.isNotBlank(region), Drama::getRegion, region)
                .apply("DATE(update_time) = CURDATE()")
                .orderByDesc(Drama::getHotScore);

        return this.list(wrapper);
    }

    /**
     * 搜索剧集。
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @return 分页结果
     */
    @Override
    public Page<Drama> searchDramas(String keyword, Integer page, Integer size) {
        Page<Drama> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Drama::getTitle, keyword)
                .or()
                .like(Drama::getOriginalTitle, keyword)
                .or()
                .like(Drama::getAliases, keyword)
                .or()
                .like(Drama::getActors, keyword)
                .or()
                .like(Drama::getDirectors, keyword)
                .orderByDesc(Drama::getTotalRating);

        return this.page(pageParam, wrapper);
    }

    /**
     * 更新剧集热度。
     *
     * @param dramaId  剧集ID
     * @param hotScore 热度增量
     */
    @Override
    public void updateHotScore(Long dramaId, Long hotScore) {
        Drama drama = this.getById(dramaId);
        if (drama != null) {
            drama.setHotScore(drama.getHotScore() == null ? hotScore : drama.getHotScore() + hotScore);
            this.updateById(drama);
        }
    }

    /**
     * 更新剧集评分。
     *
     * @param dramaId 剧集ID
     */
    @Override
    public void refreshRating(Long dramaId) {
        // TODO: 实现评分刷新逻辑
        // 1. 计算用户平均评分
        // 2. 计算 AI 情感分析评分
        // 3. 计算综合评分
        log.info("Refreshing rating for drama: {}", dramaId);
    }
}
