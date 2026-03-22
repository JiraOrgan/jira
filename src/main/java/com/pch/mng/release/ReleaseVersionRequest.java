package com.pch.mng.release;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

public class ReleaseVersionRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private Long projectId;
        @NotBlank
        private String name;
        private String description;
        private LocalDate releaseDate;
    }

    @Data
    public static class UpdateDTO {
        private String name;
        private String description;
        private LocalDate releaseDate;
    }
}
