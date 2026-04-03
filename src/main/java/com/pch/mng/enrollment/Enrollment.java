package com.pch.mng.enrollment;

import com.pch.mng.course.Course;
import com.pch.mng.global.enums.EnrollmentStatus;
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
@Table(name = "enrollment_tb", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_user_course", columnNames = {"user_id", "course_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private int progress;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Enrollment(UserAccount user, Course course, int progress, EnrollmentStatus status) {
        this.user = user;
        this.course = course;
        this.progress = progress;
        this.status = status != null ? status : EnrollmentStatus.ACTIVE;
    }
}
