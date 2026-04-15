package com.pch.mng.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardSwimlane;
import com.pch.mng.global.enums.IssueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SprintBoardRedisCacheTest {

    @Mock
    ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;

    @Mock
    StringRedisTemplate stringRedisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    BoardCacheProperties properties = new BoardCacheProperties();
    ObjectMapper objectMapper = new ObjectMapper();
    SprintBoardRedisCache cache;

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
        properties.setTtlSeconds(45);
        when(stringRedisTemplateProvider.getIfAvailable()).thenReturn(stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        cache = new SprintBoardRedisCache(properties, stringRedisTemplateProvider, objectMapper);
    }

    @Test
    @DisplayName("put 후 get으로 동일 페이로드 복원")
    void putAndGetRoundTrip() throws Exception {
        SprintBoardResponse body = new SprintBoardResponse();
        body.setSwimlane(BoardSwimlane.NONE);
        SprintBoardResponse.ColumnDTO col = new SprintBoardResponse.ColumnDTO();
        col.setStatus(IssueStatus.BACKLOG);
        col.setBuckets(List.of());
        body.setColumns(List.of(col));

        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);
        cache.put(7L, BoardSwimlane.NONE, body);
        verify(valueOps).set(anyString(), jsonCap.capture(), eq(Duration.ofSeconds(45)));

        when(valueOps.get("pch:board:sprint:7:swimlane:NONE")).thenReturn(jsonCap.getValue());
        assertThat(cache.get(7L, BoardSwimlane.NONE)).isPresent()
                .get()
                .extracting(SprintBoardResponse::getSwimlane)
                .isEqualTo(BoardSwimlane.NONE);
    }

    @Test
    @DisplayName("evictSprint는 NONE·ASSIGNEE 키를 삭제")
    void evictDeletesBothSwimlaneKeys() {
        cache.evictSprint(3L);
        verify(stringRedisTemplate).delete(List.of(
                "pch:board:sprint:3:swimlane:NONE",
                "pch:board:sprint:3:swimlane:ASSIGNEE"));
    }

    @Test
    @DisplayName("비활성화 시 put·evict·get은 Redis를 쓰지 않음")
    void whenDisabledNoRedis() {
        Mockito.reset(stringRedisTemplateProvider, stringRedisTemplate, valueOps);
        properties.setEnabled(false);
        cache = new SprintBoardRedisCache(properties, stringRedisTemplateProvider, objectMapper);

        assertThat(cache.get(1L, BoardSwimlane.NONE)).isEmpty();
        cache.put(1L, BoardSwimlane.NONE, new SprintBoardResponse());
        cache.evictSprint(1L);

        verifyNoInteractions(stringRedisTemplateProvider, stringRedisTemplate, valueOps);
    }
}
