package com.pch.mng.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
@Primary
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private record Entry(long userId, Instant expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void save(String refreshToken, long userId, Duration ttl) {
        store.put(refreshToken, new Entry(userId, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<Long> consume(String refreshToken) {
        Entry e = store.remove(refreshToken);
        if (e == null || Instant.now().isAfter(e.expiresAt)) {
            return Optional.empty();
        }
        return Optional.of(e.userId);
    }
}
