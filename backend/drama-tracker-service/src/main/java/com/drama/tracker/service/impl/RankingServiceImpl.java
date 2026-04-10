package com.drama.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drama.tracker.dao.entity.Drama;
import com.drama.tracker.dao.entity.Ranking;
import com.drama.tracker.dao.mapper.RankingMapper;
import com.drama.tracker.service.DramaService;
import com.drama.tracker.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现类。
 *
 * @author drama-tracker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl extends ServiceImpl<RankingMapper, Ranking> implements RankingService {

    private final DramaService dramaService;

    /**
     * 地区列表。
     */
    private static final String[] REGIONS = {"CN", "JP", "KR", "US", "UK", "EU"};

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
        wrapper.eq(Ranking::getRankType, 1)
                .eq(Ranking::getRankCategory, "new")
                .eq(Ranking::getStatDate, LocalDate.now())
                .orderByAsc(Ranking::getRank)
                .last("LIMIT " + limit);

        return this.list(wrapper);
    }

    /**
     * 刷新排行榜数据（每天凌晨 2 点执行）。
     * 计算日榜/周榜/月榜/地区榜/新剧榜。
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshRankings() {
        log.info("Starting to refresh rankings...");
        LocalDate today = LocalDate.now();

        try {
            // 获取所有剧集数据
            List<Drama> allDramas = dramaService.list();
            if (allDramas.isEmpty()) {
                log.info("No drama data found, skipping ranking refresh.");
                return;
            }

            // 删除今天的旧排行数据
            LambdaQueryWrapper<Ranking> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(Ranking::getStatDate, today);
            this.remove(deleteWrapper);

            List<Ranking> allRankings = new ArrayList<>();

            // 1. 热度总榜 (日/周/月)
            List<Drama> hotSorted = allDramas.stream()
                    .sorted(Comparator.comparingLong(d -> -(d.getHotScore() == null ? 0 : d.getHotScore())))
                    .limit(50)
                    .collect(Collectors.toList());
            allRankings.addAll(buildRankings(hotSorted, 1, "hot", null, today)); // 日榜
            allRankings.addAll(buildRankings(hotSorted, 2, "hot", null, today)); // 周榜
            allRankings.addAll(buildRankings(hotSorted, 3, "hot", null, today)); // 月榜

            // 2. 地区排行榜
            for (String region : REGIONS) {
                List<Drama> regionDramas = allDramas.stream()
                        .filter(d -> region.equals(d.getRegion()))
                        .sorted(Comparator.comparingLong(d -> -(d.getHotScore() == null ? 0 : d.getHotScore())))
                        .limit(20)
                        .collect(Collectors.toList());
                allRankings.addAll(buildRankings(regionDramas, 1, "region", region, today));
                allRankings.addAll(buildRankings(regionDramas, 2, "region", region, today));
                allRankings.addAll(buildRankings(regionDramas, 3, "region", region, today));
            }

            // 3. 新剧榜 (90天内的新剧)
            LocalDate newDramaCutoff = today.minusDays(90);
            List<Drama> newDramas = allDramas.stream()
                    .filter(d -> d.getReleaseDate() != null && d.getReleaseDate().isAfter(newDramaCutoff))
                    .sorted(Comparator.comparingLong(d -> -(d.getHotScore() == null ? 0 : d.getHotScore())))
                    .limit(20)
                    .collect(Collectors.toList());
            allRankings.addAll(buildRankings(newDramas, 1, "new", null, today));

            // 批量保存
            this.saveBatch(allRankings);

            log.info("Rankings refresh completed. Total {} records.", allRankings.size());
        } catch (Exception e) {
            log.error("Rankings refresh failed", e);
        }
    }

    /**
     * 构建排行榜记录。
     */
    private List<Ranking> buildRankings(List<Drama> dramas, int rankType, String category,
                                         String categoryValue, LocalDate statDate) {
        List<Ranking> rankings = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);

        // 获取昨日排行用于计算排名变化
        LocalDate yesterday = statDate.minusDays(1);

        for (Drama drama : dramas) {
            Ranking ranking = new Ranking();
            ranking.setDramaId(drama.getId());
            ranking.setRankType(rankType);
            ranking.setRankCategory(category);
            ranking.setCategoryValue(categoryValue);
            ranking.setRank(rank.getAndIncrement());
            ranking.setScore(drama.getTotalRating() != null ? drama.getTotalRating() : BigDecimal.ZERO);
            ranking.setHotScore(drama.getHotScore() != null ? drama.getHotScore() : 0L);
            ranking.setStatDate(statDate);

            // 查找昨日排名，计算变化
            LambdaQueryWrapper<Ranking> prevWrapper = new LambdaQueryWrapper<>();
            prevWrapper.eq(Ranking::getDramaId, drama.getId())
                    .eq(Ranking::getRankType, rankType)
                    .eq(Ranking::getRankCategory, category)
                    .eq(Ranking::getStatDate, yesterday);
            if (categoryValue != null) {
                prevWrapper.eq(Ranking::getCategoryValue, categoryValue);
            }
            Ranking prev = this.getOne(prevWrapper, false);
            if (prev != null) {
                ranking.setPreviousRank(prev.getRank());
                ranking.setRankChange(prev.getRank() - ranking.getRank()); // 正数表示上升
            }

            rankings.add(ranking);
        }

        return rankings;
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

        List<Ranking> rankings = this.list(wrapper);

        // 填充剧集名称
        if (!rankings.isEmpty()) {
            List<Long> dramaIds = rankings.stream()
                    .map(Ranking::getDramaId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> titleMap = dramaService.listByIds(dramaIds).stream()
                    .collect(Collectors.toMap(Drama::getId, Drama::getTitle, (a, b) -> a));
            rankings.forEach(r -> r.setDramaTitle(titleMap.get(r.getDramaId())));
        }

        return rankings;
    }
}
