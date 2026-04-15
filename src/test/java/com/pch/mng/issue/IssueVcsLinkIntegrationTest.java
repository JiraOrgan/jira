package com.pch.mng.issue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class IssueVcsLinkIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    WebApplicationContext webApplicationContext;

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
        dto.setName("VcsP");
        dto.setBoardType(BoardType.SCRUM);
        MvcResult res = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    @DisplayName("VCS 링크 CRUD 및 이슈 상세 vcsLinks 포함")
    void vcsLinksCrudAndDetailIncludesLinks() throws Exception {
        String email = "vcs-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "VC" + (System.nanoTime() % 100000));

        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"projectId":%d,"issueType":"TASK","summary":"t","priority":"MEDIUM"}
                                """
                                        .formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(issueRes.getResponse().getContentAsString()).path("data");
        String issueKey = created.path("issueKey").asText();
        assertThat(created.path("vcsLinks").isArray()).isTrue();
        assertThat(created.path("vcsLinks").size()).isZero();

        String linkBody =
                """
                {"provider":"GITHUB","linkKind":"PULL_REQUEST","url":"https://github.com/org/repo/pull/1","title":"Fix bug"}
                """;
        MvcResult postLink = mockMvc.perform(post("/api/v1/issues/{issueKey}/vcs-links", issueKey)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(linkBody))
                .andExpect(status().isCreated())
                .andReturn();
        long linkId =
                objectMapper.readTree(postLink.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/issues/{issueKey}/vcs-links", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(r -> {
                    JsonNode arr =
                            objectMapper.readTree(r.getResponse().getContentAsString()).path("data");
                    assertThat(arr.isArray()).isTrue();
                    assertThat(arr.size()).isEqualTo(1);
                    assertThat(arr.get(0).path("url").asText()).contains("github.com");
                });

        MvcResult detail = mockMvc.perform(get("/api/v1/issues/{issueKey}", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode vcs =
                objectMapper.readTree(detail.getResponse().getContentAsString()).path("data").path("vcsLinks");
        assertThat(vcs.size()).isEqualTo(1);
        assertThat(vcs.get(0).path("linkKind").asText()).isEqualTo("PULL_REQUEST");

        mockMvc.perform(post("/api/v1/issues/{issueKey}/vcs-links", issueKey)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(linkBody))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/issues/{issueKey}/vcs-links/{linkId}", issueKey, linkId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/{issueKey}/vcs-links", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(r -> {
                    JsonNode arr =
                            objectMapper.readTree(r.getResponse().getContentAsString()).path("data");
                    assertThat(arr.size()).isZero();
                });
    }

    @Test
    @DisplayName("VCS 링크: 잘못된 URL은 400")
    void vcsLinkRejectsBadUrl() throws Exception {
        String email = "vcs-bad-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "VB" + (System.nanoTime() % 100000));
        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"projectId":%d,"issueType":"TASK","summary":"t","priority":"MEDIUM"}
                                """
                                        .formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        String issueKey =
                objectMapper.readTree(issueRes.getResponse().getContentAsString()).path("data").path("issueKey").asText();

        mockMvc.perform(post("/api/v1/issues/{issueKey}/vcs-links", issueKey)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"provider":"GITLAB","linkKind":"COMMIT","url":"ftp://gitlab.com/x/y/-/commit/abc"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
