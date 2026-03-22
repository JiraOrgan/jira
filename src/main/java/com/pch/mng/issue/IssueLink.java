package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueLinkType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_link_tb")
public class IssueLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_issue_id", nullable = false)
    private Issue sourceIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_issue_id", nullable = false)
    private Issue targetIssue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueLinkType linkType;

    @Builder
    public IssueLink(Issue sourceIssue, Issue targetIssue, IssueLinkType linkType) {
        this.sourceIssue = sourceIssue;
        this.targetIssue = targetIssue;
        this.linkType = linkType;
    }
}
