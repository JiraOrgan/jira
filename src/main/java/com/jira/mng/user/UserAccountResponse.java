package com.jira.mng.user;

import lombok.Data;

public class UserAccountResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String email;
        private String name;

        private MinDTO() {}

        public static MinDTO of(UserAccount user) {
            MinDTO dto = new MinDTO();
            dto.id = user.getId();
            dto.email = user.getEmail();
            dto.name = user.getName();
            return dto;
        }
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private String email;
        private String name;
        private java.time.LocalDateTime createdAt;

        private DetailDTO() {}

        public static DetailDTO of(UserAccount user) {
            DetailDTO dto = new DetailDTO();
            dto.id = user.getId();
            dto.email = user.getEmail();
            dto.name = user.getName();
            dto.createdAt = user.getCreatedAt();
            return dto;
        }
    }
}
