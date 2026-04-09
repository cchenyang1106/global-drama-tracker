package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.Drama;
import org.apache.ibatis.annotations.Mapper;

/**
 * 剧集 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface DramaMapper extends BaseMapper<Drama> {

}
