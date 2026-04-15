package com.pch.mng.integration.github;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class GithubProjectIntegrationController {

    private final GithubIntegrationService githubIntegrationService;

    @GetMapping("/{projectId}/github/oauth/authorize-url")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<GithubIntegrationResponse.AuthorizeUrlDTO>> oauthAuthorizeUrl(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(
                ApiResponse.ok(githubIntegrationService.buildAuthorizeUrl(projectId, principal.getId())));
    }

    @GetMapping("/{projectId}/github/integration")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<GithubIntegrationResponse.StatusDTO>> integrationStatus(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(githubIntegrationService.status(projectId)));
    }

    @PostMapping("/{projectId}/github/integration")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Void>> connectRepo(
            @PathVariable Long projectId, @Valid @RequestBody GithubIntegrationRequest.ConnectRepoDTO body) {
        githubIntegrationService.connectRepo(projectId, body.getRepoFullName());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @DeleteMapping("/{projectId}/github/integration")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Void>> disconnect(@PathVariable Long projectId) {
        githubIntegrationService.disconnect(projectId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
