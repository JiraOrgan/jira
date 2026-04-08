package com.pch.mng.enrollment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class EnrollmentRequest {

    public record UpdateProgress(
            @Min(0) @Max(100) int progress
    ) {}

    private EnrollmentRequest() {}
}
