package com.pch.mng.board;

import com.pch.mng.global.enums.BoardSwimlane;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    SprintRepository sprintRepository;

    @Mock
    IssueRepository issueRepository;

    BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService(sprintRepository, issueRepository);
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
        return i;
    }
}
