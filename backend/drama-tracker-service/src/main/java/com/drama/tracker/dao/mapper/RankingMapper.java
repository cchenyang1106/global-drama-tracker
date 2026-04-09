package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.Ranking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排行榜 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface RankingMapper extends BaseMapper<Ranking> {

}
