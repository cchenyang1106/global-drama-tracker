package com.drama.tracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.config.TmdbConfig;
import com.drama.tracker.dao.entity.Drama;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * TMDB (The Movie Database) 数据抓取服务。
 * 从 TMDB 公开 API 获取全球影视数据并导入到本地数据库。
 *
 * @author drama-tracker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final TmdbConfig tmdbConfig;
    private final DramaService dramaService;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * TMDB 地区到系统地区代码映射。
     */
    private static final Map<String, String> COUNTRY_REGION_MAP = new LinkedHashMap<>();

    static {
        COUNTRY_REGION_MAP.put("CN", "CN"); // 中国
        COUNTRY_REGION_MAP.put("TW", "CN"); // 中国台湾
        COUNTRY_REGION_MAP.put("HK", "CN"); // 中国香港
        COUNTRY_REGION_MAP.put("JP", "JP"); // 日本
        COUNTRY_REGION_MAP.put("KR", "KR"); // 韩国
        COUNTRY_REGION_MAP.put("US", "US"); // 美国
        COUNTRY_REGION_MAP.put("GB", "UK"); // 英国
        COUNTRY_REGION_MAP.put("DE", "EU"); // 德国
        COUNTRY_REGION_MAP.put("FR", "EU"); // 法国
        COUNTRY_REGION_MAP.put("ES", "EU"); // 西班牙
        COUNTRY_REGION_MAP.put("IT", "EU"); // 意大利
    }

    /**
     * TMDB genre ID 到中文名映射。
     */
    private static final Map<Integer, String> TV_GENRE_MAP = new HashMap<>();
    private static final Map<Integer, String> MOVIE_GENRE_MAP = new HashMap<>();

    static {
        // TV genres
        TV_GENRE_MAP.put(10759, "动作冒险");
        TV_GENRE_MAP.put(16, "动画");
        TV_GENRE_MAP.put(35, "喜剧");
        TV_GENRE_MAP.put(80, "犯罪");
        TV_GENRE_MAP.put(99, "纪录片");
        TV_GENRE_MAP.put(18, "剧情");
        TV_GENRE_MAP.put(10751, "家庭");
        TV_GENRE_MAP.put(10762, "儿童");
        TV_GENRE_MAP.put(9648, "悬疑");
        TV_GENRE_MAP.put(10763, "新闻");
        TV_GENRE_MAP.put(10764, "真人秀");
        TV_GENRE_MAP.put(10765, "科幻奇幻");
        TV_GENRE_MAP.put(10766, "肥皂剧");
        TV_GENRE_MAP.put(10767, "脱口秀");
        TV_GENRE_MAP.put(10768, "战争");
        TV_GENRE_MAP.put(37, "西部");

        // Movie genres
        MOVIE_GENRE_MAP.put(28, "动作");
        MOVIE_GENRE_MAP.put(12, "冒险");
        MOVIE_GENRE_MAP.put(16, "动画");
        MOVIE_GENRE_MAP.put(35, "喜剧");
        MOVIE_GENRE_MAP.put(80, "犯罪");
        MOVIE_GENRE_MAP.put(99, "纪录片");
        MOVIE_GENRE_MAP.put(18, "剧情");
        MOVIE_GENRE_MAP.put(10751, "家庭");
        MOVIE_GENRE_MAP.put(14, "奇幻");
        MOVIE_GENRE_MAP.put(36, "历史");
        MOVIE_GENRE_MAP.put(27, "恐怖");
        MOVIE_GENRE_MAP.put(10402, "音乐");
        MOVIE_GENRE_MAP.put(9648, "悬疑");
        MOVIE_GENRE_MAP.put(10749, "爱情");
        MOVIE_GENRE_MAP.put(878, "科幻");
        MOVIE_GENRE_MAP.put(10770, "电视电影");
        MOVIE_GENRE_MAP.put(53, "惊悚");
        MOVIE_GENRE_MAP.put(10752, "战争");
        MOVIE_GENRE_MAP.put(37, "西部");
    }

    /**
     * 定时抓取 TMDB 数据（每天凌晨 3:00 执行）。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledFetch() {
        if (!tmdbConfig.isAutoFetchEnabled() || StringUtils.isBlank(tmdbConfig.getApiKey())) {
            log.info("TMDB auto-fetch is disabled or API key not configured, skipping...");
            return;
        }
        log.info("Starting scheduled TMDB data fetch...");
        fetchAllData();
    }

    /**
     * 手动触发全量数据抓取。
     *
     * @return 抓取结果统计
     */
    public Map<String, Object> fetchAllData() {
        Map<String, Object> result = new LinkedHashMap<>();
        int totalFetched = 0;
        int totalSaved = 0;

        try {
            // 1. 抓取热门 TV 剧集
            int[] tvPopular = fetchTvShows("tv/popular", "热门剧集");
            totalFetched += tvPopular[0];
            totalSaved += tvPopular[1];

            // 2. 抓取高分 TV 剧集
            int[] tvTopRated = fetchTvShows("tv/top_rated", "高分剧集");
            totalFetched += tvTopRated[0];
            totalSaved += tvTopRated[1];

            // 3. 抓取正在播出的 TV 剧集
            int[] tvAiring = fetchTvShows("tv/on_the_air", "正在播出");
            totalFetched += tvAiring[0];
            totalSaved += tvAiring[1];

            // 4. 抓取热门电影
            int[] moviePopular = fetchMovies("movie/popular", "热门电影");
            totalFetched += moviePopular[0];
            totalSaved += moviePopular[1];

            // 5. 抓取高分电影
            int[] movieTopRated = fetchMovies("movie/top_rated", "高分电影");
            totalFetched += movieTopRated[0];
            totalSaved += movieTopRated[1];

            // 6. 抓取正在上映的电影
            int[] movieNowPlaying = fetchMovies("movie/now_playing", "正在上映");
            totalFetched += movieNowPlaying[0];
            totalSaved += movieNowPlaying[1];

            result.put("status", "success");
            result.put("totalFetched", totalFetched);
            result.put("totalSaved", totalSaved);
            result.put("message", String.format("抓取完成: 获取 %d 条, 新增 %d 条", totalFetched, totalSaved));

        } catch (Exception e) {
            log.error("TMDB data fetch failed", e);
            result.put("status", "error");
            result.put("message", "抓取失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 按地区抓取 TMDB 剧集。
     *
     * @param region 地区代码（CN/JP/KR/US 等）
     * @return 抓取结果统计
     */
    public Map<String, Object> fetchByRegion(String region) {
        Map<String, Object> result = new LinkedHashMap<>();
        int totalFetched = 0;
        int totalSaved = 0;

        try {
            // 查找 TMDB 对应的国家代码
            String tmdbCountry = region;
            if ("UK".equals(region)) tmdbCountry = "GB";
            if ("EU".equals(region)) tmdbCountry = "DE"; // 欧洲以德国代替

            // 抓取该地区热门 TV
            int[] tvResult = fetchTvShowsByRegion(tmdbCountry, "热门剧集-" + region);
            totalFetched += tvResult[0];
            totalSaved += tvResult[1];

            // 抓取该地区热门电影
            int[] movieResult = fetchMoviesByRegion(tmdbCountry, "热门电影-" + region);
            totalFetched += movieResult[0];
            totalSaved += movieResult[1];

            result.put("status", "success");
            result.put("region", region);
            result.put("totalFetched", totalFetched);
            result.put("totalSaved", totalSaved);

        } catch (Exception e) {
            log.error("TMDB region fetch failed for {}", region, e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * 抓取 TV 剧集列表。
     */
    private int[] fetchTvShows(String endpoint, String label) {
        int fetched = 0;
        int saved = 0;
        int pages = tmdbConfig.getFetchPages();

        for (int page = 1; page <= pages; page++) {
            try {
                String url = String.format("%s/%s?api_key=%s&language=%s&page=%d",
                        tmdbConfig.getBaseUrl(), endpoint, tmdbConfig.getApiKey(),
                        tmdbConfig.getLanguage(), page);

                JsonNode data = callApi(url);
                if (data == null || !data.has("results")) continue;

                for (JsonNode item : data.get("results")) {
                    fetched++;
                    if (saveTvShow(item)) saved++;
                }

                // 避免频率限制
                Thread.sleep(300);
            } catch (Exception e) {
                log.warn("Failed to fetch {} page {}: {}", label, page, e.getMessage());
            }
        }

        log.info("[{}] 获取 {} 条, 新增 {} 条", label, fetched, saved);
        return new int[]{fetched, saved};
    }

    /**
     * 按地区抓取 TV 剧集。
     */
    private int[] fetchTvShowsByRegion(String country, String label) {
        int fetched = 0;
        int saved = 0;
        int pages = tmdbConfig.getFetchPages();

        for (int page = 1; page <= pages; page++) {
            try {
                String url = String.format(
                        "%s/discover/tv?api_key=%s&language=%s&with_origin_country=%s&sort_by=popularity.desc&page=%d",
                        tmdbConfig.getBaseUrl(), tmdbConfig.getApiKey(),
                        tmdbConfig.getLanguage(), country, page);

                JsonNode data = callApi(url);
                if (data == null || !data.has("results")) continue;

                for (JsonNode item : data.get("results")) {
                    fetched++;
                    if (saveTvShow(item)) saved++;
                }

                Thread.sleep(300);
            } catch (Exception e) {
                log.warn("Failed to fetch {} page {}: {}", label, page, e.getMessage());
            }
        }

        log.info("[{}] 获取 {} 条, 新增 {} 条", label, fetched, saved);
        return new int[]{fetched, saved};
    }

    /**
     * 抓取电影列表。
     */
    private int[] fetchMovies(String endpoint, String label) {
        int fetched = 0;
        int saved = 0;
        int pages = tmdbConfig.getFetchPages();

        for (int page = 1; page <= pages; page++) {
            try {
                String url = String.format("%s/%s?api_key=%s&language=%s&page=%d",
                        tmdbConfig.getBaseUrl(), endpoint, tmdbConfig.getApiKey(),
                        tmdbConfig.getLanguage(), page);

                JsonNode data = callApi(url);
                if (data == null || !data.has("results")) continue;

                for (JsonNode item : data.get("results")) {
                    fetched++;
                    if (saveMovie(item)) saved++;
                }

                Thread.sleep(300);
            } catch (Exception e) {
                log.warn("Failed to fetch {} page {}: {}", label, page, e.getMessage());
            }
        }

        log.info("[{}] 获取 {} 条, 新增 {} 条", label, fetched, saved);
        return new int[]{fetched, saved};
    }

    /**
     * 按地区抓取电影。
     */
    private int[] fetchMoviesByRegion(String country, String label) {
        int fetched = 0;
        int saved = 0;
        int pages = tmdbConfig.getFetchPages();

        for (int page = 1; page <= pages; page++) {
            try {
                String url = String.format(
                        "%s/discover/movie?api_key=%s&language=%s&with_origin_country=%s&sort_by=popularity.desc&page=%d",
                        tmdbConfig.getBaseUrl(), tmdbConfig.getApiKey(),
                        tmdbConfig.getLanguage(), country, page);

                JsonNode data = callApi(url);
                if (data == null || !data.has("results")) continue;

                for (JsonNode item : data.get("results")) {
                    fetched++;
                    if (saveMovie(item)) saved++;
                }

                Thread.sleep(300);
            } catch (Exception e) {
                log.warn("Failed to fetch {} page {}: {}", label, page, e.getMessage());
            }
        }

        log.info("[{}] 获取 {} 条, 新增 {} 条", label, fetched, saved);
        return new int[]{fetched, saved};
    }

    /**
     * 保存 TV 剧集数据。
     *
     * @return 是否为新增数据
     */
    private boolean saveTvShow(JsonNode item) {
        try {
            int tmdbId = item.get("id").asInt();
            String title = getTextNode(item, "name");
            String originalTitle = getTextNode(item, "original_name");

            if (StringUtils.isBlank(title) && StringUtils.isBlank(originalTitle)) return false;

            // 使用 title 去重（避免重复导入）
            String checkTitle = StringUtils.isNotBlank(title) ? title : originalTitle;
            LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drama::getTitle, checkTitle);
            if (dramaService.count(wrapper) > 0) return false;

            // 获取详情（包含演员、集数等信息）
            Drama drama = new Drama();
            drama.setTitle(StringUtils.isNotBlank(title) ? title : originalTitle);
            drama.setOriginalTitle(originalTitle);

            // 地区判定
            String region = determineRegion(item, "origin_country");
            drama.setRegion(region);

            // 类型
            drama.setType(determineTvType(item));

            // 分类标签
            drama.setGenres(extractGenres(item, TV_GENRE_MAP));

            // 评分
            double voteAvg = item.has("vote_average") ? item.get("vote_average").asDouble() : 0;
            if (voteAvg > 0) {
                BigDecimal rating = BigDecimal.valueOf(voteAvg).setScale(1, RoundingMode.HALF_UP);
                drama.setUserRating(rating);
                drama.setTotalRating(rating);
            }

            // 评分人数
            drama.setRatingCount(item.has("vote_count") ? item.get("vote_count").asInt() : 0);

            // 热度
            double popularity = item.has("popularity") ? item.get("popularity").asDouble() : 0;
            drama.setHotScore((long) (popularity * 10));

            // 首播日期
            String firstAirDate = getTextNode(item, "first_air_date");
            if (StringUtils.isNotBlank(firstAirDate)) {
                try {
                    drama.setReleaseDate(LocalDate.parse(firstAirDate));
                } catch (DateTimeParseException ignored) {
                }
            }

            // 简介
            drama.setDescription(getTextNode(item, "overview"));

            // 图片
            drama.setPosterUrl(tmdbConfig.getPosterUrl(getTextNode(item, "poster_path")));
            drama.setCoverUrl(tmdbConfig.getBackdropUrl(getTextNode(item, "backdrop_path")));

            // 状态 - 默认连载中
            drama.setStatus(1);

            // 尝试获取详情信息（演员、集数等）
            fetchAndFillTvDetails(tmdbId, drama);

            dramaService.save(drama);
            return true;

        } catch (Exception e) {
            log.debug("Failed to save TV show: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 保存电影数据。
     *
     * @return 是否为新增数据
     */
    private boolean saveMovie(JsonNode item) {
        try {
            int tmdbId = item.get("id").asInt();
            String title = getTextNode(item, "title");
            String originalTitle = getTextNode(item, "original_title");

            if (StringUtils.isBlank(title) && StringUtils.isBlank(originalTitle)) return false;

            // 去重
            String checkTitle = StringUtils.isNotBlank(title) ? title : originalTitle;
            LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drama::getTitle, checkTitle);
            if (dramaService.count(wrapper) > 0) return false;

            Drama drama = new Drama();
            drama.setTitle(StringUtils.isNotBlank(title) ? title : originalTitle);
            drama.setOriginalTitle(originalTitle);

            // 地区
            String region = determineMovieRegion(item);
            drama.setRegion(region);

            // 类型: 电影
            drama.setType(2);

            // 分类标签
            drama.setGenres(extractGenres(item, MOVIE_GENRE_MAP));

            // 评分
            double voteAvg = item.has("vote_average") ? item.get("vote_average").asDouble() : 0;
            if (voteAvg > 0) {
                BigDecimal rating = BigDecimal.valueOf(voteAvg).setScale(1, RoundingMode.HALF_UP);
                drama.setUserRating(rating);
                drama.setTotalRating(rating);
            }

            drama.setRatingCount(item.has("vote_count") ? item.get("vote_count").asInt() : 0);

            // 热度
            double popularity = item.has("popularity") ? item.get("popularity").asDouble() : 0;
            drama.setHotScore((long) (popularity * 10));

            // 上映日期
            String releaseDate = getTextNode(item, "release_date");
            if (StringUtils.isNotBlank(releaseDate)) {
                try {
                    drama.setReleaseDate(LocalDate.parse(releaseDate));
                } catch (DateTimeParseException ignored) {
                }
            }

            // 简介
            drama.setDescription(getTextNode(item, "overview"));

            // 图片
            drama.setPosterUrl(tmdbConfig.getPosterUrl(getTextNode(item, "poster_path")));
            drama.setCoverUrl(tmdbConfig.getBackdropUrl(getTextNode(item, "backdrop_path")));

            // 状态: 已完结 (电影)
            drama.setStatus(2);
            drama.setTotalEpisodes(1);
            drama.setAiredEpisodes(1);

            // 获取详情
            fetchAndFillMovieDetails(tmdbId, drama);

            dramaService.save(drama);
            return true;

        } catch (Exception e) {
            log.debug("Failed to save movie: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取并填充 TV 剧集详情。
     */
    private void fetchAndFillTvDetails(int tmdbId, Drama drama) {
        try {
            String url = String.format("%s/tv/%d?api_key=%s&language=%s&append_to_response=credits",
                    tmdbConfig.getBaseUrl(), tmdbId, tmdbConfig.getApiKey(), tmdbConfig.getLanguage());

            JsonNode detail = callApi(url);
            if (detail == null) return;

            // 集数信息
            if (detail.has("number_of_episodes")) {
                drama.setTotalEpisodes(detail.get("number_of_episodes").asInt());
            }

            // 单集时长
            if (detail.has("episode_run_time") && detail.get("episode_run_time").isArray()
                    && detail.get("episode_run_time").size() > 0) {
                drama.setEpisodeDuration(detail.get("episode_run_time").get(0).asInt());
            }

            // 状态映射
            String tmdbStatus = getTextNode(detail, "status");
            if ("Ended".equals(tmdbStatus) || "Canceled".equals(tmdbStatus)) {
                drama.setStatus(2); // 已完结
                drama.setAiredEpisodes(drama.getTotalEpisodes());
            } else if ("Returning Series".equals(tmdbStatus)) {
                drama.setStatus(1); // 连载中
            } else if ("Planned".equals(tmdbStatus) || "In Production".equals(tmdbStatus)) {
                drama.setStatus(0); // 未播出
            }

            // 播放平台
            if (detail.has("networks") && detail.get("networks").isArray()) {
                List<String> networks = new ArrayList<>();
                for (JsonNode n : detail.get("networks")) {
                    String name = getTextNode(n, "name");
                    if (StringUtils.isNotBlank(name)) networks.add(name);
                }
                if (!networks.isEmpty()) {
                    drama.setPlatforms(String.join(",", networks));
                }
            }

            // 演职员信息
            if (detail.has("credits")) {
                JsonNode credits = detail.get("credits");
                fillCredits(credits, drama);
            }

            // IMDB ID（通过 external_ids）
            fetchExternalIds("tv", tmdbId, drama);

            Thread.sleep(200);
        } catch (Exception e) {
            log.debug("Failed to fetch TV details for {}: {}", tmdbId, e.getMessage());
        }
    }

    /**
     * 获取并填充电影详情。
     */
    private void fetchAndFillMovieDetails(int tmdbId, Drama drama) {
        try {
            String url = String.format("%s/movie/%d?api_key=%s&language=%s&append_to_response=credits",
                    tmdbConfig.getBaseUrl(), tmdbId, tmdbConfig.getApiKey(), tmdbConfig.getLanguage());

            JsonNode detail = callApi(url);
            if (detail == null) return;

            // 时长
            if (detail.has("runtime") && detail.get("runtime").asInt() > 0) {
                drama.setEpisodeDuration(detail.get("runtime").asInt());
            }

            // IMDB ID
            String imdbId = getTextNode(detail, "imdb_id");
            if (StringUtils.isNotBlank(imdbId)) {
                drama.setImdbId(imdbId);
            }

            // 演职员信息
            if (detail.has("credits")) {
                JsonNode credits = detail.get("credits");
                fillCredits(credits, drama);
            }

            Thread.sleep(200);
        } catch (Exception e) {
            log.debug("Failed to fetch movie details for {}: {}", tmdbId, e.getMessage());
        }
    }

    /**
     * 获取外部 ID（IMDB 等）。
     */
    private void fetchExternalIds(String mediaType, int tmdbId, Drama drama) {
        try {
            String url = String.format("%s/%s/%d/external_ids?api_key=%s",
                    tmdbConfig.getBaseUrl(), mediaType, tmdbId, tmdbConfig.getApiKey());
            JsonNode data = callApi(url);
            if (data == null) return;

            String imdbId = getTextNode(data, "imdb_id");
            if (StringUtils.isNotBlank(imdbId)) {
                drama.setImdbId(imdbId);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch external IDs: {}", e.getMessage());
        }
    }

    /**
     * 填充演职员信息。
     */
    private void fillCredits(JsonNode credits, Drama drama) {
        // 导演
        if (credits.has("crew") && credits.get("crew").isArray()) {
            List<String> directors = new ArrayList<>();
            List<String> writers = new ArrayList<>();
            for (JsonNode crew : credits.get("crew")) {
                String job = getTextNode(crew, "job");
                String name = getTextNode(crew, "name");
                if (StringUtils.isBlank(name)) continue;
                if ("Director".equals(job)) directors.add(name);
                if ("Writer".equals(job) || "Screenplay".equals(job)) writers.add(name);
            }
            if (!directors.isEmpty()) drama.setDirectors(String.join(",", directors));
            if (!writers.isEmpty()) drama.setWriters(String.join(",", writers));
        }

        // 演员 (取前10个)
        if (credits.has("cast") && credits.get("cast").isArray()) {
            List<String> actors = new ArrayList<>();
            int count = 0;
            for (JsonNode cast : credits.get("cast")) {
                if (count >= 10) break;
                String name = getTextNode(cast, "name");
                if (StringUtils.isNotBlank(name)) {
                    actors.add(name);
                    count++;
                }
            }
            if (!actors.isEmpty()) drama.setActors(String.join(",", actors));
        }
    }

    /**
     * 判断 TV 剧集地区。
     */
    private String determineRegion(JsonNode item, String field) {
        if (item.has(field) && item.get(field).isArray() && item.get(field).size() > 0) {
            String country = item.get(field).get(0).asText();
            return COUNTRY_REGION_MAP.getOrDefault(country, "OT");
        }
        // 通过 original_language 推断
        String lang = getTextNode(item, "original_language");
        return switch (lang) {
            case "zh" -> "CN";
            case "ja" -> "JP";
            case "ko" -> "KR";
            case "en" -> "US";
            default -> "OT";
        };
    }

    /**
     * 判断电影地区。
     */
    private String determineMovieRegion(JsonNode item) {
        // 电影没有 origin_country 字段，用 original_language 判断
        String lang = getTextNode(item, "original_language");
        return switch (lang) {
            case "zh" -> "CN";
            case "ja" -> "JP";
            case "ko" -> "KR";
            case "en" -> "US";
            case "de", "fr", "es", "it" -> "EU";
            default -> "OT";
        };
    }

    /**
     * 判断 TV 剧集具体类型（电视剧/网剧/动漫/综艺/纪录片）。
     */
    private int determineTvType(JsonNode item) {
        if (!item.has("genre_ids")) return 1;
        for (JsonNode gid : item.get("genre_ids")) {
            int id = gid.asInt();
            if (id == 16) return 5;   // 动画 -> 动漫
            if (id == 99) return 6;   // 纪录片
            if (id == 10764 || id == 10767) return 4; // 真人秀/脱口秀 -> 综艺
        }
        return 1; // 默认电视剧
    }

    /**
     * 提取分类标签。
     */
    private String extractGenres(JsonNode item, Map<Integer, String> genreMap) {
        if (!item.has("genre_ids")) return null;
        List<String> genres = new ArrayList<>();
        for (JsonNode gid : item.get("genre_ids")) {
            String name = genreMap.get(gid.asInt());
            if (name != null) genres.add(name);
        }
        return genres.isEmpty() ? null : String.join(",", genres);
    }

    /**
     * 调用 TMDB API。
     */
    private JsonNode callApi(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return objectMapper.readTree(response.body().string());
                }
            }
        } catch (Exception e) {
            log.debug("API call failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 安全获取 JSON 文本节点。
     */
    private String getTextNode(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }
}
