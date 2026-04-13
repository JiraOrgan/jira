package com.pch.mng.audit;

import lombok.Data;
import java.time.LocalDateTime;

public class AuditLogResponse {

    @Data
    public static class DetailDTO {
        private Long id;
        private Long issueId;
        /** 프로젝트 감사 목록에서 이슈 식별용 */
        private String issueKey;
        private Long changedById;
        private String changedByName;
        private String fieldName;
        private String oldValue;
        private String newValue;
        private LocalDateTime changedAt;

        private DetailDTO() {}

        public static DetailDTO of(AuditLog log) {
            DetailDTO dto = new DetailDTO();
            dto.id = log.getId();
            dto.fieldName = log.getFieldName();
            dto.oldValue = log.getOldValue();
            dto.newValue = log.getNewValue();
            dto.changedAt = log.getChangedAt();
            if (log.getIssue() != null) {
                dto.issueId = log.getIssue().getId();
                dto.issueKey = log.getIssue().getIssueKey();
            }
            if (log.getChangedBy() != null) {
                dto.changedById = log.getChangedBy().getId();
                dto.changedByName = log.getChangedBy().getName();
            }
            return dto;
        }
    }
}
