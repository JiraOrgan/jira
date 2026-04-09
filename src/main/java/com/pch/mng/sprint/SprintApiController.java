package com.pch.mng.sprint;

import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sprints")
public class SprintApiController {

    private final SprintService sprintService;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<SprintResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.findByProject(projectId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurity.canReadSprint(#id)")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.canManageSprintOnProject(#reqDTO.projectId)")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> save(
            @Valid @RequestBody SprintRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(sprintService.save(reqDTO)));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("@projectSecurity.canManageSprint(#id)")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> start(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.start(id)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("@projectSecurity.canManageSprint(#id)")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.complete(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.canManageSprint(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
