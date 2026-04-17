package com.drama.tracker.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信内容安全检查服务，封装 security.msgSecCheck 接口。
 */
@Service
public class WxContentSecurityService {

    private static final Logger log = LoggerFactory.getLogger(WxContentSecurityService.class);
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

    // 内存缓存 access_token（无需 Redis）
    private volatile String cachedToken = null;
    private volatile long tokenExpireTime = 0;

    private String getAccessToken() {
        // 检查内存缓存
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken;
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
                    cachedToken = token;
                    tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
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

    public String checkText(String content, String openid, int scene) {
        if (content == null || content.trim().isEmpty()) return null;

        String accessToken = getAccessToken();
        if (accessToken == null) return null;

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
                    if (errcode == 40001 || errcode == 42001) {
                        cachedToken = null;
                        tokenExpireTime = 0;
                        return retryCheckText(content, openid, scene);
                    }
                    log.error("微信 msgSecCheck 返回错误: {}", respBody);
                    return null;
                }

                JsonNode resultNode = json.get("result");
                if (resultNode != null) {
                    String suggest = resultNode.has("suggest") ? resultNode.get("suggest").asText() : "pass";
                    int label = resultNode.has("label") ? resultNode.get("label").asInt() : 0;
                    if ("risky".equals(suggest)) {
                        log.info("微信内容安全检查不通过 - label:{}", label);
                        return getLabelDesc(label);
                    }
                }
                return null;
            }
        } catch (Exception e) {
            log.error("调用微信 msgSecCheck 异常: {}", e.getMessage());
            return null;
        }
    }

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
                if (json.has("errcode") && json.get("errcode").asInt() != 0) return null;
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
