package com.drama.tracker.controller;

import com.drama.tracker.common.result.Result;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理端认证控制器。
 *
 * @author drama-tracker
 */
@Tag(name = "管理端认证", description = "管理员登录认证接口")
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:drama2026}")
    private String adminPassword;

    /**
     * 管理员登录。
     *
     * @param body 登录信息
     * @return JWT token
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            return Result.fail("用户名或密码错误");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(username)
                .claim("role", "admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("username", username);
        result.put("role", "admin");
        return Result.success(result);
    }

    /**
     * 验证 token 是否有效。
     *
     * @param authHeader Authorization header
     * @return 验证结果
     */
    @Operation(summary = "验证管理员 token")
    @GetMapping("/verify")
    public Result<Map<String, Object>> verify(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        try {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("username", claims.getSubject());
            result.put("role", claims.get("role"));
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("token 无效或已过期");
        }
    }
}
