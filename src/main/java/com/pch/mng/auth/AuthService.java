package com.pch.mng.auth;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import com.pch.mng.user.UserAccountRequest;
import com.pch.mng.user.UserAccountResponse;
import com.pch.mng.user.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore refreshTokenStore;
    private final LoginAttemptPort loginAttemptPort;

    @Transactional
    public UserAccountResponse.DetailDTO register(UserAccountRequest.JoinDTO dto) {
        return userAccountService.save(dto);
    }

    public AuthResponse.TokenDTO login(AuthRequest.LoginDTO dto) {
        loginAttemptPort.checkLocked(dto.getEmail());

        UserAccount user = userAccountRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptPort.onFailure(dto.getEmail());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        loginAttemptPort.onSuccess(dto.getEmail());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse.TokenDTO refresh(AuthRequest.RefreshDTO dto) {
        long userId = refreshTokenStore.consume(dto.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        return issueTokens(user);
    }

    private AuthResponse.TokenDTO issueTokens(UserAccount user) {
        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refresh = UUID.randomUUID().toString();
        refreshTokenStore.save(refresh, user.getId(),
                Duration.ofMillis(jwtProperties.getRefreshExpiration()));
        long expiresInSec = jwtProperties.getAccessExpiration() / 1000;
        return AuthResponse.TokenDTO.of(access, refresh, expiresInSec);
    }
}
