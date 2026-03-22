package com.jira.mng.project;

import com.jira.mng.global.enums.BoardType;
import com.jira.mng.global.enums.ProjectRole;
import lombok.Data;
import java.time.LocalDateTime;

public class ProjectResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String key;
        private String name;
        private BoardType boardType;
        private boolean archived;

        private MinDTO() {}

        public static MinDTO of(Project project) {
            MinDTO dto = new MinDTO();
            dto.id = project.getId();
            dto.key = project.getKey();
            dto.name = project.getName();
            dto.boardType = project.getBoardType();
            dto.archived = project.isArchived();
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private String key;
        private String name;
        private String description;
        private BoardType boardType;
        private Long leadId;
        private String leadName;
        private boolean archived;
        private LocalDateTime createdAt;

        private DetailDTO() {}

        public static DetailDTO of(Project project) {
            DetailDTO dto = new DetailDTO();
            dto.id = project.getId();
            dto.key = project.getKey();
            dto.name = project.getName();
            dto.description = project.getDescription();
            dto.boardType = project.getBoardType();
            dto.archived = project.isArchived();
            dto.createdAt = project.getCreatedAt();
            if (project.getLead() != null) {
                dto.leadId = project.getLead().getId();
                dto.leadName = project.getLead().getName();
            }
            return dto;
        }
    }

    @Data
    public static class MemberDTO {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private ProjectRole role;
        private LocalDateTime joinedAt;

        private MemberDTO() {}

        public static MemberDTO of(ProjectMember member) {
            MemberDTO dto = new MemberDTO();
            dto.id = member.getId();
            dto.role = member.getRole();
            dto.joinedAt = member.getJoinedAt();
            if (member.getUser() != null) {
                dto.userId = member.getUser().getId();
                dto.userName = member.getUser().getName();
                dto.userEmail = member.getUser().getEmail();
            }
            return dto;
        }
    }
}
