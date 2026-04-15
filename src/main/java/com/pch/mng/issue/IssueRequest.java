package com.pch.mng.issue;

import com.pch.mng.global.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
        /** Epic 전용. Epic 타입일 때만 허용. */
        private LocalDate epicStartDate;
        private LocalDate epicEndDate;
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
        /** Epic 전용. true이면 epic 기간을 모두 NULL로 초기화(개별 필드보다 우선). */
        private Boolean clearEpicDates;
        /**
         * Epic 전용. true이면 epicStartDate·epicEndDate를 요청 값 그대로 반영(null 포함).
         * false/null이면 해당 필드는 변경하지 않음(하위 호환).
         */
        private Boolean patchEpicDates;
        private LocalDate epicStartDate;
        private LocalDate epicEndDate;
        /** null이면 유지. true/false로 이슈 아카이브 토글. */
        private Boolean archived;
    }

    @Data
    public static class TransitionDTO {
        @NotNull
        private IssueStatus toStatus;
        private String conditionNote;
    }

    @Data
    public static class LabelAttachDTO {
        @NotNull
        private Long labelId;
    }

    @Data
    public static class ComponentAttachDTO {
        @NotNull
        private Long componentId;
    }

    @Data
    public static class BacklogReorderDTO {
        @NotEmpty
        private List<@NotNull Long> orderedIssueIds;
    }

    @Data
    public static class SprintAssignmentDTO {
        private Long sprintId;
        @NotEmpty
        private List<@NotNull Long> issueIds;
    }
}
