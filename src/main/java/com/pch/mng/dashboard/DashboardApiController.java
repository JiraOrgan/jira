package com.pch.mng.dashboard;

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
@RequestMapping("/api/v1/dashboards")
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DashboardResponse.MinDTO>>> findAccessible(
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.findAccessible(principal.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@dashboardSecurity.canReadDashboard(#id)")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> save(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DashboardRequest.SaveDTO reqDTO) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(dashboardService.save(reqDTO, principal.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@dashboardSecurity.canWriteDashboard(#id)")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DashboardRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@dashboardSecurity.canWriteDashboard(#id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dashboardService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{dashboardId}/gadgets")
    @PreAuthorize("@dashboardSecurity.canWriteDashboard(#dashboardId)")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> addGadget(
            @PathVariable Long dashboardId,
            @Valid @RequestBody DashboardRequest.GadgetDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(dashboardService.addGadget(dashboardId, reqDTO)));
    }

    @DeleteMapping("/{dashboardId}/gadgets/{gadgetId}")
    @PreAuthorize("@dashboardSecurity.canWriteDashboard(#dashboardId)")
    public ResponseEntity<ApiResponse<Void>> removeGadget(
            @PathVariable Long dashboardId, @PathVariable Long gadgetId) {
        dashboardService.removeGadget(dashboardId, gadgetId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
