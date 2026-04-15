package com.pch.mng.integration.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.integration.crypto")
public class IntegrationCryptoProperties {

    /** AES-256 키용 UTF-8 비밀(32바이트 이상 권장). 비어 있으면 토큰 저장 생략 불가 처리. */
    private String secret = "";
}
