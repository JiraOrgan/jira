package com.jira.mng.sprint;

import com.jira.mng.global.enums.SprintStatus;
import com.jira.mng.project.Project;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "sprint_tb")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer goalPoints;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Sprint(Project project, String name, SprintStatus status, LocalDate startDate, LocalDate endDate, Integer goalPoints) {
        this.project = project;
        this.name = name;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalPoints = goalPoints;
    }
}
