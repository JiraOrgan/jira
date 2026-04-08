package com.pch.mng.course.dto;

import com.pch.mng.global.enums.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CourseDetailResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        String instructorName,
        CourseStatus status,
        long enrollmentCount,
        List<SectionSummary> sections,
        LocalDateTime createdAt
) {
    public record SectionSummary(
            Long id,
            String title,
            int orderIndex,
            List<LessonSummary> lessons
    ) {}

    public record LessonSummary(
            Long id,
            String title,
            int orderIndex,
            int durationMinutes
    ) {}
}
