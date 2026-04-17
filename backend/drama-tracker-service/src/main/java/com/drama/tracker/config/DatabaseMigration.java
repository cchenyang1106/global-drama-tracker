package com.drama.tracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库自动迁移：启动时检查并执行必要的表结构变更。
 */
@Slf4j
@Component
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // user 表补充可能缺失的字段
            addColumnIfNotExists(conn, stmt, "user", "nickname",
                    "ALTER TABLE user ADD COLUMN nickname VARCHAR(50) DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "user", "role",
                    "ALTER TABLE user ADD COLUMN role INT DEFAULT 0");
            addColumnIfNotExists(conn, stmt, "user", "status",
                    "ALTER TABLE user ADD COLUMN status INT DEFAULT 1");
            addColumnIfNotExists(conn, stmt, "user", "deleted",
                    "ALTER TABLE user ADD COLUMN deleted INT DEFAULT 0");
            addColumnIfNotExists(conn, stmt, "user", "last_login_time",
                    "ALTER TABLE user ADD COLUMN last_login_time DATETIME DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "user", "create_time",
                    "ALTER TABLE user ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP");
            addColumnIfNotExists(conn, stmt, "user", "update_time",
                    "ALTER TABLE user ADD COLUMN update_time DATETIME DEFAULT NULL");

            // activity 表补充所有可能缺失的字段
            addColumnIfNotExists(conn, stmt, "activity", "prefer_gender",
                    "ALTER TABLE activity ADD COLUMN prefer_gender INT DEFAULT 0 COMMENT '偏好性别'");
            addColumnIfNotExists(conn, stmt, "activity", "prefer_age_min",
                    "ALTER TABLE activity ADD COLUMN prefer_age_min INT DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "activity", "prefer_age_max",
                    "ALTER TABLE activity ADD COLUMN prefer_age_max INT DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "activity", "prefer_city",
                    "ALTER TABLE activity ADD COLUMN prefer_city VARCHAR(50) DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "activity", "prefer_tags",
                    "ALTER TABLE activity ADD COLUMN prefer_tags VARCHAR(200) DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "activity", "contact_info",
                    "ALTER TABLE activity ADD COLUMN contact_info VARCHAR(500) DEFAULT NULL COMMENT '通过后可见的联系方式'");
            addColumnIfNotExists(conn, stmt, "activity", "announcement",
                    "ALTER TABLE activity ADD COLUMN announcement TEXT DEFAULT NULL COMMENT '活动公告'");
            addColumnIfNotExists(conn, stmt, "activity", "team_complete",
                    "ALTER TABLE activity ADD COLUMN team_complete INT DEFAULT 0 COMMENT '0未完成 1已满员'");
            addColumnIfNotExists(conn, stmt, "activity", "view_count",
                    "ALTER TABLE activity ADD COLUMN view_count INT DEFAULT 0");
            addColumnIfNotExists(conn, stmt, "activity", "images",
                    "ALTER TABLE activity ADD COLUMN images TEXT DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "activity", "update_time",
                    "ALTER TABLE activity ADD COLUMN update_time DATETIME DEFAULT NULL");

            addColumnIfNotExists(conn, stmt, "group_member_info", "last_read_time",
                    "ALTER TABLE group_member_info ADD COLUMN last_read_time DATETIME DEFAULT NULL COMMENT '最后已读时间'");

            // user_profile 表补充可能缺失的字段
            addColumnIfNotExists(conn, stmt, "user_profile", "tags",
                    "ALTER TABLE user_profile ADD COLUMN tags VARCHAR(500) DEFAULT NULL COMMENT '个人标签'");
            addColumnIfNotExists(conn, stmt, "user_profile", "wechat",
                    "ALTER TABLE user_profile ADD COLUMN wechat VARCHAR(50) DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "user_profile", "show_wechat",
                    "ALTER TABLE user_profile ADD COLUMN show_wechat INT DEFAULT 0");
            addColumnIfNotExists(conn, stmt, "user_profile", "photos",
                    "ALTER TABLE user_profile ADD COLUMN photos TEXT DEFAULT NULL");
            addColumnIfNotExists(conn, stmt, "user_profile", "update_time",
                    "ALTER TABLE user_profile ADD COLUMN update_time DATETIME DEFAULT NULL");

            // 创建活动留言板表
            createTableIfNotExists(conn, stmt, "activity_message",
                    "CREATE TABLE activity_message (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "activity_id BIGINT NOT NULL COMMENT '活动ID'," +
                    "user_id BIGINT NOT NULL COMMENT '留言用户ID'," +
                    "content VARCHAR(500) NOT NULL COMMENT '留言内容'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "deleted INT DEFAULT 0 COMMENT '逻辑删除'," +
                    "INDEX idx_activity_id(activity_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动留言板'");

            // 预置示范数据（确保广场不为空，仅首次插入）
            insertSampleDataIfEmpty(conn, stmt);

            // 清理现有活动中的审核敏感词
            sanitizeExistingContent(stmt);
        } catch (Exception e) {
            log.warn("数据库迁移执行异常（可忽略）: {}", e.getMessage());
        }
    }

    /**
     * 如果活动表为空，插入预置的示范数据（示范用户 + 示范活动）。
     */
    private void insertSampleDataIfEmpty(Connection conn, Statement stmt) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM activity WHERE deleted = 0");
            rs.next();
            if (rs.getInt(1) > 0) { rs.close(); return; } // 已有数据，不重复插入
            rs.close();

            // 检查是否有示范用户，没有则创建
            rs = stmt.executeQuery("SELECT id FROM user WHERE phone = 'demo_user_01' LIMIT 1");
            long demoUser1;
            if (!rs.next()) {
                // 密码 demo123456 的 BCrypt 哈希
                stmt.execute("INSERT INTO user (phone, password, nickname, role, status, create_time) VALUES " +
                        "('demo_user_01', '$2a$10$xGTqFpx3R0EvDBfUMB2Iru5FGHmTx3j3BqBDRfVKx7UWAgMx0cQNa', '活动达人小明', 0, 1, NOW())");
                rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                rs.next();
                demoUser1 = rs.getLong(1);
                stmt.execute("INSERT INTO user_profile (user_id, age, gender, city, bio, hobbies) VALUES " +
                        "(" + demoUser1 + ", 25, 1, '北京', '热爱户外运动和美食探索', '跑步,摄影,烹饪')");
            } else {
                demoUser1 = rs.getLong(1);
            }
            rs.close();

            rs = stmt.executeQuery("SELECT id FROM user WHERE phone = 'demo_user_02' LIMIT 1");
            long demoUser2;
            if (!rs.next()) {
                stmt.execute("INSERT INTO user (phone, password, nickname, role, status, create_time) VALUES " +
                        "('demo_user_02', '$2a$10$xGTqFpx3R0EvDBfUMB2Iru5FGHmTx3j3BqBDRfVKx7UWAgMx0cQNa', '旅行小爱', 0, 1, NOW())");
                rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                rs.next();
                demoUser2 = rs.getLong(1);
                stmt.execute("INSERT INTO user_profile (user_id, age, gender, city, bio, hobbies) VALUES " +
                        "(" + demoUser2 + ", 23, 2, '上海', '文艺青年，喜欢旅游和读书', '旅游,阅读,咖啡')");
            } else {
                demoUser2 = rs.getLong(1);
            }
            rs.close();

            rs = stmt.executeQuery("SELECT id FROM user WHERE phone = 'demo_user_03' LIMIT 1");
            long demoUser3;
            if (!rs.next()) {
                stmt.execute("INSERT INTO user (phone, password, nickname, role, status, create_time) VALUES " +
                        "('demo_user_03', '$2a$10$xGTqFpx3R0EvDBfUMB2Iru5FGHmTx3j3BqBDRfVKx7UWAgMx0cQNa', '游戏少年阿杰', 0, 1, NOW())");
                rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                rs.next();
                demoUser3 = rs.getLong(1);
                stmt.execute("INSERT INTO user_profile (user_id, age, gender, city, bio, hobbies) VALUES " +
                        "(" + demoUser3 + ", 22, 1, '深圳', '资深游戏玩家，热衷电竞和桌游', '英雄联盟,狼人杀,密室逃脱')");
            } else {
                demoUser3 = rs.getLong(1);
            }
            rs.close();

            // 插入示范活动
            String[] activities = {
                "INSERT INTO activity (user_id, title, category, description, location, activity_time, max_people, joined_count, tags, status, view_count, team_complete, create_time) VALUES " +
                "(" + demoUser1 + ", '周末朝阳公园跑步约伴', '运动', '每周六早上7点在朝阳公园南门集合晨跑5公里，跑完一起吃早餐！适合初跑者和资深跑友，配速不限，重在坚持和陪伴。', '北京·朝阳公园南门', '2026-05-01 07:00', 6, 0, '跑步,健身,早起', 1, 15, 0, NOW())",

                "INSERT INTO activity (user_id, title, category, description, location, activity_time, max_people, joined_count, tags, status, view_count, team_complete, create_time) VALUES " +
                "(" + demoUser2 + ", '上海外滩摄影徒步之旅', '旅游', '计划沿外滩到豫园一路步行拍照，用镜头记录城市之美。会经过南京路、城隍庙等经典地标，边走边拍边聊，享受慢生活。', '上海·外滩', '2026-05-03 14:00', 4, 0, '摄影,徒步,旅游', 1, 23, 0, NOW())",

                "INSERT INTO activity (user_id, title, category, description, location, activity_time, max_people, joined_count, tags, status, view_count, team_complete, create_time) VALUES " +
                "(" + demoUser3 + ", '深圳线下狼人杀桌游局', '游戏', '本周日下午在南山区找了一间桌游吧，开一局12人狼人杀！欢迎萌新和老手，现场会有人教学。还可以玩其他桌游，阿瓦隆、谁是卧底都可以。', '深圳·南山桌游吧', '2026-05-04 14:00', 12, 0, '狼人杀,桌游,交友', 1, 31, 0, NOW())",

                "INSERT INTO activity (user_id, title, category, description, location, activity_time, max_people, joined_count, tags, status, view_count, team_complete, create_time) VALUES " +
                "(" + demoUser1 + ", '周三晚火锅美食探店', '美食', '发现了一家新开的重庆火锅店，评价很高！想约几个人一起去尝尝，AA制，人多才热闹。辣度可以选择，不吃辣的也有鸳鸯锅。', '北京·望京', '2026-04-30 18:30', 5, 0, '火锅,美食,探店', 1, 18, 0, NOW())",

                "INSERT INTO activity (user_id, title, category, description, location, activity_time, max_people, joined_count, tags, status, view_count, team_complete, create_time) VALUES " +
                "(" + demoUser2 + ", '读书会：一起聊《人类简史》', '学习', '本月读书会主题是尤瓦尔·赫拉利的《人类简史》，无论你读没读完都欢迎来交流！我们会讨论书中关于认知革命、农业革命的观点，每人分享5分钟感悟。', '线上·腾讯会议', '2026-05-05 20:00', 8, 0, '读书,学习,人文', 1, 12, 0, NOW())"
            };

            for (String sql : activities) {
                stmt.execute(sql);
            }

            log.info("数据库迁移：预置示范数据插入成功（3个用户 + 5个活动）");
        } catch (Exception e) {
            log.warn("预置示范数据插入异常（可忽略）: {}", e.getMessage());
        }
    }

    /**
     * 清理现有活动和用户数据中的审核敏感词汇（搭子→伙伴，组队→加入，一起玩→参与活动）。
     */
    private void sanitizeExistingContent(Statement stmt) {
        try {
            String[][] replacements = {
                {"搭子", "伙伴"}, {"找搭子", "找伙伴"}, {"组队", "加入"},
                {"一起玩", "参与活动"}, {"约起来", "一起参与"}
            };
            int total = 0;
            for (String[] r : replacements) {
                total += stmt.executeUpdate("UPDATE activity SET title = REPLACE(title, '" + r[0] + "', '" + r[1] + "') WHERE title LIKE '%" + r[0] + "%'");
                total += stmt.executeUpdate("UPDATE activity SET description = REPLACE(description, '" + r[0] + "', '" + r[1] + "') WHERE description LIKE '%" + r[0] + "%'");
            }
            if (total > 0) {
                log.info("数据库迁移：清理审核敏感词完成，更新了 {} 条记录", total);
            }
        } catch (Exception e) {
            log.warn("清理审核敏感词异常（可忽略）: {}", e.getMessage());
        }
    }

    private void createTableIfNotExists(Connection conn, Statement stmt, String table, String sql) throws Exception {
        ResultSet rs = conn.getMetaData().getTables(null, null, table, null);
        if (!rs.next()) {
            stmt.execute(sql);
            log.info("数据库迁移：创建 {} 表成功", table);
        }
        rs.close();
    }

    private void addColumnIfNotExists(Connection conn, Statement stmt, String table, String column, String sql) throws Exception {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
        if (!rs.next()) {
            stmt.execute(sql);
            log.info("数据库迁移：{} 表添加 {} 字段成功", table, column);
        }
        rs.close();
    }

    private void renameColumnIfNeeded(Connection conn, Statement stmt, String table, String oldCol, String newCol, String sql) throws Exception {
        ResultSet rsNew = conn.getMetaData().getColumns(null, null, table, newCol);
        if (rsNew.next()) { rsNew.close(); return; } // 新列已存在，无需操作
        rsNew.close();
        ResultSet rsOld = conn.getMetaData().getColumns(null, null, table, oldCol);
        if (rsOld.next()) {
            stmt.execute(sql);
            log.info("数据库迁移：{} 表 {} 列重命名为 {} 成功", table, oldCol, newCol);
        }
        rsOld.close();
    }
}
