package com.pch.mng.enrollment.controller;

import com.pch.mng.enrollment.dto.EnrollmentRequest;
import com.pch.mng.enrollment.dto.EnrollmentResponse;
import com.pch.mng.enrollment.service.EnrollmentService;
import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ApiResponse<EnrollmentResponse.Summary>> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EnrollmentResponse.Summary response = enrollmentService.enroll(
                userDetails.getUserAccount().getId(), courseId);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @GetMapping("/enrollments/me")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse.Summary>>> getMyEnrollments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.getMyEnrollments(userDetails.getUserAccount().getId())));
    }

    @PatchMapping("/enrollments/{id}/progress")
    public ResponseEntity<ApiResponse<EnrollmentResponse.Summary>> updateProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest.UpdateProgress request) {
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.updateProgress(id, userDetails.getUserAccount().getId(), request)));
    }
}
