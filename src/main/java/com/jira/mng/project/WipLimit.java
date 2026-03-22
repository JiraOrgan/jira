package com.jira.mng.project;

import com.jira.mng.global.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "wip_limit_tb")
public class WipLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @Column(nullable = false)
    private int maxIssues;

    @Builder
    public WipLimit(Project project, IssueStatus status, int maxIssues) {
        this.project = project;
        this.status = status;
        this.maxIssues = maxIssues;
    }
}
