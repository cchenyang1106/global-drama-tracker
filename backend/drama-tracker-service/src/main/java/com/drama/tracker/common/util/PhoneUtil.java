package com.drama.tracker.common.util;

/**
 * 手机号脱敏工具类。
 */
public class PhoneUtil {

    /**
     * 手机号脱敏：138****1234
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
