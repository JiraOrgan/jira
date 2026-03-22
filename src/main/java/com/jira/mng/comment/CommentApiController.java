package com.jira.mng.comment;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentApiController {

    private final CommentService commentService;

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<ApiResponse<List<CommentResponse.DetailDTO>>> findByIssue(
            @PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.findByIssue(issueId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse.DetailDTO>> save(
            @Valid @RequestBody CommentRequest.SaveDTO reqDTO) {
        // TODO: @AuthenticationPrincipal에서 authorId 추출
        Long authorId = 1L;
        return ResponseEntity.status(201).body(ApiResponse.created(commentService.save(reqDTO, authorId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
