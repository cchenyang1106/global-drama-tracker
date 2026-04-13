CREATE TABLE IF NOT EXISTS activity_quiz (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    question_text TEXT COMMENT '题目文字描述',
    question_images MEDIUMTEXT DEFAULT NULL COMMENT '题目图片(JSON数组,base64)',
    question_files VARCHAR(2000) DEFAULT NULL COMMENT '题目附件URL(JSON数组)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动题目表';

CREATE TABLE IF NOT EXISTS quiz_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    quiz_id BIGINT NOT NULL COMMENT '题目ID',
    user_id BIGINT NOT NULL COMMENT '答题者ID',
    answer_text TEXT COMMENT '答案文字',
    answer_images MEDIUMTEXT DEFAULT NULL COMMENT '答案图片(JSON数组,base64)',
    answer_files VARCHAR(2000) DEFAULT NULL COMMENT '答案附件(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '0待批改 1通过 2不通过',
    remark VARCHAR(500) DEFAULT NULL COMMENT '批改备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_activity_user (activity_id, user_id),
    INDEX idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答卷表';

CREATE TABLE IF NOT EXISTS group_chat (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    name VARCHAR(200) DEFAULT NULL COMMENT '群名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群聊表';

CREATE TABLE IF NOT EXISTS group_member_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role TINYINT DEFAULT 0 COMMENT '0普通成员 1群主',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_group_user (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';

CREATE TABLE IF NOT EXISTS group_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '发送者',
    content TEXT NOT NULL COMMENT '消息内容(只支持文字)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_group (group_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群消息表';

-- 群成员表添加已读时间字段（用于计算未读消息数）
ALTER TABLE group_member_info ADD COLUMN IF NOT EXISTS last_read_time DATETIME DEFAULT NULL COMMENT '最后已读时间' AFTER role;
