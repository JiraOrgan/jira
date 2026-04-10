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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class BacklogIntegrationTest {

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
        dto.setName("P");
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

    private long createTask(String token, long projectId) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"T","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private List<Long> backlogIds(String token, long projectId) throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v1/issues/project/%d/backlog".formatted(projectId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        List<Long> ids = new ArrayList<>();
        for (JsonNode n : data) {
            ids.add(n.path("id").asLong());
        }
        return ids;
    }

    @Test
    @DisplayName("백로그 순서 reorder 후 GET 순서 반영")
    void backlogReorderPersistsOrder() throws Exception {
        String email = "bl-r-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "BL" + (System.nanoTime() % 100000));
        long id1 = createTask(token, projectId);
        long id2 = createTask(token, projectId);
        long id3 = createTask(token, projectId);

        List<Long> original = backlogIds(token, projectId);
        assertThat(original).containsExactly(id1, id2, id3);

        List<Long> reversed = List.of(id3, id2, id1);
        String reorderBody = objectMapper.writeValueAsString(java.util.Map.of("orderedIssueIds", reversed));
        mockMvc.perform(put("/api/v1/issues/project/%d/backlog/order".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reorderBody))
                .andExpect(status().isOk());

        assertThat(backlogIds(token, projectId)).containsExactlyElementsOf(reversed);
    }

    @Test
    @DisplayName("스프린트 일괄 배정 후 백로그에서 제외, sprintId null로 복귀")
    void sprintAssignmentAndBackToBacklog() throws Exception {
        String email = "bl-s-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "BS" + (System.nanoTime() % 100000));
        long issueId = createTask(token, projectId);

        MvcResult sp = mockMvc.perform(post("/api/v1/sprints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + ",\"name\":\"S1\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long sprintId = objectMapper.readTree(sp.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String assignBody = objectMapper.writeValueAsString(
                java.util.Map.of("sprintId", sprintId, "issueIds", List.of(issueId)));
        mockMvc.perform(post("/api/v1/issues/project/%d/sprint-assignment".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignBody))
                .andExpect(status().isOk());

        assertThat(backlogIds(token, projectId)).isEmpty();

        String clearBody = objectMapper.writeValueAsString(
                java.util.Map.of("issueIds", List.of(issueId)));
        mockMvc.perform(post("/api/v1/issues/project/%d/sprint-assignment".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clearBody))
                .andExpect(status().isOk());

        assertThat(backlogIds(token, projectId)).containsExactly(issueId);
    }
}
