package com.pch.mng.issue;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.workflow.WorkflowTransitionResponse;
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
    private final IssueLinkService issueLinkService;

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

    @GetMapping("/{issueKey}/transitions")
    @PreAuthorize("@projectSecurity.canReadIssue(#issueKey)")
    public ResponseEntity<ApiResponse<List<WorkflowTransitionResponse.DetailDTO>>> listTransitions(
            @PathVariable String issueKey) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findTransitionsByIssueKey(issueKey)));
    }

    @PostMapping("/{issueKey}/transitions")
    @PreAuthorize("@projectSecurity.canTransitionIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> transition(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.TransitionDTO reqDTO) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(issueService.transition(issueKey, reqDTO, principal.getId())));
    }

    @GetMapping("/{issueKey}/links")
    @PreAuthorize("@projectSecurity.canReadIssue(#issueKey)")
    public ResponseEntity<ApiResponse<List<IssueLinkResponse.DetailDTO>>> listLinks(@PathVariable String issueKey) {
        return ResponseEntity.ok(ApiResponse.ok(issueLinkService.findByIssueKey(issueKey)));
    }

    @PostMapping("/{issueKey}/links")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueLinkResponse.DetailDTO>> createLink(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueLinkRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(issueLinkService.create(issueKey, reqDTO)));
    }

    @PostMapping("/{issueKey}/labels")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> addLabel(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.LabelAttachDTO reqDTO) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(issueService.addLabel(issueKey, reqDTO.getLabelId())));
    }

    @DeleteMapping("/{issueKey}/labels/{labelId}")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> removeLabel(
            @PathVariable String issueKey,
            @PathVariable Long labelId) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.removeLabel(issueKey, labelId)));
    }

    @PostMapping("/{issueKey}/components")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> addComponent(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.ComponentAttachDTO reqDTO) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(issueService.addComponent(issueKey, reqDTO.getComponentId())));
    }

    @DeleteMapping("/{issueKey}/components/{componentId}")
    @PreAuthorize("@projectSecurity.canUpdateIssue(#issueKey)")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> removeComponent(
            @PathVariable String issueKey,
            @PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.removeComponent(issueKey, componentId)));
    }
}
