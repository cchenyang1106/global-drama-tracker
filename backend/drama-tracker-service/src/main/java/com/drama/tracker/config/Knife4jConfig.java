package com.drama.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置类。
 *
 * @author drama-tracker
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Global Drama Tracker API")
                        .description("全球影视追踪平台 API 文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Drama Tracker Team")
                                .email("support@drama-tracker.com")
                                .url("https://github.com/yourusername/global-drama-tracker"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
