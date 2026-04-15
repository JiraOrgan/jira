package com.pch.mng.integration.github;

import com.pch.mng.project.Project;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "project_github_integration_tb")
public class ProjectGithubIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "access_token_enc", columnDefinition = "TEXT")
    private String accessTokenEnc;

    @Column(name = "refresh_token_enc", columnDefinition = "TEXT")
    private String refreshTokenEnc;

    private Instant tokenExpiresAt;

    @Column(name = "github_repo_full_name", length = 255)
    private String githubRepoFullName;

    private Long githubWebhookId;

    @Column(length = 255)
    private String webhookSecret;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public ProjectGithubIntegration(
            Project project,
            String accessTokenEnc,
            String refreshTokenEnc,
            Instant tokenExpiresAt,
            String githubRepoFullName,
            Long githubWebhookId,
            String webhookSecret) {
        this.project = project;
        this.accessTokenEnc = accessTokenEnc;
        this.refreshTokenEnc = refreshTokenEnc;
        this.tokenExpiresAt = tokenExpiresAt;
        this.githubRepoFullName = githubRepoFullName;
        this.githubWebhookId = githubWebhookId;
        this.webhookSecret = webhookSecret;
    }
}
