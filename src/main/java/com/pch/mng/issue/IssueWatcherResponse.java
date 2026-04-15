package com.pch.mng.issue;

import com.pch.mng.user.UserAccount;
import lombok.Data;

import java.util.List;

public class IssueWatcherResponse {

    @Data
    public static class UserDTO {
        private Long userId;
        private String name;
        private String email;

        private UserDTO() {}

        public static UserDTO of(IssueWatcher watcher) {
            UserAccount u = watcher.getUser();
            UserDTO dto = new UserDTO();
            dto.userId = u.getId();
            dto.name = u.getName();
            dto.email = u.getEmail();
            return dto;
        }
    }

    @Data
    public static class ListDTO {
        private List<UserDTO> watchers;
        private boolean selfWatching;
    }
}
