package com.pch.mng.project;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserAccountRepository userAccountRepository;

    public List<ProjectResponse.MinDTO> findAllForUser(Long userId) {
        return projectMemberRepository.findByUser_Id(userId).stream()
                .map(ProjectMember::getProject)
                .filter(p -> !p.isArchived())
                .map(ProjectResponse.MinDTO::of)
                .distinct()
                .toList();
    }

    public ProjectResponse.DetailDTO findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return ProjectResponse.DetailDTO.of(project);
    }

    @Transactional
    public ProjectResponse.DetailDTO save(ProjectRequest.SaveDTO reqDTO) {
        if (projectRepository.existsByKey(reqDTO.getKey())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        CustomUserDetails principal = currentUserDetails();
        UserAccount creator = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserAccount lead;
        if (reqDTO.getLeadId() != null) {
            lead = userAccountRepository.findById(reqDTO.getLeadId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        } else {
            lead = creator;
        }

        Project project = Project.builder()
                .key(reqDTO.getKey())
                .name(reqDTO.getName())
                .description(reqDTO.getDescription())
                .boardType(reqDTO.getBoardType())
                .lead(lead)
                .build();
        projectRepository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(creator)
                .role(ProjectRole.ADMIN)
                .build());

        if (!lead.getId().equals(creator.getId())
                && !projectMemberRepository.existsByProjectIdAndUserId(project.getId(), lead.getId())) {
            projectMemberRepository.save(ProjectMember.builder()
                    .project(project)
                    .user(lead)
                    .role(ProjectRole.DEVELOPER)
                    .build());
        }

        return ProjectResponse.DetailDTO.of(project);
    }

    @Transactional
    public ProjectResponse.DetailDTO update(Long id, ProjectRequest.UpdateDTO reqDTO) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        project.setName(reqDTO.getName());
        project.setDescription(reqDTO.getDescription());
        if (reqDTO.getLeadId() != null) {
            UserAccount newLead = userAccountRepository.findById(reqDTO.getLeadId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            project.setLead(newLead);
        }
        return ProjectResponse.DetailDTO.of(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        projectRepository.delete(project);
    }

    // -- 멤버 관리 --

    public List<ProjectResponse.MemberDTO> findMembers(Long projectId) {
        return projectMemberRepository.findByProjectIdWithUser(projectId).stream()
                .map(ProjectResponse.MemberDTO::of)
                .toList();
    }

    @Transactional
    public ProjectResponse.MemberDTO addMember(Long projectId, ProjectRequest.AddMemberDTO reqDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount user = userAccountRepository.findById(reqDTO.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, reqDTO.getUserId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(reqDTO.getRole())
                .build();
        projectMemberRepository.save(member);
        return ProjectResponse.MemberDTO.of(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!member.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        projectMemberRepository.delete(member);
    }

    private static CustomUserDetails currentUserDetails() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cd)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return cd;
    }
}
