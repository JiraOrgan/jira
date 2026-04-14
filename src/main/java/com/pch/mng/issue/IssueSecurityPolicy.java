package com.pch.mng.issue;

import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.enums.SecurityLevel;

import java.util.Objects;

/**
 * FR-031 이슈 보안 레벨(Public / Internal / Confidential) 조회·설정 규칙.
 * {@code null} 레벨은 조회 정책상 {@link SecurityLevel#PUBLIC}과 동일하게 취급한다.
 */
public final class IssueSecurityPolicy {

    private IssueSecurityPolicy() {
    }

    public static SecurityLevel effectiveLevel(SecurityLevel level) {
        return level == null ? SecurityLevel.PUBLIC : level;
    }

    public static boolean canView(Issue issue, ProjectRole role, Long userId) {
        if (issue == null || role == null || userId == null) {
            return false;
        }
        return switch (effectiveLevel(issue.getSecurityLevel())) {
            case PUBLIC -> true;
            case INTERNAL -> role != ProjectRole.VIEWER;
            case CONFIDENTIAL -> switch (role) {
                case ADMIN, DEVELOPER, QA -> true;
                case REPORTER -> Objects.equals(userId, issue.getReporter().getId())
                        || (issue.getAssignee() != null && Objects.equals(userId, issue.getAssignee().getId()));
                case VIEWER -> false;
            };
        };
    }

    /**
     * 이슈 생성·수정 시 요청 보안 레벨 설정 가능 여부.
     * Reporter는 Confidential로 올릴 수 없다(ADMIN/DEVELOPER/QA만).
     */
    public static boolean canSetSecurityLevel(ProjectRole role, SecurityLevel newLevel) {
        if (role == null || newLevel == null) {
            return false;
        }
        if (role == ProjectRole.VIEWER) {
            return false;
        }
        if (newLevel == SecurityLevel.CONFIDENTIAL && role == ProjectRole.REPORTER) {
            return false;
        }
        return true;
    }
}
