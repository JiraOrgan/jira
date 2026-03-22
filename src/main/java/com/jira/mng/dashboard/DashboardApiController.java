package com.jira.mng.dashboard;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DashboardResponse.MinDTO>>> findAccessible() {
        // TODO: @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.findAccessible(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> save(
            @Valid @RequestBody DashboardRequest.SaveDTO reqDTO) {
        Long ownerId = 1L;
        return ResponseEntity.status(201).body(ApiResponse.created(dashboardService.save(reqDTO, ownerId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DashboardRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dashboardService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PostMapping("/{dashboardId}/gadgets")
    public ResponseEntity<ApiResponse<DashboardResponse.DetailDTO>> addGadget(
            @PathVariable Long dashboardId,
            @Valid @RequestBody DashboardRequest.GadgetDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(dashboardService.addGadget(dashboardId, reqDTO)));
    }

    @DeleteMapping("/{dashboardId}/gadgets/{gadgetId}")
    public ResponseEntity<ApiResponse<Void>> removeGadget(
            @PathVariable Long dashboardId, @PathVariable Long gadgetId) {
        dashboardService.removeGadget(dashboardId, gadgetId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
