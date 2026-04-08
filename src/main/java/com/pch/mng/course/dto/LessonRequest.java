package com.pch.mng.course.dto;

import com.pch.mng.global.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class LessonRequest {

    public record Create(
            @NotBlank @Size(max = 255) String title,
            String content,
            ContentType contentType,
            String videoUrl,
            int orderIndex,
            int durationMinutes,
            Integer videoDuration
    ) {}

    public record Update(
            @NotBlank @Size(max = 255) String title,
            String content,
            ContentType contentType,
            String videoUrl,
            int orderIndex,
            int durationMinutes,
            Integer videoDuration
    ) {}

    private LessonRequest() {}
}
