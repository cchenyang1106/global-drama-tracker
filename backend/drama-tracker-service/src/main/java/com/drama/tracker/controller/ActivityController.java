package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.ContentSecurityChecker;
import com.drama.tracker.dao.entity.Activity;
import com.drama.tracker.dao.entity.ActivityMessage;
import com.drama.tracker.dao.entity.ActivityQuiz;
import com.drama.tracker.dao.entity.MatchRequest;
import com.drama.tracker.dao.entity.User;
import com.drama.tracker.dao.entity.UserProfile;
import com.drama.tracker.dao.mapper.ActivityMapper;
import com.drama.tracker.dao.mapper.ActivityMessageMapper;
import com.drama.tracker.dao.mapper.ActivityQuizMapper;
import com.drama.tracker.dao.mapper.MatchRequestMapper;
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
    private final ActivityQuizMapper quizMapper;
    private final ActivityMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final MatchRequestMapper matchRequestMapper;
    private final ContentSecurityChecker contentSecurityChecker;

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
        // 题目数量
        Long quizCount = quizMapper.selectCount(new LambdaQueryWrapper<ActivityQuiz>()
                .eq(ActivityQuiz::getActivityId, a.getId()));
        data.put("quizCount", quizCount);
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

        // 内容安全检查（本地敏感词 + 微信 msgSecCheck）
        String title = (String) body.get("title");
        String desc = (String) body.get("description");
        String secResult = contentSecurityChecker.check(title, ContentSecurityChecker.SCENE_FORUM);
        if (secResult == null) secResult = contentSecurityChecker.check(desc, ContentSecurityChecker.SCENE_FORUM);
        if (secResult != null) return Result.fail(secResult);

        Activity a = new Activity();
        a.setUserId(userId);
        a.setTitle(title);
        a.setCategory((String) body.getOrDefault("category", "其他"));
        a.setDescription((String) body.get("description"));
        a.setLocation((String) body.get("location"));
        a.setActivityTime((String) body.get("activityTime"));
        a.setMaxPeople(body.get("maxPeople") != null ? Integer.parseInt(body.get("maxPeople").toString()) : 1);
        a.setTags((String) body.get("tags"));
        a.setPreferGender(body.get("preferGender") != null ? Integer.parseInt(body.get("preferGender").toString()) : 0);
        a.setPreferAgeMin(body.get("preferAgeMin") != null ? Integer.parseInt(body.get("preferAgeMin").toString()) : null);
        a.setPreferAgeMax(body.get("preferAgeMax") != null ? Integer.parseInt(body.get("preferAgeMax").toString()) : null);
        a.setPreferCity((String) body.get("preferCity"));
        a.setPreferTags((String) body.get("preferTags"));
        a.setImages((String) body.get("images"));
        a.setContactInfo((String) body.get("contactInfo"));
        a.setStatus(1);
        a.setJoinedCount(0);
        a.setViewCount(0);
        a.setTeamComplete(0);
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

    /**
     * 更新活动公告（发布人）。
     */
    @PostMapping("/announcement/{id}")
    public Result<?> updateAnnouncement(@RequestHeader(value = "Authorization", required = false) String auth,
                                         @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");
        if (!a.getUserId().equals(userId)) return Result.fail(403, "只有发布人可以更新公告");
        // 内容安全检查
        String announcement = (String) body.get("announcement");
        String secResult = contentSecurityChecker.check(announcement, ContentSecurityChecker.SCENE_FORUM);
        if (secResult != null) return Result.fail(secResult);
        a.setAnnouncement(announcement);
        activityMapper.updateById(a);
        return Result.success("公告已更新");
    }

    /**
     * 获取活动联系方式（仅通过审核的参与者可见）。
     */
    @GetMapping("/contact/{id}")
    public Result<?> getContactInfo(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");
        // 发布人自己可以看
        if (a.getUserId().equals(userId)) {
            return Result.success(a.getContactInfo());
        }
        // 检查是否通过审核
        MatchRequest mr = matchRequestMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getActivityId, id).eq(MatchRequest::getFromUserId, userId));
        if (mr == null || mr.getStatus() != 1) return Result.fail(403, "需通过活动审核后才能查看");
        return Result.success(a.getContactInfo());
    }

    /**
     * 获取活动已通过成员列表（仅通过审核的参与者和发布人可见）。
     */
    @Operation(summary = "活动成员列表")
    @GetMapping("/members/{id}")
    public Result<?> getMembers(@RequestHeader(value = "Authorization", required = false) String auth,
                                @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");

        // 权限校验：发布人或已通过成员
        if (!a.getUserId().equals(userId)) {
            MatchRequest mr = matchRequestMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                    .eq(MatchRequest::getActivityId, id).eq(MatchRequest::getFromUserId, userId));
            if (mr == null || mr.getStatus() != 1) return Result.fail(403, "需通过活动审核后才能查看");
        }

        // 查询所有通过的成员
        List<MatchRequest> passed = matchRequestMapper.selectList(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getActivityId, id).eq(MatchRequest::getStatus, 1));

        List<Map<String, Object>> members = new ArrayList<>();
        // 发布人作为第一个成员
        User owner = userMapper.selectById(a.getUserId());
        UserProfile ownerProfile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, a.getUserId()));
        Map<String, Object> ownerMap = new LinkedHashMap<>();
        ownerMap.put("userId", a.getUserId());
        ownerMap.put("nickname", owner != null ? owner.getNickname() : "发布人");
        ownerMap.put("gender", ownerProfile != null ? ownerProfile.getGender() : null);
        ownerMap.put("city", ownerProfile != null ? ownerProfile.getCity() : null);
        ownerMap.put("role", "发起人");
        members.add(ownerMap);

        for (MatchRequest mr : passed) {
            User u = userMapper.selectById(mr.getFromUserId());
            UserProfile p = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, mr.getFromUserId()));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", mr.getFromUserId());
            m.put("nickname", u != null ? u.getNickname() : "成员");
            m.put("gender", p != null ? p.getGender() : null);
            m.put("city", p != null ? p.getCity() : null);
            m.put("role", "成员");
            members.add(m);
        }
        return Result.success(members);
    }

    /**
     * 获取活动留言板（仅通过审核的参与者和发布人可见）。
     */
    @Operation(summary = "活动留言板列表")
    @GetMapping("/messages/{id}")
    public Result<?> getMessages(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @PathVariable Long id,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "50") Integer size) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");

        // 权限校验
        if (!a.getUserId().equals(userId)) {
            MatchRequest mr = matchRequestMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                    .eq(MatchRequest::getActivityId, id).eq(MatchRequest::getFromUserId, userId));
            if (mr == null || mr.getStatus() != 1) return Result.fail(403, "需通过活动审核后才能查看");
        }

        var pageResult = messageMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                new LambdaQueryWrapper<ActivityMessage>()
                        .eq(ActivityMessage::getActivityId, id)
                        .orderByDesc(ActivityMessage::getCreateTime));

        List<Map<String, Object>> messages = new ArrayList<>();
        for (ActivityMessage msg : pageResult.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", msg.getId());
            m.put("userId", msg.getUserId());
            m.put("content", msg.getContent());
            m.put("createTime", msg.getCreateTime());
            User u = userMapper.selectById(msg.getUserId());
            m.put("nickname", u != null ? u.getNickname() : "匿名");
            m.put("isOwner", msg.getUserId().equals(a.getUserId()));
            messages.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", messages);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    /**
     * 发送活动留言（仅通过审核的参与者和发布人可发）。
     */
    @Operation(summary = "发送活动留言")
    @PostMapping("/messages/{id}")
    public Result<?> postMessage(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(id);
        if (a == null) return Result.fail(404, "活动不存在");

        // 权限校验
        if (!a.getUserId().equals(userId)) {
            MatchRequest mr = matchRequestMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                    .eq(MatchRequest::getActivityId, id).eq(MatchRequest::getFromUserId, userId));
            if (mr == null || mr.getStatus() != 1) return Result.fail(403, "需通过活动审核后才能留言");
        }

        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) return Result.fail("留言内容不能为空");

        // 内容安全检查
        String secResult = contentSecurityChecker.check(content, ContentSecurityChecker.SCENE_COMMENT);
        if (secResult != null) return Result.fail(secResult);

        ActivityMessage msg = new ActivityMessage();
        msg.setActivityId(id);
        msg.setUserId(userId);
        msg.setContent(content.trim());
        messageMapper.insert(msg);

        return Result.success("留言成功");
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
        m.put("announcement", a.getAnnouncement());
        m.put("teamComplete", a.getTeamComplete());
        m.put("status", a.getStatus());
        m.put("viewCount", a.getViewCount());
        m.put("createTime", a.getCreateTime());
        return m;
    }
}
