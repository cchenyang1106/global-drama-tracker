package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.Rating;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评分 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

}
