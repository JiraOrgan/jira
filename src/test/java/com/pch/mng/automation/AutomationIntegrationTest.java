package com.pch.mng.automation;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AutomationIntegrationTest {

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
        JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }

    private long createProject(String token, String key) throws Exception {
        ProjectRequest.SaveDTO dto = new ProjectRequest.SaveDTO();
        dto.setKey(key);
        dto.setName("AutoP");
        dto.setBoardType(BoardType.SCRUM);
        MvcResult res = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @DisplayName("자동화 규칙: SET_PRIORITY actionJson 없으면 400")
    void createRuleRejectsMissingActionJsonForSetPriority() throws Exception {
        String email = "auto-bad-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "AB" + (System.nanoTime() % 100000));

        String ruleJson = """
                {
                  "name":"r1",
                  "enabled":true,
                  "triggerType":"ISSUE_CREATED",
                  "conditionJson":"{\\"issueTypes\\":[\\"BUG\\"]}",
                  "actionType":"SET_PRIORITY"
                }
                """;
        mockMvc.perform(post("/api/v1/projects/{projectId}/automation/rules", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ISSUE_CREATED: BUG 생성 시 우선순위 자동 HIGH")
    void issueCreatedAutomationSetsPriority() throws Exception {
        String email = "auto-cr-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "AC" + (System.nanoTime() % 100000));

        String ruleJson = """
                {
                  "name":"bug-high",
                  "enabled":true,
                  "triggerType":"ISSUE_CREATED",
                  "conditionJson":"{\\"issueTypes\\":[\\"BUG\\"]}",
                  "actionType":"SET_PRIORITY",
                  "actionJson":"{\\"priority\\":\\"HIGH\\"}",
                  "sortOrder":0
                }
                """;
        mockMvc.perform(post("/api/v1/projects/{projectId}/automation/rules", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleJson))
                .andExpect(status().isCreated());

        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"projectId":%d,"issueType":"BUG","summary":"b","priority":"MEDIUM"}
                                """
                                        .formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode data = objectMapper.readTree(issueRes.getResponse().getContentAsString()).path("data");
        assertThat(data.path("priority").asText()).isEqualTo("HIGH");

        mockMvc.perform(get("/api/v1/projects/{projectId}/automation/execution-logs", projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
                    JsonNode content = root.path("data").path("content");
                    assertThat(content.isArray()).isTrue();
                    assertThat(content.size()).isGreaterThanOrEqualTo(1);
                    assertThat(content.get(0).path("success").asBoolean()).isTrue();
                });
    }

    @Test
    @DisplayName("ISSUE_STATUS_CHANGED: 백로그→선정 시 담당자=리포터")
    void statusAutomationAssignsReporter() throws Exception {
        String email = "auto-st-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "AS" + (System.nanoTime() % 100000));

        String ruleJson = """
                {
                  "name":"assign-on-select",
                  "enabled":true,
                  "triggerType":"ISSUE_STATUS_CHANGED",
                  "conditionJson":"{\\"fromStatus\\":\\"BACKLOG\\",\\"toStatus\\":\\"SELECTED\\"}",
                  "actionType":"ASSIGN_TO_REPORTER",
                  "sortOrder":0
                }
                """;
        mockMvc.perform(post("/api/v1/projects/{projectId}/automation/rules", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleJson))
                .andExpect(status().isCreated());

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
        assertThat(created.hasNonNull("assigneeId")).isFalse();
        String issueKey = created.path("issueKey").asText();
        long reporterId = created.path("reporterId").asLong();

        mockMvc.perform(post("/api/v1/issues/{issueKey}/transitions", issueKey)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"SELECTED\"}"))
                .andExpect(status().isOk());

        MvcResult after = mockMvc.perform(get("/api/v1/issues/{issueKey}", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode detail = objectMapper.readTree(after.getResponse().getContentAsString()).path("data");
        assertThat(detail.path("assigneeId").asLong()).isEqualTo(reporterId);
    }
}
