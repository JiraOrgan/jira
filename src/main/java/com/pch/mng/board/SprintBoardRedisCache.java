package com.pch.mng.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardSwimlane;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SprintBoardRedisCache {

    private static final String PREFIX = "pch:board:sprint:";

    private final BoardCacheProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public Optional<SprintBoardResponse> get(long sprintId, BoardSwimlane swimlane) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        String key = key(sprintId, swimlane);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SprintBoardResponse.class));
        } catch (JsonProcessingException e) {
            stringRedisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void put(long sprintId, BoardSwimlane swimlane, SprintBoardResponse body) {
        if (!isEnabled()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            int ttl = Math.max(1, properties.getTtlSeconds());
            stringRedisTemplate.opsForValue().set(key(sprintId, swimlane), json, Duration.ofSeconds(ttl));
        } catch (JsonProcessingException ignored) {
            // 직렬화 실패 시 캐시 생략
        }
    }

    /** 해당 스프린트의 모든 스윔레인 캐시 삭제 */
    public void evictSprint(Long sprintId) {
        if (!isEnabled() || sprintId == null) {
            return;
        }
        stringRedisTemplate.delete(List.of(
                key(sprintId, BoardSwimlane.NONE),
                key(sprintId, BoardSwimlane.ASSIGNEE)));
    }

    private static String key(long sprintId, BoardSwimlane swimlane) {
        return PREFIX + sprintId + ":swimlane:" + swimlane.name();
    }
}
