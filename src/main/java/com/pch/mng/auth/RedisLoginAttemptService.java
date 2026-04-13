package com.pch.mng.auth;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("!test & !dev")
@RequiredArgsConstructor
public class RedisLoginAttemptService implements LoginAttemptPort {

    private static final String FAIL_PREFIX = "login:fail:";
    private static final String LOCK_PREFIX = "login:lock:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login.lock-minutes:30}")
    private long lockMinutes;

    @Override
    public void checkLocked(String email) {
        Boolean locked = stringRedisTemplate.hasKey(LOCK_PREFIX + email);
        if (Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    @Override
    public void onSuccess(String email) {
        stringRedisTemplate.delete(FAIL_PREFIX + email);
    }

    @Override
    public void onFailure(String email) {
        String key = FAIL_PREFIX + email;
        Long fails = stringRedisTemplate.opsForValue().increment(key);
        if (fails != null && fails == 1L) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(lockMinutes));
        }
        if (fails != null && fails >= maxAttempts) {
            stringRedisTemplate.opsForValue().set(LOCK_PREFIX + email, "1", Duration.ofMinutes(lockMinutes));
            stringRedisTemplate.delete(key);
        }
    }
}
