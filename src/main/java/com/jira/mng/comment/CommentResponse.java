package com.jira.mng.comment;

import lombok.Data;
import java.time.LocalDateTime;

public class CommentResponse {

    @Data
    public static class DetailDTO {
        private Long id;
        private Long issueId;
        private Long authorId;
        private String authorName;
        private String body;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private DetailDTO() {}

        public static DetailDTO of(Comment comment) {
            DetailDTO dto = new DetailDTO();
            dto.id = comment.getId();
            dto.body = comment.getBody();
            dto.createdAt = comment.getCreatedAt();
            dto.updatedAt = comment.getUpdatedAt();
            if (comment.getIssue() != null) {
                dto.issueId = comment.getIssue().getId();
            }
            if (comment.getAuthor() != null) {
                dto.authorId = comment.getAuthor().getId();
                dto.authorName = comment.getAuthor().getName();
            }
            return dto;
        }
    }
}
