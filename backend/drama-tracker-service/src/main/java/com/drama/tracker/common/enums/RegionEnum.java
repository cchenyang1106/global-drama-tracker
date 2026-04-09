package com.drama.tracker.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地区枚举。
 *
 * @author drama-tracker
 */
@Getter
@AllArgsConstructor
public enum RegionEnum {

    /**
     * 中国
     */
    CHINA("CN", "中国", "中剧"),

    /**
     * 日本
     */
    JAPAN("JP", "日本", "日剧"),

    /**
     * 韩国
     */
    KOREA("KR", "韩国", "韩剧"),

    /**
     * 美国
     */
    USA("US", "美国", "美剧"),

    /**
     * 英国
     */
    UK("UK", "英国", "英剧"),

    /**
     * 其他欧洲国家
     */
    EUROPE("EU", "欧洲", "欧剧"),

    /**
     * 其他
     */
    OTHER("OT", "其他", "其他");

    /**
     * 地区代码
     */
    private final String code;

    /**
     * 地区名称
     */
    private final String name;

    /**
     * 剧集简称
     */
    private final String dramaName;

    /**
     * 根据代码获取枚举。
     *
     * @param code 地区代码
     * @return 地区枚举
     */
    public static RegionEnum getByCode(String code) {
        for (RegionEnum region : values()) {
            if (region.getCode().equals(code)) {
                return region;
            }
        }
        return OTHER;
    }
}
