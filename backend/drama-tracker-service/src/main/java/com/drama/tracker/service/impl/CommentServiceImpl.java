package com.drama.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drama.tracker.dao.entity.Comment;
import com.drama.tracker.dao.mapper.CommentMapper;
import com.drama.tracker.service.CommentService;
import com.drama.tracker.service.DramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final DramaService dramaService;

    private Map<String, Object> toMap(Comment c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("dramaId", c.getDramaId());
        map.put("nickname", c.getNickname() != null ? c.getNickname() : "匿名用户");
        map.put("content", c.getContent());
        map.put("rating", c.getRating());
        map.put("likeCount", c.getLikeCount() != null ? c.getLikeCount() : 0);
        map.put("spoiler", c.getSpoiler() != null && c.getSpoiler());
        map.put("featured", c.getFeatured() != null && c.getFeatured());
        map.put("createTime", c.getCreateTime());
        // 附带剧集名称
        try {
            var drama = dramaService.getById(c.getDramaId());
            if (drama != null) map.put("dramaTitle", drama.getTitle());
        } catch (Exception ignored) {}
        return map;
    }

    @Override
    public Page<Map<String, Object>> getCommentsByDrama(Long dramaId, Integer page, Integer size) {
        Page<Comment> commentPage = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDramaId, dramaId)
                .eq(Comment::getStatus, 1)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getFeatured)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> result = this.page(commentPage, wrapper);
        Page<Map<String, Object>> mapPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        mapPage.setRecords(result.getRecords().stream().map(this::toMap).toList());
        return mapPage;
    }

    @Override
    public Comment addComment(Long dramaId, String nickname, String content, Double rating, Boolean spoiler) {
        Comment comment = new Comment();
        comment.setDramaId(dramaId);
        comment.setNickname(nickname != null && !nickname.isBlank() ? nickname : "匿名用户");
        comment.setUserId((long) Math.abs(nickname.hashCode() % 100000));
        comment.setContent(content);
        if (rating != null && rating > 0) {
            comment.setRating(BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP));
        }
        comment.setSpoiler(spoiler != null && spoiler);
        comment.setFeatured(false);
        comment.setStatus(1);
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
            return stats;
        }
        double avg = comments.stream().mapToDouble(c -> c.getRating().doubleValue()).average().orElse(0);
        stats.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        stats.put("totalRatings", comments.size());
        return stats;
    }

    @Override
    public List<Map<String, Object>> getFeaturedComments(Long dramaId, Integer limit) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus, 1)
                .eq(Comment::getFeatured, true);
        if (dramaId != null) {
            wrapper.eq(Comment::getDramaId, dramaId);
        }
        wrapper.orderByDesc(Comment::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 10));
        return this.list(wrapper).stream().map(this::toMap).toList();
    }

    @Override
    public void setFeatured(Long commentId, boolean featured) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setFeatured(featured);
            this.updateById(comment);
        }
    }

    @Override
    public void deleteComment(Long commentId) {
        this.removeById(commentId);
    }

    @Override
    public Page<Map<String, Object>> getAllComments(Integer page, Integer size) {
        Page<Comment> commentPage = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        Page<Comment> result = this.page(commentPage, wrapper);
        Page<Map<String, Object>> mapPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        mapPage.setRecords(result.getRecords().stream().map(this::toMap).toList());
        return mapPage;
    }
}
