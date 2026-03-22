package com.pch.mng.audit;

import com.pch.mng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogApiController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse.DetailDTO>>> findByIssue(
            @PathVariable Long issueId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse.DetailDTO> page = auditLogRepository
                .findByIssueIdOrderByChangedAtDesc(issueId, pageable)
                .map(AuditLogResponse.DetailDTO::of);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
}
