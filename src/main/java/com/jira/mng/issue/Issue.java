package com.jira.mng.issue;

import com.jira.mng.global.enums.IssueStatus;
import com.jira.mng.global.enums.IssueType;
import com.jira.mng.global.enums.Priority;
import com.jira.mng.global.enums.SecurityLevel;
import com.jira.mng.project.Project;
import com.jira.mng.sprint.Sprint;
import com.jira.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_tb")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String issueKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType issueType;

    @Column(nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private Integer storyPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserAccount assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserAccount reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Issue parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @Enumerated(EnumType.STRING)
    private SecurityLevel securityLevel;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Issue(String issueKey, Project project, IssueType issueType, String summary,
                 String description, IssueStatus status, Priority priority, Integer storyPoints,
                 UserAccount assignee, UserAccount reporter, Issue parent, Sprint sprint,
                 SecurityLevel securityLevel) {
        this.issueKey = issueKey;
        this.project = project;
        this.issueType = issueType;
        this.summary = summary;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.storyPoints = storyPoints;
        this.assignee = assignee;
        this.reporter = reporter;
        this.parent = parent;
        this.sprint = sprint;
        this.securityLevel = securityLevel;
    }
}
