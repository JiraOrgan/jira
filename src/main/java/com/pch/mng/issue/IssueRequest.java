package com.pch.mng.issue;

import com.pch.mng.global.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class IssueRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private Long projectId;
        @NotNull
        private IssueType issueType;
        @NotBlank
        private String summary;
        private String description;
        @NotNull
        private Priority priority;
        private Integer storyPoints;
        private Long assigneeId;
        private Long parentId;
        private Long sprintId;
        private SecurityLevel securityLevel;
    }

    @Data
    public static class UpdateDTO {
        private String summary;
        private String description;
        private Priority priority;
        private Integer storyPoints;
        private Long assigneeId;
        private Long sprintId;
        private SecurityLevel securityLevel;
    }

    @Data
    public static class TransitionDTO {
        @NotNull
        private IssueStatus toStatus;
        private String conditionNote;
    }
}
