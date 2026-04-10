package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.Activity;
import com.drama.tracker.dao.entity.User;
import com.drama.tracker.dao.entity.UserProfile;
import com.drama.tracker.dao.mapper.ActivityMapper;
import com.drama.tracker.dao.mapper.UserMapper;
import com.drama.tracker.dao.mapper.UserProfileMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "活动", description = "活动发布与管理接口")
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

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
     * 活动列表（广场）。
     */
    @Operation(summary = "活动广场列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> listActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, 1);
        if (StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)) {
            wrapper.eq(Activity::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword));
        }
        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> pageResult = activityMapper.selectPage(new Page<>(page, size), wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Activity a : pageResult.getRecords()) {
            Map<String, Object> item = activityToMap(a);
            // 附加发布者信息
            User user = userMapper.selectById(a.getUserId());
            UserProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, a.getUserId()));
            if (user != null) item.put("authorName", user.getNickname());
            if (profile != null) {
                // 列表不返回 base64 头像（太大），只返回城市
                item.put("authorCity", profile.getCity());
                item.put("authorGender", profile.getGender());
            }
            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 活动详情。
     */
    @Operation(summary = "活动详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getActivity(@PathVariable Long id) {
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");
        // 浏览量+1
        a.setViewCount(a.getViewCount() + 1);
        activityMapper.updateById(a);

        Map<String, Object> data = activityToMap(a);
        User user = userMapper.selectById(a.getUserId());
        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, a.getUserId()));
        if (user != null) data.put("authorName", user.getNickname());
        if (profile != null) {
            data.put("authorCity", profile.getCity());
            data.put("authorAge", profile.getAge());
            data.put("authorGender", profile.getGender());
            data.put("authorBio", profile.getBio());
        }
        return Result.success(data);
    }

    /**
     * 发布活动。
     */
    @Operation(summary = "发布活动")
    @PostMapping("/publish")
    public Result<Long> publish(@RequestHeader(value = "Authorization", required = false) String auth,
                                @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Activity a = new Activity();
        a.setUserId(userId);
        a.setTitle((String) body.get("title"));
        a.setCategory((String) body.getOrDefault("category", "其他"));
        a.setDescription((String) body.get("description"));
        a.setLocation((String) body.get("location"));
        a.setActivityTime((String) body.get("activityTime"));
        a.setMaxPeople(body.get("maxPeople") != null ? Integer.parseInt(body.get("maxPeople").toString()) : 1);
        a.setTags((String) body.get("tags"));
        a.setImages((String) body.get("images"));
        a.setStatus(1);
        a.setJoinedCount(0);
        a.setViewCount(0);
        activityMapper.insert(a);
        return Result.success(a.getId());
    }

    /**
     * 我发布的活动列表。
     */
    @Operation(summary = "我发布的活动")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myActivities(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        List<Activity> list = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>().eq(Activity::getUserId, userId).orderByDesc(Activity::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Activity a : list) result.add(activityToMap(a));
        return Result.success(result);
    }

    /**
     * 关闭活动。
     */
    @Operation(summary = "关闭活动")
    @PostMapping("/close/{id}")
    public Result<String> closeActivity(@RequestHeader(value = "Authorization", required = false) String auth,
                                         @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");
        if (!a.getUserId().equals(userId)) return Result.fail(403, "无权操作");
        a.setStatus(0);
        activityMapper.updateById(a);
        return Result.success("已关闭");
    }

    private Map<String, Object> activityToMap(Activity a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("userId", a.getUserId());
        m.put("title", a.getTitle());
        m.put("category", a.getCategory());
        m.put("description", a.getDescription());
        m.put("location", a.getLocation());
        m.put("activityTime", a.getActivityTime());
        m.put("maxPeople", a.getMaxPeople());
        m.put("joinedCount", a.getJoinedCount());
        m.put("tags", a.getTags());
        m.put("images", a.getImages());
        m.put("status", a.getStatus());
        m.put("viewCount", a.getViewCount());
        m.put("createTime", a.getCreateTime());
        return m;
    }
}
