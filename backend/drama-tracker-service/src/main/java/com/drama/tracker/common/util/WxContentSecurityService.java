package com.drama.tracker.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信内容安全检查服务，封装 security.msgSecCheck 接口。
 * 文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/sec-center/sec-check/msgSecCheck.html
 */
@Service
public class WxContentSecurityService {

    private static final Logger log = LoggerFactory.getLogger(WxContentSecurityService.class);
    private static final String ACCESS_TOKEN_KEY = "wx:access_token";
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String MSG_SEC_CHECK_URL = "https://api.weixin.qq.com/wxa/msg_sec_check";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    private final StringRedisTemplate redisTemplate;

    public WxContentSecurityService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取微信 access_token（带 Redis 缓存）。
     */
    private String getAccessToken() {
        // 先从缓存取
        try {
            String cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis 获取 access_token 失败，直接请求微信接口: {}", e.getMessage());
        }

        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            log.warn("微信 appid/secret 未配置，跳过微信内容安全检查");
            return null;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                JsonNode json = MAPPER.readTree(body);
                if (json.has("access_token")) {
                    String token = json.get("access_token").asText();
                    int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 7200;
                    // 缓存，提前 5 分钟过期
                    try {
                        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token,
                                Math.max(expiresIn - 300, 60), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.warn("Redis 缓存 access_token 失败: {}", e.getMessage());
                    }
                    return token;
                } else {
                    log.error("获取微信 access_token 失败: {}", body);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("请求微信 access_token 异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 调用微信 security.msgSecCheck 接口进行文本内容安全检查。
     *
     * @param content 待检查文本
     * @param openid  用户的 openid（可传空字符串，但建议传真实值以提高准确度）
     * @param scene   场景值：1-资料，2-评论，3-论坛，4-社交日志
     * @return 检测结果：null 表示通过或接口不可用（降级到本地检查），非 null 为违规原因
     */
    public String checkText(String content, String openid, int scene) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String accessToken = getAccessToken();
        if (accessToken == null) {
            log.info("access_token 不可用，跳过微信内容安全检查");
            return null;
        }

        try {
            String url = MSG_SEC_CHECK_URL + "?access_token=" + accessToken;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", content);
            requestBody.put("version", 2);
            requestBody.put("scene", scene);
            requestBody.put("openid", openid != null ? openid : "");

            String jsonBody = MAPPER.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                JsonNode json = MAPPER.readTree(respBody);

                int errcode = json.has("errcode") ? json.get("errcode").asInt() : -1;
                if (errcode != 0) {
                    // access_token 过期，清缓存重试一次
                    if (errcode == 40001 || errcode == 42001) {
                        try {
                            redisTemplate.delete(ACCESS_TOKEN_KEY);
                        } catch (Exception ignored) {}
                        return retryCheckText(content, openid, scene);
                    }
                    log.error("微信 msgSecCheck 返回错误: {}", respBody);
                    return null; // 接口异常时降级，不阻断用户
                }

                // 解析 result
                JsonNode resultNode = json.get("result");
                if (resultNode != null) {
                    String suggest = resultNode.has("suggest") ? resultNode.get("suggest").asText() : "pass";
                    int label = resultNode.has("label") ? resultNode.get("label").asInt() : 0;

                    if ("risky".equals(suggest)) {
                        String labelDesc = getLabelDesc(label);
                        log.info("微信内容安全检查不通过 - suggest:{}, label:{}, content:{}",
                                suggest, label, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                        return labelDesc;
                    }
                    // review 建议人工审核，这里也放行
                    if ("review".equals(suggest)) {
                        log.info("微信内容安全检查建议人工审核 - label:{}, content:{}",
                                label, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                        return null;
                    }
                }
                return null; // pass
            }
        } catch (Exception e) {
            log.error("调用微信 msgSecCheck 异常: {}", e.getMessage());
            return null; // 异常时降级
        }
    }

    /**
     * token 过期后重试一次。
     */
    private String retryCheckText(String content, String openid, int scene) {
        String newToken = getAccessToken();
        if (newToken == null) return null;

        try {
            String url = MSG_SEC_CHECK_URL + "?access_token=" + newToken;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", content);
            requestBody.put("version", 2);
            requestBody.put("scene", scene);
            requestBody.put("openid", openid != null ? openid : "");

            String jsonBody = MAPPER.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                JsonNode json = MAPPER.readTree(respBody);
                int errcode = json.has("errcode") ? json.get("errcode").asInt() : -1;
                if (errcode != 0) {
                    log.error("微信 msgSecCheck 重试仍失败: {}", respBody);
                    return null;
                }
                JsonNode resultNode = json.get("result");
                if (resultNode != null && "risky".equals(resultNode.path("suggest").asText())) {
                    return getLabelDesc(resultNode.path("label").asInt());
                }
                return null;
            }
        } catch (Exception e) {
            log.error("微信 msgSecCheck 重试异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将微信返回的 label 转为中文描述。
     */
    private String getLabelDesc(int label) {
        switch (label) {
            case 10001: return "广告内容";
            case 20001: return "时政内容";
            case 20002: return "色情内容";
            case 20003: return "辱骂内容";
            case 20006: return "违法犯罪内容";
            case 20008: return "欺诈内容";
            case 20012: return "低俗内容";
            case 20013: return "版权内容";
            case 21000: return "其他违规内容";
            default: return "违规内容";
        }
    }
}
