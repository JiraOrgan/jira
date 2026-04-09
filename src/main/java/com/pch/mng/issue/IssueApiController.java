package com.pch.mng.issue;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/issues")
public class IssueApiController {

    private final IssueService issueService;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<Page<IssueResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findByProject(projectId, pageable)));
    }

    @GetMapping("/project/{projectId}/backlog")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<IssueResponse.MinDTO>>> findBacklog(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findBacklog(projectId)));
    }

    @GetMapping("/{issueKey}")
    @PreAuthorize("@projectSecurity.canReadIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> findByKey(
            @PathVariable String issueKey) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findByKey(issueKey)));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.canCreateIssue(#reqDTO.projectId)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> save(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IssueRequest.SaveDTO reqDTO) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(issueService.save(reqDTO, principal.getId())));
    }

    @PutMapping("/{issueKey}")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> update(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.update(issueKey, reqDTO)));
    }

    @DeleteMapping("/{issueKey}")
    @PreAuthorize("@projectSecurity.canDeleteIssue(#issueKey)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String issueKey) {
        issueService.delete(issueKey);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{issueKey}/transitions")
    @PreAuthorize("@projectSecurity.canTransitionIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> transition(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.TransitionDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.transition(issueKey, reqDTO)));
    }
}
