package com.pch.mng.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CourseRequest {

    public record Create(
            @NotBlank @Size(max = 255) String title,
            String description,
            String thumbnailUrl
    ) {}

    public record Update(
            @NotBlank @Size(max = 255) String title,
            String description,
            String thumbnailUrl
    ) {}

    private CourseRequest() {}
}
