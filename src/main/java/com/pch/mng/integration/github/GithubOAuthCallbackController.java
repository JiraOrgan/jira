package com.pch.mng.integration.github;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/integrations/github")
@RequiredArgsConstructor
public class GithubOAuthCallbackController {

    private final GithubIntegrationService githubIntegrationService;

    @GetMapping("/callback")
    public ResponseEntity<Void> oauthCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        URI location = githubIntegrationService.completeOAuthAndRedirect(code, state);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }
}
