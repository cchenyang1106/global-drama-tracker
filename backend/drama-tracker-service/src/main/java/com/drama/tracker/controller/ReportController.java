package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.dao.entity.UserReport;
import com.drama.tracker.dao.mapper.UserReportMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 用户举报 Controller。
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private UserReportMapper reportMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    /**
     * 提交举报。
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestHeader(value = "Authorization", required = false) String auth,
                            @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String reason = (String) body.get("reason");
        String detail = (String) body.get("detail");

        if (targetType == null || targetId == null || reason == null) {
            return Result.fail("参数不完整");
        }

        // 防重复举报
        Long existing = reportMapper.selectCount(new LambdaQueryWrapper<UserReport>()
                .eq(UserReport::getReporterId, userId)
                .eq(UserReport::getTargetType, targetType)
                .eq(UserReport::getTargetId, targetId)
                .eq(UserReport::getStatus, 0));
        if (existing > 0) return Result.fail("你已经举报过了，请等待处理");

        UserReport report = new UserReport();
        report.setReporterId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDetail(detail);
        report.setStatus(0);
        reportMapper.insert(report);

        return Result.success("举报已提交，我们会尽快处理");
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
