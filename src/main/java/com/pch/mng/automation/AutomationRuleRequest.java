package com.pch.mng.automation;

import com.pch.mng.global.enums.AutomationActionType;
import com.pch.mng.global.enums.AutomationTriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AutomationRuleRequest {

    @Data
    public static class SaveDTO {
        @NotBlank
        @Size(max = 200)
        private String name;

        private Boolean enabled = Boolean.TRUE;

        @NotNull
        private AutomationTriggerType triggerType;

        /** JSON 문자열 또는 null */
        private String conditionJson;

        @NotNull
        private AutomationActionType actionType;

        private String actionJson;

        private Integer sortOrder;
    }

    @Data
    public static class UpdateDTO {
        @Size(max = 200)
        private String name;

        private Boolean enabled;

        private AutomationTriggerType triggerType;

        private String conditionJson;

        private AutomationActionType actionType;

        private String actionJson;

        private Integer sortOrder;
    }
}
