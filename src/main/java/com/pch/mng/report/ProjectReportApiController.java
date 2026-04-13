package com.pch.mng.report;

import com.pch.mng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects/{projectId}/reports")
public class ProjectReportApiController {

    private final ReportService reportService;

    @GetMapping("/sprints/{sprintId}/burndown")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<ReportResponse.BurndownDTO>> burndown(
            @PathVariable Long projectId,
            @PathVariable Long sprintId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.burndown(projectId, sprintId)));
    }

    @GetMapping("/velocity")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<ReportResponse.VelocityDTO>> velocity(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.velocity(projectId, limit)));
    }

    @GetMapping("/cfd")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<ApiResponse<ReportResponse.CfdDTO>> cfd(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.cumulativeFlow(projectId, sprintId, days)));
    }
}
