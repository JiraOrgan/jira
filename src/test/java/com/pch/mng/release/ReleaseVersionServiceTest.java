package com.pch.mng.release;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.VersionStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.IssueFixVersionRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseVersionServiceTest {

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private IssueFixVersionRepository issueFixVersionRepository;

    @InjectMocks
    private ReleaseVersionService releaseVersionService;

    @Test
    @DisplayName("findByProject: MinDTO 목록 매핑")
    void findByProject() {
        Project p = Project.builder().key("K").name("N").boardType(BoardType.SCRUM).build();
        ReleaseVersion v = ReleaseVersion.builder()
                .project(p)
                .name("1.0.0")
                .status(VersionStatus.UNRELEASED)
                .build();
        ReflectionTestUtils.setField(v, "id", 3L);
        when(releaseVersionRepository.findByProjectIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(v));

        List<ReleaseVersionResponse.MinDTO> list = releaseVersionService.findByProject(10L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(3L);
        assertThat(list.get(0).getName()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("findById: 없으면 ENTITY_NOT_FOUND")
    void findByIdMissing() {
        when(releaseVersionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> releaseVersionService.findById(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("findById: 상세 반환")
    void findByIdOk() {
        Project p = Project.builder().key("K").name("N").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 5L);
        ReleaseVersion v = ReleaseVersion.builder()
                .project(p)
                .name("2.0.0")
                .description("d")
                .status(VersionStatus.RELEASED)
                .build();
        ReflectionTestUtils.setField(v, "id", 8L);
        when(releaseVersionRepository.findById(8L)).thenReturn(Optional.of(v));

        ReleaseVersionResponse.DetailDTO dto = releaseVersionService.findById(8L);

        assertThat(dto.getId()).isEqualTo(8L);
        assertThat(dto.getProjectId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("2.0.0");
        assertThat(dto.getStatus()).isEqualTo(VersionStatus.RELEASED);
    }

    @Test
    @DisplayName("save: 프로젝트 없으면 ENTITY_NOT_FOUND")
    void saveProjectMissing() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        ReleaseVersionRequest.SaveDTO req = new ReleaseVersionRequest.SaveDTO();
        req.setProjectId(1L);
        req.setName("v");
        assertThatThrownBy(() -> releaseVersionService.save(req))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("save: UNRELEASED로 저장")
    void saveOk() {
        Project p = Project.builder().key("K").name("N").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 2L);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(p));
        when(releaseVersionRepository.save(any(ReleaseVersion.class))).thenAnswer(inv -> {
            ReleaseVersion v = inv.getArgument(0);
            ReflectionTestUtils.setField(v, "id", 100L);
            return v;
        });

        ReleaseVersionRequest.SaveDTO req = new ReleaseVersionRequest.SaveDTO();
        req.setProjectId(2L);
        req.setName("1.2.3");
        req.setDescription("notes");
        req.setReleaseDate(LocalDate.of(2026, 5, 1));

        ReleaseVersionResponse.DetailDTO dto = releaseVersionService.save(req);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getName()).isEqualTo("1.2.3");
        assertThat(dto.getStatus()).isEqualTo(VersionStatus.UNRELEASED);

        ArgumentCaptor<ReleaseVersion> cap = ArgumentCaptor.forClass(ReleaseVersion.class);
        verify(releaseVersionRepository).save(cap.capture());
        assertThat(cap.getValue().getProject()).isSameAs(p);
        assertThat(cap.getValue().getDescription()).isEqualTo("notes");
        assertThat(cap.getValue().getReleaseDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    @DisplayName("release: 없으면 ENTITY_NOT_FOUND")
    void releaseMissing() {
        when(releaseVersionRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> releaseVersionService.release(9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("release: 상태를 RELEASED로")
    void releaseOk() {
        ReleaseVersion v = ReleaseVersion.builder()
                .project(Project.builder().key("K").name("N").boardType(BoardType.SCRUM).build())
                .name("1.0.0")
                .status(VersionStatus.UNRELEASED)
                .build();
        when(releaseVersionRepository.findById(4L)).thenReturn(Optional.of(v));

        ReleaseVersionResponse.DetailDTO dto = releaseVersionService.release(4L);

        assertThat(dto.getStatus()).isEqualTo(VersionStatus.RELEASED);
        assertThat(v.getStatus()).isEqualTo(VersionStatus.RELEASED);
    }

    @Test
    @DisplayName("delete: 없으면 ENTITY_NOT_FOUND")
    void deleteMissing() {
        when(releaseVersionRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> releaseVersionService.delete(9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("delete: 저장소에 삭제 위임")
    void deleteOk() {
        ReleaseVersion v = ReleaseVersion.builder()
                .project(Project.builder().key("K").name("N").boardType(BoardType.SCRUM).build())
                .name("x")
                .status(VersionStatus.UNRELEASED)
                .build();
        when(releaseVersionRepository.findById(6L)).thenReturn(Optional.of(v));

        releaseVersionService.delete(6L);

        verify(releaseVersionRepository).delete(v);
    }
}
