package com.pch.mng.course.dto;

import com.pch.mng.course.Section;

import java.time.LocalDateTime;

public final class SectionResponse {

    public record Summary(
            Long id,
            String title,
            int orderIndex
    ) {
        public Summary(Section section) {
            this(section.getId(), section.getTitle(), section.getOrderIndex());
        }
    }

    public record Detail(
            Long id,
            Long courseId,
            String title,
            int orderIndex,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public Detail(Section section) {
            this(
                    section.getId(),
                    section.getCourse() != null ? section.getCourse().getId() : null,
                    section.getTitle(),
                    section.getOrderIndex(),
                    section.getCreatedAt(),
                    section.getUpdatedAt()
            );
        }
    }

    private SectionResponse() {}
}
