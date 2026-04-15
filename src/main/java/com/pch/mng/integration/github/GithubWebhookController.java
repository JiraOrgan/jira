package com.pch.mng.integration.github;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations/github")
@RequiredArgsConstructor
public class GithubWebhookController {

    private final GithubWebhookService githubWebhookService;

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> webhook(
            @RequestBody byte[] body,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature256,
            @RequestHeader(value = "X-GitHub-Event", required = false) String event) {
        githubWebhookService.dispatch(event, body, signature256 != null ? signature256 : "");
        return ResponseEntity.ok("ok");
    }
}
