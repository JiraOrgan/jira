package com.pch.mng.board;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.board.cache")
public class BoardCacheProperties {

    /** false면 GET 캐시 미사용·무효화 호출 무시 (테스트·로컬 디버그용) */
    private boolean enabled = true;

    /** Redis TTL (초) */
    private int ttlSeconds = 60;
}
