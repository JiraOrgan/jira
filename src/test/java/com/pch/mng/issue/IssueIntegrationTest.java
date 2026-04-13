package com.pch.mng.issue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.global.enums.BoardType;
import com.pch.mng.label.Label;
import com.pch.mng.label.LabelRepository;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectComponent;
import com.pch.mng.project.ProjectComponentRepository;
import com.pch.mng.project.ProjectRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class IssueIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectComponentRepository projectComponentRepository;

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

    private String createTaskAndGetKey(String token, long projectId) throws Exception {
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

    private void transitionOk(String token, String issueKey, String toStatus) throws Exception {
        mockMvc.perform(post("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"" + toStatus + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허용되지 않는 워크플로 전환은 409")
    void workflowRejectsIllegalTransition() throws Exception {
        String email = "wf-bad-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "WF" + (System.nanoTime() % 100000));
        String issueKey = createTaskAndGetKey(token, projectId);

        mockMvc.perform(post("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"DONE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("표준 전환 체인과 전환 이력 조회")
    void workflowHappyPathAndTransitionHistory() throws Exception {
        String email = "wf-ok-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "WH" + (System.nanoTime() % 100000));
        String issueKey = createTaskAndGetKey(token, projectId);

        transitionOk(token, issueKey, "SELECTED");
        transitionOk(token, issueKey, "IN_PROGRESS");
        transitionOk(token, issueKey, "CODE_REVIEW");
        transitionOk(token, issueKey, "QA");
        transitionOk(token, issueKey, "DONE");

        mockMvc.perform(get("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString())
                            .path("data");
                    assertThat(arr.isArray()).isTrue();
                    assertThat(arr.size()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("동일 상태로의 전환 요청은 멱등(이력 추가 없음)")
    void workflowIdempotentWhenAlreadyAtTargetStatus() throws Exception {
        String email = "wf-idem-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "WI" + (System.nanoTime() % 100000));
        String issueKey = createTaskAndGetKey(token, projectId);

        transitionOk(token, issueKey, "SELECTED");
        mockMvc.perform(post("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"SELECTED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/%s/transitions".formatted(issueKey))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString())
                            .path("data");
                    assertThat(arr.size()).isEqualTo(1);
                });
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
    @DisplayName("EPIC은 부모 이슈를 가질 수 없다 (PRD L1)")
    void epicCannotHaveParent() throws Exception {
        String email = "iss-epic-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "PE" + (System.nanoTime() % 100000));

        MvcResult storyRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"STORY","summary":"s","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long storyId = objectMapper.readTree(storyRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"EPIC","summary":"e","priority":"MEDIUM","parentId":%d}
                                """.formatted(projectId, storyId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SUBTASK의 부모는 EPIC이 될 수 없다 (PRD: L3는 L2 하위만)")
    void subtaskRejectParentEpic() throws Exception {
        String email = "iss-ste-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SE" + (System.nanoTime() % 100000));

        MvcResult epicRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"EPIC","summary":"e","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long epicId = objectMapper.readTree(epicRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"st","priority":"LOW","parentId":%d}
                                """.formatted(projectId, epicId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SUBTASK 아래에 SUBTASK를 둘 수 없다")
    void subtaskRejectNestedSubtask() throws Exception {
        String email = "iss-nst-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "NS" + (System.nanoTime() % 100000));

        MvcResult storyRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"STORY","summary":"s","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long storyId = objectMapper.readTree(storyRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        MvcResult subRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"a","priority":"LOW","parentId":%d}
                                """.formatted(projectId, storyId)))
                .andExpect(status().isCreated())
                .andReturn();
        long subId = objectMapper.readTree(subRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"b","priority":"LOW","parentId":%d}
                                """.formatted(projectId, subId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("STORY의 부모는 EPIC만 허용 (TASK를 부모로 두면 거절)")
    void storyRejectNonEpicParent() throws Exception {
        String email = "iss-snp-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "SN" + (System.nanoTime() % 100000));

        MvcResult taskRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"t","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long taskId = objectMapper.readTree(taskRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"STORY","summary":"s","priority":"MEDIUM","parentId":%d}
                                """.formatted(projectId, taskId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PRD 계층 허용: EPIC → STORY → SUBTASK")
    void prdHierarchyHappyPath() throws Exception {
        String email = "iss-hpy-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "HP" + (System.nanoTime() % 100000));

        MvcResult epicRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"EPIC","summary":"e","priority":"MEDIUM"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        long epicId = objectMapper.readTree(epicRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        MvcResult storyRes = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"STORY","summary":"s","priority":"MEDIUM","parentId":%d}
                                """.formatted(projectId, epicId)))
                .andExpect(status().isCreated())
                .andReturn();
        long storyId = objectMapper.readTree(storyRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"SUBTASK","summary":"st","priority":"LOW","parentId":%d}
                                """.formatted(projectId, storyId)))
                .andExpect(status().isCreated());
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

    @Test
    @DisplayName("이슈 링크 생성·목록·수정·삭제 (같은 프로젝트)")
    void issueLinkCrudHappyPath() throws Exception {
        String email = "link-ok-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        String pk = "LK" + (System.nanoTime() % 100000);
        long projectId = createProject(token, pk);
        String keyA = createTaskAndGetKey(token, projectId);
        String keyB = createTaskAndGetKey(token, projectId);

        MvcResult createRes = mockMvc.perform(post("/api/v1/issues/%s/links".formatted(keyA))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetIssueKey\":\"" + keyB + "\",\"linkType\":\"BLOCKS\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long linkId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/issues/%s/links".formatted(keyA))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(arr.isArray()).isTrue();
                    assertThat(arr.size()).isEqualTo(1);
                    assertThat(arr.get(0).path("linkType").asText()).isEqualTo("BLOCKS");
                });

        mockMvc.perform(put("/api/v1/issues/links/%d".formatted(linkId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"linkType\":\"RELATES_TO\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString())
                        .path("data").path("linkType").asText()).isEqualTo("RELATES_TO"));

        mockMvc.perform(delete("/api/v1/issues/links/%d".formatted(linkId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/%s/links".formatted(keyA))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString())
                        .path("data").size()).isEqualTo(0));
    }

    @Test
    @DisplayName("다른 프로젝트 이슈에는 링크할 수 없다")
    void issueLinkRejectsCrossProject() throws Exception {
        String email = "link-xp-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectA = createProject(token, "XA" + (System.nanoTime() % 100000));
        long projectB = createProject(token, "XB" + (System.nanoTime() % 100000));
        String keyA = createTaskAndGetKey(token, projectA);
        String keyB = createTaskAndGetKey(token, projectB);

        mockMvc.perform(post("/api/v1/issues/%s/links".formatted(keyA))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetIssueKey\":\"" + keyB + "\",\"linkType\":\"RELATES_TO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이슈에 레이블·컴포넌트 연결 및 상세 응답에 포함")
    void issueLabelAndComponentAttach() throws Exception {
        String email = "lc-ok-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "LC" + (System.nanoTime() % 100000));
        String issueKey = createTaskAndGetKey(token, projectId);

        Project project = projectRepository.findById(projectId).orElseThrow();
        Label label = labelRepository.save(Label.builder().name("lbl-" + System.nanoTime()).build());
        ProjectComponent comp = projectComponentRepository.save(
                ProjectComponent.builder().project(project).name("api").build());

        mockMvc.perform(post("/api/v1/issues/%s/labels".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelId\":" + label.getId() + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/issues/%s/components".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"componentId\":" + comp.getId() + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/issues/%s".formatted(issueKey))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("labels").size()).isEqualTo(1);
                    assertThat(data.path("labels").get(0).path("name").asText()).isEqualTo(label.getName());
                    assertThat(data.path("components").size()).isEqualTo(1);
                    assertThat(data.path("components").get(0).path("name").asText()).isEqualTo("api");
                });

        mockMvc.perform(delete("/api/v1/issues/%s/labels/%d".formatted(issueKey, label.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/issues/%s/components/%d".formatted(issueKey, comp.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/issues/%s".formatted(issueKey))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                    assertThat(data.path("labels").size()).isEqualTo(0);
                    assertThat(data.path("components").size()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("다른 프로젝트 컴포넌트는 이슈에 연결할 수 없다")
    void componentMustMatchIssueProject() throws Exception {
        String email = "lc-xp-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectA = createProject(token, "CA" + (System.nanoTime() % 100000));
        long projectB = createProject(token, "CB" + (System.nanoTime() % 100000));
        String issueKey = createTaskAndGetKey(token, projectA);
        Project projB = projectRepository.findById(projectB).orElseThrow();
        ProjectComponent compB = projectComponentRepository.save(
                ProjectComponent.builder().project(projB).name("other").build());

        mockMvc.perform(post("/api/v1/issues/%s/components".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"componentId\":" + compB.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이슈 생성·수정 시 감사 로그가 쌓인다 (프로젝트 ADMIN)")
    void auditLogsRecordedOnIssueCreateAndUpdate() throws Exception {
        String email = "aud-" + System.nanoTime() + "@ex.com";
        String token = registerAndLogin(email);
        long projectId = createProject(token, "AD" + (System.nanoTime() % 100000));
        MvcResult create = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"issueType":"TASK","summary":"S1","priority":"HIGH"}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(create.getResponse().getContentAsString()).path("data");
        long issueId = created.path("id").asLong();
        String issueKey = created.path("issueKey").asText();

        MvcResult audit1 = mockMvc.perform(get("/api/v1/audit-logs/issue/" + issueId)
                        .header("Authorization", "Bearer " + token)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        int n1 = objectMapper.readTree(audit1.getResponse().getContentAsString())
                .path("data").path("content").size();
        assertThat(n1).isGreaterThanOrEqualTo(1);

        mockMvc.perform(put("/api/v1/issues/%s".formatted(issueKey))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"summary\":\"S2\"}"))
                .andExpect(status().isOk());

        MvcResult audit2 = mockMvc.perform(get("/api/v1/audit-logs/issue/" + issueId)
                        .header("Authorization", "Bearer " + token)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        int n2 = objectMapper.readTree(audit2.getResponse().getContentAsString())
                .path("data").path("content").size();
        assertThat(n2).isGreaterThan(n1);
    }
}
