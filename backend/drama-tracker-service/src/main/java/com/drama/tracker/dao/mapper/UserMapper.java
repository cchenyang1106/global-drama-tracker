package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
