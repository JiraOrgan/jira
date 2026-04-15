package com.pch.mng.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile("!test & !dev")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String PREFIX = "rt:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String refreshToken, long userId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(PREFIX + refreshToken, String.valueOf(userId), ttl);
    }

    @Override
    public Optional<Long> consume(String refreshToken) {
        String key = PREFIX + refreshToken;
        String val = stringRedisTemplate.opsForValue().getAndDelete(key);
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(val));
    }
}
