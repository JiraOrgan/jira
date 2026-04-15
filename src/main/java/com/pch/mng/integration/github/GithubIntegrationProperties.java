package com.pch.mng.integration.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.github")
public class GithubIntegrationProperties {

    /**
     * GitHub 웹훅이 호출할 공개 베이스 URL(스킴+호스트+포트, 경로 없음).
     * 예: https://api.example.com 또는 로컬 터널 URL.
     */
    private String publicBaseUrl = "http://localhost:8080";
}
