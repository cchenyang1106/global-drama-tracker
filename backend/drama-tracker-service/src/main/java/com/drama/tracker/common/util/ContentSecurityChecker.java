package com.drama.tracker.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 统一内容安全检查器：本地敏感词过滤 + 微信 msgSecCheck 双重检测。
 * <p>
 * 检测策略：
 * 1. 先执行本地 DFA 敏感词检测（快速、无网络开销）
 * 2. 本地通过后，再调用微信 security.msgSecCheck 接口（更全面）
 * 3. 微信接口异常时自动降级，不阻断用户操作
 */
@Component
public class ContentSecurityChecker {

    private static final Logger log = LoggerFactory.getLogger(ContentSecurityChecker.class);

    /** 场景值常量 */
    public static final int SCENE_PROFILE = 1;  // 资料
    public static final int SCENE_COMMENT = 2;  // 评论
    public static final int SCENE_FORUM = 3;    // 论坛（活动发布、答题）
    public static final int SCENE_SOCIAL = 4;   // 社交日志

    private final WxContentSecurityService wxSecurityService;

    public ContentSecurityChecker(WxContentSecurityService wxSecurityService) {
        this.wxSecurityService = wxSecurityService;
    }

    /**
     * 检查文本内容是否违规（本地 + 微信双重检测）。
     *
     * @param text   待检查文本
     * @param scene  场景值（1-资料，2-评论，3-论坛，4-社交日志）
     * @return null 表示通过，非 null 为违规描述
     */
    public String check(String text, int scene) {
        return check(text, "", scene);
    }

    /**
     * 检查文本内容是否违规（本地 + 微信双重检测）。
     *
     * @param text   待检查文本
     * @param openid 用户 openid（可选）
     * @param scene  场景值
     * @return null 表示通过，非 null 为违规描述
     */
    public String check(String text, String openid, int scene) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 第一步：本地敏感词检测（快速）
        String localResult = SensitiveWordFilter.detect(text);
        if (localResult != null) {
            log.info("本地敏感词检测命中: {}", localResult);
            return "内容包含违规词汇「" + localResult + "」，请修改后重试";
        }

        // 第二步：微信 msgSecCheck 检测（更全面）
        try {
            String wxResult = wxSecurityService.checkText(text, openid, scene);
            if (wxResult != null) {
                log.info("微信内容安全检查不通过: {}", wxResult);
                return "内容未通过安全审核（" + wxResult + "），请修改后重试";
            }
        } catch (Exception e) {
            // 微信接口异常时降级，仅依赖本地检测结果
            log.warn("微信内容安全检查异常，已降级处理: {}", e.getMessage());
        }

        return null; // 通过
    }

    /**
     * 过滤文本中的敏感词（替换为***），仅用本地过滤。
     * 聊天消息等场景使用。
     */
    public String filter(String text) {
        return SensitiveWordFilter.filter(text);
    }
}
