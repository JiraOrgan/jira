package com.pch.mng.jql;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.QIssue;
import com.pch.mng.jql.ast.JqlExpression;
import com.pch.mng.jql.ast.JqlField;
import com.pch.mng.jql.ast.JqlOperator;
import com.pch.mng.jql.ast.JqlOrderBy;
import com.pch.mng.jql.ast.JqlOrderField;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * JQL AST → QueryDSL 술어 (프로젝트 스코프는 서비스에서 별도 AND).
 * {@link JqlField#PROJECT} 절은 스코프 검증만 하고 술어에는 포함하지 않는다.
 */
public final class JqlQueryTranslator {

    private JqlQueryTranslator() {}

    /** DB마다 상수 TRUE 표현이 달라질 수 있어 항등 술어로 대체한다. */
    private static BooleanExpression tautology(QIssue issue) {
        return issue.id.eq(issue.id);
    }

    public static void validateProjectClauses(JqlExpression expression, String projectKey) {
        if (expression == null) {
            return;
        }
        walkValidateProject(expression, projectKey);
    }

    private static void walkValidateProject(JqlExpression ex, String projectKey) {
        switch (ex) {
            case JqlExpression.And(var l, var r) -> {
                walkValidateProject(l, projectKey);
                walkValidateProject(r, projectKey);
            }
            case JqlExpression.Or(var l, var r) -> {
                walkValidateProject(l, projectKey);
                walkValidateProject(r, projectKey);
            }
            case JqlExpression.Clause c when c.field() == JqlField.PROJECT -> validateProjectClause(c, projectKey);
            case JqlExpression.Clause ignored -> {
            }
        }
    }

    private static void validateProjectClause(JqlExpression.Clause c, String projectKey) {
        switch (c.operator()) {
            case IS_EMPTY, CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
            case EQ -> {
                if (c.values().size() != 1 || !c.values().get(0).equalsIgnoreCase(projectKey)) {
                    throw new BusinessException(ErrorCode.JQL_PROJECT_KEY_MISMATCH);
                }
            }
            case NE -> { /* 항상 허용 — 단일 프로젝트 스코프 내 의미만 제한 */ }
            case IN -> {
                if (c.values().isEmpty()) {
                    throw new BusinessException(ErrorCode.JQL_INVALID_VALUE);
                }
                for (String v : c.values()) {
                    if (!v.equalsIgnoreCase(projectKey)) {
                        throw new BusinessException(ErrorCode.JQL_PROJECT_KEY_MISMATCH);
                    }
                }
            }
        }
    }

    public static BooleanExpression buildPredicate(JqlExpression expression, QIssue issue) {
        if (expression == null) {
            return tautology(issue);
        }
        return walk(expression, issue);
    }

    private static BooleanExpression walk(JqlExpression ex, QIssue issue) {
        return switch (ex) {
            case JqlExpression.And(var l, var r) -> walk(l, issue).and(walk(r, issue));
            case JqlExpression.Or(var l, var r) -> walk(l, issue).or(walk(r, issue));
            case JqlExpression.Clause c when c.field() == JqlField.PROJECT -> tautology(issue);
            case JqlExpression.Clause c -> clauseNonProject(c, issue);
        };
    }

    private static BooleanExpression clauseNonProject(JqlExpression.Clause c, QIssue issue) {
        return switch (c.field()) {
            case PROJECT -> tautology(issue);
            case STATUS -> statusClause(c, issue);
            case TYPE -> typeClause(c, issue);
            case PRIORITY -> priorityClause(c, issue);
            case ASSIGNEE -> assigneeClause(c, issue);
            case SPRINT -> sprintClause(c, issue);
            case TEXT -> textClause(c, issue);
        };
    }

    private static BooleanExpression statusClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case IS_EMPTY -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
            case EQ -> issue.status.eq(parseEnum(IssueStatus.class, c.values().get(0)));
            case NE -> issue.status.ne(parseEnum(IssueStatus.class, c.values().get(0)));
            case IN -> {
                BooleanExpression acc = null;
                for (String v : c.values()) {
                    IssueStatus s = parseEnum(IssueStatus.class, v);
                    BooleanExpression p = issue.status.eq(s);
                    acc = acc == null ? p : acc.or(p);
                }
                yield acc != null ? acc : throwInvalid();
            }
            case CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
        };
    }

    private static BooleanExpression typeClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case IS_EMPTY, CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
            case EQ -> issue.issueType.eq(parseEnum(IssueType.class, c.values().get(0)));
            case NE -> issue.issueType.ne(parseEnum(IssueType.class, c.values().get(0)));
            case IN -> {
                BooleanExpression acc = null;
                for (String v : c.values()) {
                    IssueType t = parseEnum(IssueType.class, v);
                    BooleanExpression p = issue.issueType.eq(t);
                    acc = acc == null ? p : acc.or(p);
                }
                yield acc != null ? acc : throwInvalid();
            }
        };
    }

    private static BooleanExpression priorityClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case IS_EMPTY, CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
            case EQ -> issue.priority.eq(parseEnum(Priority.class, c.values().get(0)));
            case NE -> issue.priority.ne(parseEnum(Priority.class, c.values().get(0)));
            case IN -> {
                BooleanExpression acc = null;
                for (String v : c.values()) {
                    Priority p = parseEnum(Priority.class, v);
                    BooleanExpression e = issue.priority.eq(p);
                    acc = acc == null ? e : acc.or(e);
                }
                yield acc != null ? acc : throwInvalid();
            }
        };
    }

    private static BooleanExpression assigneeClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case IS_EMPTY -> issue.assignee.isNull();
            case EQ -> assigneeEq(issue, c.values().get(0));
            case NE -> assigneeNe(issue, c.values().get(0));
            case IN -> {
                BooleanExpression acc = null;
                for (String v : c.values()) {
                    BooleanExpression p = assigneeEq(issue, v);
                    acc = acc == null ? p : acc.or(p);
                }
                yield acc != null ? acc : throwInvalid();
            }
            case CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
        };
    }

    private static BooleanExpression assigneeEq(QIssue issue, String raw) {
        String v = raw.trim();
        if (v.chars().allMatch(Character::isDigit)) {
            return issue.assignee.id.eq(Long.parseLong(v));
        }
        if (v.contains("@")) {
            return issue.assignee.email.equalsIgnoreCase(v);
        }
        return issue.assignee.name.eq(v);
    }

    private static BooleanExpression assigneeNe(QIssue issue, String raw) {
        String v = raw.trim();
        if (v.chars().allMatch(Character::isDigit)) {
            long id = Long.parseLong(v);
            return issue.assignee.isNull().or(issue.assignee.id.ne(id));
        }
        if (v.contains("@")) {
            return issue.assignee.isNull()
                    .or(issue.assignee.email.lower().ne(v.toLowerCase(Locale.ROOT)));
        }
        return issue.assignee.isNull().or(issue.assignee.name.ne(v));
    }

    private static BooleanExpression sprintClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case IS_EMPTY -> issue.sprint.isNull();
            case CONTAINS -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
            case EQ -> sprintEq(issue, c.values().get(0));
            case NE -> sprintNe(issue, c.values().get(0));
            case IN -> {
                BooleanExpression acc = null;
                for (String v : c.values()) {
                    BooleanExpression p = sprintEq(issue, v);
                    acc = acc == null ? p : acc.or(p);
                }
                yield acc != null ? acc : throwInvalid();
            }
        };
    }

    private static BooleanExpression sprintEq(QIssue issue, String raw) {
        String v = raw.trim();
        if (v.chars().allMatch(Character::isDigit)) {
            return issue.sprint.id.eq(Long.parseLong(v));
        }
        return issue.sprint.name.eq(v);
    }

    private static BooleanExpression sprintNe(QIssue issue, String raw) {
        String v = raw.trim();
        if (v.chars().allMatch(Character::isDigit)) {
            long id = Long.parseLong(v);
            return issue.sprint.isNull().or(issue.sprint.id.ne(id));
        }
        return issue.sprint.isNull().or(issue.sprint.name.ne(v));
    }

    private static BooleanExpression textClause(JqlExpression.Clause c, QIssue issue) {
        return switch (c.operator()) {
            case CONTAINS -> textContains(issue, c.values().get(0));
            case EQ, NE, IN, IS_EMPTY -> throw new BusinessException(ErrorCode.JQL_UNSUPPORTED_CLAUSE);
        };
    }

    private static BooleanExpression textContains(QIssue issue, String needle) {
        return issue.summary.containsIgnoreCase(needle)
                .or(issue.description.containsIgnoreCase(needle));
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        try {
            String n = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            return Enum.valueOf(type, n);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.JQL_INVALID_VALUE);
        }
    }

    private static BooleanExpression throwInvalid() {
        throw new BusinessException(ErrorCode.JQL_INVALID_VALUE);
    }

    public static OrderSpecifier<?>[] toOrderSpecifiers(List<JqlOrderBy> orderBy, QIssue issue) {
        if (orderBy == null || orderBy.isEmpty()) {
            return new OrderSpecifier<?>[]{issue.updatedAt.desc()};
        }
        List<OrderSpecifier<?>> list = new ArrayList<>();
        for (JqlOrderBy ob : orderBy) {
            Order dir = ob.ascending() ? Order.ASC : Order.DESC;
            OrderSpecifier<?> spec = switch (ob.field()) {
                case KEY -> new OrderSpecifier<>(dir, issue.issueKey);
                case CREATED -> new OrderSpecifier<>(dir, issue.createdAt);
                case UPDATED -> new OrderSpecifier<>(dir, issue.updatedAt);
                case STATUS -> new OrderSpecifier<>(dir, issue.status);
                case TYPE -> new OrderSpecifier<>(dir, issue.issueType);
                case PRIORITY -> new OrderSpecifier<>(dir, issue.priority);
                case PROJECT -> new OrderSpecifier<>(dir, issue.project.key);
                case ASSIGNEE -> new OrderSpecifier<>(dir, issue.assignee.name);
                case SPRINT -> new OrderSpecifier<>(dir, issue.sprint.name);
            };
            list.add(spec);
        }
        return list.toArray(OrderSpecifier[]::new);
    }
}
