package com.pch.mng.user;

import com.pch.mng.global.enums.UserRole;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    @Transactional
    public AuthResponse.TokenDTO signup(AuthRequest.SignupDTO reqDTO) {
        if (userAccountRepository.existsByEmail(reqDTO.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserAccount user = UserAccount.builder()
                .email(reqDTO.getEmail())
                .password(passwordEncoder.encode(reqDTO.getPassword()))
                .name(reqDTO.getName())
                .role(UserRole.LEARNER)
                .build();

        userAccountRepository.save(user);

        return generateTokens(user);
    }

    @Transactional
    public AuthResponse.TokenDTO login(AuthRequest.LoginDTO reqDTO) {
        UserAccount user = userAccountRepository.findByEmail(reqDTO.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isAccountLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(reqDTO.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userAccountRepository.save(user);
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.resetFailedAttempts();
        userAccountRepository.save(user);

        return generateTokens(user);
    }

    public AuthResponse.TokenDTO refresh(AuthRequest.RefreshDTO reqDTO) {
        String refreshToken = reqDTO.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String storedToken = (String) redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_PREFIX + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return generateTokens(user);
    }

    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    private AuthResponse.TokenDTO generateTokens(UserAccount user) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthResponse.TokenDTO.of(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }
}
