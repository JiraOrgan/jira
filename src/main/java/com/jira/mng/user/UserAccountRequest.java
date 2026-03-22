package com.jira.mng.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserAccountRequest {

    @Data
    public static class JoinDTO {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8)
        private String password;
        @NotBlank
        private String name;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank
        private String name;
    }
}
