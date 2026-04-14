package com.pch.mng.report;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.project.Project;
import com.pch.mng.security.IssueVisibilityEvaluator;
import com.pch.mng.user.UserAccount;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.workflow.WorkflowTransitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private IssueRepository issueRepository;
    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;
    @Mock
    private IssueVisibilityEvaluator issueVisibilityEvaluator;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        lenient().when(issueVisibilityEvaluator.requiredContextForProject(anyLong()))
                .thenReturn(new IssueVisibilityEvaluator.MemberViewContext(1L, ProjectRole.ADMIN));
    }

    @Test
    @DisplayName("burndown: 스프린트 없으면 ENTITY_NOT_FOUND")
    void burndownMissingSprint() {
        when(sprintRepository.findByIdWithProject(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reportService.burndown(1L, 9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("burndown: 프로젝트 불일치 시 SPRINT_PROJECT_MISMATCH")
    void burndownProjectMismatch() {
        Project p = Project.builder().key("A").name("A").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 2L);
        Sprint s = Sprint.builder().project(p).name("S").status(SprintStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(3))
                .build();
        ReflectionTestUtils.setField(s, "id", 5L);
        when(sprintRepository.findByIdWithProject(5L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> reportService.burndown(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_PROJECT_MISMATCH);
    }

    @Test
    @DisplayName("velocity: 완료 스프린트별 DONE 스토리 포인트 합")
    void velocityOk() {
        Project p = Project.builder().key("A").name("A").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 1L);
        Sprint s1 = Sprint.builder().project(p).name("S1").status(SprintStatus.COMPLETED)
                .endDate(LocalDate.of(2026, 4, 1))
                .build();
        ReflectionTestUtils.setField(s1, "id", 10L);

        when(sprintRepository.findByProjectIdAndStatusOrderByEndDateDesc(1L, SprintStatus.COMPLETED))
                .thenReturn(List.of(s1));
        UserAccount rep = UserAccount.builder().email("r@x.com").password("x").name("R").build();
        ReflectionTestUtils.setField(rep, "id", 1L);
        Issue done = Issue.builder()
                .issueKey("A-1")
                .project(p)
                .issueType(com.pch.mng.global.enums.IssueType.TASK)
                .summary("done")
                .status(IssueStatus.DONE)
                .priority(Priority.MEDIUM)
                .reporter(rep)
                .storyPoints(13)
                .backlogRank(0)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(done, "id", 100L);
        when(issueRepository.findByProjectIdAndSprintIdAndArchivedFalseOrderByCreatedAtDesc(1L, 10L))
                .thenReturn(List.of(done));

        ReportResponse.VelocityDTO dto = reportService.velocity(1L, 6);
        assertThat(dto.getSprints()).hasSize(1);
        assertThat(dto.getSprints().get(0).getCompletedStoryPoints()).isEqualTo(13L);
    }

    @Test
    @DisplayName("cfd: 프로젝트 전체 이슈에 대해 일자별 시리즈 생성")
    void cfdProjectScope() {
        Project p = Project.builder().key("A").name("A").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 1L);
        Issue i = Issue.builder()
                .issueKey("A-1")
                .project(p)
                .issueType(com.pch.mng.global.enums.IssueType.TASK)
                .summary("x")
                .status(IssueStatus.BACKLOG)
                .priority(com.pch.mng.global.enums.Priority.MEDIUM)
                .reporter(null)
                .backlogRank(0)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(i, "id", 100L);

        when(issueRepository.findByProjectIdAndArchivedFalseOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(i)));
        when(workflowTransitionRepository.findByIssueIdInOrderByIssueAndTimeAsc(List.of(100L)))
                .thenReturn(List.of());

        ReportResponse.CfdDTO dto = reportService.cumulativeFlow(1L, null, 7);
        assertThat(dto.getSprintId()).isNull();
        assertThat(dto.getWindowDays()).isEqualTo(7);
        assertThat(dto.getSeries()).hasSize(7);
        assertThat(dto.getSeries().get(6).getByStatus().stream().mapToInt(ReportResponse.CfdStatusCountDTO::getCount).sum())
                .isEqualTo(1);
    }
}
