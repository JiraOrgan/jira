package com.pch.mng.release;

import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/versions")
public class ReleaseVersionApiController {

    private final ReleaseVersionService releaseVersionService;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<ReleaseVersionResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.findByProject(projectId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurity.canReadRelease(#id)")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.canManageReleaseOnProject(#reqDTO.projectId)")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> save(
            @Valid @RequestBody ReleaseVersionRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(releaseVersionService.save(reqDTO)));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("@projectSecurity.canManageRelease(#id)")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> release(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.release(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.canManageRelease(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        releaseVersionService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
