package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.User;
import com.drama.tracker.dao.entity.UserProfile;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户资料", description = "个人资料管理接口")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileMapper profileMapper;
    private final UserMapper userMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    private Long getUserIdFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            String token = auth.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject());
        } catch (Exception e) { return null; }
    }

    /**
     * 获取当前用户资料。
     */
    @Operation(summary = "获取我的资料")
    @GetMapping("/me")
    public Result<Map<String, Object>> getMyProfile(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        User user = userMapper.selectById(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("phone", user != null ? user.getUsername() : null);
        data.put("nickname", user != null ? user.getNickname() : null);
        if (profile != null) {
            data.put("profileId", profile.getId());
            data.put("avatarUrl", profile.getAvatarUrl());
            data.put("realName", profile.getRealName());
            data.put("age", profile.getAge());
            data.put("gender", profile.getGender());
            data.put("city", profile.getCity());
            data.put("occupation", profile.getOccupation());
            data.put("bio", profile.getBio());
            data.put("hobbies", profile.getHobbies());
            data.put("wechat", profile.getWechat());
            data.put("photos", profile.getPhotos());
        }
        return Result.success(data);
    }

    /**
     * 保存/更新个人资料。
     */
    @Operation(summary = "保存个人资料")
    @PostMapping("/save")
    public Result<String> saveProfile(@RequestHeader(value = "Authorization", required = false) String auth,
                                      @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        if (body.containsKey("avatarUrl")) profile.setAvatarUrl((String) body.get("avatarUrl"));
        if (body.containsKey("realName")) profile.setRealName((String) body.get("realName"));
        if (body.containsKey("age")) profile.setAge(body.get("age") != null ? Integer.parseInt(body.get("age").toString()) : null);
        if (body.containsKey("gender")) profile.setGender(body.get("gender") != null ? Integer.parseInt(body.get("gender").toString()) : null);
        if (body.containsKey("city")) profile.setCity((String) body.get("city"));
        if (body.containsKey("occupation")) profile.setOccupation((String) body.get("occupation"));
        if (body.containsKey("bio")) profile.setBio((String) body.get("bio"));
        if (body.containsKey("hobbies")) profile.setHobbies((String) body.get("hobbies"));
        if (body.containsKey("wechat")) profile.setWechat((String) body.get("wechat"));
        if (body.containsKey("photos")) profile.setPhotos((String) body.get("photos"));

        // 同步昵称到 user 表
        if (body.containsKey("nickname")) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setNickname((String) body.get("nickname"));
                userMapper.updateById(user);
            }
        }

        if (profile.getId() == null) {
            profileMapper.insert(profile);
        } else {
            profileMapper.updateById(profile);
        }
        return Result.success("保存成功");
    }

    /**
     * 查看他人资料。
     */
    @Operation(summary = "查看用户资料")
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail(404, "用户不存在");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("nickname", user.getNickname());
        if (profile != null) {
            data.put("avatarUrl", profile.getAvatarUrl());
            data.put("age", profile.getAge());
            data.put("gender", profile.getGender());
            data.put("city", profile.getCity());
            data.put("occupation", profile.getOccupation());
            data.put("bio", profile.getBio());
            data.put("hobbies", profile.getHobbies());
            data.put("photos", profile.getPhotos());
            // wechat 不公开，只有组队成功后可见
        }
        return Result.success(data);
    }
}
