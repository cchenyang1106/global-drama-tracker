package com.drama.tracker.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 剧集类型枚举。
 *
 * @author drama-tracker
 */
@Getter
@AllArgsConstructor
public enum DramaTypeEnum {

    /**
     * 电视剧
     */
    TV_SERIES(1, "电视剧"),

    /**
     * 电影
     */
    MOVIE(2, "电影"),

    /**
     * 网剧
     */
    WEB_SERIES(3, "网剧"),

    /**
     * 综艺
     */
    VARIETY(4, "综艺"),

    /**
     * 动漫
     */
    ANIME(5, "动漫"),

    /**
     * 纪录片
     */
    DOCUMENTARY(6, "纪录片");

    /**
     * 类型代码
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举。
     *
     * @param code 类型代码
     * @return 类型枚举
     */
    public static DramaTypeEnum getByCode(Integer code) {
        for (DramaTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
