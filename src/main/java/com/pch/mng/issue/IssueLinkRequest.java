package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueLinkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class IssueLinkRequest {

    @Data
    public static class SaveDTO {
        @NotBlank
        private String targetIssueKey;
        @NotNull
        private IssueLinkType linkType;
    }

    @Data
    public static class UpdateDTO {
        @NotNull
        private IssueLinkType linkType;
    }
}
