package com.jira.mng.project;

import com.jira.mng.global.enums.BoardType;
import com.jira.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "project_tb")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`key`", unique = true, nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private UserAccount lead;

    @Column(nullable = false)
    private boolean archived = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Project(String key, String name, String description, BoardType boardType, UserAccount lead, boolean archived) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.boardType = boardType;
        this.lead = lead;
        this.archived = archived;
    }
}
