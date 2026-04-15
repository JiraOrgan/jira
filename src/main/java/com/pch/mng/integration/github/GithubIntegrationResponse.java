package com.pch.mng.integration.github;

import lombok.Builder;
import lombok.Value;

public class GithubIntegrationResponse {

    @Value
    @Builder
    public static class StatusDTO {
        boolean oauthComplete;
        String githubRepoFullName;
        Long githubWebhookId;
    }

    @Value
    @Builder
    public static class AuthorizeUrlDTO {
        String authorizeUrl;
    }
}
