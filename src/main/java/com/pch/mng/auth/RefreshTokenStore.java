package com.pch.mng.auth;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

    void save(String refreshToken, long userId, Duration ttl);

    Optional<Long> consume(String refreshToken);
}
