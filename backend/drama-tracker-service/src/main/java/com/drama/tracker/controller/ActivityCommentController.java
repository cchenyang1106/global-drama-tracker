package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.SensitiveWordFilter;
import com.drama.tracker.dao.entity.Activity;
import com.drama.tracker.dao.entity.ActivityComment;
import com.drama.tracker.dao.entity.User;
import com.drama.tracker.dao.entity.UserProfile;
import com.drama.tracker.dao.mapper.ActivityCommentMapper;
import com.drama.tracker.dao.mapper.ActivityMapper;
import com.drama.tracker.dao.mapper.UserMapper;
import com.drama.tracker.dao.mapper.UserProfileMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tag(name = "活动评论", description = "活动评论接口")
@RestController
@RequestMapping("/api/activity-comment")
@RequiredArgsConstructor
public class ActivityCommentController {

    private final ActivityCommentMapper commentMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    private Long getUserIdFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload().getSubject());
        } catch (Exception e) { return null; }
    }

    /**
     * 获取活动评论列表。
     */
    @Operation(summary = "评论列表")
    @GetMapping("/list/{activityId}")
    public Result<Map<String, Object>> list(@PathVariable Long activityId,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "20") Integer size) {
        Page<ActivityComment> pageResult = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ActivityComment>()
                        .eq(ActivityComment::getActivityId, activityId)
                        .eq(ActivityComment::getHidden, 0)
                        .orderByDesc(ActivityComment::getPinned)
                        .orderByDesc(ActivityComment::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (ActivityComment c : pageResult.getRecords()) {
            Map<String, Object> item = commentToMap(c);
            attachUser(item, c.getUserId());
            // 如果是回复，附加被回复者信息
            if (c.getParentId() != null) {
                ActivityComment parent = commentMapper.selectById(c.getParentId());
                if (parent != null) {
                    User parentUser = userMapper.selectById(parent.getUserId());
                    item.put("replyToName", parentUser != null ? parentUser.getNickname() : "已删除");
                }
            }
            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    /**
     * 发表评论。
     */
    @Operation(summary = "发表评论")
    @PostMapping("/add")
    public Result<Long> add(@RequestHeader(value = "Authorization", required = false) String auth,
                            @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        String content = (String) body.get("content");
        String badWord = SensitiveWordFilter.detect(content);
        if (badWord != null) return Result.fail("评论包含违规词汇「" + badWord + "」，请修改");

        ActivityComment c = new ActivityComment();
        c.setActivityId(Long.valueOf(body.get("activityId").toString()));
        c.setUserId(userId);
        c.setContent(content);
        if (body.get("parentId") != null) {
            c.setParentId(Long.valueOf(body.get("parentId").toString()));
        }
        c.setPinned(0);
        c.setHidden(0);
        commentMapper.insert(c);
        return Result.success(c.getId());
    }

    /**
     * 置顶/取消置顶评论（仅活动发布者）。
     */
    @Operation(summary = "置顶评论")
    @PostMapping("/pin/{id}")
    public Result<String> pin(@RequestHeader(value = "Authorization", required = false) String auth,
                               @PathVariable Long id, @RequestParam Integer pin) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        ActivityComment c = commentMapper.selectById(id);
        if (c == null) return Result.fail(404, "评论不存在");

        Activity a = activityMapper.selectById(c.getActivityId());
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "仅活动发布者可操作");

        c.setPinned(pin);
        commentMapper.updateById(c);
        return Result.success(pin == 1 ? "已置顶" : "已取消置顶");
    }

    /**
     * 隐藏评论（仅活动发布者）。
     */
    @Operation(summary = "隐藏评论")
    @PostMapping("/hide/{id}")
    public Result<String> hide(@RequestHeader(value = "Authorization", required = false) String auth,
                                @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        ActivityComment c = commentMapper.selectById(id);
        if (c == null) return Result.fail(404, "评论不存在");

        Activity a = activityMapper.selectById(c.getActivityId());
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "仅活动发布者可操作");

        c.setHidden(1);
        commentMapper.updateById(c);
        return Result.success("已隐藏");
    }

    /**
     * 删除评论（评论者本人或活动发布者）。
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<String> delete(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        ActivityComment c = commentMapper.selectById(id);
        if (c == null) return Result.fail(404, "评论不存在");

        Activity a = activityMapper.selectById(c.getActivityId());
        boolean isOwner = a != null && a.getUserId().equals(userId);
        boolean isAuthor = c.getUserId().equals(userId);
        if (!isOwner && !isAuthor) return Result.fail(403, "无权操作");

        commentMapper.deleteById(id);
        return Result.success("已删除");
    }

    private void attachUser(Map<String, Object> item, Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            item.put("authorName", user.getNickname());
        }
        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile != null) {
            item.put("authorAvatar", profile.getAvatarUrl());
        }
    }

    private Map<String, Object> commentToMap(ActivityComment c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("activityId", c.getActivityId());
        m.put("userId", c.getUserId());
        m.put("content", c.getContent());
        m.put("parentId", c.getParentId());
        m.put("pinned", c.getPinned());
        m.put("createTime", c.getCreateTime());
        return m;
    }
}
