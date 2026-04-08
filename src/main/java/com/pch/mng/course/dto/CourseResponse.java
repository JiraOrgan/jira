package com.pch.mng.course.dto;

import com.pch.mng.course.Course;
import com.pch.mng.global.enums.CourseStatus;

import java.time.LocalDateTime;

public final class CourseResponse {

    public record Summary(
            Long id,
            String title,
            String instructorName,
            CourseStatus status,
            LocalDateTime createdAt
    ) {
        public Summary(Course course) {
            this(
                    course.getId(),
                    course.getTitle(),
                    course.getInstructor() != null ? course.getInstructor().getName() : null,
                    course.getStatus(),
                    course.getCreatedAt()
            );
        }
    }

    public record Detail(
            Long id,
            String title,
            String description,
            String thumbnailUrl,
            Long instructorId,
            String instructorName,
            CourseStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public Detail(Course course) {
            this(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    course.getInstructor() != null ? course.getInstructor().getId() : null,
                    course.getInstructor() != null ? course.getInstructor().getName() : null,
                    course.getStatus(),
                    course.getCreatedAt(),
                    course.getUpdatedAt()
            );
        }
    }

    private CourseResponse() {}
}
