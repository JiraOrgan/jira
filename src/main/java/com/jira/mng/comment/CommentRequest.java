package com.jira.mng.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class CommentRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private Long issueId;
        @NotBlank
        private String body;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank
        private String body;
    }
}
