package com.pch.mng.course.service;

import com.pch.mng.course.dto.FileUploadResponse;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${storage.bucket:learnflow}")
    private String bucket;

    @Value("${storage.endpoint:http://localhost:9000}")
    private String endpoint;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;   // 10MB
    private static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024; // 50MB

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "text/markdown");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime");

    public FileUploadResponse upload(MultipartFile file, String directory) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String objectKey = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String url = endpoint + "/" + bucket + "/" + objectKey;

            return new FileUploadResponse(
                    objectKey,
                    url,
                    file.getSize(),
                    file.getContentType(),
                    LocalDateTime.now()
            );
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void delete(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String contentType = file.getContentType();
        long size = file.getSize();

        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            if (size > MAX_IMAGE_SIZE) {
                throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
            }
        } else if (ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            if (size > MAX_DOCUMENT_SIZE) {
                throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
            }
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            // Video size limits can be configured separately
        } else {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }
}
