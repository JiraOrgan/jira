package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
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
        return IssueResponse.DetailDTO.of(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO save(IssueRequest.SaveDTO reqDTO, Long reporterId) {
        Project project = projectRepository.findById(reqDTO.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount reporter = userAccountRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이슈 키 생성: 프로젝트 키 + 시퀀스
        long count = issueRepository.findByProjectIdOrderByCreatedAtDesc(project.getId(), Pageable.unpaged()).getTotalElements();
        String issueKey = project.getKey() + "-" + (count + 1);

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
        if (reqDTO.getParentId() != null) {
            Issue parent = issueRepository.findById(reqDTO.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            builder.parent(parent);
        }
        if (reqDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(reqDTO.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            builder.sprint(sprint);
        }

        Issue issue = builder.build();
        issueRepository.save(issue);
        return IssueResponse.DetailDTO.of(issue);
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
            Sprint sprint = sprintRepository.findById(reqDTO.getSprintId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            issue.setSprint(sprint);
        }

        return IssueResponse.DetailDTO.of(issue);
    }

    @Transactional
    public void delete(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueRepository.delete(issue);
    }

    @Transactional
    public IssueResponse.DetailDTO transition(String issueKey, IssueRequest.TransitionDTO reqDTO) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        // TODO: 워크플로우 전환 규칙 검증 로직 추가
        issue.setStatus(reqDTO.getToStatus());
        return IssueResponse.DetailDTO.of(issue);
    }
}
