package com.pch.mng.dashboard;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String name;
        private boolean shared;
        private String ownerName;

        private MinDTO() {}

        public static MinDTO of(Dashboard dashboard) {
            MinDTO dto = new MinDTO();
            dto.id = dashboard.getId();
            dto.name = dashboard.getName();
            dto.shared = dashboard.isShared();
            if (dashboard.getOwner() != null) {
                dto.ownerName = dashboard.getOwner().getName();
            }
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private String name;
        private boolean shared;
        private Long ownerId;
        private String ownerName;
        private LocalDateTime createdAt;
        private List<GadgetDTO> gadgets;

        private DetailDTO() {}

        public static DetailDTO of(Dashboard dashboard) {
            DetailDTO dto = new DetailDTO();
            dto.id = dashboard.getId();
            dto.name = dashboard.getName();
            dto.shared = dashboard.isShared();
            dto.createdAt = dashboard.getCreatedAt();
            if (dashboard.getOwner() != null) {
                dto.ownerId = dashboard.getOwner().getId();
                dto.ownerName = dashboard.getOwner().getName();
            }
            if (dashboard.getGadgets() != null) {
                dto.gadgets = dashboard.getGadgets().stream()
                        .map(GadgetDTO::of)
                        .toList();
            }
            return dto;
        }
    }

    @Data
    public static class GadgetDTO {
        private Long id;
        private String gadgetType;
        private int position;
        private String configJson;

        private GadgetDTO() {}

        public static GadgetDTO of(DashboardGadget gadget) {
            GadgetDTO dto = new GadgetDTO();
            dto.id = gadget.getId();
            dto.gadgetType = gadget.getGadgetType();
            dto.position = gadget.getPosition();
            dto.configJson = gadget.getConfigJson();
            return dto;
        }
    }
}
