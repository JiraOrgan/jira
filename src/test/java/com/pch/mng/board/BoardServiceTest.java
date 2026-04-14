package com.pch.mng.board;

import com.pch.mng.global.enums.BoardSwimlane;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.security.IssueVisibilityEvaluator;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    SprintRepository sprintRepository;

    @Mock
    IssueRepository issueRepository;

    @Mock
    SprintBoardRedisCache sprintBoardRedisCache;

    @Mock
    IssueVisibilityEvaluator issueVisibilityEvaluator;

    BoardService boardService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.when(sprintBoardRedisCache.isEnabled()).thenReturn(false);
        lenient().when(issueVisibilityEvaluator.canView(any(Issue.class))).thenReturn(true);
        boardService = new BoardService(sprintRepository, issueRepository, sprintBoardRedisCache,
                issueVisibilityEvaluator);
    }

    @Test
    @DisplayName("ASSIGNEE 스윔레인: 미배정 이슈는 동일 버킷에 모인다")
    void assigneeSwimlaneGroupsUnassigned() {
        when(sprintRepository.existsById(10L)).thenReturn(true);
        LocalDateTime t = LocalDateTime.of(2026, 4, 10, 12, 0);
        Issue i1 = issue(1L, "P-1", IssueStatus.BACKLOG, null, t);
        Issue i2 = issue(2L, "P-2", IssueStatus.BACKLOG, null, t);
        when(issueRepository.findForSprintBoard(10L)).thenReturn(List.of(i1, i2));

        SprintBoardResponse r = boardService.getSprintBoard(10L, BoardSwimlane.ASSIGNEE);

        assertThat(r.getSwimlane()).isEqualTo(BoardSwimlane.ASSIGNEE);
        assertThat(r.getColumns().get(0).getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(r.getColumns().get(0).getBuckets()).hasSize(1);
        assertThat(r.getColumns().get(0).getBuckets().get(0).getIssues()).hasSize(2);
    }

    @Test
    @DisplayName("ASSIGNEE 스윔레인: 담당자별 버킷 분리")
    void assigneeSwimlaneSplitsByAssignee() {
        when(sprintRepository.existsById(10L)).thenReturn(true);
        UserAccount u = UserAccount.builder().email("a@x.com").password("x").name("Kim").build();
        org.springframework.test.util.ReflectionTestUtils.setField(u, "id", 99L);

        LocalDateTime t = LocalDateTime.of(2026, 4, 10, 12, 0);
        Issue i1 = issue(1L, "P-1", IssueStatus.IN_PROGRESS, null, t);
        Issue i2 = issue(2L, "P-2", IssueStatus.IN_PROGRESS, u, t);
        when(issueRepository.findForSprintBoard(10L)).thenReturn(List.of(i1, i2));

        SprintBoardResponse r = boardService.getSprintBoard(10L, BoardSwimlane.ASSIGNEE);

        SprintBoardResponse.ColumnDTO col = r.getColumns().stream()
                .filter(c -> c.getStatus() == IssueStatus.IN_PROGRESS)
                .findFirst()
                .orElseThrow();
        assertThat(col.getBuckets()).hasSize(2);
    }

    @Test
    @DisplayName("스프린트가 없으면 ENTITY_NOT_FOUND")
    void sprintMissing() {
        when(sprintRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> boardService.getSprintBoard(99L, BoardSwimlane.NONE))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("swimlane NONE: 컬럼당 단일 버킷, 이슈는 BACKLOG 컬럼에만")
    void noneSwimlaneSingleBucketPerColumn() {
        when(sprintRepository.existsById(10L)).thenReturn(true);
        LocalDateTime t = LocalDateTime.of(2026, 4, 10, 12, 0);
        Issue i1 = issue(1L, "P-1", IssueStatus.BACKLOG, null, t);
        when(issueRepository.findForSprintBoard(10L)).thenReturn(List.of(i1));

        SprintBoardResponse r = boardService.getSprintBoard(10L, BoardSwimlane.NONE);

        assertThat(r.getSwimlane()).isEqualTo(BoardSwimlane.NONE);
        assertThat(r.getColumns()).hasSize(IssueStatus.values().length);
        assertThat(r.getColumns().get(0).getBuckets()).hasSize(1);
        assertThat(r.getColumns().get(0).getBuckets().get(0).getIssues()).hasSize(1);
        for (int i = 1; i < r.getColumns().size(); i++) {
            assertThat(r.getColumns().get(i).getBuckets().get(0).getIssues()).isEmpty();
        }
    }

    @Test
    @DisplayName("캐시 히트 시 DB 조회 없음")
    void cacheHitSkipsRepositories() {
        when(sprintBoardRedisCache.isEnabled()).thenReturn(true);
        SprintBoardResponse cached = new SprintBoardResponse();
        cached.setSwimlane(BoardSwimlane.NONE);
        when(sprintBoardRedisCache.get(10L, BoardSwimlane.NONE)).thenReturn(Optional.of(cached));

        SprintBoardResponse r = boardService.getSprintBoard(10L, BoardSwimlane.NONE);

        assertThat(r).isSameAs(cached);
        verify(sprintRepository, never()).existsById(anyLong());
        verify(issueRepository, never()).findForSprintBoard(anyLong());
        verify(sprintBoardRedisCache, never()).put(anyLong(), any(BoardSwimlane.class), any(SprintBoardResponse.class));
    }

    @Test
    @DisplayName("캐시 미스 시 조회 후 put")
    void cacheMissLoadsAndPuts() {
        when(sprintBoardRedisCache.isEnabled()).thenReturn(true);
        when(sprintBoardRedisCache.get(10L, BoardSwimlane.NONE)).thenReturn(Optional.empty());
        when(sprintRepository.existsById(10L)).thenReturn(true);
        when(issueRepository.findForSprintBoard(10L)).thenReturn(List.of());

        boardService.getSprintBoard(10L, BoardSwimlane.NONE);

        verify(sprintBoardRedisCache).put(eq(10L), eq(BoardSwimlane.NONE), any(SprintBoardResponse.class));
    }

    private static Issue issue(long id, String key, IssueStatus status, UserAccount assignee, LocalDateTime t) {
        Issue i = new Issue();
        i.setId(id);
        i.setIssueKey(key);
        i.setIssueType(IssueType.TASK);
        i.setSummary("s");
        i.setStatus(status);
        i.setPriority(Priority.MEDIUM);
        i.setBacklogRank(0L);
        i.setAssignee(assignee);
        i.setUpdatedAt(t);
        i.setCreatedAt(t);
        i.setArchived(false);
        return i;
    }
}
