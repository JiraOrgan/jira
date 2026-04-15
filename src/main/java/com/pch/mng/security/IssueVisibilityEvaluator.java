package com.pch.mng.security;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueSecurityPolicy;
import com.pch.mng.project.ProjectMember;
import com.pch.mng.project.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 현재 인증 사용자 기준 이슈 가시성(FR-031) 평가.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueVisibilityEvaluator {

    private final ProjectMemberRepository projectMemberRepository;

    public record MemberViewContext(long userId, ProjectRole role) {
    }

    public Optional<MemberViewContext> contextForProject(long projectId) {
        Long uid = currentUserId();
        if (uid == null) {
            return Optional.empty();
        }
        return projectMemberRepository.findByProjectIdAndUserId(projectId, uid)
                .map(ProjectMember::getRole)
                .map(role -> new MemberViewContext(uid, role));
    }

    public MemberViewContext requiredContextForProject(long projectId) {
        return contextForProject(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
    }

    public boolean canView(Issue issue) {
        if (issue == null || issue.getProject() == null) {
            return false;
        }
        return contextForProject(issue.getProject().getId())
                .map(ctx -> IssueSecurityPolicy.canView(issue, ctx.role(), ctx.userId()))
                .orElse(false);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cd)) {
            return null;
        }
        return cd.getId();
    }
}
