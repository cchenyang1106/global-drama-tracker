package com.drama.tracker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drama.tracker.dao.entity.Comment;

import java.util.Map;

/**
 * 评论服务接口。
 *
 * @author drama-tracker
 */
public interface CommentService extends IService<Comment> {

    /**
     * 分页获取剧集评论。
     *
     * @param dramaId 剧集ID
     * @param page    页码
     * @param size    每页大小
     * @return 评论分页数据
     */
    Page<Map<String, Object>> getCommentsByDrama(Long dramaId, Integer page, Integer size);

    /**
     * 发表评论。
     *
     * @param dramaId  剧集ID
     * @param nickname 昵称
     * @param content  评论内容
     * @param rating   评分（可选）
     * @param spoiler  是否剧透
     * @return 评论对象
     */
    Comment addComment(Long dramaId, String nickname, String content, Double rating, Boolean spoiler);

    /**
     * 点赞评论。
     *
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 获取剧集评分统计。
     *
     * @param dramaId 剧集ID
     * @return 评分统计
     */
    Map<String, Object> getRatingStats(Long dramaId);
}
