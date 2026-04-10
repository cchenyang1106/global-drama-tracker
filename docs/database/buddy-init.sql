-- 交友搭子 App 数据库初始化
-- 删除旧的剧集相关表
DROP TABLE IF EXISTS like_record, ranking, user_watchlist, reply, discussion, rating, comment, episode, drama;
DROP TABLE IF EXISTS page_view, chat_message, match_request, activity, user_profile;

-- 用户资料表
CREATE TABLE user_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    age INT DEFAULT NULL COMMENT '年龄',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    city VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
    occupation VARCHAR(100) DEFAULT NULL COMMENT '职业',
    bio TEXT DEFAULT NULL COMMENT '个人简介',
    hobbies VARCHAR(500) DEFAULT NULL COMMENT '兴趣爱好(逗号分隔)',
    wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号(匹配成功后可见)',
    photos VARCHAR(2000) DEFAULT NULL COMMENT '个人照片URL(JSON数组)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 活动/需求发布表
CREATE TABLE activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    description TEXT COMMENT '活动描述',
    location VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    activity_time VARCHAR(100) DEFAULT NULL COMMENT '活动时间描述',
    max_people INT DEFAULT 1 COMMENT '需要人数',
    joined_count INT DEFAULT 0 COMMENT '已报名人数',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
    images VARCHAR(2000) DEFAULT NULL COMMENT '图片URL(JSON数组)',
    status TINYINT DEFAULT 1 COMMENT '0已关闭 1进行中 2已满员',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动发布表';

-- 匹配申请表
CREATE TABLE match_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    from_user_id BIGINT NOT NULL COMMENT '申请者ID',
    to_user_id BIGINT NOT NULL COMMENT '活动发布者ID',
    message TEXT COMMENT '申请留言',
    status TINYINT DEFAULT 0 COMMENT '0待处理 1已同意 2已拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_activity (activity_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    UNIQUE INDEX idx_unique_apply (activity_id, from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配申请表';

-- 聊天消息表
CREATE TABLE chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL COMMENT '关联匹配ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    msg_type TINYINT DEFAULT 0 COMMENT '0文本 1图片',
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_match (match_id),
    INDEX idx_receiver (receiver_id, is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 页面访问记录表
CREATE TABLE page_view (
    id BIGINT NOT NULL AUTO_INCREMENT,
    path VARCHAR(255) NOT NULL,
    page_title VARCHAR(255) DEFAULT NULL,
    referrer VARCHAR(500) DEFAULT NULL,
    visitor_id VARCHAR(64) NOT NULL,
    ip VARCHAR(50) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    screen_width INT DEFAULT NULL,
    screen_height INT DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_path (path),
    INDEX idx_visitor_id (visitor_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面访问记录表';
