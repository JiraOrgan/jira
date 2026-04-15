package com.pch.mng.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubApiClient {

    private final GithubOAuthProperties oauthProperties;
    private final ObjectMapper objectMapper;

    private final RestClient githubCom = RestClient.builder().baseUrl("https://github.com").build();
    private final RestClient githubApi = RestClient.builder().baseUrl("https://api.github.com").build();

    public OAuthTokenResult exchangeAuthorizationCode(String code) {
        if (!StringUtils.hasText(oauthProperties.getClientId())
                || !StringUtils.hasText(oauthProperties.getClientSecret())) {
            throw new BusinessException(ErrorCode.GITHUB_NOT_CONFIGURED);
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", oauthProperties.getClientId());
        form.add("client_secret", oauthProperties.getClientSecret());
        form.add("code", code);
        form.add("redirect_uri", oauthProperties.getRedirectUri());

        String json;
        try {
            json = githubCom
                    .post()
                    .uri("/login/oauth/access_token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_EXCHANGE_FAILED);
        }
        if (json == null) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_EXCHANGE_FAILED);
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.hasNonNull("error")) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_EXCHANGE_FAILED);
            }
            String access = text(root, "access_token");
            if (!StringUtils.hasText(access)) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_EXCHANGE_FAILED);
            }
            String refresh = text(root, "refresh_token");
            Long expiresIn = root.has("expires_in") && root.get("expires_in").canConvertToLong()
                    ? root.get("expires_in").asLong()
                    : null;
            return new OAuthTokenResult(access, refresh, expiresIn);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_EXCHANGE_FAILED);
        }
    }

    public long createRepoWebhook(String accessToken, String owner, String repo, String callbackUrl, String secret) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("url", callbackUrl);
        config.put("content_type", "json");
        config.put("secret", secret);
        config.put("insecure_ssl", "0");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "web");
        body.put("active", true);
        body.put("events", List.of("push", "pull_request"));
        body.put("config", config);
        try {
            String json = githubApi
                    .post()
                    .uri("/repos/{owner}/{repo}/hooks", owner, repo)
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.add("X-GitHub-Api-Version", "2022-11-28");
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, res) -> {
                                throw new BusinessException(ErrorCode.GITHUB_API_ERROR);
                            })
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("id")) {
                throw new BusinessException(ErrorCode.GITHUB_API_ERROR);
            }
            return root.get("id").asLong();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_API_ERROR);
        }
    }

    public void deleteRepoWebhook(String accessToken, String owner, String repo, long hookId) {
        try {
            githubApi
                    .delete()
                    .uri("/repos/{owner}/{repo}/hooks/{hookId}", owner, repo, hookId)
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.add("X-GitHub-Api-Version", "2022-11-28");
                    })
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, res) -> {
                                throw new BusinessException(ErrorCode.GITHUB_API_ERROR);
                            })
                    .toBodilessEntity();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_API_ERROR);
        }
    }

    private static String text(JsonNode n, String field) {
        return n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }

    public record OAuthTokenResult(String accessToken, String refreshToken, Long expiresInSeconds) {}
}
