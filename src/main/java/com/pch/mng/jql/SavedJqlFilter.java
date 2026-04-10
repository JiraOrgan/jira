package com.pch.mng.jql;

import com.pch.mng.project.Project;
import com.pch.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "saved_jql_filter_tb")
public class SavedJqlFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 4000)
    private String jql;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public SavedJqlFilter(UserAccount owner, Project project, String name, String jql) {
        this.owner = owner;
        this.project = project;
        this.name = name;
        this.jql = jql;
    }
}
