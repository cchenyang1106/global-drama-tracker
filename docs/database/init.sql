-- =============================================
-- Global Drama Tracker 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS drama_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE drama_tracker;

-- =============================================
-- 1. 剧集/电影表
-- =============================================
CREATE TABLE IF NOT EXISTS `drama` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) NOT NULL COMMENT '剧集名称',
    `original_title` VARCHAR(255) DEFAULT NULL COMMENT '原名（外语原名）',
    `aliases` VARCHAR(500) DEFAULT NULL COMMENT '别名（多个用逗号分隔）',
    `region` VARCHAR(10) NOT NULL COMMENT '地区代码 (CN/JP/KR/US/UK/EU/OT)',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 (1:电视剧 2:电影 3:网剧 4:综艺 5:动漫 6:纪录片)',
    `genres` VARCHAR(200) DEFAULT NULL COMMENT '分类标签（多个用逗号分隔）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 (0:未播出 1:连载中 2:已完结 3:已停播)',
    `release_date` DATE DEFAULT NULL COMMENT '首播日期',
    `end_date` DATE DEFAULT NULL COMMENT '完结日期',
    `total_episodes` INT DEFAULT NULL COMMENT '总集数',
    `aired_episodes` INT DEFAULT 0 COMMENT '已播集数',
    `episode_duration` INT DEFAULT NULL COMMENT '单集时长（分钟）',
    `directors` VARCHAR(500) DEFAULT NULL COMMENT '导演',
    `writers` VARCHAR(500) DEFAULT NULL COMMENT '编剧',
    `actors` TEXT DEFAULT NULL COMMENT '主演',
    `description` TEXT DEFAULT NULL COMMENT '简介',
    `poster_url` VARCHAR(500) DEFAULT NULL COMMENT '海报图片URL',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '横版封面URL',
    `user_rating` DECIMAL(3,1) DEFAULT 0.0 COMMENT '用户平均评分',
    `rating_count` INT DEFAULT 0 COMMENT '评分人数',
    `ai_rating` DECIMAL(3,1) DEFAULT NULL COMMENT 'AI情感分析评分',
    `total_rating` DECIMAL(3,1) DEFAULT 0.0 COMMENT '综合评分',
    `hot_score` BIGINT DEFAULT 0 COMMENT '热度值',
    `platforms` VARCHAR(200) DEFAULT NULL COMMENT '播放平台',
    `official_url` VARCHAR(500) DEFAULT NULL COMMENT '官方网站',
    `douban_id` VARCHAR(20) DEFAULT NULL COMMENT '豆瓣ID',
    `imdb_id` VARCHAR(20) DEFAULT NULL COMMENT 'IMDB ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_region` (`region`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_release_date` (`release_date`),
    INDEX `idx_total_rating` (`total_rating`),
    INDEX `idx_hot_score` (`hot_score`),
    INDEX `idx_douban_id` (`douban_id`),
    INDEX `idx_imdb_id` (`imdb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧集/电影表';

-- =============================================
-- 2. 剧集分集信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `episode` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drama_id` BIGINT NOT NULL COMMENT '剧集ID',
    `season` INT DEFAULT 1 COMMENT '季数',
    `episode_number` INT NOT NULL COMMENT '集数',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '分集标题',
    `description` TEXT DEFAULT NULL COMMENT '分集简介',
    `air_date` DATE DEFAULT NULL COMMENT '播出日期',
    `duration` INT DEFAULT NULL COMMENT '时长（分钟）',
    `aired` TINYINT(1) DEFAULT 0 COMMENT '是否已播出',
    `still_url` VARCHAR(500) DEFAULT NULL COMMENT '剧照URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_air_date` (`air_date`),
    UNIQUE INDEX `uk_drama_season_episode` (`drama_id`, `season`, `episode_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧集分集信息表';

-- =============================================
-- 3. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 (0:未知 1:男 2:女)',
    `role` TINYINT DEFAULT 0 COMMENT '角色 (0:普通用户 1:VIP 9:管理员)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用 1:正常)',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    UNIQUE INDEX `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 4. 评论表
-- =============================================
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drama_id` BIGINT NOT NULL COMMENT '剧集ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `rating` DECIMAL(3,1) DEFAULT NULL COMMENT '用户评分 (1-10)',
    `sentiment_score` DECIMAL(4,3) DEFAULT NULL COMMENT 'AI情感分析得分 (-1到1)',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT DEFAULT 0 COMMENT '回复数',
    `spoiler` TINYINT(1) DEFAULT 0 COMMENT '是否包含剧透',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:待审核 1:已通过 2:已拒绝 3:已删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 5. 评分表
-- =============================================
CREATE TABLE IF NOT EXISTS `rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drama_id` BIGINT NOT NULL COMMENT '剧集ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `score` DECIMAL(3,1) NOT NULL COMMENT '评分 (1-10)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_drama_user` (`drama_id`, `user_id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

-- =============================================
-- 6. 讨论帖表
-- =============================================
CREATE TABLE IF NOT EXISTS `discussion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drama_id` BIGINT DEFAULT NULL COMMENT '关联剧集ID',
    `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `type` TINYINT DEFAULT 1 COMMENT '类型 (1:剧评 2:吐槽 3:提问 4:资讯 5:杂谈)',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `reply_count` INT DEFAULT 0 COMMENT '回复数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    `featured` TINYINT(1) DEFAULT 0 COMMENT '是否精华',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:待审核 1:已发布 2:已关闭 3:已删除)',
    `last_reply_time` DATETIME DEFAULT NULL COMMENT '最后回复时间',
    `last_reply_user_id` BIGINT DEFAULT NULL COMMENT '最后回复用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_last_reply_time` (`last_reply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论帖表';

-- =============================================
-- 7. 讨论回复表
-- =============================================
CREATE TABLE IF NOT EXISTS `reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `discussion_id` BIGINT NOT NULL COMMENT '讨论帖ID',
    `user_id` BIGINT NOT NULL COMMENT '回复用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父回复ID',
    `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:待审核 1:已发布 2:已删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_discussion_id` (`discussion_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论回复表';

-- =============================================
-- 8. 用户追剧清单表
-- =============================================
CREATE TABLE IF NOT EXISTS `user_watchlist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `drama_id` BIGINT NOT NULL COMMENT '剧集ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (1:想看 2:在看 3:看过 4:弃剧)',
    `watched_episodes` INT DEFAULT 0 COMMENT '已看集数',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_drama` (`user_id`, `drama_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户追剧清单表';

-- =============================================
-- 9. 排行榜快照表
-- =============================================
CREATE TABLE IF NOT EXISTS `ranking` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `drama_id` BIGINT NOT NULL COMMENT '剧集ID',
    `rank_type` TINYINT NOT NULL COMMENT '榜单类型 (1:日榜 2:周榜 3:月榜 4:总榜)',
    `rank_category` VARCHAR(20) NOT NULL COMMENT '榜单分类 (region/genre/hot/new)',
    `category_value` VARCHAR(20) DEFAULT NULL COMMENT '分类值',
    `rank` INT NOT NULL COMMENT '排名',
    `previous_rank` INT DEFAULT NULL COMMENT '上期排名',
    `rank_change` INT DEFAULT 0 COMMENT '排名变化',
    `score` DECIMAL(5,2) DEFAULT NULL COMMENT '综合得分',
    `hot_score` BIGINT DEFAULT NULL COMMENT '热度值',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_drama_id` (`drama_id`),
    INDEX `idx_rank_type` (`rank_type`),
    INDEX `idx_rank_category` (`rank_category`),
    INDEX `idx_stat_date` (`stat_date`),
    INDEX `idx_rank` (`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜快照表';

-- =============================================
-- 10. 点赞记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型 (1:评论 2:讨论帖 3:回复)',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_target` (`user_id`, `target_type`, `target_id`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt7zE3S', '管理员', 'admin@drama-tracker.com', 9, 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt7zE3S', '测试用户', 'test@drama-tracker.com', 0, 1);

-- 插入测试剧集数据
INSERT INTO `drama` (`title`, `original_title`, `region`, `type`, `genres`, `status`, `release_date`, `total_episodes`, `aired_episodes`, `directors`, `actors`, `description`, `user_rating`, `rating_count`, `total_rating`, `hot_score`) VALUES
('繁花', '繁花', 'CN', 1, '剧情,年代', 2, '2023-12-27', 30, 30, '王家卫', '胡歌,马伊琍,唐嫣,辛芷蕾', '20世纪90年代初，阿宝在上海黄河路开启了他的传奇人生...', 8.5, 12500, 8.5, 985000),
('最后生还者', 'The Last of Us', 'US', 1, '剧情,惊悚,科幻', 2, '2023-01-15', 9, 9, 'Craig Mazin', 'Pedro Pascal,Bella Ramsey', '末日后的世界，乔尔和艾莉踏上了一段危险的旅程...', 9.0, 25000, 9.0, 1250000),
('非自然死亡', 'アンナチュラル', 'JP', 1, '悬疑,医疗', 2, '2018-01-12', 10, 10, '�的康之', '石原里美,井�的政�的,的上�的季', 'UDI实验室的法医们，揭开非自然死亡背后的真相...', 9.2, 18000, 9.2, 750000),
('黑暗荣耀', '더 글로리', 'KR', 1, '剧情,悬疑,复仇', 2, '2022-12-30', 16, 16, '安吉镐', '宋慧乔,李到贤,林智妍', '遭受校园暴力的女主角，展开长达18年的复仇计划...', 8.9, 32000, 8.9, 1580000);
