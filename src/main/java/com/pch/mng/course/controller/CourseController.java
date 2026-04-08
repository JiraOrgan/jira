package com.pch.mng.course.controller;

import com.pch.mng.course.dto.CourseRequest;
import com.pch.mng.course.dto.CourseResponse;
import com.pch.mng.course.dto.LessonRequest;
import com.pch.mng.course.dto.LessonResponse;
import com.pch.mng.course.dto.SectionRequest;
import com.pch.mng.course.dto.SectionResponse;
import com.pch.mng.course.service.CourseService;
import com.pch.mng.course.service.LessonService;
import com.pch.mng.course.service.SectionService;
import com.pch.mng.global.common.PageResponse;
import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;
    private final SectionService sectionService;
    private final LessonService lessonService;

    // === Course CRUD ===

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse.Summary>>> getCourses(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourses(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse.Detail>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourse(id)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse.Detail>> createCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourseRequest.Create request) {
        CourseResponse.Detail response = courseService.createCourse(
                userDetails.getUserAccount().getId(), request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse.Detail>> updateCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CourseRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.updateCourse(id, userDetails.getUserAccount().getId(), request)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        courseService.deleteCourse(id, userDetails.getUserAccount().getId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // === Section CRUD ===

    @GetMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse.Summary>>> getSections(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSections(courseId)));
    }

    @GetMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse.Detail>> getSection(
            @PathVariable Long courseId,
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSection(sectionId)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse.Detail>> createSection(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SectionRequest.Create request) {
        SectionResponse.Detail response = sectionService.createSection(
                courseId, userDetails.getUserAccount().getId(), request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PutMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse.Detail>> updateSection(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SectionRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
                sectionService.updateSection(sectionId, userDetails.getUserAccount().getId(), request)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sectionService.deleteSection(sectionId, userDetails.getUserAccount().getId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // === Lesson CRUD ===

    @GetMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonResponse.Summary>>> getLessons(
            @PathVariable Long courseId,
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(lessonService.getLessons(sectionId)));
    }

    @GetMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse.Detail>> getLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(ApiResponse.ok(lessonService.getLesson(lessonId)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse.Detail>> createLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LessonRequest.Create request) {
        LessonResponse.Detail response = lessonService.createLesson(
                sectionId, userDetails.getUserAccount().getId(), request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PutMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse.Detail>> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LessonRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
                lessonService.updateLesson(lessonId, userDetails.getUserAccount().getId(), request)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        lessonService.deleteLesson(lessonId, userDetails.getUserAccount().getId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
