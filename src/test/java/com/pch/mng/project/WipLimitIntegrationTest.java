package com.pch.mng.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardType;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class WipLimitIntegrationTest {

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

    private long createKanbanProject(String token, String key) throws Exception {
        ProjectRequest.SaveDTO dto = new ProjectRequest.SaveDTO();
        dto.setKey(key);
        dto.setName("Kan");
        dto.setBoardType(BoardType.KANBAN);
        MvcResult res = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private String createTask(String token, long projectId) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"T","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("issueKey").asText();
    }

    private void transition(String token, String issueKey, String toStatus) throws Exception {
        mockMvc.perform(post("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"" + toStatus + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("KANBAN에서 WIP 초과 시 상태 전환 409")
    void kanbanWipBlocksTransition() throws Exception {
        String email = "wip-k-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createKanbanProject(token, "WK" + (System.nanoTime() % 100000));
        String k1 = createTask(token, projectId);
        String k2 = createTask(token, projectId);

        String wipBody = """
                {"limits":[{"status":"SELECTED","maxIssues":1}]}
                """;
        mockMvc.perform(put("/api/v1/projects/%d/wip-limits".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wipBody))
                .andExpect(status().isOk());

        transition(token, k1, "SELECTED");

        mockMvc.perform(post("/api/v1/issues/%s/transitions".formatted(k2))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"SELECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("SCRUM 프로젝트는 WIP 설정 API 거절")
    void scrumRejectsWipLimitsPut() throws Exception {
        String email = "wip-s-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        ProjectRequest.SaveDTO dto = new ProjectRequest.SaveDTO();
        dto.setKey("WS" + (System.nanoTime() % 100000));
        dto.setName("Scrum");
        dto.setBoardType(BoardType.SCRUM);
        MvcResult res = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        long projectId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/projects/%d/wip-limits".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limits\":[{\"status\":\"IN_PROGRESS\",\"maxIssues\":2}]}"))
                .andExpect(status().isBadRequest());
    }
}
