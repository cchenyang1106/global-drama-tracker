package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.PageView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PageViewMapper extends BaseMapper<PageView> {

    @Select("SELECT DATE(create_time) as date, COUNT(*) as pv, COUNT(DISTINCT visitor_id) as uv " +
            "FROM page_view WHERE create_time >= #{startDate} GROUP BY DATE(create_time) ORDER BY date DESC")
    List<Map<String, Object>> getDailyStats(String startDate);

    @Select("SELECT path, COUNT(*) as pv, COUNT(DISTINCT visitor_id) as uv " +
            "FROM page_view WHERE create_time >= #{startDate} GROUP BY path ORDER BY pv DESC LIMIT 20")
    List<Map<String, Object>> getPageStats(String startDate);

    @Select("SELECT COUNT(*) as totalPv, COUNT(DISTINCT visitor_id) as totalUv FROM page_view WHERE DATE(create_time) = CURDATE()")
    Map<String, Object> getTodayStats();

    @Select("SELECT COUNT(*) as totalPv, COUNT(DISTINCT visitor_id) as totalUv FROM page_view")
    Map<String, Object> getAllTimeStats();

    @Select("SELECT HOUR(create_time) as hour, COUNT(*) as pv FROM page_view " +
            "WHERE DATE(create_time) = CURDATE() GROUP BY HOUR(create_time) ORDER BY hour")
    List<Map<String, Object>> getHourlyStats();
}
