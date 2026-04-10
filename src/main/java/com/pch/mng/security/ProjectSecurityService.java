package com.pch.mng.security;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.attachment.AttachmentRepository;
import com.pch.mng.comment.CommentRepository;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueLinkRepository;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.project.ProjectMember;
import com.pch.mng.project.ProjectMemberRepository;
import com.pch.mng.release.ReleaseVersionRepository;
import com.pch.mng.sprint.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 프로젝트 멤버십 기반 RBAC (FR-030). SpEL에서 {@code @projectSecurity} 빈 이름으로 참조한다.
 */
@Service("projectSecurity")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectSecurityService {

    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final IssueRepository issueRepository;
    private final IssueLinkRepository issueLinkRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cd)) {
            return null;
        }
        return cd.getId();
    }

    private Optional<ProjectRole> roleOnProject(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return Optional.empty();
        }
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId).map(ProjectMember::getRole);
    }

    private boolean hasAnyRole(Long projectId, ProjectRole... roles) {
        Long uid = currentUserId();
        if (uid == null) {
            return false;
        }
        return roleOnProject(projectId, uid)
                .map(r -> Arrays.stream(roles).anyMatch(x -> x == r))
                .orElse(false);
    }

    public boolean isMember(Long projectId) {
        Long uid = currentUserId();
        if (uid == null || projectId == null) {
            return false;
        }
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, uid);
    }

    /** 프로젝트 설정·멤버 관리·삭제 (ADMIN 전용) */
    public boolean isProjectAdmin(Long projectId) {
        return hasAnyRole(projectId, ProjectRole.ADMIN);
    }

    /** 스프린트 생성/시작/완료/삭제 */
    public boolean canManageSprintOnProject(Long projectId) {
        return hasAnyRole(projectId, ProjectRole.ADMIN, ProjectRole.DEVELOPER);
    }

    public boolean canManageSprint(Long sprintId) {
        if (sprintId == null) {
            return false;
        }
        return sprintRepository.findByIdWithProject(sprintId)
                .map(s -> canManageSprintOnProject(s.getProject().getId()))
                .orElse(false);
    }

    public boolean canReadSprint(Long sprintId) {
        if (sprintId == null) {
            return false;
        }
        return sprintRepository.findByIdWithProject(sprintId)
                .map(s -> isMember(s.getProject().getId()))
                .orElse(false);
    }

    /** 릴리즈/버전 쓰기 (ADMIN 전용) */
    public boolean canManageReleaseOnProject(Long projectId) {
        return hasAnyRole(projectId, ProjectRole.ADMIN);
    }

    public boolean canManageRelease(Long versionId) {
        if (versionId == null) {
            return false;
        }
        return releaseVersionRepository.findByIdWithProject(versionId)
                .map(v -> canManageReleaseOnProject(v.getProject().getId()))
                .orElse(false);
    }

    public boolean canReadRelease(Long versionId) {
        if (versionId == null) {
            return false;
        }
        return releaseVersionRepository.findByIdWithProject(versionId)
                .map(v -> isMember(v.getProject().getId()))
                .orElse(false);
    }

    public boolean canReadIssue(String issueKey) {
        if (issueKey == null) {
            return false;
        }
        return issueRepository.findByIssueKeyWithProject(issueKey)
                .map(i -> isMember(i.getProject().getId()))
                .orElse(false);
    }

    public boolean canCreateIssue(Long projectId) {
        return hasAnyRole(projectId, ProjectRole.ADMIN, ProjectRole.DEVELOPER, ProjectRole.QA, ProjectRole.REPORTER);
    }

    public boolean canUpdateIssue(String issueKey) {
        if (issueKey == null) {
            return false;
        }
        Optional<Issue> opt = issueRepository.findByIssueKeyWithProject(issueKey);
        if (opt.isEmpty()) {
            return false;
        }
        Issue issue = opt.get();
        Long projectId = issue.getProject().getId();
        Long uid = currentUserId();
        if (uid == null) {
            return false;
        }
        Optional<ProjectRole> role = roleOnProject(projectId, uid);
        if (role.isEmpty()) {
            return false;
        }
        return switch (role.get()) {
            case VIEWER -> false;
            case REPORTER -> Objects.equals(issue.getReporter().getId(), uid)
                    || (issue.getAssignee() != null && Objects.equals(issue.getAssignee().getId(), uid));
            case QA, DEVELOPER, ADMIN -> true;
        };
    }

    public boolean canDeleteIssue(String issueKey) {
        if (issueKey == null) {
            return false;
        }
        return issueRepository.findByIssueKeyWithProject(issueKey)
                .map(i -> hasAnyRole(i.getProject().getId(), ProjectRole.ADMIN, ProjectRole.DEVELOPER))
                .orElse(false);
    }

    public boolean canTransitionIssue(String issueKey) {
        if (issueKey == null) {
            return false;
        }
        return issueRepository.findByIssueKeyWithProject(issueKey)
                .map(i -> hasAnyRole(i.getProject().getId(), ProjectRole.ADMIN, ProjectRole.DEVELOPER, ProjectRole.QA))
                .orElse(false);
    }

    public boolean canReadIssueById(Long issueId) {
        if (issueId == null) {
            return false;
        }
        return issueRepository.findByIdWithProject(issueId)
                .map(i -> isMember(i.getProject().getId()))
                .orElse(false);
    }

    /** 댓글 작성: VIEWER 제외 멤버 */
    public boolean canCommentOnIssue(Long issueId) {
        if (issueId == null) {
            return false;
        }
        return issueRepository.findByIdWithProject(issueId)
                .map(i -> hasAnyRole(i.getProject().getId(), ProjectRole.ADMIN, ProjectRole.DEVELOPER,
                        ProjectRole.QA, ProjectRole.REPORTER))
                .orElse(false);
    }

    /**
     * 이슈 링크 수정/삭제: 소스 또는 타겟 이슈 중 하나라도 {@link #canUpdateIssue(String)}를 만족하면 허용.
     */
    public boolean canModifyIssueLink(Long linkId) {
        if (linkId == null) {
            return false;
        }
        return issueLinkRepository.findByIdWithIssues(linkId)
                .map(link -> canUpdateIssue(link.getSourceIssue().getIssueKey())
                        || canUpdateIssue(link.getTargetIssue().getIssueKey()))
                .orElse(false);
    }

    public boolean canModifyComment(Long commentId) {
        if (commentId == null) {
            return false;
        }
        Long uid = currentUserId();
        if (uid == null) {
            return false;
        }
        return commentRepository.findIssueProjectIdByCommentId(commentId)
                .map(pid -> hasAnyRole(pid, ProjectRole.ADMIN, ProjectRole.DEVELOPER)
                        || commentRepository.existsByIdAndAuthor_Id(commentId, uid))
                .orElse(false);
    }

    /** 감사 로그: 프로젝트 ADMIN만 (FR-028 매트릭스) */
    public boolean canViewAuditForIssue(Long issueId) {
        if (issueId == null) {
            return false;
        }
        return issueRepository.findByIdWithProject(issueId)
                .map(i -> isProjectAdmin(i.getProject().getId()))
                .orElse(false);
    }

    public boolean canReadAttachment(Long attachmentId) {
        if (attachmentId == null) {
            return false;
        }
        return attachmentRepository.findIssueKeyByAttachmentId(attachmentId)
                .map(this::canReadIssue)
                .orElse(false);
    }

    public boolean canDeleteAttachment(Long attachmentId) {
        if (attachmentId == null) {
            return false;
        }
        return attachmentRepository.findIssueKeyByAttachmentId(attachmentId)
                .map(this::canUpdateIssue)
                .orElse(false);
    }
}
