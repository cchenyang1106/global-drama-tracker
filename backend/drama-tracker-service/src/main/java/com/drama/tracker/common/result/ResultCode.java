package com.drama.tracker.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举。
 *
 * @author drama-tracker
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1001, "用户名或密码错误"),

    /**
     * 用户已存在
     */
    USER_EXISTS(1002, "用户已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1003, "用户不存在"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(1004, "验证码错误"),

    /**
     * Token 过期
     */
    TOKEN_EXPIRED(1005, "Token 已过期，请重新登录"),

    /**
     * Token 无效
     */
    TOKEN_INVALID(1006, "Token 无效"),

    /**
     * 剧集不存在
     */
    DRAMA_NOT_FOUND(2001, "剧集不存在"),

    /**
     * 评论不存在
     */
    COMMENT_NOT_FOUND(2002, "评论不存在"),

    /**
     * 讨论帖不存在
     */
    DISCUSSION_NOT_FOUND(2003, "讨论帖不存在"),

    /**
     * 重复操作
     */
    DUPLICATE_OPERATION(3001, "重复操作");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
