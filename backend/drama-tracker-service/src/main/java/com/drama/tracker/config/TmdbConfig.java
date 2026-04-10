package com.drama.tracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TMDB API 配置。
 *
 * @author drama-tracker
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tmdb")
public class TmdbConfig {

    /**
     * TMDB API Key
     */
    private String apiKey;

    /**
     * API 基础 URL
     */
    private String baseUrl = "https://api.themoviedb.org/3";

    /**
     * 图片基础 URL
     */
    private String imageBaseUrl = "https://image.tmdb.org/t/p";

    /**
     * 默认语言
     */
    private String language = "zh-CN";

    /**
     * 是否启用自动抓取
     */
    private boolean autoFetchEnabled = false;

    /**
     * 每次抓取页数
     */
    private int fetchPages = 3;

    /**
     * 获取海报完整 URL。
     *
     * @param path 图片路径
     * @return 完整 URL
     */
    public String getPosterUrl(String path) {
        if (path == null || path.isEmpty()) return null;
        return imageBaseUrl + "/w500" + path;
    }

    /**
     * 获取封面完整 URL。
     *
     * @param path 图片路径
     * @return 完整 URL
     */
    public String getBackdropUrl(String path) {
        if (path == null || path.isEmpty()) return null;
        return imageBaseUrl + "/w1280" + path;
    }
}
