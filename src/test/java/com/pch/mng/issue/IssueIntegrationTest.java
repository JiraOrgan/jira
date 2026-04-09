package com.pch.mng.issue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardType;
import com.pch.mng.project.ProjectRequest;
import com.pch.mng.sprint.SprintRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class IssueIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
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

    @Test
    @DisplayName("같은 프로젝트에서 이슈 키 번호가 단조 증가한다")
    void issueKeysIncrementMonotonically() throws Exception {
        String email = "iss-seq-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String key = "IK" + (System.nanoTime() % 100000);
        long projectId = createProject(token, key);

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"A","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated());

        MvcResult second = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"BUG","summary":"B","priority":"HIGH"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueKey = objectMapper.readTree(second.getResponse().getContentAsString())
                .path("data").path("issueKey").asText();
        assertThat(issueKey).isEqualTo(key + "-2");
    }

    @Test
    @DisplayName("SUBTASK는 부모 없이 생성할 수 없다")
    void subtaskRequiresParent() throws Exception {
        String email = "iss-sub-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SK" + (System.nanoTime() % 100000));

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"X","priority":"LOW"}
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("부모 이슈는 같은 프로젝트에 있어야 한다")
    void parentMustBeSameProject() throws Exception {
        String email = "iss-par-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectA = createProject(token, "PA" + (System.nanoTime() % 100000));
        long projectB = createProject(token, "PB" + (System.nanoTime() % 100000));

        MvcResult parentRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"STORY","summary":"parent","priority":"MEDIUM"}
                                """.formatted(projectA)))
                .andExpect(status().isCreated())
                .andReturn();
        long parentId = objectMapper.readTree(parentRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"child","priority":"LOW","parentId":%d}
                                """.formatted(projectB, parentId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("스프린트는 이슈 프로젝트와 일치해야 한다")
    void sprintMustMatchProject() throws Exception {
        String email = "iss-spr-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectA = createProject(token, "SA" + (System.nanoTime() % 100000));
        long projectB = createProject(token, "SB" + (System.nanoTime() % 100000));

        SprintRequest.SaveDTO sprintReq = new SprintRequest.SaveDTO();
        sprintReq.setProjectId(projectB);
        sprintReq.setName("S1");
        MvcResult sprintRes = mockMvc.perform(post("/api/v1/sprints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sprintReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long sprintId = objectMapper.readTree(sprintRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"t","priority":"MEDIUM","sprintId":%d}
                                """.formatted(projectA, sprintId)))
                .andExpect(status().isBadRequest());
    }
}
