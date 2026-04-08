package com.pch.mng.user;

public class AuthResponse {

    public record TokenDTO(
            String accessToken,
            String refreshToken,
            String email,
            String role
    ) {
        public static TokenDTO of(String accessToken, String refreshToken, String email, String role) {
            return new TokenDTO(accessToken, refreshToken, email, role);
        }
    }
}
