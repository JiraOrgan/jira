package com.pch.mng.jql;

import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.global.enums.SecurityLevel;
import com.pch.mng.issue.QIssue;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * JQL(QueryDSL)용 FR-031 가시성 술어. {@link JqlQueryTranslator#tautology}와 동일하게 항등식으로 TRUE를 표현한다.
 */
public final class JqlIssueVisibility {

    private JqlIssueVisibility() {
    }

    public static BooleanExpression visibleTo(QIssue issue, Long userId, ProjectRole role) {
        if (role == null || userId == null) {
            return issue.id.ne(issue.id);
        }
        BooleanExpression isPublic = issue.securityLevel.isNull().or(issue.securityLevel.eq(SecurityLevel.PUBLIC));
        return switch (role) {
            case VIEWER -> isPublic;
            case REPORTER -> {
                BooleanExpression confidentialOk = issue.securityLevel.eq(SecurityLevel.CONFIDENTIAL)
                        .and(issue.reporter.id.eq(userId)
                                .or(issue.assignee.isNotNull().and(issue.assignee.id.eq(userId))));
                yield isPublic.or(issue.securityLevel.eq(SecurityLevel.INTERNAL)).or(confidentialOk);
            }
            default -> issue.id.eq(issue.id);
        };
    }
}
