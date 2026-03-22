package com.jira.mng.project;

import com.jira.mng.global.enums.ProjectRole;
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
@Table(name = "project_member_tb", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Builder
    public ProjectMember(Project project, UserAccount user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }
}
