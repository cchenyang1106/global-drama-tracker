package com.drama.tracker.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.*;

/**
 * 敏感词过滤工具（DFA + 拼音变体检测）。
 */
public class SensitiveWordFilter {

    private static final Map<Character, Object> WORD_MAP = new HashMap<>();
    private static final Set<String> PINYIN_WORDS = new HashSet<>();
    private static final char END = '\u0000';

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();
    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    // 敏感词库
    private static final String[] RAW_WORDS = {
        "傻逼", "操你", "你妈", "去死", "废物", "智障", "脑残", "白痴", "贱人", "滚蛋",
        "他妈的", "草泥马", "尼玛", "卧槽", "日你", "妈的", "狗日", "混蛋", "王八蛋",
        "约炮", "一夜情", "援交", "卖淫", "嫖娼", "色情", "裸聊", "性服务",
        "赌博", "代孕", "贩毒", "洗钱", "传销", "刷单", "兼职赚钱",
        "加微信转账", "裸照", "私密照",
        "免费领取", "点击链接", "扫码领红包", "日赚千元", "轻松月入"
    };

    static {
        for (String word : RAW_WORDS) {
            addWord(word);
            // 生成拼音形式加入集合
            String pinyin = toPinyin(word);
            if (pinyin != null && !pinyin.equals(word)) {
                PINYIN_WORDS.add(pinyin);
            }
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
        current.put(END, null);
    }

    /**
     * 将中文转拼音（无声调、纯小写）。
     */
    private static String toPinyin(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            try {
                String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (arr != null && arr.length > 0) {
                    sb.append(arr[0]);
                } else {
                    sb.append(c);
                }
            } catch (Exception e) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 检测文本是否包含敏感词（原文 + 拼音）。
     */
    public static String detect(String text) {
        if (text == null || text.isEmpty()) return null;

        // 1. 原文 DFA 检测
        String found = dfaDetect(text);
        if (found != null) return found;

        // 2. 将输入转拼音后与敏感词拼音集合比对
        String inputPinyin = toPinyin(text.toLowerCase());
        if (inputPinyin != null) {
            for (String pinyinWord : PINYIN_WORDS) {
                if (inputPinyin.contains(pinyinWord)) {
                    return "[拼音变体]";
                }
            }
        }

        // 3. 去掉空格、特殊符号后再检测（防止 "傻 逼"、"傻*逼" 绕过）
        String cleaned = text.replaceAll("[\\s\\p{Punct}*#@!！。，、·~`\\-_=+|\\\\/<>《》（）()\\[\\]{}]+", "");
        if (!cleaned.equals(text)) {
            found = dfaDetect(cleaned);
            if (found != null) return found;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static String dfaDetect(String text) {
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> current = WORD_MAP;
            int j = i;
            int matchEnd = -1;
            while (j < text.length()) {
                Object next = current.get(text.charAt(j));
                if (next == null) break;
                if (next instanceof Map) {
                    Map<Character, Object> nextMap = (Map<Character, Object>) next;
                    if (nextMap.containsKey(END)) {
                        matchEnd = j;
                    }
                    current = nextMap;
                }
                j++;
            }
            if (matchEnd >= i) {
                return text.substring(i, matchEnd + 1);
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

        // 先检测拼音变体，如果命中直接返回替换
        String inputPinyin = toPinyin(text.toLowerCase());
        if (inputPinyin != null) {
            for (String pinyinWord : PINYIN_WORDS) {
                if (inputPinyin.contains(pinyinWord)) {
                    return "***";
                }
            }
        }

        // 原文 DFA 替换
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
                    if (nextMap.containsKey(END)) {
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
