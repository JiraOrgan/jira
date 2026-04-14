package com.pch.mng.project;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.ProjectRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

public class ProjectRequest {

    @Data
    public static class SaveDTO {
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,9}$", message = "프로젝트 키는 대문자로 시작, 2~10자")
        private String key;
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private BoardType boardType;
        private Long leadId;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank
        private String name;
        private String description;
        private Long leadId;
        /** null이면 아카이브 플래그는 변경하지 않음 */
        private Boolean archived;
    }

    @Data
    public static class AddMemberDTO {
        @NotNull
        private Long userId;
        @NotNull
        private ProjectRole role;
    }

    @Data
    public static class WipLimitItemDTO {
        @NotNull
        private IssueStatus status;
        @NotNull
        @Min(1)
        private Integer maxIssues;
    }

    @Data
    public static class WipLimitsReplaceDTO {
        @NotNull
        private List<@Valid WipLimitItemDTO> limits;
    }
}
