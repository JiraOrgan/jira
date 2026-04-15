package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.enums.SecurityLevel;
import com.pch.mng.global.enums.BoardType;
import com.pch.mng.project.Project;
import com.pch.mng.user.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class IssueSecurityPolicyTest {

    @Test
    @DisplayName("VIEWER는 INTERNAL 이슈를 볼 수 없다")
    void viewerCannotSeeInternal() {
        Issue issue = baseIssue(1L, 10L, 20L, SecurityLevel.INTERNAL);
        assertThat(IssueSecurityPolicy.canView(issue, ProjectRole.VIEWER, 99L)).isFalse();
    }

    @Test
    @DisplayName("VIEWER는 PUBLIC(및 null) 이슈를 볼 수 있다")
    void viewerSeesPublic() {
        Issue issueNull = baseIssue(1L, 10L, 20L, null);
        assertThat(IssueSecurityPolicy.canView(issueNull, ProjectRole.VIEWER, 99L)).isTrue();
        Issue issuePub = baseIssue(1L, 10L, 20L, SecurityLevel.PUBLIC);
        assertThat(IssueSecurityPolicy.canView(issuePub, ProjectRole.VIEWER, 99L)).isTrue();
    }

    @Test
    @DisplayName("REPORTER는 타인의 CONFIDENTIAL 이슈를 볼 수 없다")
    void reporterCannotSeeOthersConfidential() {
        Issue issue = baseIssue(1L, 10L, 20L, SecurityLevel.CONFIDENTIAL);
        assertThat(IssueSecurityPolicy.canView(issue, ProjectRole.REPORTER, 99L)).isFalse();
    }

    @Test
    @DisplayName("REPORTER는 본인이 보고한 CONFIDENTIAL 이슈를 볼 수 있다")
    void reporterSeesOwnConfidential() {
        Issue issue = baseIssue(1L, 10L, 20L, SecurityLevel.CONFIDENTIAL);
        assertThat(IssueSecurityPolicy.canView(issue, ProjectRole.REPORTER, 10L)).isTrue();
    }

    @Test
    @DisplayName("REPORTER는 CONFIDENTIAL 레벨로 설정할 수 없다")
    void reporterCannotSetConfidential() {
        assertThat(IssueSecurityPolicy.canSetSecurityLevel(ProjectRole.REPORTER, SecurityLevel.CONFIDENTIAL))
                .isFalse();
        assertThat(IssueSecurityPolicy.canSetSecurityLevel(ProjectRole.DEVELOPER, SecurityLevel.CONFIDENTIAL))
                .isTrue();
    }

    private static Issue baseIssue(long id, long reporterId, long assigneeId, SecurityLevel level) {
        Project p = Project.builder().key("P").name("P").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 1L);
        UserAccount rep = UserAccount.builder().email("r@x.com").password("x").name("R").build();
        ReflectionTestUtils.setField(rep, "id", reporterId);
        UserAccount asg = UserAccount.builder().email("a@x.com").password("x").name("A").build();
        ReflectionTestUtils.setField(asg, "id", assigneeId);
        Issue issue = Issue.builder()
                .issueKey("P-1")
                .project(p)
                .issueType(IssueType.TASK)
                .summary("s")
                .status(IssueStatus.BACKLOG)
                .priority(Priority.MEDIUM)
                .reporter(rep)
                .assignee(asg)
                .backlogRank(0L)
                .securityLevel(level)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(issue, "id", id);
        return issue;
    }
}
