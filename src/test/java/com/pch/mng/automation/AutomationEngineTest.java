package com.pch.mng.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.audit.IssueAuditService;
import com.pch.mng.board.SprintBoardRedisCache;
import com.pch.mng.global.enums.AutomationActionType;
import com.pch.mng.global.enums.AutomationTriggerType;
import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.project.Project;
import com.pch.mng.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutomationEngineTest {

    @Mock
    private AutomationRuleRepository ruleRepository;

    @Mock
    private AutomationExecutionLogRepository executionLogRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueAuditService issueAuditService;

    @Mock
    private SprintBoardRedisCache sprintBoardRedisCache;

    private AutomationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AutomationEngine(
                ruleRepository,
                executionLogRepository,
                issueRepository,
                issueAuditService,
                sprintBoardRedisCache,
                new ObjectMapper());
    }

    @Test
    @DisplayName("validatePayload: SET_PRIORITY에 priority 없으면 AUTOMATION_INVALID_SPEC")
    void validatePayloadRejectsSetPriorityWithoutPriority() {
        assertThatThrownBy(
                        () -> engine.validatePayload(
                                AutomationTriggerType.ISSUE_CREATED,
                                AutomationActionType.SET_PRIORITY,
                                null,
                                "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTOMATION_INVALID_SPEC);
    }

    @Test
    @DisplayName("validatePayload: SET_PRIORITY 정상")
    void validatePayloadAcceptsSetPriority() {
        engine.validatePayload(
                AutomationTriggerType.ISSUE_CREATED,
                AutomationActionType.SET_PRIORITY,
                "{\"issueTypes\":[\"BUG\"]}",
                "{\"priority\":\"LOW\"}");
    }

    @Test
    @DisplayName("onIssueCreated: BUG + 규칙 매칭 시 HIGH로 변경·로그·save")
    void onIssueCreatedAppliesSetPriority() {
        Project project = new Project();
        project.setId(10L);
        project.setKey("P");
        project.setName("Proj");
        project.setBoardType(BoardType.SCRUM);
        project.setArchived(false);
        project.setIssueSequence(0L);

        UserAccount reporter = new UserAccount();
        reporter.setId(99L);
        reporter.setEmail("r@test.com");
        reporter.setName("Rep");

        Issue issue =
                Issue.builder()
                        .issueKey("P-1")
                        .project(project)
                        .issueType(IssueType.BUG)
                        .summary("bug")
                        .status(IssueStatus.BACKLOG)
                        .priority(Priority.MEDIUM)
                        .reporter(reporter)
                        .backlogRank(0L)
                        .archived(false)
                        .build();
        issue.setId(555L);

        AutomationRule rule =
                AutomationRule.builder()
                        .project(project)
                        .name("bug-high")
                        .enabled(true)
                        .triggerType(AutomationTriggerType.ISSUE_CREATED)
                        .conditionJson("{\"issueTypes\":[\"BUG\"]}")
                        .actionType(AutomationActionType.SET_PRIORITY)
                        .actionJson("{\"priority\":\"HIGH\"}")
                        .sortOrder(0)
                        .build();
        rule.setId(1L);

        when(ruleRepository.findEnabledByProjectIdOrderBySortOrderAscIdAsc(10L)).thenReturn(List.of(rule));

        engine.onIssueCreated(issue, reporter);

        assertThat(issue.getPriority()).isEqualTo(Priority.HIGH);
        verify(issueRepository).save(issue);
        verify(issueAuditService).record(eq(issue), eq(reporter), eq("priority"), eq("MEDIUM"), eq("HIGH"));
        ArgumentCaptor<AutomationExecutionLog> logCap = ArgumentCaptor.forClass(AutomationExecutionLog.class);
        verify(executionLogRepository).save(logCap.capture());
        assertThat(logCap.getValue().isSuccess()).isTrue();
        assertThat(logCap.getValue().getMessage()).contains("HIGH");
    }

    @Test
    @DisplayName("onIssueCreated: 아카이브 이슈는 규칙 조회 없음")
    void onIssueCreatedSkipsArchived() {
        Project project = new Project();
        project.setId(1L);
        UserAccount reporter = new UserAccount();
        reporter.setId(1L);
        Issue issue =
                Issue.builder()
                        .issueKey("P-2")
                        .project(project)
                        .issueType(IssueType.TASK)
                        .summary("t")
                        .status(IssueStatus.BACKLOG)
                        .priority(Priority.MEDIUM)
                        .reporter(reporter)
                        .backlogRank(0L)
                        .archived(true)
                        .build();

        engine.onIssueCreated(issue, reporter);

        verify(ruleRepository, never()).findEnabledByProjectIdOrderBySortOrderAscIdAsc(any());
        verify(executionLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("onIssueStatusChanged: UNASSIGN 시 담당 해제·감사")
    void onIssueStatusChangedUnassigns() {
        Project project = new Project();
        project.setId(20L);
        project.setKey("Q");
        project.setName("Q");
        project.setBoardType(BoardType.SCRUM);
        project.setArchived(false);
        project.setIssueSequence(0L);

        UserAccount reporter = new UserAccount();
        reporter.setId(1L);
        UserAccount assignee = new UserAccount();
        assignee.setId(2L);

        Issue issue =
                Issue.builder()
                        .issueKey("Q-1")
                        .project(project)
                        .issueType(IssueType.TASK)
                        .summary("t")
                        .status(IssueStatus.SELECTED)
                        .priority(Priority.MEDIUM)
                        .assignee(assignee)
                        .reporter(reporter)
                        .backlogRank(0L)
                        .archived(false)
                        .build();
        issue.setId(100L);

        AutomationRule rule =
                AutomationRule.builder()
                        .project(project)
                        .name("unsel")
                        .enabled(true)
                        .triggerType(AutomationTriggerType.ISSUE_STATUS_CHANGED)
                        .conditionJson(
                                "{\"fromStatus\":\"BACKLOG\",\"toStatus\":\"SELECTED\"}")
                        .actionType(AutomationActionType.UNASSIGN)
                        .sortOrder(0)
                        .build();
        rule.setId(2L);

        when(ruleRepository.findEnabledByProjectIdOrderBySortOrderAscIdAsc(20L)).thenReturn(List.of(rule));

        engine.onIssueStatusChanged(issue, IssueStatus.BACKLOG, IssueStatus.SELECTED, reporter);

        assertThat(issue.getAssignee()).isNull();
        verify(issueAuditService).record(eq(issue), eq(reporter), eq("assigneeId"), eq("2"), eq((String) null));
        verify(issueRepository).save(issue);
    }
}
