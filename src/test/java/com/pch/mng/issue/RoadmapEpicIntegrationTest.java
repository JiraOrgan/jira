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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class RoadmapEpicIntegrationTest {

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
        dto.setName("Roadmap P");
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
    @DisplayName("로드맵 API: Epic만 반환하고 effective 날짜 포함")
    void roadmapListsEpicsWithEffectiveDates() throws Exception {
        String email = "rm-ok-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pk = "RM" + (System.nanoTime() % 100000);
        long projectId = createProject(token, pk);

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"EPIC","summary":"E1","priority":"MEDIUM","epicStartDate":"2026-01-01","epicEndDate":"2026-06-30"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"projectId\":%d,\"issueType\":\"TASK\",\"summary\":\"T1\",\"priority\":\"MEDIUM\"}"
                                        .formatted(projectId)))
                .andExpect(status().isCreated());

        MvcResult res = mockMvc.perform(get("/api/v1/projects/%d/roadmap/epics".formatted(projectId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data).hasSize(1);
        JsonNode row = data.get(0);
        assertThat(row.path("issueKey").asText()).startsWith(pk);
        assertThat(row.path("effectiveStart").asText()).isEqualTo("2026-01-01");
        assertThat(row.path("effectiveEnd").asText()).isEqualTo("2026-06-30");
    }

    @Test
    @DisplayName("비-Epic 이슈 생성 시 Epic 기간 필드 전송하면 400")
    void taskWithEpicDatesRejected() throws Exception {
        String email = "rm-bad-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "RX" + (System.nanoTime() % 100000));

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"T","priority":"MEDIUM","epicStartDate":"2026-01-01"}
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Epic 종료일이 시작일보다 이전이면 400")
    void epicInvalidRangeRejected() throws Exception {
        String email = "rm-rng-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "RY" + (System.nanoTime() % 100000));

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"EPIC","summary":"E","priority":"MEDIUM","epicStartDate":"2026-06-01","epicEndDate":"2026-01-01"}
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest());
    }
}
