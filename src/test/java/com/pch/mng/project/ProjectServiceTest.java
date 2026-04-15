package com.pch.mng.project;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private WipLimitRepository wipLimitRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("findById: 없으면 ENTITY_NOT_FOUND")
    void findByIdMissing() {
        when(projectRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.findById(9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("findById: 프로젝트 반환")
    void findByIdOk() {
        Project p = Project.builder()
                .key("K")
                .name("N")
                .boardType(BoardType.SCRUM)
                .build();
        ReflectionTestUtils.setField(p, "id", 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        ProjectResponse.DetailDTO dto = projectService.findById(1L);
        assertThat(dto.getKey()).isEqualTo("K");
    }

    @Test
    @DisplayName("addMember: 이미 멤버면 DUPLICATE_RESOURCE")
    void addMemberDuplicate() {
        ProjectRequest.AddMemberDTO req = new ProjectRequest.AddMemberDTO();
        req.setUserId(3L);
        req.setRole(ProjectRole.DEVELOPER);

        Project p = Project.builder().key("X").name("X").boardType(BoardType.KANBAN).build();
        ReflectionTestUtils.setField(p, "id", 10L);
        UserAccount u = UserAccount.builder().email("m@ex.com").password("x").name("M").build();
        ReflectionTestUtils.setField(u, "id", 3L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(p));
        when(userAccountRepository.findById(3L)).thenReturn(Optional.of(u));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addMember(10L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("removeMember: 프로젝트 불일치면 ENTITY_NOT_FOUND")
    void removeMemberWrongProject() {
        Project p = Project.builder().key("P").name("P").boardType(BoardType.SCRUM).build();
        ReflectionTestUtils.setField(p, "id", 99L);

        ProjectMember member = ProjectMember.builder().project(p).user(new UserAccount()).role(ProjectRole.ADMIN).build();
        ReflectionTestUtils.setField(member, "id", 77L);

        when(projectMemberRepository.findById(77L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> projectService.removeMember(10L, 77L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);

        verify(projectMemberRepository, never()).delete(any(ProjectMember.class));
    }

    @Test
    @DisplayName("findDetailByKeyForUser: 키 없으면 ENTITY_NOT_FOUND")
    void findDetailByKeyMissing() {
        when(projectRepository.findByKey("NOPE")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.findDetailByKeyForUser("NOPE", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("findDetailByKeyForUser: 비멤버면 FORBIDDEN")
    void findDetailByKeyForbidden() {
        Project p = Project.builder()
                .key("K")
                .name("N")
                .boardType(BoardType.SCRUM)
                .archived(false)
                .build();
        ReflectionTestUtils.setField(p, "id", 5L);
        when(projectRepository.findByKey("K")).thenReturn(Optional.of(p));
        when(projectMemberRepository.existsByProjectIdAndUserId(5L, 9L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.findDetailByKeyForUser("K", 9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("findDetailByKeyForUser: 멤버면 상세 반환")
    void findDetailByKeyOk() {
        Project p = Project.builder()
                .key("K")
                .name("N")
                .boardType(BoardType.SCRUM)
                .archived(true)
                .build();
        ReflectionTestUtils.setField(p, "id", 5L);
        when(projectRepository.findByKey("K")).thenReturn(Optional.of(p));
        when(projectMemberRepository.existsByProjectIdAndUserId(5L, 9L)).thenReturn(true);

        assertThat(projectService.findDetailByKeyForUser("K", 9L).isArchived()).isTrue();
    }

    @Test
    @DisplayName("update: archived null이면 플래그 유지")
    void updateArchivedNullKeeps() {
        Project p = Project.builder()
                .key("K")
                .name("Old")
                .boardType(BoardType.SCRUM)
                .archived(true)
                .build();
        ReflectionTestUtils.setField(p, "id", 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        ProjectRequest.UpdateDTO dto = new ProjectRequest.UpdateDTO();
        dto.setName("New");
        dto.setArchived(null);

        projectService.update(1L, dto);
        assertThat(p.isArchived()).isTrue();
        assertThat(p.getName()).isEqualTo("New");
    }

    @Test
    @DisplayName("update: archived false 반영")
    void updateArchivedFalse() {
        Project p = Project.builder()
                .key("K")
                .name("N")
                .boardType(BoardType.SCRUM)
                .archived(true)
                .build();
        ReflectionTestUtils.setField(p, "id", 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        ProjectRequest.UpdateDTO dto = new ProjectRequest.UpdateDTO();
        dto.setName("N");
        dto.setArchived(false);

        projectService.update(1L, dto);
        assertThat(p.isArchived()).isFalse();
    }
}
