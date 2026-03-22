package com.jira.mng.dashboard;

import jakarta.validation.constraints.NotBlank;
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
}
