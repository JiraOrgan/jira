package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.label.Label;
import com.pch.mng.label.LabelRepository;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectComponent;
import com.pch.mng.project.ProjectComponentRepository;
import com.pch.mng.project.ProjectRepository;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import com.pch.mng.workflow.IssueWorkflowPolicy;
import com.pch.mng.workflow.WorkflowTransition;
import com.pch.mng.workflow.WorkflowTransitionRepository;
import com.pch.mng.workflow.WorkflowTransitionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public Page<IssueResponse.MinDTO> findByProject(Long projectId, Pageable pageable) {
        return issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(IssueResponse.MinDTO::of);
    }

    public List<IssueResponse.MinDTO> findBacklog(Long projectId) {
        return issueRepository.findByProjectIdAndSprintIsNullOrderByCreatedAtDesc(projectId).stream()
                .map(IssueResponse.MinDTO::of)
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
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new BusinessException(ErrorCode.SPRINT_PROJECT_MISMATCH);
            }
        }

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

        Issue issue = builder.build();
        issueRepository.save(issue);
        return toDetail(issue);
    }

    private Issue resolveAndValidateParent(IssueType issueType, Long parentId, Project project) {
        if (issueType == IssueType.SUBTASK) {
            if (parentId == null) {
                throw new BusinessException(ErrorCode.INVALID_ISSUE_HIERARCHY);
            }
        }
        if (parentId == null) {
            return null;
        }
        Issue p = issueRepository.findByIdWithProject(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!p.getProject().getId().equals(project.getId())) {
            throw new BusinessException(ErrorCode.ISSUE_PARENT_PROJECT_MISMATCH);
        }
        return p;
    }

    @Transactional
    public IssueResponse.DetailDTO update(String issueKey, IssueRequest.UpdateDTO reqDTO) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (reqDTO.getSummary() != null) issue.setSummary(reqDTO.getSummary());
        if (reqDTO.getDescription() != null) issue.setDescription(reqDTO.getDescription());
        if (reqDTO.getPriority() != null) issue.setPriority(reqDTO.getPriority());
        if (reqDTO.getStoryPoints() != null) issue.setStoryPoints(reqDTO.getStoryPoints());
        if (reqDTO.getSecurityLevel() != null) issue.setSecurityLevel(reqDTO.getSecurityLevel());
        if (reqDTO.getAssigneeId() != null) {
            UserAccount assignee = userAccountRepository.findById(reqDTO.getAssigneeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            issue.setAssignee(assignee);
        }
        if (reqDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdWithProject(reqDTO.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            if (!sprint.getProject().getId().equals(issue.getProject().getId())) {
                throw new BusinessException(ErrorCode.SPRINT_PROJECT_MISMATCH);
            }
            issue.setSprint(sprint);
        }

        return toDetail(issue);
    }

    @Transactional
    public void delete(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueRepository.delete(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO transition(String issueKey, IssueRequest.TransitionDTO reqDTO, Long actorUserId) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        IssueStatus from = issue.getStatus();
        IssueStatus to = reqDTO.getToStatus();
        if (from == to) {
            return toDetail(issue);
        }
        issueWorkflowPolicy.assertTransition(from, to);
        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        issue.setStatus(to);
        issueRepository.save(issue);

        WorkflowTransition log = new WorkflowTransition();
        log.setIssue(issue);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setChangedBy(actor);
        log.setConditionNote(reqDTO.getConditionNote());
        workflowTransitionRepository.save(log);

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
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO removeLabel(String issueKey, Long labelId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!issueLabelRepository.existsByIssue_IdAndLabel_Id(issue.getId(), labelId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        issueLabelRepository.deleteByIssueIdAndLabelId(issue.getId(), labelId);
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
        return toDetail(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO removeComponent(String issueKey, Long componentId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!issueComponentRepository.existsByIssue_IdAndComponent_Id(issue.getId(), componentId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        issueComponentRepository.deleteByIssue_IdAndComponent_Id(issue.getId(), componentId);
        return toDetail(issue);
    }

    private IssueResponse.DetailDTO toDetail(Issue issue) {
        List<IssueLabel> labels = issueLabelRepository.findByIssueIdWithLabel(issue.getId());
        List<IssueComponent> components = issueComponentRepository.findByIssueIdWithComponent(issue.getId());
        return IssueResponse.DetailDTO.of(issue, labels, components);
    }
}
