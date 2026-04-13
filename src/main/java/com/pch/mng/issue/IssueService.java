package com.pch.mng.issue;

import com.pch.mng.global.enums.BoardType;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.label.Label;
import com.pch.mng.label.LabelRepository;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectComponent;
import com.pch.mng.project.ProjectComponentRepository;
import com.pch.mng.project.ProjectRepository;
import com.pch.mng.project.WipLimitRepository;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import com.pch.mng.workflow.IssueWorkflowPolicy;
import com.pch.mng.workflow.WorkflowTransition;
import com.pch.mng.workflow.WorkflowTransitionRepository;
import com.pch.mng.workflow.WorkflowTransitionResponse;
import com.pch.mng.audit.IssueAuditService;
import com.pch.mng.board.SprintBoardRedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserAccountRepository userAccountRepository;
    private final SprintRepository sprintRepository;
    private final IssueWorkflowPolicy issueWorkflowPolicy;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final IssueLabelRepository issueLabelRepository;
    private final IssueComponentRepository issueComponentRepository;
    private final LabelRepository labelRepository;
    private final ProjectComponentRepository projectComponentRepository;
    private final WipLimitRepository wipLimitRepository;
    private final SprintBoardRedisCache sprintBoardRedisCache;
    private final IssueAuditService issueAuditService;

    public Page<IssueResponse.MinDTO> findByProject(Long projectId, Pageable pageable) {
        return issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(IssueResponse.MinDTO::of);
    }

    public List<IssueResponse.MinDTO> findBacklog(Long projectId) {
        return issueRepository.findByProjectIdAndSprintIsNullOrderByBacklogRankAscIdAsc(projectId).stream()
                .map(IssueResponse.MinDTO::of)
                .toList();
    }

    public List<RoadmapEpicResponse> listRoadmapEpics(Long projectId) {
        return issueRepository
                .findByProjectIdAndIssueTypeOrderByEpicStartDateAscIdAsc(projectId, IssueType.EPIC)
                .stream()
                .map(RoadmapEpicResponse::of)
                .toList();
    }

    public IssueResponse.DetailDTO findByKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKeyWithDetails(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return toDetail(issue);
    }

    public List<WorkflowTransitionResponse.DetailDTO> findTransitionsByIssueKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return workflowTransitionRepository.findByIssueIdWithActorOrderByTransitionedAtDesc(issue.getId()).stream()
                .map(WorkflowTransitionResponse.DetailDTO::of)
                .toList();
    }

    @Transactional
    public IssueResponse.DetailDTO save(IssueRequest.SaveDTO reqDTO, Long reporterId) {
        Project project = projectRepository.findByIdForUpdate(reqDTO.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount reporter = userAccountRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Issue parent = resolveAndValidateParent(reqDTO.getIssueType(), reqDTO.getParentId(), project);

        Sprint sprint = null;
        if (reqDTO.getSprintId() != null) {
            sprint = sprintRepository.findByIdWithProject(reqDTO.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            assertSprintOpenAndSameProject(sprint, project);
        }

        long backlogRank = sprint == null
                ? issueRepository.maxBacklogRankForProjectBacklog(project.getId()) + 1000L
                : 0L;

        assertEpicDatesOnlyForEpicType(reqDTO.getIssueType(), reqDTO.getEpicStartDate(), reqDTO.getEpicEndDate());
        validateEpicStartEndOrder(reqDTO.getEpicStartDate(), reqDTO.getEpicEndDate());

        long nextSeq = project.getIssueSequence() + 1;
        project.setIssueSequence(nextSeq);
        String issueKey = project.getKey() + "-" + nextSeq;

        Issue.IssueBuilder builder = Issue.builder()
                .issueKey(issueKey)
                .project(project)
                .issueType(reqDTO.getIssueType())
                .summary(reqDTO.getSummary())
                .description(reqDTO.getDescription())
                .status(IssueStatus.BACKLOG)
                .priority(reqDTO.getPriority())
                .storyPoints(reqDTO.getStoryPoints())
                .reporter(reporter)
                .backlogRank(backlogRank)
                .securityLevel(reqDTO.getSecurityLevel());

        if (reqDTO.getAssigneeId() != null) {
            UserAccount assignee = userAccountRepository.findById(reqDTO.getAssigneeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            builder.assignee(assignee);
        }
        if (parent != null) {
            builder.parent(parent);
        }
        if (sprint != null) {
            builder.sprint(sprint);
        }
        if (reqDTO.getIssueType() == IssueType.EPIC) {
            builder.epicStartDate(reqDTO.getEpicStartDate()).epicEndDate(reqDTO.getEpicEndDate());
        }

        Issue issue = builder.build();
        issueRepository.save(issue);
        issueAuditService.logIssueCreated(issue, reporter);
        evictBoardForSprintOf(issue);
        return toDetail(issue);
    }

    private void assertWipAllows(Project project, IssueStatus toStatus) {
        if (project.getBoardType() != BoardType.KANBAN) {
            return;
        }
        wipLimitRepository.findByProjectIdAndStatus(project.getId(), toStatus).ifPresent(limit -> {
            long n = issueRepository.countByProjectIdAndStatus(project.getId(), toStatus);
            if (n >= limit.getMaxIssues()) {
                throw new BusinessException(ErrorCode.WIP_LIMIT_EXCEEDED);
            }
        });
    }

    private void assertSprintOpenAndSameProject(Sprint sprint, Project project) {
        if (!sprint.getProject().getId().equals(project.getId())) {
            throw new BusinessException(ErrorCode.SPRINT_PROJECT_MISMATCH);
        }
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SPRINT_NOT_ASSIGNABLE);
        }
    }

    private Issue resolveAndValidateParent(IssueType issueType, Long parentId, Project project) {
        Issue parent = null;
        if (parentId != null) {
            parent = issueRepository.findByIdWithProject(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            if (!parent.getProject().getId().equals(project.getId())) {
                throw new BusinessException(ErrorCode.ISSUE_PARENT_PROJECT_MISMATCH);
            }
        }
        IssueHierarchyPolicy.assertValidParent(issueType, parent);
        return parent;
    }

    @Transactional
    public IssueResponse.DetailDTO update(String issueKey, IssueRequest.UpdateDTO reqDTO) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Long oldSprintId = issue.getSprint() != null ? issue.getSprint().getId() : null;

        assertEpicUpdateAllowedForIssueType(issue, reqDTO);

        if (reqDTO.getSummary() != null && !Objects.equals(issue.getSummary(), reqDTO.getSummary())) {
            String old = issue.getSummary();
            issue.setSummary(reqDTO.getSummary());
            issueAuditService.recordFromContext(issue, "summary", old, reqDTO.getSummary());
        }
        if (reqDTO.getDescription() != null
                && !Objects.equals(issue.getDescription(), reqDTO.getDescription())) {
            String old = issue.getDescription();
            issue.setDescription(reqDTO.getDescription());
            issueAuditService.recordFromContext(issue, "description", old, reqDTO.getDescription());
        }
        if (reqDTO.getPriority() != null && issue.getPriority() != reqDTO.getPriority()) {
            Priority old = issue.getPriority();
            issue.setPriority(reqDTO.getPriority());
            issueAuditService.recordFromContext(issue, "priority", String.valueOf(old),
                    String.valueOf(reqDTO.getPriority()));
        }
        if (reqDTO.getStoryPoints() != null
                && !Objects.equals(issue.getStoryPoints(), reqDTO.getStoryPoints())) {
            Integer old = issue.getStoryPoints();
            issue.setStoryPoints(reqDTO.getStoryPoints());
            issueAuditService.recordFromContext(issue, "storyPoints",
                    old != null ? String.valueOf(old) : null,
                    String.valueOf(reqDTO.getStoryPoints()));
        }
        if (reqDTO.getSecurityLevel() != null && issue.getSecurityLevel() != reqDTO.getSecurityLevel()) {
            var old = issue.getSecurityLevel();
            issue.setSecurityLevel(reqDTO.getSecurityLevel());
            issueAuditService.recordFromContext(issue, "securityLevel",
                    old != null ? String.valueOf(old) : null,
                    String.valueOf(reqDTO.getSecurityLevel()));
        }
        if (reqDTO.getAssigneeId() != null) {
            UserAccount assignee = userAccountRepository.findById(reqDTO.getAssigneeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            Long oldId = issue.getAssignee() != null ? issue.getAssignee().getId() : null;
            Long newId = assignee.getId();
            if (!Objects.equals(oldId, newId)) {
                issue.setAssignee(assignee);
                issueAuditService.recordFromContext(issue, "assigneeId",
                        oldId != null ? String.valueOf(oldId) : null, String.valueOf(newId));
            }
        }
        if (reqDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdWithProject(reqDTO.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            assertSprintOpenAndSameProject(sprint, issue.getProject());
            Long oldSid = issue.getSprint() != null ? issue.getSprint().getId() : null;
            Long newSid = sprint.getId();
            issue.setSprint(sprint);
            issue.setBacklogRank(0L);
            if (!Objects.equals(oldSid, newSid)) {
                issueAuditService.recordFromContext(issue, "sprintId",
                        oldSid != null ? String.valueOf(oldSid) : null, String.valueOf(newSid));
            }
        }

        LocalDate epicStartBefore = issue.getEpicStartDate();
        LocalDate epicEndBefore = issue.getEpicEndDate();
        patchEpicDatesOnUpdate(issue, reqDTO);
        if (!Objects.equals(epicStartBefore, issue.getEpicStartDate())) {
            issueAuditService.recordFromContext(issue, "epicStartDate",
                    epicStartBefore != null ? String.valueOf(epicStartBefore) : null,
                    issue.getEpicStartDate() != null ? String.valueOf(issue.getEpicStartDate()) : null);
        }
        if (!Objects.equals(epicEndBefore, issue.getEpicEndDate())) {
            issueAuditService.recordFromContext(issue, "epicEndDate",
                    epicEndBefore != null ? String.valueOf(epicEndBefore) : null,
                    issue.getEpicEndDate() != null ? String.valueOf(issue.getEpicEndDate()) : null);
        }

        Long newSprintId = issue.getSprint() != null ? issue.getSprint().getId() : null;
        evictBoardForSprintPair(oldSprintId, newSprintId);
        return toDetail(issue);
    }

    @Transactional
    public List<IssueResponse.MinDTO> reorderBacklog(Long projectId, IssueRequest.BacklogReorderDTO dto) {
        List<Long> ordered = dto.getOrderedIssueIds();
        List<Issue> backlog = issueRepository.findByProjectIdAndSprintIsNullOrderByBacklogRankAscIdAsc(projectId);
        Set<Long> expected = new HashSet<>();
        for (Issue i : backlog) {
            expected.add(i.getId());
        }
        if (ordered.size() != expected.size() || ordered.stream().distinct().count() != ordered.size()) {
            throw new BusinessException(ErrorCode.BACKLOG_REORDER_INVALID);
        }
        if (!expected.equals(new HashSet<>(ordered))) {
            throw new BusinessException(ErrorCode.BACKLOG_REORDER_INVALID);
        }
        long step = 1000L;
        for (int i = 0; i < ordered.size(); i++) {
            Long id = ordered.get(i);
            Issue issue = issueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BACKLOG_REORDER_INVALID));
            if (!issue.getProject().getId().equals(projectId) || issue.getSprint() != null) {
                throw new BusinessException(ErrorCode.BACKLOG_REORDER_INVALID);
            }
            issue.setBacklogRank(i * step);
        }
        return findBacklog(projectId);
    }

    @Transactional
    public void assignSprintToIssues(Long projectId, IssueRequest.SprintAssignmentDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Sprint sprint = null;
        if (dto.getSprintId() != null) {
            sprint = sprintRepository.findByIdWithProject(dto.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            assertSprintOpenAndSameProject(sprint, project);
        }
        Set<Long> sprintsToEvict = new HashSet<>();
        if (dto.getSprintId() != null) {
            sprintsToEvict.add(dto.getSprintId());
        }
        long rankCursor = issueRepository.maxBacklogRankForProjectBacklog(projectId);
        for (Long issueId : dto.getIssueIds()) {
            Issue issue = issueRepository.findByIdWithProject(issueId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            if (!issue.getProject().getId().equals(projectId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (issue.getSprint() != null) {
                sprintsToEvict.add(issue.getSprint().getId());
            }
            Long oldSid = issue.getSprint() != null ? issue.getSprint().getId() : null;
            issue.setSprint(sprint);
            if (sprint == null) {
                rankCursor += 1000L;
                issue.setBacklogRank(rankCursor);
            } else {
                issue.setBacklogRank(0L);
            }
            Long newSid = sprint != null ? sprint.getId() : null;
            if (!Objects.equals(oldSid, newSid)) {
                issueAuditService.recordFromContext(issue, "sprintId",
                        oldSid != null ? String.valueOf(oldSid) : null,
                        newSid != null ? String.valueOf(newSid) : null);
            }
        }
        for (Long sid : sprintsToEvict) {
            sprintBoardRedisCache.evictSprint(sid);
        }
    }

    @Transactional
    public void delete(String issueKey) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Long sprintId = issue.getSprint() != null ? issue.getSprint().getId() : null;
        issueAuditService.deleteAllForIssue(issue.getId());
        issueRepository.delete(issue);
        sprintBoardRedisCache.evictSprint(sprintId);
    }

    @Transactional
    public IssueResponse.DetailDTO transition(String issueKey, IssueRequest.TransitionDTO reqDTO, Long actorUserId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        IssueStatus from = issue.getStatus();
        IssueStatus to = reqDTO.getToStatus();
        if (from == to) {
            return toDetail(issue);
        }
        issueWorkflowPolicy.assertTransition(from, to);
        assertWipAllows(issue.getProject(), to);
        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        issue.setStatus(to);
        issueRepository.save(issue);
        issueAuditService.record(issue, actor, "status", String.valueOf(from), String.valueOf(to));

        WorkflowTransition log = new WorkflowTransition();
        log.setIssue(issue);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setChangedBy(actor);
        log.setConditionNote(reqDTO.getConditionNote());
        workflowTransitionRepository.save(log);

        evictBoardForSprintOf(issue);
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO addLabel(String issueKey, Long labelId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (issueLabelRepository.existsByIssue_IdAndLabel_Id(issue.getId(), labelId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        IssueLabel link = new IssueLabel();
        link.setIssue(issue);
        link.setLabel(label);
        issueLabelRepository.save(link);
        issueAuditService.recordFromContext(issue, "label.added", null, label.getName());
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO removeLabel(String issueKey, Long labelId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!issueLabelRepository.existsByIssue_IdAndLabel_Id(issue.getId(), labelId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueLabelRepository.deleteByIssueIdAndLabelId(issue.getId(), labelId);
        issueAuditService.recordFromContext(issue, "label.removed", label.getName(), null);
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO addComponent(String issueKey, Long componentId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        ProjectComponent component = projectComponentRepository.findByIdWithProject(componentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!component.getProject().getId().equals(issue.getProject().getId())) {
            throw new BusinessException(ErrorCode.COMPONENT_PROJECT_MISMATCH);
        }
        if (issueComponentRepository.existsByIssue_IdAndComponent_Id(issue.getId(), componentId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        IssueComponent link = new IssueComponent();
        link.setIssue(issue);
        link.setComponent(component);
        issueComponentRepository.save(link);
        issueAuditService.recordFromContext(issue, "component.added", null, component.getName());
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO removeComponent(String issueKey, Long componentId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!issueComponentRepository.existsByIssue_IdAndComponent_Id(issue.getId(), componentId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        ProjectComponent component = projectComponentRepository.findById(componentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueComponentRepository.deleteByIssue_IdAndComponent_Id(issue.getId(), componentId);
        issueAuditService.recordFromContext(issue, "component.removed", component.getName(), null);
        return toDetail(issue);
    }

    private static void assertEpicDatesOnlyForEpicType(IssueType type, LocalDate start, LocalDate end) {
        if (type == IssueType.EPIC) {
            return;
        }
        if (start != null || end != null) {
            throw new BusinessException(ErrorCode.EPIC_DATE_NOT_ALLOWED);
        }
    }

    private static void validateEpicStartEndOrder(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(ErrorCode.EPIC_DATE_RANGE_INVALID);
        }
    }

    private static void assertEpicUpdateAllowedForIssueType(Issue issue, IssueRequest.UpdateDTO dto) {
        if (issue.getIssueType() == IssueType.EPIC) {
            return;
        }
        if (Boolean.TRUE.equals(dto.getClearEpicDates())) {
            throw new BusinessException(ErrorCode.EPIC_DATE_NOT_ALLOWED);
        }
        if (Boolean.TRUE.equals(dto.getPatchEpicDates())) {
            throw new BusinessException(ErrorCode.EPIC_DATE_NOT_ALLOWED);
        }
        if (dto.getEpicStartDate() != null || dto.getEpicEndDate() != null) {
            throw new BusinessException(ErrorCode.EPIC_DATE_NOT_ALLOWED);
        }
    }

    private static void patchEpicDatesOnUpdate(Issue issue, IssueRequest.UpdateDTO dto) {
        if (issue.getIssueType() != IssueType.EPIC) {
            return;
        }
        if (Boolean.TRUE.equals(dto.getClearEpicDates())) {
            issue.setEpicStartDate(null);
            issue.setEpicEndDate(null);
            return;
        }
        if (Boolean.TRUE.equals(dto.getPatchEpicDates())) {
            issue.setEpicStartDate(dto.getEpicStartDate());
            issue.setEpicEndDate(dto.getEpicEndDate());
            validateEpicStartEndOrder(issue.getEpicStartDate(), issue.getEpicEndDate());
            return;
        }
        if (dto.getEpicStartDate() != null) {
            issue.setEpicStartDate(dto.getEpicStartDate());
        }
        if (dto.getEpicEndDate() != null) {
            issue.setEpicEndDate(dto.getEpicEndDate());
        }
        validateEpicStartEndOrder(issue.getEpicStartDate(), issue.getEpicEndDate());
    }

    private void evictBoardForSprintOf(Issue issue) {
        if (issue.getSprint() != null) {
            sprintBoardRedisCache.evictSprint(issue.getSprint().getId());
        }
    }

    private void evictBoardForSprintPair(Long oldSprintId, Long newSprintId) {
        if (Objects.equals(oldSprintId, newSprintId)) {
            sprintBoardRedisCache.evictSprint(oldSprintId);
        } else {
            sprintBoardRedisCache.evictSprint(oldSprintId);
            sprintBoardRedisCache.evictSprint(newSprintId);
        }
    }

    private IssueResponse.DetailDTO toDetail(Issue issue) {
        List<IssueLabel> labels = issueLabelRepository.findByIssueIdWithLabel(issue.getId());
        List<IssueComponent> components = issueComponentRepository.findByIssueIdWithComponent(issue.getId());
        return IssueResponse.DetailDTO.of(issue, labels, components);
    }
}
