package com.pch.mng.automation;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AutomationExecutionLogResponse {
    Long id;
    Long ruleId;
    String ruleName;
    Long issueId;
    String issueKey;
    boolean success;
    String message;
    LocalDateTime executedAt;

    public static AutomationExecutionLogResponse of(AutomationExecutionLog e) {
        return AutomationExecutionLogResponse.builder()
                .id(e.getId())
                .ruleId(e.getRule().getId())
                .ruleName(e.getRule().getName())
                .issueId(e.getIssue().getId())
                .issueKey(e.getIssue().getIssueKey())
                .success(e.isSuccess())
                .message(e.getMessage())
                .executedAt(e.getExecutedAt())
                .build();
    }
}
