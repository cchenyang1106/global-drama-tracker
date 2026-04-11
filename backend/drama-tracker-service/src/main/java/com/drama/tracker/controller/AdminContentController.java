package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.*;
import com.drama.tracker.dao.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "管理端-内容管理", description = "管理活动、用户、评论")
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final ActivityCommentMapper commentMapper;

    /**
     * 活动列表（管理端）。
     */
    @Operation(summary = "活动列表")
    @GetMapping("/activities")
    public Result<Map<String, Object>> listActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword));
        }
        if (status != null) w.eq(Activity::getStatus, status);
        w.orderByDesc(Activity::getCreateTime);

        Page<Activity> p = activityMapper.selectPage(new Page<>(page, size), w);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Activity a : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("category", a.getCategory());
            item.put("description", a.getDescription());
            item.put("location", a.getLocation());
            item.put("activityTime", a.getActivityTime());
            item.put("status", a.getStatus());
            item.put("viewCount", a.getViewCount());
            item.put("joinedCount", a.getJoinedCount());
            item.put("maxPeople", a.getMaxPeople());
            item.put("createTime", a.getCreateTime());
            User user = userMapper.selectById(a.getUserId());
            item.put("authorId", a.getUserId());
            item.put("authorName", user != null ? user.getNickname() : "未知");
            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    /**
     * 关闭/删除活动。
     */
    @Operation(summary = "关闭活动")
    @PostMapping("/activities/{id}/close")
    public Result<String> closeActivity(@PathVariable Long id) {
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");
        a.setStatus(0);
        activityMapper.updateById(a);
        return Result.success("已关闭");
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/activities/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        activityMapper.deleteById(id);
        commentMapper.delete(new LambdaQueryWrapper<ActivityComment>()
                .eq(ActivityComment::getActivityId, id));
        return Result.success("已删除");
    }

    /**
     * 用户列表。
     */
    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(User::getNickname, keyword).or().like(User::getUsername, keyword));
        }
        w.orderByDesc(User::getCreateTime);

        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        List<Map<String, Object>> records = new ArrayList<>();
        for (User u : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("phone", u.getUsername());
            item.put("nickname", u.getNickname());
            item.put("status", u.getStatus());
            item.put("role", u.getRole());
            item.put("createTime", u.getCreateTime());
            item.put("lastLoginTime", u.getLastLoginTime());
            // 不返回密码和敏感信息
            UserProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, u.getId()));
            if (profile != null) {
                item.put("city", profile.getCity());
                item.put("gender", profile.getGender());
                item.put("age", profile.getAge());
            }
            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    /**
     * 禁用/启用用户。
     */
    @Operation(summary = "禁用/启用用户")
    @PostMapping("/users/{id}/toggle")
    public Result<String> toggleUser(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.fail(404, "用户不存在");
        u.setStatus(u.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(u);
        return Result.success(u.getStatus() == 1 ? "已启用" : "已禁用");
    }

    /**
     * 评论列表（管理端）。
     */
    @Operation(summary = "评论列表")
    @GetMapping("/comments")
    public Result<Map<String, Object>> listComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<ActivityComment> p = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ActivityComment>().orderByDesc(ActivityComment::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (ActivityComment c : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("activityId", c.getActivityId());
            item.put("content", c.getContent());
            item.put("hidden", c.getHidden());
            item.put("pinned", c.getPinned());
            item.put("createTime", c.getCreateTime());
            User user = userMapper.selectById(c.getUserId());
            item.put("authorName", user != null ? user.getNickname() : "未知");
            Activity a = activityMapper.selectById(c.getActivityId());
            item.put("activityTitle", a != null ? a.getTitle() : "已删除");
            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    /**
     * 删除评论。
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentMapper.deleteById(id);
        return Result.success("已删除");
    }

    /**
     * 隐藏/显示评论。
     */
    @Operation(summary = "隐藏/显示评论")
    @PostMapping("/comments/{id}/toggle-hide")
    public Result<String> toggleHide(@PathVariable Long id) {
        ActivityComment c = commentMapper.selectById(id);
        if (c == null) return Result.fail(404, "评论不存在");
        c.setHidden(c.getHidden() == 1 ? 0 : 1);
        commentMapper.updateById(c);
        return Result.success(c.getHidden() == 1 ? "已隐藏" : "已显示");
    }
}
