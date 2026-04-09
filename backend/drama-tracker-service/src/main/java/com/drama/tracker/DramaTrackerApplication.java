package com.drama.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 全球影视追踪平台启动类。
 *
 * @author drama-tracker
 */
@SpringBootApplication
@EnableScheduling
public class DramaTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DramaTrackerApplication.class, args);
    }

}
