package com.jira.mng.release;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/versions")
public class ReleaseVersionApiController {

    private final ReleaseVersionService releaseVersionService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ReleaseVersionResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.findByProject(projectId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> save(
            @Valid @RequestBody ReleaseVersionRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(releaseVersionService.save(reqDTO)));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse.DetailDTO>> release(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(releaseVersionService.release(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        releaseVersionService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
