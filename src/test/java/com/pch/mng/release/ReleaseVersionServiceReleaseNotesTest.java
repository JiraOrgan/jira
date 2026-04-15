package com.pch.mng.release;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.VersionStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueFixVersion;
import com.pch.mng.issue.IssueFixVersionRepository;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseVersionServiceReleaseNotesTest {

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private IssueFixVersionRepository issueFixVersionRepository;

    @InjectMocks
    private ReleaseVersionService releaseVersionService;

    @Test
    @DisplayName("generateReleaseNotes: 버전 없으면 ENTITY_NOT_FOUND")
    void notFound() {
        when(releaseVersionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> releaseVersionService.generateReleaseNotes(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("generateReleaseNotes: 연결 이슈 없을 때 안내 문구")
    void emptyIssues() {
        ReleaseVersion version = ReleaseVersion.builder()
                .project(mock(Project.class))
                .name("1.0.0")
                .description(null)
                .releaseDate(null)
                .status(VersionStatus.UNRELEASED)
                .build();
        when(releaseVersionRepository.findById(5L)).thenReturn(Optional.of(version));
        when(issueFixVersionRepository.findByVersionIdWithIssuesOrderByIssueKey(5L)).thenReturn(List.of());

        ReleaseNotesResponse.DTO dto = releaseVersionService.generateReleaseNotes(5L);

        assertThat(dto.getIssueCount()).isZero();
        assertThat(dto.getMarkdown()).contains("연결된 이슈가 없습니다");
    }

    @Test
    @DisplayName("generateReleaseNotes: 타입별 마크다운 섹션")
    void markdownGroupedByType() {
        Project project = mock(Project.class);
        ReleaseVersion version = ReleaseVersion.builder()
                .project(project)
                .name("2.1.0")
                .description(null)
                .releaseDate(null)
                .status(VersionStatus.UNRELEASED)
                .build();
        when(releaseVersionRepository.findById(9L)).thenReturn(Optional.of(version));

        Issue bug = mock(Issue.class);
        when(bug.getIssueKey()).thenReturn("DEMO-2");
        when(bug.getSummary()).thenReturn("로그인 오류");
        when(bug.getIssueType()).thenReturn(IssueType.BUG);
        when(bug.getStatus()).thenReturn(IssueStatus.DONE);

        Issue story = mock(Issue.class);
        when(story.getIssueKey()).thenReturn("DEMO-1");
        when(story.getSummary()).thenReturn("대시보드");
        when(story.getIssueType()).thenReturn(IssueType.STORY);
        when(story.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        IssueFixVersion l1 = mock(IssueFixVersion.class);
        when(l1.getIssue()).thenReturn(bug);
        IssueFixVersion l2 = mock(IssueFixVersion.class);
        when(l2.getIssue()).thenReturn(story);

        when(issueFixVersionRepository.findByVersionIdWithIssuesOrderByIssueKey(9L)).thenReturn(List.of(l1, l2));

        ReleaseNotesResponse.DTO dto = releaseVersionService.generateReleaseNotes(9L);

        assertThat(dto.getIssueCount()).isEqualTo(2);
        assertThat(dto.getIssues()).hasSize(2);
        assertThat(dto.getMarkdown())
                .contains("## 스토리")
                .contains("## 버그")
                .contains("DEMO-1")
                .contains("DEMO-2")
                .contains("릴리즈 노트 — 2.1.0");
    }
}
