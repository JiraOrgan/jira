package com.pch.mng.audit;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.issue.Issue;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 이슈 필드 변경 감사 로그 (FR-028). HTTP 요청 컨텍스트의 인증 사용자를 변경자로 기록한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class IssueAuditService {

    private static final int MAX_VALUE_LEN = 4000;

    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;

    public void record(Issue issue, UserAccount changedBy, String fieldName, String oldValue, String newValue) {
        if (changedBy == null || fieldName == null || fieldName.isBlank()) {
            return;
        }
        String o = normalize(oldValue);
        String n = normalize(newValue);
        if (Objects.equals(o, n)) {
            return;
        }
        auditLogRepository.save(AuditLog.builder()
                .issue(issue)
                .changedBy(changedBy)
                .fieldName(fieldName)
                .oldValue(truncate(o))
                .newValue(truncate(n))
                .build());
    }

    /** {@link SecurityContextHolder}의 로그인 사용자로 기록 (실패 시 무시). */
    public void recordFromContext(Issue issue, String fieldName, String oldValue, String newValue) {
        UserAccount actor = currentUserOrNull();
        if (actor == null) {
            return;
        }
        record(issue, actor, fieldName, oldValue, newValue);
    }

    /** 이슈 삭제 전 감사 행 제거 (FK 제약). */
    public void deleteAllForIssue(Long issueId) {
        if (issueId == null) {
            return;
        }
        auditLogRepository.deleteByIssue_Id(issueId);
    }

    public void logIssueCreated(Issue issue, UserAccount creator) {
        if (creator == null) {
            return;
        }
        record(issue, creator, "issueType", null, String.valueOf(issue.getIssueType()));
        record(issue, creator, "summary", null, issue.getSummary());
        record(issue, creator, "status", null, String.valueOf(issue.getStatus()));
        record(issue, creator, "priority", null, String.valueOf(issue.getPriority()));
        record(issue, creator, "description", null, issue.getDescription());
        if (issue.getStoryPoints() != null) {
            record(issue, creator, "storyPoints", null, String.valueOf(issue.getStoryPoints()));
        }
        if (issue.getAssignee() != null) {
            record(issue, creator, "assigneeId", null, String.valueOf(issue.getAssignee().getId()));
        }
        if (issue.getSprint() != null) {
            record(issue, creator, "sprintId", null, String.valueOf(issue.getSprint().getId()));
        }
        if (issue.getParent() != null) {
            record(issue, creator, "parentId", null, String.valueOf(issue.getParent().getId()));
        }
        if (issue.getSecurityLevel() != null) {
            record(issue, creator, "securityLevel", null, String.valueOf(issue.getSecurityLevel()));
        }
        if (issue.getEpicStartDate() != null) {
            record(issue, creator, "epicStartDate", null, String.valueOf(issue.getEpicStartDate()));
        }
        if (issue.getEpicEndDate() != null) {
            record(issue, creator, "epicEndDate", null, String.valueOf(issue.getEpicEndDate()));
        }
    }

    private static String normalize(String v) {
        return v == null ? "" : v;
    }

    private static String truncate(String v) {
        if (v == null) {
            return null;
        }
        if (v.length() <= MAX_VALUE_LEN) {
            return v;
        }
        return v.substring(0, MAX_VALUE_LEN) + "…";
    }

    private UserAccount currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cd)) {
            return null;
        }
        return userAccountRepository.getReferenceById(cd.getId());
    }
}
