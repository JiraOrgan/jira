package com.pch.mng.course;

import com.pch.mng.global.enums.CourseStatus;
import com.pch.mng.user.UserAccount;
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
@Table(name = "course_tb")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private UserAccount instructor;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    private String thumbnailUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Course(String title, String description, UserAccount instructor, CourseStatus status, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.status = status != null ? status : CourseStatus.DRAFT;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void update(String title, String description, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void publish() {
        this.status = CourseStatus.PUBLISHED;
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }

    public boolean isOwnedBy(Long userId) {
        return this.instructor != null && this.instructor.getId().equals(userId);
    }
}
