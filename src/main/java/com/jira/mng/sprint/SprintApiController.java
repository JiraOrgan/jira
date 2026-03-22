package com.jira.mng.sprint;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sprints")
public class SprintApiController {

    private final SprintService sprintService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<SprintResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.findByProject(projectId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> save(
            @Valid @RequestBody SprintRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(sprintService.save(reqDTO)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> start(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.start(id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<SprintResponse.DetailDTO>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sprintService.complete(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
