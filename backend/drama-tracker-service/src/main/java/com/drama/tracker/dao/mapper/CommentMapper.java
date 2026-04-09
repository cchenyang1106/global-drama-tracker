package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper 接口。
 *
 * @author drama-tracker
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
