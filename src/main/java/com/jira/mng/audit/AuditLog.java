package com.jira.mng.audit;

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
@Table(name = "audit_log_tb")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserAccount changedBy;

    @Column(nullable = false)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    private LocalDateTime changedAt;

    @Builder
    public AuditLog(Long id, Issue issue, UserAccount changedBy, String fieldName, String oldValue, String newValue) {
        this.id = id;
        this.issue = issue;
        this.changedBy = changedBy;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
