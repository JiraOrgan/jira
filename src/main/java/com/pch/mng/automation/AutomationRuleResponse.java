package com.pch.mng.automation;

import com.pch.mng.global.enums.AutomationActionType;
import com.pch.mng.global.enums.AutomationTriggerType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AutomationRuleResponse {
    Long id;
    Long projectId;
    String name;
    boolean enabled;
    AutomationTriggerType triggerType;
    String conditionJson;
    AutomationActionType actionType;
    String actionJson;
    int sortOrder;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static AutomationRuleResponse of(AutomationRule r) {
        return AutomationRuleResponse.builder()
                .id(r.getId())
                .projectId(r.getProject().getId())
                .name(r.getName())
                .enabled(r.isEnabled())
                .triggerType(r.getTriggerType())
                .conditionJson(r.getConditionJson())
                .actionType(r.getActionType())
                .actionJson(r.getActionJson())
                .sortOrder(r.getSortOrder())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
