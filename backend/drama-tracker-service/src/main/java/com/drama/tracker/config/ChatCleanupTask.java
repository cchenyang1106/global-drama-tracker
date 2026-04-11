package com.drama.tracker.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.dao.entity.ChatMessage;
import com.drama.tracker.dao.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 聊天记录定期清理任务。
 * 每天凌晨 3 点删除 30 天前的聊天记录。
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ChatCleanupTask {

    private final ChatMessageMapper chatMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        long deleted = chatMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .lt(ChatMessage::getCreateTime, cutoff));
        if (deleted > 0) {
            log.info("已清理 {} 条超过30天的聊天记录", deleted);
        }
    }
}
