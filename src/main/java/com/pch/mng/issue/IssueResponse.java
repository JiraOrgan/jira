package com.pch.mng.issue;

import com.pch.mng.global.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class IssueResponse {

    @Data
    public static class MinDTO {
        private Long id;
        private String issueKey;
        private IssueType issueType;
        private String summary;
        private IssueStatus status;
        private Priority priority;
        private Integer storyPoints;
        private long backlogRank;
        private String assigneeName;
        private boolean archived;

        private MinDTO() {}

        public static MinDTO of(Issue issue) {
            MinDTO dto = new MinDTO();
            dto.id = issue.getId();
            dto.issueKey = issue.getIssueKey();
            dto.issueType = issue.getIssueType();
            dto.summary = issue.getSummary();
            dto.status = issue.getStatus();
            dto.priority = issue.getPriority();
            dto.storyPoints = issue.getStoryPoints();
            dto.backlogRank = issue.getBacklogRank();
            dto.archived = issue.isArchived();
            if (issue.getAssignee() != null) {
                dto.assigneeName = issue.getAssignee().getName();
            }
            return dto;
        }
    }

    @Data
    public static class LabelItemDTO {
        private Long id;
        private String name;
    }

    @Data
    public static class ComponentItemDTO {
        private Long id;
        private String name;
    }

    /** FR-033: GitHub/GitLab 커밋·PR URL 메타데이터. */
    @Data
    public static class VcsLinkItemDTO {
        private Long id;
        private VcsProvider provider;
        private VcsLinkKind linkKind;
        private String url;
        private String title;
        private Long createdById;
        private String createdByName;
        private LocalDateTime createdAt;
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private String issueKey;
        private Long projectId;
        private String projectKey;
        private IssueType issueType;
        private String summary;
        private String description;
        private IssueStatus status;
        private Priority priority;
        private Integer storyPoints;
        private Long assigneeId;
        private String assigneeName;
        private Long reporterId;
        private String reporterName;
        private Long parentId;
        private String parentKey;
        private Long sprintId;
        private long backlogRank;
        private SecurityLevel securityLevel;
        private LocalDate epicStartDate;
        private LocalDate epicEndDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean archived;
        private List<LabelItemDTO> labels = Collections.emptyList();
        private List<ComponentItemDTO> components = Collections.emptyList();
        private List<VcsLinkItemDTO> vcsLinks = Collections.emptyList();

        private DetailDTO() {}

        public static DetailDTO of(Issue issue) {
            return of(issue, Collections.emptyList(), Collections.emptyList());
        }

        public static DetailDTO of(Issue issue, List<IssueLabel> issueLabels, List<IssueComponent> issueComponents) {
            return of(issue, issueLabels, issueComponents, Collections.emptyList());
        }

        public static DetailDTO of(
                Issue issue,
                List<IssueLabel> issueLabels,
                List<IssueComponent> issueComponents,
                List<IssueVcsLink> vcsLinkEntities) {
            DetailDTO dto = new DetailDTO();
            dto.id = issue.getId();
            dto.issueKey = issue.getIssueKey();
            dto.issueType = issue.getIssueType();
            dto.summary = issue.getSummary();
            dto.description = issue.getDescription();
            dto.status = issue.getStatus();
            dto.priority = issue.getPriority();
            dto.storyPoints = issue.getStoryPoints();
            dto.securityLevel = issue.getSecurityLevel();
            dto.epicStartDate = issue.getEpicStartDate();
            dto.epicEndDate = issue.getEpicEndDate();
            dto.createdAt = issue.getCreatedAt();
            dto.updatedAt = issue.getUpdatedAt();
            dto.archived = issue.isArchived();
            if (issue.getProject() != null) {
                dto.projectId = issue.getProject().getId();
                dto.projectKey = issue.getProject().getKey();
            }
            if (issue.getAssignee() != null) {
                dto.assigneeId = issue.getAssignee().getId();
                dto.assigneeName = issue.getAssignee().getName();
            }
            if (issue.getReporter() != null) {
                dto.reporterId = issue.getReporter().getId();
                dto.reporterName = issue.getReporter().getName();
            }
            if (issue.getParent() != null) {
                dto.parentId = issue.getParent().getId();
                dto.parentKey = issue.getParent().getIssueKey();
            }
            if (issue.getSprint() != null) {
                dto.sprintId = issue.getSprint().getId();
            }
            dto.backlogRank = issue.getBacklogRank();
            dto.labels = issueLabels.stream().map(il -> {
                LabelItemDTO l = new LabelItemDTO();
                l.setId(il.getLabel().getId());
                l.setName(il.getLabel().getName());
                return l;
            }).toList();
            dto.components = issueComponents.stream().map(ic -> {
                ComponentItemDTO c = new ComponentItemDTO();
                c.setId(ic.getComponent().getId());
                c.setName(ic.getComponent().getName());
                return c;
            }).toList();
            dto.vcsLinks = vcsLinkEntities.stream().map(IssueResponse::toVcsLinkItem).toList();
            return dto;
        }
    }

    private static VcsLinkItemDTO toVcsLinkItem(IssueVcsLink link) {
        VcsLinkItemDTO d = new VcsLinkItemDTO();
        d.setId(link.getId());
        d.setProvider(link.getProvider());
        d.setLinkKind(link.getLinkKind());
        d.setUrl(link.getUrl());
        d.setTitle(link.getTitle());
        d.setCreatedById(link.getCreatedBy().getId());
        d.setCreatedByName(link.getCreatedBy().getName());
        d.setCreatedAt(link.getCreatedAt());
        return d;
    }
}
