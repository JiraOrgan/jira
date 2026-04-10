package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.SecurityLevel;
import com.pch.mng.label.Label;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectComponent;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.user.UserAccount;
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
class IssueResponseMappingTest {

    @Mock
    private Issue issue;

    @Mock
    private Project project;

    @Mock
    private UserAccount assignee;

    @Mock
    private UserAccount reporter;

    @Mock
    private Issue parent;

    @Mock
    private Sprint sprint;

    @Test
    @DisplayName("MinDTO는 담당자 이름을 매핑한다")
    void minDtoMapsAssigneeName() {
        when(issue.getId()).thenReturn(10L);
        when(issue.getIssueKey()).thenReturn("TST-1");
        when(issue.getIssueType()).thenReturn(IssueType.TASK);
        when(issue.getSummary()).thenReturn("s");
        when(issue.getStatus()).thenReturn(IssueStatus.BACKLOG);
        when(issue.getPriority()).thenReturn(Priority.MEDIUM);
        when(issue.getStoryPoints()).thenReturn(3);
        when(issue.getAssignee()).thenReturn(assignee);
        when(assignee.getName()).thenReturn("Kim");

        IssueResponse.MinDTO dto = IssueResponse.MinDTO.of(issue);
        assertThat(dto.getIssueKey()).isEqualTo("TST-1");
        assertThat(dto.getAssigneeName()).isEqualTo("Kim");
    }

    @Test
    @DisplayName("DetailDTO는 프로젝트·부모·스프린트·라벨·컴포넌트를 매핑한다")
    void detailDtoMapsNestedAndCollections() {
        when(issue.getId()).thenReturn(1L);
        when(issue.getIssueKey()).thenReturn("P-9");
        when(issue.getIssueType()).thenReturn(IssueType.BUG);
        when(issue.getSummary()).thenReturn("bug");
        when(issue.getDescription()).thenReturn("d");
        when(issue.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
        when(issue.getPriority()).thenReturn(Priority.HIGH);
        when(issue.getStoryPoints()).thenReturn(null);
        when(issue.getSecurityLevel()).thenReturn(SecurityLevel.INTERNAL);
        LocalDateTime now = LocalDateTime.of(2026, 4, 10, 12, 0);
        when(issue.getCreatedAt()).thenReturn(now);
        when(issue.getUpdatedAt()).thenReturn(now);

        when(issue.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(100L);
        when(project.getKey()).thenReturn("P");

        when(issue.getAssignee()).thenReturn(assignee);
        when(assignee.getId()).thenReturn(20L);
        when(assignee.getName()).thenReturn("A");

        when(issue.getReporter()).thenReturn(reporter);
        when(reporter.getId()).thenReturn(30L);
        when(reporter.getName()).thenReturn("R");

        when(issue.getParent()).thenReturn(parent);
        when(parent.getId()).thenReturn(2L);
        when(parent.getIssueKey()).thenReturn("P-1");

        when(issue.getSprint()).thenReturn(sprint);
        when(sprint.getId()).thenReturn(50L);

        Label label = Label.builder().name("backend").build();
        org.springframework.test.util.ReflectionTestUtils.setField(label, "id", 7L);
        IssueLabel il = new IssueLabel();
        il.setLabel(label);

        ProjectComponent comp = ProjectComponent.builder().project(project).name("api").build();
        org.springframework.test.util.ReflectionTestUtils.setField(comp, "id", 8L);
        IssueComponent ic = new IssueComponent();
        ic.setComponent(comp);

        IssueResponse.DetailDTO dto = IssueResponse.DetailDTO.of(issue, List.of(il), List.of(ic));

        assertThat(dto.getProjectKey()).isEqualTo("P");
        assertThat(dto.getParentKey()).isEqualTo("P-1");
        assertThat(dto.getSprintId()).isEqualTo(50L);
        assertThat(dto.getLabels()).hasSize(1);
        assertThat(dto.getLabels().getFirst().getName()).isEqualTo("backend");
        assertThat(dto.getComponents()).hasSize(1);
        assertThat(dto.getComponents().getFirst().getName()).isEqualTo("api");
    }
}
