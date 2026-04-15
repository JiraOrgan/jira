package com.pch.mng.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardSwimlane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SprintBoardRedisCache {

    private static final String PREFIX = "pch:board:sprint:";

    private final BoardCacheProperties properties;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public Optional<SprintBoardResponse> get(long sprintId, BoardSwimlane swimlane) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
        if (redis == null) {
            return Optional.empty();
        }
        String key = key(sprintId, swimlane);
        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(objectMapper.readValue(json, SprintBoardResponse.class));
            } catch (JsonProcessingException e) {
                try {
                    redis.delete(key);
                } catch (DataAccessException ignored) {
                    log.debug("Redis board cache delete skipped: {}", ignored.getMessage());
                }
                return Optional.empty();
            }
        } catch (DataAccessException e) {
            log.debug("Redis board cache get skipped: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void put(long sprintId, BoardSwimlane swimlane, SprintBoardResponse body) {
        if (!isEnabled()) {
            return;
        }
        StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            int ttl = Math.max(1, properties.getTtlSeconds());
            redis.opsForValue().set(key(sprintId, swimlane), json, Duration.ofSeconds(ttl));
        } catch (JsonProcessingException ignored) {
            // 직렬화 실패 시 캐시 생략
        } catch (DataAccessException e) {
            log.debug("Redis board cache put skipped: {}", e.getMessage());
        }
    }

    /** 해당 스프린트의 모든 스윔레인 캐시 삭제 */
    public void evictSprint(Long sprintId) {
        if (!isEnabled() || sprintId == null) {
            return;
        }
        StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            redis.delete(List.of(
                    key(sprintId, BoardSwimlane.NONE),
                    key(sprintId, BoardSwimlane.ASSIGNEE)));
        } catch (DataAccessException e) {
            log.debug("Redis board cache evict skipped: {}", e.getMessage());
        }
    }

    private static String key(long sprintId, BoardSwimlane swimlane) {
        return PREFIX + sprintId + ":swimlane:" + swimlane.name();
    }
}
