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
            // 检查 group_member_info 表是否有 last_read_time 字段
            ResultSet rs = conn.getMetaData().getColumns(null, null, "group_member_info", "last_read_time");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE group_member_info ADD COLUMN last_read_time DATETIME DEFAULT NULL COMMENT '最后已读时间' AFTER role");
                log.info("数据库迁移：group_member_info 表添加 last_read_time 字段成功");
            } else {
                log.info("数据库迁移：last_read_time 字段已存在，跳过");
            }
            rs.close();
        } catch (Exception e) {
            log.warn("数据库迁移执行异常（可忽略）: {}", e.getMessage());
        }
    }
}
