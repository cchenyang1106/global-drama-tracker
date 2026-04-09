package com.drama.tracker.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 剧集状态枚举。
 *
 * @author drama-tracker
 */
@Getter
@AllArgsConstructor
public enum DramaStatusEnum {

    /**
     * 未播出
     */
    NOT_AIRED(0, "未播出"),

    /**
     * 连载中
     */
    ONGOING(1, "连载中"),

    /**
     * 已完结
     */
    FINISHED(2, "已完结"),

    /**
     * 已停播
     */
    CANCELLED(3, "已停播");

    /**
     * 状态代码
     */
    private final Integer code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据代码获取枚举。
     *
     * @param code 状态代码
     * @return 状态枚举
     */
    public static DramaStatusEnum getByCode(Integer code) {
        for (DramaStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
