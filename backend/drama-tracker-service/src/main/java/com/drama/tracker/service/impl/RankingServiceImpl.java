package com.drama.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drama.tracker.dao.entity.Ranking;
import com.drama.tracker.dao.mapper.RankingMapper;
import com.drama.tracker.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 排行榜服务实现类。
 *
 * @author drama-tracker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl extends ServiceImpl<RankingMapper, Ranking> implements RankingService {

    /**
     * 获取日榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Override
    public List<Ranking> getDailyRanking(String category, String value, Integer limit) {
        return getRanking(1, category, value, limit);
    }

    /**
     * 获取周榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Override
    public List<Ranking> getWeeklyRanking(String category, String value, Integer limit) {
        return getRanking(2, category, value, limit);
    }

    /**
     * 获取月榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    @Override
    public List<Ranking> getMonthlyRanking(String category, String value, Integer limit) {
        return getRanking(3, category, value, limit);
    }

    /**
     * 获取地区排行榜。
     *
     * @param region 地区代码
     * @param type   榜单类型
     * @param limit  数量限制
     * @return 排行榜数据
     */
    @Override
    public List<Ranking> getRegionRanking(String region, Integer type, Integer limit) {
        return getRanking(type, "region", region, limit);
    }

    /**
     * 获取新剧热度榜。
     *
     * @param days  天数范围
     * @param limit 数量限制
     * @return 排行榜数据
     */
    @Override
    public List<Ranking> getNewDramaRanking(Integer days, Integer limit) {
        LambdaQueryWrapper<Ranking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ranking::getRankType, 1) // 日榜
                .eq(Ranking::getRankCategory, "new")
                .eq(Ranking::getStatDate, LocalDate.now())
                .orderByAsc(Ranking::getRank)
                .last("LIMIT " + limit);

        return this.list(wrapper);
    }

    /**
     * 刷新排行榜数据（每天凌晨 2 点执行）。
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshRankings() {
        log.info("Starting to refresh rankings...");
        // TODO: 实现排行榜刷新逻辑
        // 1. 计算日榜
        // 2. 计算周榜
        // 3. 计算月榜
        // 4. 计算地区榜
        // 5. 计算新剧榜
        log.info("Rankings refresh completed.");
    }

    /**
     * 获取排行榜通用方法。
     *
     * @param rankType 榜单类型
     * @param category 分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    private List<Ranking> getRanking(Integer rankType, String category, String value, Integer limit) {
        LambdaQueryWrapper<Ranking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ranking::getRankType, rankType)
                .eq(Ranking::getRankCategory, category)
                .eq(StringUtils.isNotBlank(value), Ranking::getCategoryValue, value)
                .eq(Ranking::getStatDate, LocalDate.now())
                .orderByAsc(Ranking::getRank)
                .last("LIMIT " + limit);

        return this.list(wrapper);
    }
}
