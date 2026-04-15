package com.pch.mng.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.VcsLinkKind;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.issue.IssueVcsLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GithubWebhookService {

    private static final List<String> PR_ACTIONS =
            List.of("opened", "synchronize", "reopened", "edited", "closed");

    private final ObjectMapper objectMapper;
    private final ProjectGithubIntegrationRepository integrationRepository;
    private final IssueRepository issueRepository;
    private final IssueVcsLinkService issueVcsLinkService;

    @Transactional
    public void dispatch(String eventType, byte[] rawBody, String signatureHeader) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            return;
        }
        JsonNode repository = root.get("repository");
        if (repository == null || !repository.hasNonNull("full_name")) {
            return;
        }
        String fullName = repository.get("full_name").asText();
        ProjectGithubIntegration integration =
                integrationRepository.findByGithubRepoFullNameIgnoreCase(fullName).orElse(null);
        if (integration == null) {
            return;
        }
        String secret = integration.getWebhookSecret();
        if (!StringUtils.hasText(secret)
                || !StringUtils.hasText(signatureHeader)
                || !GithubWebhookSignatures.isValid(secret, rawBody, signatureHeader)) {
            throw new BusinessException(ErrorCode.GITHUB_WEBHOOK_SIGNATURE_INVALID);
        }
        if (eventType == null || "ping".equalsIgnoreCase(eventType)) {
            return;
        }

        String projectKey = integration.getProject().getKey();
        Long projectId = integration.getProject().getId();

        if ("push".equalsIgnoreCase(eventType)) {
            handlePush(root, fullName, projectId, projectKey);
        } else if ("pull_request".equalsIgnoreCase(eventType)) {
            handlePullRequest(root, projectId, projectKey);
        }
    }

    private void handlePush(JsonNode root, String fullName, Long projectId, String projectKey) {
        JsonNode commitsNode = root.get("commits");
        if (commitsNode == null || !commitsNode.isArray()) {
            return;
        }

        Set<String> allShas = new LinkedHashSet<>();
        for (JsonNode c : commitsNode) {
            if (c.hasNonNull("id")) {
                allShas.add(c.get("id").asText());
            }
        }

        Map<String, Set<String>> issueKeyToShas = new LinkedHashMap<>();
        if (root.hasNonNull("ref") && !allShas.isEmpty()) {
            Set<String> refKeys = new LinkedHashSet<>();
            collectFromText(root.get("ref").asText(), projectKey, refKeys);
            for (String ik : refKeys) {
                for (String sha : allShas) {
                    issueKeyToShas.computeIfAbsent(ik, k -> new LinkedHashSet<>()).add(sha);
                }
            }
        }
        for (JsonNode c : commitsNode) {
            if (!c.hasNonNull("id")) {
                continue;
            }
            String sha = c.get("id").asText();
            Set<String> msgKeys = new LinkedHashSet<>();
            collectFromText(c.path("message").asText(""), projectKey, msgKeys);
            for (String ik : msgKeys) {
                issueKeyToShas.computeIfAbsent(ik, k -> new LinkedHashSet<>()).add(sha);
            }
        }

        for (Map.Entry<String, Set<String>> e : issueKeyToShas.entrySet()) {
            issueRepository
                    .findByIssueKeyWithProject(e.getKey())
                    .filter(issue -> issue.getProject().getId().equals(projectId))
                    .ifPresent(issue -> {
                        for (String sha : e.getValue()) {
                            String url = "https://github.com/" + fullName + "/commit/" + sha;
                            JsonNode commitNode = findCommitBySha(commitsNode, sha);
                            String title =
                                    commitNode != null
                                            ? truncate(firstLine(commitNode.path("message").asText("")), 500)
                                            : "commit";
                            issueVcsLinkService.ensureGithubLinkFromAutomation(
                                    issue, VcsLinkKind.COMMIT, url, title);
                        }
                    });
        }
    }

    private static JsonNode findCommitBySha(JsonNode commits, String sha) {
        for (JsonNode c : commits) {
            if (c.hasNonNull("id") && sha.equals(c.get("id").asText())) {
                return c;
            }
        }
        return null;
    }

    private void handlePullRequest(JsonNode root, Long projectId, String projectKey) {
        String action = root.path("action").asText("");
        if (!PR_ACTIONS.contains(action)) {
            return;
        }
        JsonNode pr = root.get("pull_request");
        if (pr == null || !pr.hasNonNull("html_url")) {
            return;
        }
        String url = pr.get("html_url").asText();
        String prTitle = truncate(pr.path("title").asText(""), 500);

        Set<String> issueKeys = new LinkedHashSet<>();
        if (pr.hasNonNull("title")) {
            collectFromText(pr.get("title").asText(), projectKey, issueKeys);
        }
        if (pr.hasNonNull("body")) {
            collectFromText(pr.get("body").asText(), projectKey, issueKeys);
        }
        JsonNode head = pr.get("head");
        if (head != null && head.hasNonNull("ref")) {
            collectFromText(head.get("ref").asText(), projectKey, issueKeys);
        }

        for (String issueKey : issueKeys) {
            issueRepository
                    .findByIssueKeyWithProject(issueKey)
                    .filter(issue -> issue.getProject().getId().equals(projectId))
                    .ifPresent(
                            issue -> issueVcsLinkService.ensureGithubLinkFromAutomation(
                                    issue, VcsLinkKind.PULL_REQUEST, url, prTitle));
        }
    }

    private static void collectFromText(String text, String projectKey, Set<String> keys) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        Pattern p = Pattern.compile(
                "\\b" + Pattern.quote(projectKey) + "-(\\d+)\\b", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        while (m.find()) {
            keys.add(projectKey + "-" + m.group(1));
        }
    }

    private static String firstLine(String message) {
        if (!StringUtils.hasText(message)) {
            return "commit";
        }
        int nl = message.indexOf('\n');
        return nl > 0 ? message.substring(0, nl).trim() : message.trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
