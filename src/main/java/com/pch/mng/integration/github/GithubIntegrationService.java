package com.pch.mng.integration.github;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class GithubIntegrationService {

    private static final Pattern REPO_FULL_NAME = Pattern.compile("^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$");

    private final ProjectRepository projectRepository;
    private final ProjectGithubIntegrationRepository integrationRepository;
    private final GithubOAuthProperties oauthProperties;
    private final GithubIntegrationProperties integrationProperties;
    private final GithubOAuthStateService oauthStateService;
    private final GithubApiClient githubApiClient;
    private final AesGcmStringEncryptor aesGcmStringEncryptor;

    @Transactional(readOnly = true)
    public GithubIntegrationResponse.AuthorizeUrlDTO buildAuthorizeUrl(long projectId, long userId) {
        assertOAuthClientConfigured();
        String state = oauthStateService.createState(projectId, userId);
        String url = UriComponentsBuilder.fromUriString(oauthProperties.getAuthorizeUrl())
                .queryParam("client_id", oauthProperties.getClientId())
                .queryParam("redirect_uri", oauthProperties.getRedirectUri())
                .queryParam("scope", "repo")
                .queryParam("state", state)
                .encode()
                .build()
                .toUriString();
        return GithubIntegrationResponse.AuthorizeUrlDTO.builder().authorizeUrl(url).build();
    }

    public URI completeOAuthAndRedirect(String code, String state) {
        assertOAuthClientConfigured();
        if (!aesGcmStringEncryptor.isConfigured()) {
            return redirectError("crypto");
        }
        GithubOAuthStateService.ParsedState parsed;
        try {
            parsed = oauthStateService.verifyAndParse(state);
        } catch (BusinessException e) {
            return redirectError("state");
        }
        Project project = projectRepository.findById(parsed.projectId()).orElse(null);
        if (project == null) {
            return redirectError("project");
        }
        GithubApiClient.OAuthTokenResult token;
        try {
            token = githubApiClient.exchangeAuthorizationCode(code);
        } catch (BusinessException e) {
            return redirectError("token");
        }

        ProjectGithubIntegration row = integrationRepository
                .findByProject_Id(project.getId())
                .orElseGet(() -> ProjectGithubIntegration.builder().project(project).build());
        row.setProject(project);
        row.setAccessTokenEnc(aesGcmStringEncryptor.encrypt(token.accessToken()));
        row.setRefreshTokenEnc(
                StringUtils.hasText(token.refreshToken())
                        ? aesGcmStringEncryptor.encrypt(token.refreshToken())
                        : null);
        if (token.expiresInSeconds() != null && token.expiresInSeconds() > 0) {
            row.setTokenExpiresAt(Instant.now().plusSeconds(token.expiresInSeconds()));
        } else {
            row.setTokenExpiresAt(null);
        }
        row.setGithubRepoFullName(null);
        row.setGithubWebhookId(null);
        row.setWebhookSecret(null);
        integrationRepository.save(row);

        return frontendRedirect(project.getKey(), "github_oauth=ok");
    }

    @Transactional(readOnly = true)
    public GithubIntegrationResponse.StatusDTO status(long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return integrationRepository
                .findByProject_Id(projectId)
                .map(
                        row -> GithubIntegrationResponse.StatusDTO.builder()
                                .oauthComplete(StringUtils.hasText(row.getAccessTokenEnc()))
                                .githubRepoFullName(row.getGithubRepoFullName())
                                .githubWebhookId(row.getGithubWebhookId())
                                .build())
                .orElseGet(() -> GithubIntegrationResponse.StatusDTO.builder()
                        .oauthComplete(false)
                        .githubRepoFullName(null)
                        .githubWebhookId(null)
                        .build());
    }

    public void connectRepo(long projectId, String repoFullNameRaw) {
        if (!aesGcmStringEncryptor.isConfigured()) {
            throw new BusinessException(ErrorCode.GITHUB_CRYPTO_NOT_CONFIGURED);
        }
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        String repoFullName = normalizeRepoFullName(repoFullNameRaw);
        if (!REPO_FULL_NAME.matcher(repoFullName).matches()) {
            throw new BusinessException(ErrorCode.GITHUB_REPO_INVALID);
        }
        integrationRepository
                .findByGithubRepoFullNameIgnoreCase(repoFullName)
                .filter(other -> !other.getProject().getId().equals(projectId))
                .ifPresent(x -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
                });

        ProjectGithubIntegration row = integrationRepository
                .findByProject_Id(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_OAUTH_INCOMPLETE));
        if (!StringUtils.hasText(row.getAccessTokenEnc())) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_INCOMPLETE);
        }
        String accessToken = aesGcmStringEncryptor.decrypt(row.getAccessTokenEnc());
        String[] ownerRepo = repoFullName.split("/", 2);
        String owner = ownerRepo[0];
        String repo = ownerRepo[1];

        String base = integrationProperties.getPublicBaseUrl().replaceAll("/$", "");
        String callbackUrl = base + "/api/v1/integrations/github/webhook";
        String secret = UUID.randomUUID().toString().replace("-", "");

        if (row.getGithubWebhookId() != null && StringUtils.hasText(row.getGithubRepoFullName())) {
            String[] old = row.getGithubRepoFullName().split("/", 2);
            if (old.length == 2) {
                try {
                    githubApiClient.deleteRepoWebhook(accessToken, old[0], old[1], row.getGithubWebhookId());
                } catch (BusinessException ignored) {
                    // 이미 삭제된 훅 등은 무시
                }
            }
        }

        long hookId = githubApiClient.createRepoWebhook(accessToken, owner, repo, callbackUrl, secret);
        row.setGithubRepoFullName(repoFullName);
        row.setGithubWebhookId(hookId);
        row.setWebhookSecret(secret);
        integrationRepository.save(row);
    }

    public void disconnect(long projectId) {
        if (!aesGcmStringEncryptor.isConfigured()) {
            throw new BusinessException(ErrorCode.GITHUB_CRYPTO_NOT_CONFIGURED);
        }
        ProjectGithubIntegration row = integrationRepository
                .findByProject_Id(projectId)
                .orElse(null);
        if (row == null) {
            return;
        }
        if (row.getGithubWebhookId() != null && StringUtils.hasText(row.getGithubRepoFullName())) {
            String[] parts = row.getGithubRepoFullName().split("/", 2);
            if (parts.length == 2 && StringUtils.hasText(row.getAccessTokenEnc())) {
                try {
                    String accessToken = aesGcmStringEncryptor.decrypt(row.getAccessTokenEnc());
                    githubApiClient.deleteRepoWebhook(accessToken, parts[0], parts[1], row.getGithubWebhookId());
                } catch (BusinessException ignored) {
                    // ignore
                }
            }
        }
        integrationRepository.delete(row);
    }

    private URI redirectError(String code) {
        String base = oauthProperties.getFrontendRedirectBase().replaceAll("/$", "");
        return URI.create(base + "/?github_oauth_error=" + code);
    }

    private URI frontendRedirect(String projectKey, String query) {
        return UriComponentsBuilder.fromUriString(oauthProperties.getFrontendRedirectBase())
                .path("/project/{projectKey}/settings")
                .replaceQuery(query)
                .buildAndExpand(projectKey)
                .encode()
                .toUri();
    }

    private void assertOAuthClientConfigured() {
        if (!StringUtils.hasText(oauthProperties.getClientId())
                || !StringUtils.hasText(oauthProperties.getClientSecret())
                || !StringUtils.hasText(oauthProperties.getStateHmacSecret())) {
            throw new BusinessException(ErrorCode.GITHUB_NOT_CONFIGURED);
        }
    }

    private static String normalizeRepoFullName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", "");
    }
}
