package com.drama.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drama.tracker.dao.entity.Ranking;

import java.util.List;

/**
 * 排行榜服务接口。
 *
 * @author drama-tracker
 */
public interface RankingService extends IService<Ranking> {

    /**
     * 获取日榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    List<Ranking> getDailyRanking(String category, String value, Integer limit);

    /**
     * 获取周榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    List<Ranking> getWeeklyRanking(String category, String value, Integer limit);

    /**
     * 获取月榜。
     *
     * @param category 榜单分类
     * @param value    分类值
     * @param limit    数量限制
     * @return 排行榜数据
     */
    List<Ranking> getMonthlyRanking(String category, String value, Integer limit);

    /**
     * 获取地区排行榜。
     *
     * @param region 地区代码
     * @param type   榜单类型
     * @param limit  数量限制
     * @return 排行榜数据
     */
    List<Ranking> getRegionRanking(String region, Integer type, Integer limit);

    /**
     * 获取新剧热度榜。
     *
     * @param days  天数范围
     * @param limit 数量限制
     * @return 排行榜数据
     */
    List<Ranking> getNewDramaRanking(Integer days, Integer limit);

    /**
     * 刷新排行榜数据（定时任务调用）。
     */
    void refreshRankings();
}
