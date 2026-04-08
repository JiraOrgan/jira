package com.pch.mng.course.controller;

import com.pch.mng.course.dto.FileUploadResponse;
import com.pch.mng.course.service.FileStorageService;
import com.pch.mng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "courses") String directory) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(fileStorageService.upload(file, directory)));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam("objectKey") String objectKey) {
        fileStorageService.delete(objectKey);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
