package com.pch.mng.comment;

import com.pch.mng.user.UserAccount;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    @Data
    public static class MentionDTO {
        private Long userId;
        private String userName;

        private MentionDTO() {}

        public static MentionDTO of(UserAccount user) {
            MentionDTO dto = new MentionDTO();
            if (user != null) {
                dto.userId = user.getId();
                dto.userName = user.getName();
            }
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private Long issueId;
        private Long authorId;
        private String authorName;
        private String body;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<MentionDTO> mentionedUsers;

        private DetailDTO() {}

        public static DetailDTO of(Comment comment) {
            return of(comment, List.of());
        }

        public static DetailDTO of(Comment comment, List<MentionDTO> mentionedUsers) {
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
            dto.mentionedUsers =
                    mentionedUsers == null ? List.of() : List.copyOf(mentionedUsers);
            return dto;
        }
    }
}
