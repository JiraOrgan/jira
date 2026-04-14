package com.pch.mng.audit;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.SecurityLevel;
import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.issue.Issue;
import com.pch.mng.project.Project;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueAuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private IssueAuditService issueAuditService;

    @Test
    @DisplayName("record: changedBy가 null이면 저장하지 않음")
    void recordSkipsWhenNoActor() {
        Issue issue = mockIssue();
        issueAuditService.record(issue, null, "summary", "a", "b");
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("record: fieldName이 비어 있으면 저장하지 않음")
    void recordSkipsBlankField() {
        UserAccount u = actor(1L);
        Issue issue = mockIssue();
        issueAuditService.record(issue, u, "  ", "a", "b");
        issueAuditService.record(issue, u, "", "a", "b");
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("record: old/new 정규화 후 동일하면 저장하지 않음")
    void recordSkipsNoOp() {
        UserAccount u = actor(1L);
        Issue issue = mockIssue();
        issueAuditService.record(issue, u, "x", "same", "same");
        issueAuditService.record(issue, u, "x", null, "");
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("record: 변경 시 AuditLog 저장")
    void recordPersists() {
        UserAccount u = actor(1L);
        Issue issue = mockIssue();
        issueAuditService.record(issue, u, "priority", "HIGH", "LOW");

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getIssue()).isSameAs(issue);
        assertThat(cap.getValue().getChangedBy()).isSameAs(u);
        assertThat(cap.getValue().getFieldName()).isEqualTo("priority");
        assertThat(cap.getValue().getOldValue()).isEqualTo("HIGH");
        assertThat(cap.getValue().getNewValue()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("record: newValue 4000자 초과 시 잘라서 저장")
    void recordTruncatesLongValue() {
        UserAccount u = actor(1L);
        Issue issue = mockIssue();
        String longNew = "N".repeat(4001);
        issueAuditService.record(issue, u, "description", "o", longNew);

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getNewValue()).hasSize(4001);
        assertThat(cap.getValue().getNewValue()).startsWith("N".repeat(4000));
        assertThat(cap.getValue().getNewValue()).endsWith("…");
    }

    @Test
    @DisplayName("deleteAllForIssue: id null이면 호출 없음")
    void deleteSkipsNullId() {
        issueAuditService.deleteAllForIssue(null);
        verify(auditLogRepository, never()).deleteByIssue_Id(any());
    }

    @Test
    @DisplayName("deleteAllForIssue: 이슈별 삭제 위임")
    void deleteAllForIssue() {
        issueAuditService.deleteAllForIssue(99L);
        verify(auditLogRepository).deleteByIssue_Id(99L);
    }

    @Test
    @DisplayName("logIssueCreated: 주요 필드에 대해 record 호출")
    void logIssueCreatedWritesMultipleRows() {
        UserAccount creator = actor(5L);
        Project p = Project.builder().key("P").name("P").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 1L);
        UserAccount assignee = actor(9L);
        Sprint sprint = Sprint.builder().project(p).name("S").status(SprintStatus.PLANNING).build();
        ReflectionTestUtils.setField(sprint, "id", 3L);
        Issue parent = Issue.builder()
                .issueKey("P-1")
                .project(p)
                .issueType(IssueType.EPIC)
                .summary("parent")
                .status(IssueStatus.BACKLOG)
                .priority(Priority.MEDIUM)
                .reporter(creator)
                .backlogRank(0L)
                .build();
        ReflectionTestUtils.setField(parent, "id", 100L);

        Issue issue = Issue.builder()
                .issueKey("P-2")
                .project(p)
                .issueType(IssueType.TASK)
                .summary("child")
                .description("desc")
                .status(IssueStatus.BACKLOG)
                .priority(Priority.HIGH)
                .storyPoints(5)
                .reporter(creator)
                .assignee(assignee)
                .sprint(sprint)
                .parent(parent)
                .backlogRank(0L)
                .securityLevel(SecurityLevel.INTERNAL)
                .epicStartDate(LocalDate.of(2026, 4, 1))
                .epicEndDate(LocalDate.of(2026, 4, 30))
                .build();
        ReflectionTestUtils.setField(issue, "id", 2L);

        issueAuditService.logIssueCreated(issue, creator);

        verify(auditLogRepository, times(12)).save(any());
    }

    private static UserAccount actor(long id) {
        UserAccount u = UserAccount.builder().email(id + "@x.com").password("p").name("U").build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private static Issue mockIssue() {
        Project p = Project.builder().key("X").name("X").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 1L);
        UserAccount r = actor(2L);
        return Issue.builder()
                .issueKey("X-1")
                .project(p)
                .issueType(IssueType.TASK)
                .summary("s")
                .status(IssueStatus.BACKLOG)
                .priority(Priority.MEDIUM)
                .reporter(r)
                .backlogRank(0L)
                .build();
    }
}
