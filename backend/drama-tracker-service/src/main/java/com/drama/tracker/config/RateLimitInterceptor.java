package com.drama.tracker.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * API 限流拦截器（滑动窗口，基于 IP）。
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // IP -> 请求时间戳列表
    private final Map<String, CopyOnWriteArrayList<Long>> requestMap = new ConcurrentHashMap<>();

    // 通用接口：60 秒内最多 60 次请求
    private static final int WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS = 60;

    // 写接口（发布/评论/聊天）：60 秒内最多 15 次
    private static final int WRITE_MAX = 15;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String ip = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 只限制 POST/DELETE 写操作用更严格的限制
        boolean isWrite = "POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
        String key = ip + (isWrite ? ":write" : ":read");
        int limit = isWrite ? WRITE_MAX : MAX_REQUESTS;

        long now = System.currentTimeMillis();
        CopyOnWriteArrayList<Long> timestamps = requestMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());

        // 清理过期时间戳
        timestamps.removeIf(t -> now - t > WINDOW_MS);

        if (timestamps.size() >= limit) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            return false;
        }

        timestamps.add(now);

        // 定期清理不活跃 IP（防内存泄漏）
        if (requestMap.size() > 10000) {
            requestMap.entrySet().removeIf(e -> {
                e.getValue().removeIf(t -> now - t > WINDOW_MS * 5);
                return e.getValue().isEmpty();
            });
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) return ip.split(",")[0].trim();
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) return ip;
        return request.getRemoteAddr();
    }
}
