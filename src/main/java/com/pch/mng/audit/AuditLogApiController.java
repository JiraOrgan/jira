package com.pch.mng.audit;

import com.pch.mng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogApiController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/issue/{issueId}")
    @PreAuthorize("@projectSecurity.canViewAuditForIssue(#issueId)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse.DetailDTO>>> findByIssue(
            @PathVariable Long issueId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse.DetailDTO> page = auditLogRepository
                .findByIssueIdOrderByChangedAtDesc(issueId, pageable)
                .map(AuditLogResponse.DetailDTO::of);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    /** 프로젝트 소속 이슈의 감사 로그 (프로젝트 ADMIN, SCR-014) */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse.DetailDTO>>> findByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<AuditLogResponse.DetailDTO> page = auditLogRepository
                .findByIssue_Project_IdOrderByChangedAtDesc(projectId, pageable)
                .map(AuditLogResponse.DetailDTO::of);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
}
