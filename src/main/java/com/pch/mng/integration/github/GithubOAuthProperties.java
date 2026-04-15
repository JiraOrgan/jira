package com.pch.mng.integration.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.github.oauth")
public class GithubOAuthProperties {

    /** 비어 있으면 GitHub 연동 API가 비활성(501/설정 안내). */
    private String clientId = "";

    private String clientSecret = "";

    /** GitHub OAuth App에 등록한 콜백 URL(백엔드). */
    private String redirectUri = "http://localhost:8080/api/v1/integrations/github/callback";

    /** OAuth 완료 후 브라우저 리다이렉트(프론트). 예: http://localhost:5173 */
    private String frontendRedirectBase = "http://localhost:5173";

    /** OAuth state HMAC용 비밀(UTF-8 문자열, 충분히 길게). */
    private String stateHmacSecret = "";

    private String authorizeUrl = "https://github.com/login/oauth/authorize";

    private String accessTokenUrl = "https://github.com/login/oauth/access_token";
}
