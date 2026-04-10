package com.pch.mng.attachment;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class AttachmentApiController {

    private final AttachmentService attachmentService;

    @GetMapping("/issues/{issueKey}/attachments")
    @PreAuthorize("@projectSecurity.canReadIssue(#issueKey)")
    public ResponseEntity<ApiResponse<List<AttachmentResponse.DetailDTO>>> list(@PathVariable String issueKey) {
        return ResponseEntity.ok(ApiResponse.ok(attachmentService.findByIssueKey(issueKey)));
    }

    @PostMapping(value = "/issues/{issueKey}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<AttachmentResponse.DetailDTO>> upload(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String issueKey,
            @RequestPart("file") MultipartFile file) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(201)
                .body(ApiResponse.created(attachmentService.upload(issueKey, file, principal.getId())));
    }

    @GetMapping("/attachments/{id}/file")
    @PreAuthorize("@projectSecurity.canReadAttachment(#id)")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment attachment = attachmentService.loadForDownload(id);
        InputStreamResource body = new InputStreamResource(attachmentService.openStream(attachment));
        String encoded = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(attachment.getFileSize())
                .body(body);
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("@projectSecurity.canDeleteAttachment(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
