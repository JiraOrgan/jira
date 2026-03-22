package com.jira.mng.project;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectApiController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse.MinDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> save(
            @Valid @RequestBody ProjectRequest.SaveDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(projectService.save(reqDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // -- 멤버 관리 --

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<List<ProjectResponse.MemberDTO>>> findMembers(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findMembers(projectId)));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<ProjectResponse.MemberDTO>> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest.AddMemberDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(projectService.addMember(projectId, reqDTO)));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
