package com.pch.mng.release;

import com.pch.mng.global.enums.VersionStatus;
import com.pch.mng.project.Project;
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
@Table(name = "release_version_tb")
public class ReleaseVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public ReleaseVersion(Project project, String name, String description, LocalDate releaseDate, VersionStatus status) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.status = status;
    }
}
