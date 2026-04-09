package com.pch.mng.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class AuthResponse {

    private AuthResponse() {
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenDTO {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        /** 액세스 토큰 만료까지 초 단위 */
        private long expiresIn;

        public static TokenDTO of(String accessToken, String refreshToken, long expiresInSeconds) {
            return new TokenDTO(accessToken, refreshToken, "Bearer", expiresInSeconds);
        }
    }
}
