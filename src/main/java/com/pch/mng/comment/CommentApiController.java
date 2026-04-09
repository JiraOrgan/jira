package com.pch.mng.comment;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
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
@RequestMapping("/api/v1/comments")
public class CommentApiController {

    private final CommentService commentService;

    @GetMapping("/issue/{issueId}")
    @PreAuthorize("@projectSecurity.canReadIssueById(#issueId)")
    public ResponseEntity<ApiResponse<List<CommentResponse.DetailDTO>>> findByIssue(
            @PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.findByIssue(issueId)));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.canCommentOnIssue(#reqDTO.issueId)")
    public ResponseEntity<ApiResponse<CommentResponse.DetailDTO>> save(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CommentRequest.SaveDTO reqDTO) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(commentService.save(reqDTO, principal.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@projectSecurity.canModifyComment(#id)")
    public ResponseEntity<ApiResponse<CommentResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.canModifyComment(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
