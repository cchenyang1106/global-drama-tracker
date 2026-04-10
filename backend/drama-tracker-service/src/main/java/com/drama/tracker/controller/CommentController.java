package com.drama.tracker.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.Comment;
import com.drama.tracker.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器。
 *
 * @author drama-tracker
 */
@Tag(name = "评论管理", description = "剧集评论、评分相关接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取剧集评论列表。
     *
     * @param dramaId 剧集ID
     * @param page    页码
     * @param size    每页大小
     * @return 评论分页数据
     */
    @Operation(summary = "获取剧集评论列表")
    @GetMapping("/list/{dramaId}")
    public Result<Page<Map<String, Object>>> getComments(
            @PathVariable Long dramaId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.getCommentsByDrama(dramaId, page, size));
    }

    /**
     * 发表评论。
     *
     * @param body 评论内容
     * @return 评论对象
     */
    @Operation(summary = "发表评论")
    @PostMapping("/add")
    public Result<Comment> addComment(@RequestBody Map<String, Object> body) {
        Long dramaId = Long.valueOf(body.get("dramaId").toString());
        String nickname = body.getOrDefault("nickname", "匿名用户").toString();
        String content = body.get("content").toString();
        Double rating = body.containsKey("rating") && body.get("rating") != null
                ? Double.parseDouble(body.get("rating").toString()) : null;
        Boolean spoiler = body.containsKey("spoiler") && Boolean.TRUE.equals(body.get("spoiler"));

        Comment comment = commentService.addComment(dramaId, nickname, content, rating, spoiler);
        return Result.success(comment);
    }

    /**
     * 点赞评论。
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @Operation(summary = "点赞评论")
    @PostMapping("/like/{id}")
    public Result<String> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success("点赞成功");
    }

    /**
     * 获取剧集评分统计。
     *
     * @param dramaId 剧集ID
     * @return 评分统计
     */
    @Operation(summary = "获取剧集评分统计")
    @GetMapping("/rating-stats/{dramaId}")
    public Result<Map<String, Object>> getRatingStats(@PathVariable Long dramaId) {
        return Result.success(commentService.getRatingStats(dramaId));
    }
}
