package com.jira.mng.issue;

import com.jira.mng.release.ReleaseVersion;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_fix_version_tb", uniqueConstraints = @UniqueConstraint(columnNames = {"issue_id", "version_id"}))
public class IssueFixVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ReleaseVersion version;
}
