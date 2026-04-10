package com.drama.tracker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drama.tracker.dao.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口。
 *
 * @author drama-tracker
 */
public interface CommentService extends IService<Comment> {

    Page<Map<String, Object>> getCommentsByDrama(Long dramaId, Integer page, Integer size);

    Comment addComment(Long dramaId, String nickname, String content, Double rating, Boolean spoiler);

    void likeComment(Long commentId);

    Map<String, Object> getRatingStats(Long dramaId);

    /** 获取精选评论 */
    List<Map<String, Object>> getFeaturedComments(Long dramaId, Integer limit);

    /** 设置/取消精选 */
    void setFeatured(Long commentId, boolean featured);

    /** 删除评论 */
    void deleteComment(Long commentId);

    /** 获取所有评论（管理端分页） */
    Page<Map<String, Object>> getAllComments(Integer page, Integer size);
}
