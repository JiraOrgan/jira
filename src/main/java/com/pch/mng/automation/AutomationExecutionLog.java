package com.pch.mng.automation;

import com.pch.mng.issue.Issue;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "automation_execution_log_tb")
public class AutomationExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AutomationRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 2000)
    private String message;

    @CreationTimestamp
    private LocalDateTime executedAt;

    @Builder
    public AutomationExecutionLog(
            Long id, AutomationRule rule, Issue issue, boolean success, String message) {
        this.id = id;
        this.rule = rule;
        this.issue = issue;
        this.success = success;
        this.message = message;
    }
}
