package com.drama.tracker.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drama.tracker.dao.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
