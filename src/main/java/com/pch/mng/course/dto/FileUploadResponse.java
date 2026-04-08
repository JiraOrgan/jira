package com.pch.mng.course.dto;

import java.time.LocalDateTime;

public record FileUploadResponse(
        String objectKey,
        String url,
        long size,
        String contentType,
        LocalDateTime uploadedAt
) {}
