package com.pch.mng.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("LearnFlow AI API")
                        .version("v1")
                        .description("LLM 기반 적응형 학습 관리 시스템 API"))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1-Public")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/courses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi learnerApi() {
        return GroupedOpenApi.builder()
                .group("2-Learner")
                .pathsToMatch("/api/v1/enrollments/**", "/api/v1/ai/**", "/api/v1/onboarding/**")
                .build();
    }

    @Bean
    public GroupedOpenApi instructorApi() {
        return GroupedOpenApi.builder()
                .group("3-Instructor")
                .pathsToMatch("/api/v1/instructor/**", "/api/v1/files/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("4-Admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}
