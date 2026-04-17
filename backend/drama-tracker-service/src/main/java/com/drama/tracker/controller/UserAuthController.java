package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.PhoneUtil;
import com.drama.tracker.dao.entity.User;
import com.drama.tracker.dao.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "用户认证", description = "手机号注册登录")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Operation(summary = "手机号注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        String nickname = body.getOrDefault("nickname", "");

        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("请输入正确的手机号");
        }
        if (password == null || password.length() < 6) {
            return Result.fail("密码至少6位");
        }

        // 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.fail("该手机号已注册");
        }

        User user = new User();
        user.setUsername(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname.isBlank() ? "用户" + phone.substring(7) : nickname);
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        return Result.success(buildTokenResult(user));
    }

    @Operation(summary = "手机号登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String password = body.get("password");

            if (phone == null || password == null) {
                return Result.fail("手机号和密码不能为空");
            }

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, phone);
            User user = userMapper.selectOne(wrapper);

            if (user == null) {
                return Result.fail("该手机号未注册");
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.fail("账号已被禁用");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return Result.fail("密码错误");
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            return Result.success(buildTokenResult(user));
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 重置密码（通过手机号）。
     */
    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String newPassword = body.get("newPassword");

        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("请输入正确的手机号");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码至少6位");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, phone));
        if (user == null) return Result.fail("该手机号未注册");

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("密码重置成功，请用新密码登录");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        try {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Long userId = Long.parseLong(claims.getSubject());
            User user = userMapper.selectById(userId);
            if (user == null) return Result.fail("用户不存在");

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", user.getId());
            info.put("phone", PhoneUtil.mask(user.getUsername()));
            info.put("nickname", user.getNickname());
            return Result.success(info);
        } catch (Exception e) {
            return Result.fail("登录已过期");
        }
    }

    private Map<String, Object> buildTokenResult(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("nickname", user.getNickname())
                .claim("phone", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("id", user.getId());
        result.put("phone", PhoneUtil.mask(user.getUsername()));
        result.put("nickname", user.getNickname());
        return result;
    }
}
