package com.pch.mng.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HMAC 키 (Base64 또는 UTF-8 문자열, jjwt는 최소 길이 요구)
     */
    private String secret = "change-me";

    private long accessExpiration = 3_600_000L;

    private long refreshExpiration = 604_800_000L;
}
