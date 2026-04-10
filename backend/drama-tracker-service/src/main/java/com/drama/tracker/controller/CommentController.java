package com.drama.tracker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.Comment;
import com.drama.tracker.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评论管理", description = "剧集评论、评分相关接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取剧集评论列表")
    @GetMapping("/list/{dramaId}")
    public Result<Page<Map<String, Object>>> getComments(
            @PathVariable Long dramaId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.getCommentsByDrama(dramaId, page, size));
    }

    @Operation(summary = "获取精选评论")
    @GetMapping("/featured")
    public Result<List<Map<String, Object>>> getFeaturedComments(
            @RequestParam(required = false) Long dramaId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(commentService.getFeaturedComments(dramaId, limit));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/add")
    public Result<Comment> addComment(@RequestBody Map<String, Object> body) {
        Long dramaId = Long.valueOf(body.get("dramaId").toString());
        String nickname = body.getOrDefault("nickname", "匿名用户").toString();
        String content = body.get("content").toString();
        Double rating = body.containsKey("rating") && body.get("rating") != null
                ? Double.parseDouble(body.get("rating").toString()) : null;
        Boolean spoiler = body.containsKey("spoiler") && Boolean.TRUE.equals(body.get("spoiler"));
        return Result.success(commentService.addComment(dramaId, nickname, content, rating, spoiler));
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/like/{id}")
    public Result<String> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success("点赞成功");
    }

    @Operation(summary = "获取剧集评分统计")
    @GetMapping("/rating-stats/{dramaId}")
    public Result<Map<String, Object>> getRatingStats(@PathVariable Long dramaId) {
        return Result.success(commentService.getRatingStats(dramaId));
    }

    // ========== 管理端接口 ==========

    @Operation(summary = "获取所有评论（管理端）")
    @GetMapping("/admin/list")
    public Result<Page<Map<String, Object>>> getAllComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.getAllComments(page, size));
    }

    @Operation(summary = "设置/取消精选")
    @PostMapping("/admin/featured/{id}")
    public Result<String> setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        commentService.setFeatured(id, featured);
        return Result.success(featured ? "已加精" : "已取消加精");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/admin/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功");
    }
}
