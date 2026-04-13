package com.pch.mng.dashboard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class DashboardRequest {

    @Data
    public static class SaveDTO {
        @NotBlank
        private String name;
        private boolean shared;
    }

    @Data
    public static class UpdateDTO {
        private String name;
        private Boolean shared;
    }

    @Data
    public static class GadgetDTO {
        @NotBlank
        private String gadgetType;
        private int position;
        private String configJson;
    }

    @Data
    public static class GadgetUpdateDTO {
        private String gadgetType;
        private Integer position;
        private String configJson;
    }

    @Data
    public static class GadgetReorderDTO {
        @NotNull
        @Valid
        private List<GadgetPositionDTO> positions;
    }

    @Data
    public static class GadgetPositionDTO {
        @NotNull
        private Long gadgetId;
        @NotNull
        private Integer position;
    }
}
