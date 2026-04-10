package com.pch.mng.attachment;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.storage.BlobStorage;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AttachmentService {

    private static final int MAX_UPLOAD_BYTES = 20 * 1024 * 1024;

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final UserAccountRepository userAccountRepository;
    private final BlobStorage blobStorage;

    public List<AttachmentResponse.DetailDTO> findByIssueKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return attachmentRepository.findByIssueIdWithUploaderOrderByCreatedAtDesc(issue.getId()).stream()
                .map(AttachmentResponse.DetailDTO::of)
                .toList();
    }

    @Transactional
    public AttachmentResponse.DetailDTO upload(String issueKey, MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_REQUIRED);
        }
        long size = file.getSize();
        if (size > MAX_UPLOAD_BYTES) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount uploader = userAccountRepository.findById(uploaderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String safeName = safeOriginalName(file.getOriginalFilename());
        String objectKey = "issues/" + issue.getId() + "/" + UUID.randomUUID() + "_" + safeName;
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String storedKey;
        try {
            storedKey = blobStorage.put(objectKey, file.getInputStream(), size, contentType);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Attachment attachment = Attachment.builder()
                    .issue(issue)
                    .uploader(uploader)
                    .fileName(safeName)
                    .filePath(storedKey)
                    .fileSize(size)
                    .mimeType(contentType)
                    .build();
            attachmentRepository.save(attachment);
            return AttachmentResponse.DetailDTO.of(attachment);
        } catch (RuntimeException e) {
            try {
                blobStorage.delete(storedKey);
            } catch (IOException ignored) {
                // 보상 삭제 실패는 로그만; DB는 롤백됨
            }
            throw e;
        }
    }

    public Attachment loadForDownload(Long attachmentId) {
        return attachmentRepository.findByIdWithIssueAndUploader(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public InputStream openStream(Attachment attachment) {
        try {
            return blobStorage.get(attachment.getFilePath());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = attachmentRepository.findByIdWithIssueAndUploader(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        String path = attachment.getFilePath();
        attachmentRepository.delete(attachment);
        try {
            blobStorage.delete(path);
        } catch (IOException ignored) {
        }
    }

    private static String safeOriginalName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        String base = name.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        base = base.replaceAll("[^a-zA-Z0-9._\\-가-힣]", "_");
        return base.length() > 200 ? base.substring(0, 200) : base;
    }
}
