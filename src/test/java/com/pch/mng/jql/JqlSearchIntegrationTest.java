package com.pch.mng.jql;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class JqlSearchIntegrationTest {

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
                {"email":"%s","password":"password12","name":"Searcher"}
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
        dto.setName("JQL Project");
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

    private void createTask(String token, long projectId, String summary) throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"%s","priority":"MEDIUM"}
                                """.formatted(projectId, summary)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("JQL 검색: 타입·텍스트 조건 및 페이징 메타")
    void jqlSearchFiltersAndPaging() throws Exception {
        String email = "jql-s-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pk = "JQ" + (System.nanoTime() % 100000);
        long projectId = createProject(token, pk);
        createTask(token, projectId, "alpha unique token");
        createTask(token, projectId, "beta other");

        JqlSearchRequest searchReq = new JqlSearchRequest();
        searchReq.setJql("type = TASK AND text ~ \"alpha\"");
        searchReq.setStartAt(0);
        searchReq.setMaxResults(10);
        MvcResult res = mockMvc.perform(post("/api/v1/projects/%d/jql/search".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(1L);
        assertThat(data.path("issues").size()).isEqualTo(1);
        assertThat(data.path("issues").get(0).path("summary").asText()).contains("alpha");
    }

    @Test
    @DisplayName("저장 필터 CRUD")
    void savedFilterCrud() throws Exception {
        String email = "jql-f-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pk = "JF" + (System.nanoTime() % 100000);
        long projectId = createProject(token, pk);

        SavedJqlFilterRequest filterReq = new SavedJqlFilterRequest();
        filterReq.setName("내 백로그");
        filterReq.setJql("project = \"" + pk + "\" AND status = BACKLOG");
        MvcResult create = mockMvc.perform(post("/api/v1/projects/%d/jql/filters".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long filterId = objectMapper.readTree(create.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/projects/%d/jql/filters".formatted(projectId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(arr.isArray()).isTrue();
                    assertThat(arr.size()).isEqualTo(1);
                    assertThat(arr.get(0).path("name").asText()).isEqualTo("내 백로그");
                });

        mockMvc.perform(delete("/api/v1/projects/%d/jql/filters/%d".formatted(projectId, filterId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JQL: archived 미명시 시 비아카이브만, archived=true면 아카이브 포함")
    void jqlArchivedExplicitClause() throws Exception {
        String email = "jql-a-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pk = "JA" + (System.nanoTime() % 100000);
        long projectId = createProject(token, pk);

        String marker = "marker-arch-jql-" + System.nanoTime();
        MvcResult create = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"%s","priority":"MEDIUM"}
                                """.formatted(projectId, marker)))
                .andExpect(status().isCreated())
                .andReturn();
        String issueKey = objectMapper.readTree(create.getResponse().getContentAsString())
                .path("data").path("issueKey").asText();

        mockMvc.perform(put("/api/v1/issues/%s".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"archived\":true}"))
                .andExpect(status().isOk());

        JqlSearchRequest hidden = new JqlSearchRequest();
        hidden.setJql("text ~ \"" + marker + "\"");
        hidden.setStartAt(0);
        hidden.setMaxResults(10);
        mockMvc.perform(post("/api/v1/projects/%d/jql/search".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hidden)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("total").asLong()).isEqualTo(0L);
                });

        JqlSearchRequest shown = new JqlSearchRequest();
        shown.setJql("archived = true AND text ~ \"" + marker + "\"");
        shown.setStartAt(0);
        shown.setMaxResults(10);
        mockMvc.perform(post("/api/v1/projects/%d/jql/search".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shown)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("total").asLong()).isEqualTo(1L);
                    assertThat(data.path("issues").get(0).path("issueKey").asText()).isEqualTo(issueKey);
                });

        JqlSearchRequest explicitFalse = new JqlSearchRequest();
        explicitFalse.setJql("archived = false AND text ~ \"" + marker + "\"");
        explicitFalse.setStartAt(0);
        explicitFalse.setMaxResults(10);
        mockMvc.perform(post("/api/v1/projects/%d/jql/search".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(explicitFalse)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("total").asLong()).isEqualTo(0L);
                });
    }

    @Test
    @DisplayName("다른 프로젝트 키를 JQL project에 쓰면 400")
    void jqlRejectsWrongProjectKey() throws Exception {
        String email = "jql-bad-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "OK" + (System.nanoTime() % 100000));

        JqlSearchRequest badReq = new JqlSearchRequest();
        badReq.setJql("project = \"WRONG\" AND status = BACKLOG");
        mockMvc.perform(post("/api/v1/projects/%d/jql/search".formatted(projectId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest());
    }
}
