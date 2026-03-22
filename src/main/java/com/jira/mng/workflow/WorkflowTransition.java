package com.jira.mng.workflow;

import com.jira.mng.global.enums.IssueStatus;
import com.jira.mng.issue.Issue;
import com.jira.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "workflow_transition_tb")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserAccount changedBy;

    @Column(columnDefinition = "TEXT")
    private String conditionNote;

    @CreationTimestamp
    private LocalDateTime transitionedAt;

    @Builder
    public WorkflowTransition(Long id, Issue issue, IssueStatus fromStatus, IssueStatus toStatus, UserAccount changedBy, String conditionNote) {
        this.id = id;
        this.issue = issue;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.conditionNote = conditionNote;
    }
}
