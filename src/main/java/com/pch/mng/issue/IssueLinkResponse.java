package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueLinkType;
import lombok.Data;

public class IssueLinkResponse {

    @Data
    public static class DetailDTO {
        private Long id;
        private String sourceIssueKey;
        private String targetIssueKey;
        private IssueLinkType linkType;

        private DetailDTO() {}

        public static DetailDTO of(IssueLink link) {
            DetailDTO dto = new DetailDTO();
            dto.id = link.getId();
            dto.linkType = link.getLinkType();
            if (link.getSourceIssue() != null) {
                dto.sourceIssueKey = link.getSourceIssue().getIssueKey();
            }
            if (link.getTargetIssue() != null) {
                dto.targetIssueKey = link.getTargetIssue().getIssueKey();
            }
            return dto;
        }
    }
}
