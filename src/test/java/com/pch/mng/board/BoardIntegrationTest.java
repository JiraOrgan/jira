package com.pch.mng.board;

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
class BoardIntegrationTest {

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

    private long createStartedSprint(String token, long projectId) throws Exception {
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
        return sprintId;
    }

    private void createIssueInSprint(String token, long projectId, long sprintId) throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"B","priority":"MEDIUM","sprintId":%d}
                                """.formatted(projectId, sprintId)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("스프린트 보드는 6개 컬럼과 swimlane NONE 단일 버킷")
    void sprintBoardNoneSwimlane() throws Exception {
        String email = "bd-n-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "BD" + (System.nanoTime() % 100000));
        long sprintId = createStartedSprint(token, projectId);
        createIssueInSprint(token, projectId, sprintId);

        MvcResult res = mockMvc.perform(get("/api/v1/sprints/{id}/board", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        assertThat(root.path("swimlane").asText()).isEqualTo("NONE");
        JsonNode columns = root.path("columns");
        assertThat(columns.size()).isEqualTo(6);
        JsonNode backlogCol = columns.get(0);
        assertThat(backlogCol.path("status").asText()).isEqualTo("BACKLOG");
        assertThat(backlogCol.path("buckets").size()).isEqualTo(1);
        assertThat(backlogCol.path("buckets").get(0).path("issues").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("swimlane=ASSIGNEE 시 미배정 이슈가 한 버킷에 모임")
    void sprintBoardAssigneeSwimlane() throws Exception {
        String email = "bd-a-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "BA" + (System.nanoTime() % 100000));
        long sprintId = createStartedSprint(token, projectId);
        createIssueInSprint(token, projectId, sprintId);
        createIssueInSprint(token, projectId, sprintId);

        MvcResult res = mockMvc.perform(get("/api/v1/sprints/{id}/board", sprintId)
                        .param("swimlane", "ASSIGNEE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        assertThat(root.path("swimlane").asText()).isEqualTo("ASSIGNEE");
        JsonNode backlogCol = root.path("columns").get(0);
        assertThat(backlogCol.path("buckets").size()).isEqualTo(1);
        assertThat(backlogCol.path("buckets").get(0).path("issues").size()).isEqualTo(2);
    }
}
