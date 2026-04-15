package com.pch.mng.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardType;
import com.pch.mng.project.ProjectRepository;
import com.pch.mng.project.ProjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class GithubWebhookIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ProjectGithubIntegrationRepository projectGithubIntegrationRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AesGcmStringEncryptor aesGcmStringEncryptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    private String registerAndLogin(String email) throws Exception {
        String body = """
                {"email":"%s","password":"password12","name":"U"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password12\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }

    private long createProject(String token, String key) throws Exception {
        ProjectRequest.SaveDTO dto = new ProjectRequest.SaveDTO();
        dto.setKey(key);
        dto.setName("GhHook");
        dto.setBoardType(BoardType.SCRUM);
        MvcResult res = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private static String hubSignature256(String secret, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(body);
        return "sha256=" + HexFormat.of().formatHex(digest);
    }

    @Test
    @DisplayName("GitHub push 웹훅이 이슈 키를 파싱해 VCS 커밋 링크를 추가한다")
    void pushWebhookAddsCommitVcsLink() throws Exception {
        String email = "gh-hook-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pkey = "GH" + (System.nanoTime() % 90000 + 10000);
        long projectId = createProject(token, pkey);

        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"projectId":%d,"issueType":"TASK","summary":"hook","priority":"MEDIUM"}
                                """
                                        .formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(issueRes.getResponse().getContentAsString()).path("data");
        String issueKey = created.path("issueKey").asText();

        String hookSecret = "whsec-test-secret-32bytes-minimum!!";
        var project = projectRepository.findById(projectId).orElseThrow();
        ProjectGithubIntegration integration = ProjectGithubIntegration.builder()
                .project(project)
                .accessTokenEnc(aesGcmStringEncryptor.encrypt("dummy-github-token"))
                .githubRepoFullName("acme/demo")
                .githubWebhookId(999L)
                .webhookSecret(hookSecret)
                .build();
        projectGithubIntegrationRepository.save(integration);

        String sha = "abc123def456789012345678901234567890abcd";
        String json =
                """
                {
                  "repository": { "full_name": "acme/demo" },
                  "ref": "refs/heads/main",
                  "commits": [
                    { "id": "%s", "message": "fix %s in demo" }
                  ]
                }
                """
                        .formatted(sha, issueKey);
        byte[] raw = json.getBytes(StandardCharsets.UTF_8);
        String sig = hubSignature256(hookSecret, raw);

        mockMvc.perform(post("/api/v1/integrations/github/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", sig)
                        .content(raw))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/{issueKey}", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(r -> {
                    JsonNode data =
                            objectMapper.readTree(r.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("vcsLinks").isArray()).isTrue();
                    assertThat(data.path("vcsLinks").size()).isEqualTo(1);
                    assertThat(data.path("vcsLinks").get(0).path("url").asText())
                            .contains("/commit/" + sha);
                });
    }
}
