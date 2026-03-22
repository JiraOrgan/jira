package com.jira.mng.sprint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

public class SprintRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private Long projectId;
        @NotBlank
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer goalPoints;
    }

    @Data
    public static class UpdateDTO {
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer goalPoints;
    }
}
