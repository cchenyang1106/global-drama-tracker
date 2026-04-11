package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.AesUtil;
import com.drama.tracker.common.util.PhoneUtil;
import com.drama.tracker.dao.entity.*;
import com.drama.tracker.dao.mapper.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户数据导出与删除（GDPR/个人信息保护法）。
 */
@RestController
@RequestMapping("/api/user-data")
public class UserDataController {

    @Autowired private UserMapper userMapper;
    @Autowired private UserProfileMapper profileMapper;
    @Autowired private ActivityMapper activityMapper;
    @Autowired private MatchRequestMapper matchMapper;
    @Autowired private ChatMessageMapper chatMapper;
    @Autowired private ActivityCommentMapper commentMapper;
    @Autowired private UserReportMapper reportMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    @Value("${app.chat-secret:buddy-finder-chat-aes256-secret}")
    private String chatSecret;

    /**
     * 导出用户个人数据（JSON 格式）。
     */
    @GetMapping("/export")
    public Result<?> exportData(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Map<String, Object> data = new LinkedHashMap<>();

        // 基本信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            Map<String, Object> basic = new LinkedHashMap<>();
            basic.put("手机号", PhoneUtil.mask(user.getUsername()));
            basic.put("昵称", user.getNickname());
            basic.put("注册时间", user.getCreateTime());
            basic.put("最后登录", user.getLastLoginTime());
            data.put("基本信息", basic);
        }

        // 个人资料
        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile != null) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("真实姓名", profile.getRealName());
            p.put("年龄", profile.getAge());
            p.put("性别", profile.getGender() == 1 ? "男" : profile.getGender() == 2 ? "女" : "未知");
            p.put("城市", profile.getCity());
            p.put("职业", profile.getOccupation());
            p.put("简介", profile.getBio());
            p.put("爱好", profile.getHobbies());
            data.put("个人资料", p);
        }

        // 发布的活动
        List<Activity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>().eq(Activity::getUserId, userId).eq(Activity::getDeleted, 0));
        data.put("发布的活动", activities.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("标题", a.getTitle());
            m.put("分类", a.getCategory());
            m.put("描述", a.getDescription());
            m.put("发布时间", a.getCreateTime());
            return m;
        }).collect(Collectors.toList()));

        // 聊天记录（解密）
        List<ChatMessage> messages = chatMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSenderId, userId)
                        .orderByDesc(ChatMessage::getCreateTime).last("LIMIT 200"));
        data.put("发送的消息(最近200条)", messages.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("内容", AesUtil.decrypt(m.getContent(), chatSecret));
            map.put("时间", m.getCreateTime());
            return map;
        }).collect(Collectors.toList()));

        // 评论
        List<ActivityComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<ActivityComment>().eq(ActivityComment::getUserId, userId));
        data.put("评论", comments.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("内容", c.getContent());
            m.put("时间", c.getCreateTime());
            return m;
        }).collect(Collectors.toList()));

        return Result.success("数据导出成功", data);
    }

    /**
     * 删除用户所有数据（注销账号）。
     */
    @PostMapping("/delete-account")
    public Result<?> deleteAccount(@RequestHeader(value = "Authorization", required = false) String auth,
                                   @RequestBody(required = false) Map<String, String> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        // 需要输入密码确认
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");

        // 逻辑删除用户
        user.setDeleted(1);
        user.setStatus(0);
        user.setNickname("已注销用户");
        user.setUsername("deleted_" + userId); // 释放手机号
        userMapper.updateById(user);

        // 删除个人资料
        profileMapper.delete(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

        // 关闭所有活动
        activityMapper.selectList(new LambdaQueryWrapper<Activity>().eq(Activity::getUserId, userId))
                .forEach(a -> { a.setStatus(0); a.setDeleted(1); activityMapper.updateById(a); });

        // 删除评论
        commentMapper.delete(new LambdaQueryWrapper<ActivityComment>().eq(ActivityComment::getUserId, userId));

        // 删除聊天记录
        chatMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSenderId, userId));

        return Result.success("账号已注销，所有数据已删除");
    }

    private Long getUserIdFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload().getSubject());
        } catch (Exception e) { return null; }
    }
}
