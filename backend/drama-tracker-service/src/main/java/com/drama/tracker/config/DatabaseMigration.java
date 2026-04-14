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
            addColumnIfNotExists(conn, stmt, "group_member_info", "last_read_time",
                    "ALTER TABLE group_member_info ADD COLUMN last_read_time DATETIME DEFAULT NULL COMMENT '最后已读时间' AFTER role");
            addColumnIfNotExists(conn, stmt, "activity", "contact_info",
                    "ALTER TABLE activity ADD COLUMN contact_info VARCHAR(500) DEFAULT NULL COMMENT '通过后可见的联系方式' AFTER images");
            addColumnIfNotExists(conn, stmt, "activity", "announcement",
                    "ALTER TABLE activity ADD COLUMN announcement TEXT DEFAULT NULL COMMENT '活动公告' AFTER contact_info");
        } catch (Exception e) {
            log.warn("数据库迁移执行异常（可忽略）: {}", e.getMessage());
        }
    }

    private void addColumnIfNotExists(Connection conn, Statement stmt, String table, String column, String sql) throws Exception {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
        if (!rs.next()) {
            stmt.execute(sql);
            log.info("数据库迁移：{} 表添加 {} 字段成功", table, column);
        }
        rs.close();
    }
}
