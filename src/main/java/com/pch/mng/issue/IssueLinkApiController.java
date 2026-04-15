package com.pch.mng.issue;

import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/issues/links")
public class IssueLinkApiController {

    private final IssueLinkService issueLinkService;

    @PutMapping("/{linkId}")
    @PreAuthorize("@projectSecurity.canModifyIssueLink(#linkId)")
    public ResponseEntity<ApiResponse<IssueLinkResponse.DetailDTO>> update(
            @PathVariable Long linkId,
            @Valid @RequestBody IssueLinkRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(issueLinkService.update(linkId, reqDTO)));
    }

    @DeleteMapping("/{linkId}")
    @PreAuthorize("@projectSecurity.canModifyIssueLink(#linkId)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long linkId) {
        issueLinkService.delete(linkId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
