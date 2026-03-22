package com.jira.mng.workflow;

import com.jira.mng.global.enums.IssueStatus;
import lombok.Data;
import java.time.LocalDateTime;

public class WorkflowTransitionResponse {

    @Data
    public static class DetailDTO {
        private Long id;
        private Long issueId;
        private IssueStatus fromStatus;
        private IssueStatus toStatus;
        private String changedByName;
        private String conditionNote;
        private LocalDateTime transitionedAt;

        private DetailDTO() {}

        public static DetailDTO of(WorkflowTransition transition) {
            DetailDTO dto = new DetailDTO();
            dto.id = transition.getId();
            dto.fromStatus = transition.getFromStatus();
            dto.toStatus = transition.getToStatus();
            dto.conditionNote = transition.getConditionNote();
            dto.transitionedAt = transition.getTransitionedAt();
            if (transition.getIssue() != null) {
                dto.issueId = transition.getIssue().getId();
            }
            if (transition.getChangedBy() != null) {
                dto.changedByName = transition.getChangedBy().getName();
            }
            return dto;
        }
    }
}
