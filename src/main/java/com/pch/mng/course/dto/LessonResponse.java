package com.pch.mng.course.dto;

import com.pch.mng.course.Lesson;
import com.pch.mng.global.enums.ContentType;

import java.time.LocalDateTime;

public final class LessonResponse {

    public record Summary(
            Long id,
            String title,
            ContentType contentType,
            int orderIndex,
            int durationMinutes
    ) {
        public Summary(Lesson lesson) {
            this(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getContentType(),
                    lesson.getOrderIndex(),
                    lesson.getDurationMinutes()
            );
        }
    }

    public record Detail(
            Long id,
            Long sectionId,
            String title,
            String content,
            ContentType contentType,
            String videoUrl,
            int orderIndex,
            int durationMinutes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public Detail(Lesson lesson) {
            this(
                    lesson.getId(),
                    lesson.getSection() != null ? lesson.getSection().getId() : null,
                    lesson.getTitle(),
                    lesson.getContent(),
                    lesson.getContentType(),
                    lesson.getVideoUrl(),
                    lesson.getOrderIndex(),
                    lesson.getDurationMinutes(),
                    lesson.getCreatedAt(),
                    lesson.getUpdatedAt()
            );
        }
    }

    private LessonResponse() {}
}
