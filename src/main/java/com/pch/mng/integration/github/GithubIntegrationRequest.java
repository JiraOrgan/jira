package com.pch.mng.integration.github;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class GithubIntegrationRequest {

    @Data
    public static class ConnectRepoDTO {
        /** GitHub 저장소 전체 이름 (예: octocat/Hello-World) */
        @NotBlank
        private String repoFullName;
    }
}
