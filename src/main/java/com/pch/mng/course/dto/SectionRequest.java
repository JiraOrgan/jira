package com.pch.mng.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class SectionRequest {

    public record Create(
            @NotBlank @Size(max = 255) String title,
            int orderIndex
    ) {}

    public record Update(
            @NotBlank @Size(max = 255) String title,
            int orderIndex
    ) {}

    private SectionRequest() {}
}
