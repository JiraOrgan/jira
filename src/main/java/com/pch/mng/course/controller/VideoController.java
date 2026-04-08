package com.pch.mng.course.controller;

import com.pch.mng.course.Lesson;
import com.pch.mng.course.LessonRepository;
import com.pch.mng.course.dto.FileUploadResponse;
import com.pch.mng.course.service.FileStorageService;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final FileStorageService fileStorageService;
    private final LessonRepository lessonRepository;

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lessonId") Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (!lesson.getSection().getCourse().isOwnedBy(userDetails.getUserAccount().getId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        FileUploadResponse response = fileStorageService.upload(file,
                "videos/" + lesson.getSection().getCourse().getId());

        lesson.setVideoUrl(response.url());

        lessonRepository.save(lesson);

        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @GetMapping("/{lessonId}/url")
    public ResponseEntity<ApiResponse<String>> getVideoUrl(@PathVariable Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getVideoUrl() == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return ResponseEntity.ok(ApiResponse.ok(lesson.getVideoUrl()));
    }
}
