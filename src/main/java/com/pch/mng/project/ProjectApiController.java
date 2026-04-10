package com.pch.mng.project;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectApiController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProjectResponse.MinDTO>>> findAll(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findAllForUser(principal.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurity.isMember(#id)")
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> save(
            @Valid @RequestBody ProjectRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(projectService.save(reqDTO)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#id)")
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<ProjectResponse.MemberDTO>>> findMembers(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findMembers(projectId)));
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<ProjectResponse.MemberDTO>> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest.AddMemberDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(projectService.addMember(projectId, reqDTO)));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/{projectId}/wip-limits")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<ProjectResponse.WipLimitDTO>>> findWipLimits(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findWipLimits(projectId)));
    }

    @PutMapping("/{projectId}/wip-limits")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<List<ProjectResponse.WipLimitDTO>>> replaceWipLimits(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest.WipLimitsReplaceDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.replaceWipLimits(projectId, dto)));
    }
}
