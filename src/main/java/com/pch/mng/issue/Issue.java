package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.Priority;
import com.pch.mng.global.enums.SecurityLevel;
import com.pch.mng.project.Project;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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

    /** 스프린트 미배정(backlog) 이슈 정렬용. 스프린트에 넣은 이슈는 0으로 둘 수 있다. */
    @Column(nullable = false)
    private long backlogRank;

    @Enumerated(EnumType.STRING)
    private SecurityLevel securityLevel;

    /** Epic 전용 로드맵 기간 (FR-012). 비-Epic는 null 유지. */
    @Column(name = "epic_start_date")
    private LocalDate epicStartDate;

    @Column(name = "epic_end_date")
    private LocalDate epicEndDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Issue(String issueKey, Project project, IssueType issueType, String summary,
                 String description, IssueStatus status, Priority priority, Integer storyPoints,
                 UserAccount assignee, UserAccount reporter, Issue parent, Sprint sprint,
                 long backlogRank, SecurityLevel securityLevel,
                 LocalDate epicStartDate, LocalDate epicEndDate) {
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
        this.backlogRank = backlogRank;
        this.securityLevel = securityLevel;
        this.epicStartDate = epicStartDate;
        this.epicEndDate = epicEndDate;
    }
}
