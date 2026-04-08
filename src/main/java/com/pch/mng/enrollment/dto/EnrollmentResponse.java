package com.pch.mng.enrollment.dto;

import com.pch.mng.enrollment.Enrollment;
import com.pch.mng.global.enums.EnrollmentStatus;

import java.time.LocalDateTime;

public final class EnrollmentResponse {

    public record Summary(
            Long id,
            Long courseId,
            String courseTitle,
            int progress,
            EnrollmentStatus status,
            LocalDateTime enrolledAt
    ) {
        public Summary(Enrollment enrollment) {
            this(
                    enrollment.getId(),
                    enrollment.getCourse().getId(),
                    enrollment.getCourse().getTitle(),
                    enrollment.getProgress(),
                    enrollment.getStatus(),
                    enrollment.getCreatedAt()
            );
        }
    }

    private EnrollmentResponse() {}
}
