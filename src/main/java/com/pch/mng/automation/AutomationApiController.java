package com.pch.mng.automation;

import com.pch.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects/{projectId}/automation")
public class AutomationApiController {

    private final AutomationRuleService automationRuleService;
    private final AutomationExecutionLogRepository automationExecutionLogRepository;

    @GetMapping("/rules")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<List<AutomationRuleResponse>>> listRules(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.listRules(projectId)));
    }

    @PostMapping("/rules")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createRule(
            @PathVariable Long projectId, @Valid @RequestBody AutomationRuleRequest.SaveDTO dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(automationRuleService.create(projectId, dto)));
    }

    @PutMapping("/rules/{ruleId}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> updateRule(
            @PathVariable Long projectId,
            @PathVariable Long ruleId,
            @Valid @RequestBody AutomationRuleRequest.UpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.update(projectId, ruleId, dto)));
    }

    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long projectId, @PathVariable Long ruleId) {
        automationRuleService.delete(projectId, ruleId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/execution-logs")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId)")
    public ResponseEntity<ApiResponse<Page<AutomationExecutionLogResponse>>> executionLogs(
            @PathVariable Long projectId, @PageableDefault(size = 20) Pageable pageable) {
        Page<AutomationExecutionLogResponse> page =
                automationExecutionLogRepository
                        .findByRule_Project_IdOrderByIdDesc(projectId, pageable)
                        .map(AutomationExecutionLogResponse::of);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
}
