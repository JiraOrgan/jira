package com.jira.mng.issue;

import com.jira.mng.project.ProjectComponent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_component_tb", uniqueConstraints = @UniqueConstraint(columnNames = {"issue_id", "component_id"}))
public class IssueComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ProjectComponent component;
}
