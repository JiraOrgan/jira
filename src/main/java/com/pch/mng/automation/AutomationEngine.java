package com.pch.mng.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.audit.IssueAuditService;
import com.pch.mng.board.SprintBoardRedisCache;
import com.pch.mng.global.enums.AutomationActionType;
import com.pch.mng.global.enums.AutomationTriggerType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.user.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * FR-015 MVP: 프로젝트별 규칙을 평가하고 액션을 실행하며 {@link AutomationExecutionLog}에 남긴다.
 * 이슈 서비스에서 호출되므로 동일 트랜잭션에서 동작한다.
 */
@Service
@RequiredArgsConstructor
public class AutomationEngine {

    private static final int MSG_MAX = 1900;

    private final AutomationRuleRepository ruleRepository;
    private final AutomationExecutionLogRepository executionLogRepository;
    private final IssueRepository issueRepository;
    private final IssueAuditService issueAuditService;
    private final SprintBoardRedisCache sprintBoardRedisCache;
    private final ObjectMapper objectMapper;

    public void validatePayload(
            AutomationTriggerType triggerType,
            AutomationActionType actionType,
            String conditionJson,
            String actionJson) {
        parseCondition(triggerType, conditionJson);
        parseActionRequirement(actionType, actionJson);
    }

    public void onIssueCreated(Issue issue, UserAccount actor) {
        if (issue == null || issue.isArchived()) {
            return;
        }
        runMatchingRules(AutomationTriggerType.ISSUE_CREATED, issue, actor, null, null);
    }

    public void onIssueStatusChanged(Issue issue, IssueStatus from, IssueStatus to, UserAccount actor) {
        if (issue == null || issue.isArchived()) {
            return;
        }
        runMatchingRules(AutomationTriggerType.ISSUE_STATUS_CHANGED, issue, actor, from, to);
    }

    private void runMatchingRules(
            AutomationTriggerType trigger,
            Issue issue,
            UserAccount actor,
            IssueStatus from,
            IssueStatus to) {
        Long projectId = issue.getProject().getId();
        List<AutomationRule> rules =
                ruleRepository.findEnabledByProjectIdOrderBySortOrderAscIdAsc(projectId);
        for (AutomationRule rule : rules) {
            if (rule.getTriggerType() != trigger) {
                continue;
            }
            JsonNode condition = parseCondition(rule.getTriggerType(), rule.getConditionJson());
            if (!matchesCondition(condition, issue, from, to, trigger)) {
                continue;
            }
            try {
                String summary = applyAction(rule, issue, actor);
                issueRepository.save(issue);
                evictBoardIfNeeded(issue);
                saveLog(rule, issue, true, truncate(summary));
            } catch (Exception ex) {
                saveLog(rule, issue, false, truncate(ex.getMessage()));
            }
        }
    }

    private void evictBoardIfNeeded(Issue issue) {
        if (issue.getSprint() != null) {
            sprintBoardRedisCache.evictSprint(issue.getSprint().getId());
        }
    }

    private void saveLog(AutomationRule rule, Issue issue, boolean success, String message) {
        executionLogRepository.save(
                AutomationExecutionLog.builder()
                        .rule(rule)
                        .issue(issue)
                        .success(success)
                        .message(message)
                        .build());
    }

    private String applyAction(AutomationRule rule, Issue issue, UserAccount actor) {
        return switch (rule.getActionType()) {
            case SET_PRIORITY -> applySetPriority(issue, actor, rule.getActionJson());
            case ASSIGN_TO_REPORTER -> applyAssignToReporter(issue, actor);
            case UNASSIGN -> applyUnassign(issue, actor);
        };
    }

    private String applySetPriority(Issue issue, UserAccount actor, String actionJson) {
        JsonNode root = readJsonObject(actionJson, false);
        if (!root.hasNonNull("priority")) {
            throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
        }
        Priority p;
        try {
            p = Priority.valueOf(root.get("priority").asText().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
        }
        Priority old = issue.getPriority();
        if (old == p) {
            return "SET_PRIORITY noop (already " + p + ")";
        }
        issue.setPriority(p);
        issueAuditService.record(issue, actor, "priority", String.valueOf(old), String.valueOf(p));
        return "SET_PRIORITY " + old + " -> " + p;
    }

    private String applyAssignToReporter(Issue issue, UserAccount actor) {
        UserAccount reporter = issue.getReporter();
        if (reporter == null) {
            return "ASSIGN_TO_REPORTER skipped (no reporter)";
        }
        Long oldId = issue.getAssignee() != null ? issue.getAssignee().getId() : null;
        Long newId = reporter.getId();
        if (oldId != null && oldId.equals(newId)) {
            return "ASSIGN_TO_REPORTER noop";
        }
        issue.setAssignee(reporter);
        issueAuditService.record(
                issue,
                actor,
                "assigneeId",
                oldId != null ? String.valueOf(oldId) : null,
                String.valueOf(newId));
        return "ASSIGN_TO_REPORTER -> user " + newId;
    }

    private String applyUnassign(Issue issue, UserAccount actor) {
        if (issue.getAssignee() == null) {
            return "UNASSIGN noop";
        }
        Long oldId = issue.getAssignee().getId();
        issue.setAssignee(null);
        issueAuditService.record(issue, actor, "assigneeId", String.valueOf(oldId), null);
        return "UNASSIGN";
    }

    private boolean matchesCondition(
            JsonNode condition, Issue issue, IssueStatus from, IssueStatus to, AutomationTriggerType trigger) {
        if (condition == null || condition.isNull() || !condition.isObject()) {
            return true;
        }
        if (condition.hasNonNull("issueTypes") && condition.get("issueTypes").isArray()) {
            JsonNode arr = condition.get("issueTypes");
            if (arr.isEmpty()) {
                return matchesStatusPortion(condition, issue, from, to, trigger);
            }
            for (JsonNode n : arr) {
                if (n.isTextual()) {
                    try {
                        IssueType t = IssueType.valueOf(n.asText().trim().toUpperCase());
                        if (t == issue.getIssueType()) {
                            return matchesStatusPortion(condition, issue, from, to, trigger);
                        }
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return false;
        }
        return matchesStatusPortion(condition, issue, from, to, trigger);
    }

    private boolean matchesStatusPortion(
            JsonNode condition, Issue issue, IssueStatus from, IssueStatus to, AutomationTriggerType trigger) {
        if (trigger == AutomationTriggerType.ISSUE_CREATED) {
            if (condition.hasNonNull("status")) {
                try {
                    IssueStatus expected = IssueStatus.valueOf(condition.get("status").asText().trim().toUpperCase());
                    if (issue.getStatus() != expected) {
                        return false;
                    }
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
            return true;
        }
        if (from == null || to == null) {
            return false;
        }
        if (condition.hasNonNull("fromStatus")) {
            try {
                IssueStatus f = IssueStatus.valueOf(condition.get("fromStatus").asText().trim().toUpperCase());
                if (f != from) {
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        if (condition.hasNonNull("toStatus")) {
            try {
                IssueStatus t = IssueStatus.valueOf(condition.get("toStatus").asText().trim().toUpperCase());
                if (t != to) {
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return true;
    }

    private JsonNode parseCondition(AutomationTriggerType triggerType, String conditionJson) {
        if (!StringUtils.hasText(conditionJson)) {
            return objectMapper.nullNode();
        }
        try {
            JsonNode n = objectMapper.readTree(conditionJson);
            if (!n.isObject()) {
                throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
            }
            if (n.has("issueTypes") && !n.get("issueTypes").isArray()) {
                throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
            }
            if (triggerType == AutomationTriggerType.ISSUE_STATUS_CHANGED) {
                if (n.has("fromStatus") && !n.get("fromStatus").isTextual()) {
                    throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
                }
                if (n.has("toStatus") && !n.get("toStatus").isTextual()) {
                    throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
                }
            }
            return n;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
        }
    }

    private void parseActionRequirement(AutomationActionType actionType, String actionJson) {
        switch (actionType) {
            case SET_PRIORITY -> {
                JsonNode root = readJsonObject(actionJson, false);
                if (!root.hasNonNull("priority")) {
                    throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
                }
                try {
                    Priority.valueOf(root.get("priority").asText().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
                }
            }
            case ASSIGN_TO_REPORTER, UNASSIGN -> {
                if (StringUtils.hasText(actionJson)) {
                    JsonNode n = readJsonObject(actionJson, false);
                    Iterator<String> it = n.fieldNames();
                    if (it.hasNext()) {
                        throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
                    }
                }
            }
        }
    }

    private JsonNode readJsonObject(String json, boolean allowEmptyAsEmptyObject) {
        if (!StringUtils.hasText(json)) {
            if (allowEmptyAsEmptyObject) {
                return objectMapper.createObjectNode();
            }
            throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
        }
        try {
            JsonNode n = objectMapper.readTree(json);
            if (!n.isObject()) {
                throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
            }
            return n;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTOMATION_INVALID_SPEC);
        }
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= MSG_MAX) {
            return s;
        }
        return s.substring(0, MSG_MAX) + "…";
    }
}
