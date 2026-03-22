package com.pch.mng.release;

import com.pch.mng.global.enums.VersionStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReleaseVersionResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String name;
        private VersionStatus status;
        private LocalDate releaseDate;

        private MinDTO() {}

        public static MinDTO of(ReleaseVersion version) {
            MinDTO dto = new MinDTO();
            dto.id = version.getId();
            dto.name = version.getName();
            dto.status = version.getStatus();
            dto.releaseDate = version.getReleaseDate();
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private Long projectId;
        private String name;
        private String description;
        private VersionStatus status;
        private LocalDate releaseDate;
        private LocalDateTime createdAt;

        private DetailDTO() {}

        public static DetailDTO of(ReleaseVersion version) {
            DetailDTO dto = new DetailDTO();
            dto.id = version.getId();
            dto.name = version.getName();
            dto.description = version.getDescription();
            dto.status = version.getStatus();
            dto.releaseDate = version.getReleaseDate();
            dto.createdAt = version.getCreatedAt();
            if (version.getProject() != null) {
                dto.projectId = version.getProject().getId();
            }
            return dto;
        }
    }
}
