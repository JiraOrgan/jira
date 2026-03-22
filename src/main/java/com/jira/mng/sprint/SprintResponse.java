package com.jira.mng.sprint;

import com.jira.mng.global.enums.SprintStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SprintResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String name;
        private SprintStatus status;
        private LocalDate startDate;
        private LocalDate endDate;

        private MinDTO() {}

        public static MinDTO of(Sprint sprint) {
            MinDTO dto = new MinDTO();
            dto.id = sprint.getId();
            dto.name = sprint.getName();
            dto.status = sprint.getStatus();
            dto.startDate = sprint.getStartDate();
            dto.endDate = sprint.getEndDate();
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private Long projectId;
        private String name;
        private SprintStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer goalPoints;
        private LocalDateTime createdAt;

        private DetailDTO() {}

        public static DetailDTO of(Sprint sprint) {
            DetailDTO dto = new DetailDTO();
            dto.id = sprint.getId();
            dto.name = sprint.getName();
            dto.status = sprint.getStatus();
            dto.startDate = sprint.getStartDate();
            dto.endDate = sprint.getEndDate();
            dto.goalPoints = sprint.getGoalPoints();
            dto.createdAt = sprint.getCreatedAt();
            if (sprint.getProject() != null) {
                dto.projectId = sprint.getProject().getId();
            }
            return dto;
        }
    }
}
