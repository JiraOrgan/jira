package com.pch.mng.sprint;

import com.pch.mng.board.SprintBoardRedisCache;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.SprintIncompleteIssueDisposition;
import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    SprintRepository sprintRepository;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    IssueRepository issueRepository;

    @Mock
    SprintBoardRedisCache sprintBoardRedisCache;

    @InjectMocks
    SprintService sprintService;

    @Test
    @DisplayName("save: 프로젝트 없으면 ENTITY_NOT_FOUND")
    void saveProjectMissing() {
        SprintRequest.SaveDTO dto = new SprintRequest.SaveDTO();
        dto.setProjectId(9L);
        dto.setName("S1");
        when(projectRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.save(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("save: PLANNING 스프린트 저장")
    void savePersistsPlanning() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(inv -> {
            Sprint s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 50L);
            return s;
        });

        SprintRequest.SaveDTO dto = new SprintRequest.SaveDTO();
        dto.setProjectId(1L);
        dto.setName("Alpha");

        SprintResponse.DetailDTO res = sprintService.save(dto);

        assertThat(res.getId()).isEqualTo(50L);
        assertThat(res.getStatus()).isEqualTo(SprintStatus.PLANNING);
        assertThat(res.getName()).isEqualTo("Alpha");

        ArgumentCaptor<Sprint> cap = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(SprintStatus.PLANNING);
        assertThat(cap.getValue().getProject()).isSameAs(project);
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("start: PLANNING → ACTIVE, 보드 캐시 무효화")
    void startActivatesAndEvicts() {
        Sprint sprint = sprintEntity(3L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(3L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatusAndIdNot(10L, SprintStatus.ACTIVE, 3L)).thenReturn(false);

        SprintResponse.DetailDTO res = sprintService.start(3L);

        assertThat(res.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        verify(sprintBoardRedisCache).evictSprint(3L);
    }

    @Test
    @DisplayName("start: PLANNING이 아니면 SPRINT_INVALID_TRANSITION")
    void startRejectsNonPlanning() {
        Sprint sprint = sprintEntity(3L, 10L, SprintStatus.ACTIVE);
        when(sprintRepository.findByIdWithProject(3L)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.start(3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_INVALID_TRANSITION);
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("start: 동일 프로젝트에 다른 ACTIVE가 있으면 409")
    void startRejectsWhenAnotherActive() {
        Sprint sprint = sprintEntity(3L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(3L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatusAndIdNot(10L, SprintStatus.ACTIVE, 3L)).thenReturn(true);

        assertThatThrownBy(() -> sprintService.start(3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_ACTIVE_ALREADY_EXISTS);
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("complete: ACTIVE → COMPLETED, 보드 캐시 무효화")
    void completeFinishesAndEvicts() {
        Sprint sprint = sprintEntity(4L, 10L, SprintStatus.ACTIVE);
        when(sprintRepository.findByIdWithProject(4L)).thenReturn(Optional.of(sprint));

        SprintResponse.DetailDTO res = sprintService.complete(4L, null);

        assertThat(res.getStatus()).isEqualTo(SprintStatus.COMPLETED);
        verify(sprintBoardRedisCache).evictSprint(4L);
    }

    @Test
    @DisplayName("complete: ACTIVE가 아니면 SPRINT_INVALID_TRANSITION")
    void completeRejectsNonActive() {
        Sprint sprint = sprintEntity(4L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(4L)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.complete(4L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_INVALID_TRANSITION);
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("complete: 미완료 이슈는 기본적으로 제품 백로그로 이동")
    void completeMovesIncompleteIssuesToBacklog() {
        Sprint sprint = sprintEntity(4L, 10L, SprintStatus.ACTIVE);
        when(sprintRepository.findByIdWithProject(4L)).thenReturn(Optional.of(sprint));
        UserAccount reporter = UserAccount.builder()
                .email("u@x.com")
                .password("p")
                .name("U")
                .build();
        ReflectionTestUtils.setField(reporter, "id", 1L);
        Project project = sprint.getProject();
        Issue open = Issue.builder()
                .issueKey("P-9")
                .project(project)
                .issueType(IssueType.TASK)
                .summary("t")
                .status(IssueStatus.IN_PROGRESS)
                .priority(Priority.MEDIUM)
                .reporter(reporter)
                .sprint(sprint)
                .backlogRank(0L)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(open, "id", 90L);
        Issue done = Issue.builder()
                .issueKey("P-10")
                .project(project)
                .issueType(IssueType.TASK)
                .summary("d")
                .status(IssueStatus.DONE)
                .priority(Priority.MEDIUM)
                .reporter(reporter)
                .sprint(sprint)
                .backlogRank(0L)
                .archived(false)
                .build();
        when(issueRepository.findBySprint_IdAndArchivedFalse(4L)).thenReturn(List.of(open, done));
        when(issueRepository.maxBacklogRankForProjectBacklog(10L)).thenReturn(2000L);

        SprintResponse.DetailDTO res = sprintService.complete(4L, null);

        assertThat(res.getStatus()).isEqualTo(SprintStatus.COMPLETED);
        assertThat(open.getSprint()).isNull();
        assertThat(open.getStatus()).isEqualTo(IssueStatus.BACKLOG);
        assertThat(open.getBacklogRank()).isEqualTo(3000L);
        assertThat(done.getSprint()).isSameAs(sprint);
        verify(issueRepository).saveAll(List.of(open));
        verify(sprintBoardRedisCache).evictSprint(4L);
    }

    @Test
    @DisplayName("complete: NEXT_SPRINT이면 nextSprintId 필수")
    void completeNextSprintRequiresTarget() {
        Sprint sprint = sprintEntity(4L, 10L, SprintStatus.ACTIVE);
        when(sprintRepository.findByIdWithProject(4L)).thenReturn(Optional.of(sprint));
        Issue open = incompleteIssue(sprint, IssueStatus.SELECTED);
        when(issueRepository.findBySprint_IdAndArchivedFalse(4L)).thenReturn(List.of(open));
        SprintRequest.CompleteDTO dto = new SprintRequest.CompleteDTO();
        dto.setDisposition(SprintIncompleteIssueDisposition.NEXT_SPRINT);

        assertThatThrownBy(() -> sprintService.complete(4L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_COMPLETE_NEXT_REQUIRED);
        verify(issueRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("complete: NEXT_SPRINT — PLANNING 스프린트로 미완료 이슈 이관")
    void completeMovesIncompleteToPlanningSprint() {
        Sprint active = sprintEntity(4L, 10L, SprintStatus.ACTIVE);
        Sprint next = sprintEntity(5L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(4L)).thenReturn(Optional.of(active));
        when(sprintRepository.findByIdWithProject(5L)).thenReturn(Optional.of(next));
        Issue open = incompleteIssue(active, IssueStatus.CODE_REVIEW);
        when(issueRepository.findBySprint_IdAndArchivedFalse(4L)).thenReturn(List.of(open));

        SprintRequest.CompleteDTO dto = new SprintRequest.CompleteDTO();
        dto.setDisposition(SprintIncompleteIssueDisposition.NEXT_SPRINT);
        dto.setNextSprintId(5L);

        sprintService.complete(4L, dto);

        assertThat(open.getSprint()).isSameAs(next);
        assertThat(open.getStatus()).isEqualTo(IssueStatus.CODE_REVIEW);
        verify(issueRepository).saveAll(List.of(open));
        verify(sprintBoardRedisCache).evictSprint(5L);
        verify(sprintBoardRedisCache, times(1)).evictSprint(4L);
    }

    @Test
    @DisplayName("delete: ACTIVE 스프린트는 삭제 불가")
    void deleteRejectsActive() {
        Sprint sprint = sprintEntity(5L, 10L, SprintStatus.ACTIVE);
        when(sprintRepository.findByIdWithProject(5L)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.delete(5L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_DELETE_FORBIDDEN);
        verify(sprintRepository, never()).delete(any());
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("delete: 이슈가 배정된 스프린트는 삭제 불가")
    void deleteRejectsWhenIssuesAssigned() {
        Sprint sprint = sprintEntity(5L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(5L)).thenReturn(Optional.of(sprint));
        when(issueRepository.countBySprint_IdAndArchivedFalse(5L)).thenReturn(1L);

        assertThatThrownBy(() -> sprintService.delete(5L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SPRINT_DELETE_FORBIDDEN);
        verify(sprintRepository, never()).delete(any());
        verify(sprintBoardRedisCache, never()).evictSprint(any());
    }

    @Test
    @DisplayName("delete: 허용 시 evict 후 repository.delete")
    void deleteEvictsThenRemoves() {
        Sprint sprint = sprintEntity(5L, 10L, SprintStatus.PLANNING);
        when(sprintRepository.findByIdWithProject(5L)).thenReturn(Optional.of(sprint));
        when(issueRepository.countBySprint_IdAndArchivedFalse(5L)).thenReturn(0L);

        sprintService.delete(5L);

        verify(sprintBoardRedisCache).evictSprint(5L);
        verify(sprintRepository).delete(eq(sprint));
    }

    @Test
    @DisplayName("findById: 없으면 ENTITY_NOT_FOUND")
    void findByIdMissing() {
        when(sprintRepository.findByIdWithProject(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.findById(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("findByProject: 스프린트 MinDTO 목록")
    void findByProjectMapsMinDtos() {
        Project p = new Project();
        ReflectionTestUtils.setField(p, "id", 1L);
        Sprint a = Sprint.builder().project(p).name("S1").status(SprintStatus.PLANNING).build();
        ReflectionTestUtils.setField(a, "id", 11L);
        Sprint b = Sprint.builder().project(p).name("S2").status(SprintStatus.ACTIVE).build();
        ReflectionTestUtils.setField(b, "id", 12L);
        when(sprintRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(a, b));

        List<SprintResponse.MinDTO> list = sprintService.findByProject(1L);

        assertThat(list).extracting(SprintResponse.MinDTO::getId).containsExactly(11L, 12L);
    }

    private static Sprint sprintEntity(long sprintId, long projectId, SprintStatus status) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", projectId);
        Sprint sprint = Sprint.builder()
                .project(project)
                .name("S")
                .status(status)
                .build();
        ReflectionTestUtils.setField(sprint, "id", sprintId);
        return sprint;
    }

    private static Issue incompleteIssue(Sprint sprint, IssueStatus status) {
        Project project = sprint.getProject();
        UserAccount reporter = UserAccount.builder()
                .email("u@x.com")
                .password("p")
                .name("U")
                .build();
        ReflectionTestUtils.setField(reporter, "id", 1L);
        Issue issue = Issue.builder()
                .issueKey("P-1")
                .project(project)
                .issueType(IssueType.TASK)
                .summary("s")
                .status(status)
                .priority(Priority.MEDIUM)
                .reporter(reporter)
                .sprint(sprint)
                .backlogRank(0L)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(issue, "id", 77L);
        return issue;
    }
}
