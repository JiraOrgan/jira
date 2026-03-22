package com.jira.mng.issue;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/issues")
public class IssueApiController {

    private final IssueService issueService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<IssueResponse.MinDTO>>> findByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findByProject(projectId, pageable)));
    }

    @GetMapping("/project/{projectId}/backlog")
    public ResponseEntity<ApiResponse<List<IssueResponse.MinDTO>>> findBacklog(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findBacklog(projectId)));
    }

    @GetMapping("/{issueKey}")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> findByKey(
            @PathVariable String issueKey) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.findByKey(issueKey)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> save(
            @Valid @RequestBody IssueRequest.SaveDTO reqDTO) {
        // TODO: @AuthenticationPrincipal에서 reporterId 추출
        Long reporterId = 1L;
        return ResponseEntity.status(201).body(ApiResponse.created(issueService.save(reqDTO, reporterId)));
    }

    @PutMapping("/{issueKey}")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> update(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.update(issueKey, reqDTO)));
    }

    @DeleteMapping("/{issueKey}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String issueKey) {
        issueService.delete(issueKey);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{issueKey}/transitions")
    public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> transition(
            @PathVariable String issueKey,
            @Valid @RequestBody IssueRequest.TransitionDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.transition(issueKey, reqDTO)));
    }
}
