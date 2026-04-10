package com.drama.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drama.tracker.dao.entity.Comment;
import com.drama.tracker.dao.mapper.CommentMapper;
import com.drama.tracker.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现类。
 *
 * @author drama-tracker
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public Page<Map<String, Object>> getCommentsByDrama(Long dramaId, Integer page, Integer size) {
        Page<Comment> commentPage = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDramaId, dramaId)
                .eq(Comment::getStatus, 1)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> result = this.page(commentPage, wrapper);

        // 转换为包含额外信息的 Map
        Page<Map<String, Object>> mapPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Map<String, Object>> records = result.getRecords().stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("dramaId", c.getDramaId());
            map.put("content", c.getContent());
            map.put("rating", c.getRating());
            map.put("likeCount", c.getLikeCount() != null ? c.getLikeCount() : 0);
            map.put("spoiler", c.getSpoiler() != null && c.getSpoiler());
            map.put("createTime", c.getCreateTime());
            // 使用 userId 作为昵称（简化处理，无需登录）
            map.put("nickname", c.getUserId() != null ? "用户" + c.getUserId() : "匿名用户");
            return map;
        }).toList();
        mapPage.setRecords(records);
        return mapPage;
    }

    @Override
    public Comment addComment(Long dramaId, String nickname, String content, Double rating, Boolean spoiler) {
        Comment comment = new Comment();
        comment.setDramaId(dramaId);
        // 用 nickname 的 hashCode 作为 userId（简化处理，无需真实用户系统）
        comment.setUserId((long) Math.abs(nickname.hashCode() % 100000));
        comment.setContent(content);
        if (rating != null && rating > 0) {
            comment.setRating(BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP));
        }
        comment.setSpoiler(spoiler != null && spoiler);
        comment.setStatus(1); // 直接通过
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        this.save(comment);
        return comment;
    }

    @Override
    public void likeComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setLikeCount((comment.getLikeCount() != null ? comment.getLikeCount() : 0) + 1);
            this.updateById(comment);
        }
    }

    @Override
    public Map<String, Object> getRatingStats(Long dramaId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDramaId, dramaId)
                .eq(Comment::getStatus, 1)
                .isNotNull(Comment::getRating);

        List<Comment> comments = this.list(wrapper);
        Map<String, Object> stats = new LinkedHashMap<>();

        if (comments.isEmpty()) {
            stats.put("avgRating", 0);
            stats.put("totalRatings", 0);
            stats.put("distribution", new int[10]);
            return stats;
        }

        double sum = comments.stream()
                .mapToDouble(c -> c.getRating().doubleValue())
                .sum();
        double avg = sum / comments.size();

        // 评分分布 (1-10)
        int[] distribution = new int[10];
        for (Comment c : comments) {
            int idx = Math.min(c.getRating().intValue() - 1, 9);
            if (idx >= 0) distribution[idx]++;
        }

        stats.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        stats.put("totalRatings", comments.size());
        stats.put("distribution", distribution);
        return stats;
    }
}
