package com.pch.mng.user;

import lombok.Data;

public class AuthResponse {

    @Data
    public static class TokenDTO {
        private String accessToken;
        private String refreshToken;
        private String email;
        private String role;

        private TokenDTO() {}

        public static TokenDTO of(String accessToken, String refreshToken, String email, String role) {
            TokenDTO dto = new TokenDTO();
            dto.accessToken = accessToken;
            dto.refreshToken = refreshToken;
            dto.email = email;
            dto.role = role;
            return dto;
        }
    }
}
