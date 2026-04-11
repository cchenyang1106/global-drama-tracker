package com.drama.tracker.common.util;

import java.util.*;

/**
 * 敏感词过滤工具（DFA 算法）。
 */
public class SensitiveWordFilter {

    private static final Map<Character, Object> WORD_MAP = new HashMap<>();
    private static final String END_FLAG = "isEnd";

    static {
        // 基础敏感词库（可扩展）
        String[] words = {
            // 脏话/侮辱
            "傻逼", "操你", "你妈", "去死", "废物", "智障", "脑残", "白痴", "贱人", "滚蛋",
            "他妈的", "草泥马", "尼玛", "卧槽", "日你", "妈的", "狗日", "混蛋", "王八蛋",
            // 色情
            "约炮", "一夜情", "援交", "卖淫", "嫖娼", "色情", "裸聊", "性服务",
            // 诈骗/违法
            "赌博", "代孕", "贩毒", "洗钱", "传销", "刷单", "兼职赚钱",
            // 骚扰
            "加微信转账", "裸照", "私密照",
            // 广告
            "免费领取", "点击链接", "扫码领红包", "日赚千元", "轻松月入"
        };
        for (String word : words) {
            addWord(word);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addWord(String word) {
        Map<Character, Object> current = WORD_MAP;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object next = current.get(c);
            if (next == null) {
                Map<Character, Object> newMap = new HashMap<>();
                current.put(c, newMap);
                current = newMap;
            } else {
                current = (Map<Character, Object>) next;
            }
        }
        current.put(END_FLAG.charAt(0), null); // 结束标记
    }

    /**
     * 检测文本是否包含敏感词。
     * @return 找到的第一个敏感词，无则返回 null
     */
    @SuppressWarnings("unchecked")
    public static String detect(String text) {
        if (text == null || text.isEmpty()) return null;
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> current = WORD_MAP;
            int j = i;
            while (j < text.length()) {
                Object next = current.get(text.charAt(j));
                if (next == null) break;
                if (next instanceof Map) {
                    Map<Character, Object> nextMap = (Map<Character, Object>) next;
                    if (nextMap.containsKey(END_FLAG.charAt(0))) {
                        return text.substring(i, j + 1);
                    }
                    current = nextMap;
                }
                j++;
            }
            // 单字匹配
            if (current.containsKey(END_FLAG.charAt(0)) && j > i) {
                return text.substring(i, j);
            }
        }
        return null;
    }

    /**
     * 替换敏感词为 ***。
     */
    @SuppressWarnings("unchecked")
    public static String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < sb.length(); i++) {
            Map<Character, Object> current = WORD_MAP;
            int matchEnd = -1;
            int j = i;
            while (j < sb.length()) {
                Object next = current.get(sb.charAt(j));
                if (next == null) break;
                if (next instanceof Map) {
                    Map<Character, Object> nextMap = (Map<Character, Object>) next;
                    if (nextMap.containsKey(END_FLAG.charAt(0))) {
                        matchEnd = j;
                    }
                    current = nextMap;
                }
                j++;
            }
            if (matchEnd >= i) {
                for (int k = i; k <= matchEnd; k++) {
                    sb.setCharAt(k, '*');
                }
                i = matchEnd;
            }
        }
        return sb.toString();
    }
}
