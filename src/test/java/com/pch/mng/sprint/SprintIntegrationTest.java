package com.pch.mng.sprint;

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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class SprintIntegrationTest {

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

    private long createSprint(String token, long projectId, String name) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/sprints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + ",\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @DisplayName("스프린트 PLANNING → start → complete (FR-011)")
    void lifecyclePlanningToCompleted() throws Exception {
        String email = "sp-lc-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SP" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "S1");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/sprints/{id}/complete", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/sprints/{id}", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("프로젝트당 ACTIVE 스프린트는 하나만")
    void onlyOneActiveSprintPerProject() throws Exception {
        String email = "sp-1a-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "S2" + (System.nanoTime() % 100000));
        long s1 = createSprint(token, projectId, "A");
        long s2 = createSprint(token, projectId, "B");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", s1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sprints/{id}/start", s2)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PLANNING 상태에서 complete 불가")
    void completeFromPlanningRejected() throws Exception {
        String email = "sp-cp-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "S3" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "X");

        mockMvc.perform(post("/api/v1/sprints/{id}/complete", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ACTIVE 스프린트는 삭제 불가")
    void deleteActiveRejected() throws Exception {
        String email = "sp-da-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "S4" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "Run");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/sprints/{id}", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("이슈가 배정된 스프린트는 삭제 불가")
    void deleteWithIssuesRejected() throws Exception {
        String email = "sp-di-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "S5" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "WithIssues");

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"I","priority":"MEDIUM","sprintId":%d}
                                """.formatted(projectId, sprintId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/sprints/{id}", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("스프린트 완료 시 미완료 이슈는 기본적으로 제품 백로그로 이동")
    void completeMovesOpenIssuesToBacklog() throws Exception {
        String email = "sp-bk-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SB" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "ActiveRun");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"Open","priority":"MEDIUM","sprintId":%d}
                                """.formatted(projectId, sprintId)))
                .andExpect(status().isCreated())
                .andReturn();
        String issueKey = objectMapper.readTree(issueRes.getResponse().getContentAsString())
                .path("data")
                .path("issueKey")
                .asText();

        mockMvc.perform(post("/api/v1/sprints/{id}/complete", sprintId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/issues/{key}", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BACKLOG"))
                .andExpect(jsonPath("$.data.sprintId").value(nullValue()));
    }

    @Test
    @DisplayName("스프린트 완료 시 NEXT_SPRINT로 PLANNING 스프린트에 이관")
    void completeMovesOpenIssuesToNextPlanningSprint() throws Exception {
        String email = "sp-nx-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SN" + (System.nanoTime() % 100000));
        long sprintActive = createSprint(token, projectId, "Cur");
        long sprintNext = createSprint(token, projectId, "Next");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintActive)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        MvcResult issueRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"Carry","priority":"MEDIUM","sprintId":%d}
                                """.formatted(projectId, sprintActive)))
                .andExpect(status().isCreated())
                .andReturn();
        String issueKey = objectMapper.readTree(issueRes.getResponse().getContentAsString())
                .path("data")
                .path("issueKey")
                .asText();

        String body = """
                {"disposition":"NEXT_SPRINT","nextSprintId":%d}
                """.formatted(sprintNext);
        mockMvc.perform(post("/api/v1/sprints/{id}/complete", sprintActive)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/{key}", issueKey)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sprintId").value((int) sprintNext));
    }

    @Test
    @DisplayName("이슈 없는 PLANNING 스프린트 삭제 허용")
    void deleteEmptyPlanningOk() throws Exception {
        String email = "sp-de-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "S6" + (System.nanoTime() % 100000));
        long sprintId = createSprint(token, projectId, "Trash");

        mockMvc.perform(delete("/api/v1/sprints/{id}", sprintId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
