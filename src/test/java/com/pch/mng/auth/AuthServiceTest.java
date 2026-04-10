package com.pch.mng.auth;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import com.pch.mng.user.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private LoginAttemptPort loginAttemptPort;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 잠금 해제·토큰 발급")
    void loginSuccess() {
        UserAccount user = UserAccount.builder()
                .email("a@ex.com")
                .password("{bcrypt}hash")
                .name("A")
                .build();
        ReflectionTestUtils.setField(user, "id", 5L);

        AuthRequest.LoginDTO dto = new AuthRequest.LoginDTO();
        dto.setEmail("a@ex.com");
        dto.setPassword("plain");

        when(userAccountRepository.findByEmail("a@ex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(5L, "a@ex.com")).thenReturn("access.jwt");
        when(jwtProperties.getRefreshExpiration()).thenReturn(604_800_000L);
        when(jwtProperties.getAccessExpiration()).thenReturn(3_600_000L);

        AuthResponse.TokenDTO tokens = authService.login(dto);

        assertThat(tokens.getAccessToken()).isEqualTo("access.jwt");
        assertThat(tokens.getTokenType()).isEqualTo("Bearer");
        verify(loginAttemptPort).checkLocked("a@ex.com");
        verify(loginAttemptPort).onSuccess("a@ex.com");
        verify(loginAttemptPort, never()).onFailure(any());
        verify(refreshTokenStore).save(any(String.class), eq(5L), any());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 실패 카운트·INVALID_CREDENTIALS")
    void loginWrongPassword() {
        UserAccount user = UserAccount.builder()
                .email("a@ex.com")
                .password("{bcrypt}hash")
                .name("A")
                .build();
        ReflectionTestUtils.setField(user, "id", 5L);

        AuthRequest.LoginDTO dto = new AuthRequest.LoginDTO();
        dto.setEmail("a@ex.com");
        dto.setPassword("wrong");

        when(userAccountRepository.findByEmail("a@ex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(loginAttemptPort).onFailure("a@ex.com");
        verify(loginAttemptPort, never()).onSuccess(any());
    }

    @Test
    @DisplayName("리프레시 토큰 없으면 INVALID_REFRESH_TOKEN")
    void refreshUnknownToken() {
        AuthRequest.RefreshDTO dto = new AuthRequest.RefreshDTO();
        dto.setRefreshToken("unknown");
        when(refreshTokenStore.consume("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
