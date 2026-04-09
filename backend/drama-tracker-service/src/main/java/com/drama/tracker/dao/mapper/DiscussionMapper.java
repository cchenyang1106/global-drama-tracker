package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.Discussion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 讨论帖 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface DiscussionMapper extends BaseMapper<Discussion> {

}
