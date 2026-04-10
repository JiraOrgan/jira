package com.pch.mng.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-hmac-secret-min-32-chars!!";

    @Mock
    private JwtProperties jwtProperties;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        when(jwtProperties.getAccessExpiration()).thenReturn(3_600_000L);
        provider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    @DisplayName("액세스 토큰 생성 후 검증·클레임 추출")
    void createValidateAndReadClaims() {
        String token = provider.createAccessToken(42L, "u@ex.com");
        assertThat(provider.validateAccessToken(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo(42L);
        assertThat(provider.getEmail(token)).isEqualTo("u@ex.com");
    }

    @Test
    @DisplayName("잘못된 토큰은 검증 실패")
    void invalidTokenNotValid() {
        assertThat(provider.validateAccessToken("not.a.jwt")).isFalse();
        assertThat(provider.validateAccessToken("")).isFalse();
    }
}
