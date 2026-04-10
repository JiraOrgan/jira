package com.pch.mng.jql;

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
@RequestMapping("/api/v1/projects/{projectId}/jql")
public class JqlSearchApiController {

    private final JqlSearchService jqlSearchService;

    @PostMapping("/search")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<JqlSearchResponse>> search(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody JqlSearchRequest request) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(jqlSearchService.search(projectId, request)));
    }

    @PostMapping("/filters")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<SavedJqlFilterResponse>> saveFilter(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody SavedJqlFilterRequest request) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(201)
                .body(ApiResponse.created(jqlSearchService.saveFilter(projectId, request, principal.getId())));
    }

    @GetMapping("/filters")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<List<SavedJqlFilterResponse>>> listFilters(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(jqlSearchService.listFilters(projectId, principal.getId())));
    }

    @DeleteMapping("/filters/{filterId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<Void>> deleteFilter(
            @PathVariable Long projectId,
            @PathVariable Long filterId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        jqlSearchService.deleteFilter(projectId, filterId, principal.getId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
