package com.pch.mng.automation;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationExecutionLogRepository automationExecutionLogRepository;
    private final ProjectRepository projectRepository;
    private final AutomationEngine automationEngine;

    public List<AutomationRuleResponse> listRules(Long projectId) {
        return automationRuleRepository.findByProject_IdOrderBySortOrderAscIdAsc(projectId).stream()
                .map(AutomationRuleResponse::of)
                .toList();
    }

    @Transactional
    public AutomationRuleResponse create(Long projectId, AutomationRuleRequest.SaveDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        automationEngine.validatePayload(
                dto.getTriggerType(), dto.getActionType(), dto.getConditionJson(), dto.getActionJson());
        int sort = dto.getSortOrder() != null ? dto.getSortOrder() : 0;
        boolean enabled = dto.getEnabled() == null || dto.getEnabled();
        AutomationRule rule =
                AutomationRule.builder()
                        .project(project)
                        .name(dto.getName().trim())
                        .enabled(enabled)
                        .triggerType(dto.getTriggerType())
                        .conditionJson(trimToNull(dto.getConditionJson()))
                        .actionType(dto.getActionType())
                        .actionJson(trimToNull(dto.getActionJson()))
                        .sortOrder(sort)
                        .build();
        return AutomationRuleResponse.of(automationRuleRepository.save(rule));
    }

    @Transactional
    public AutomationRuleResponse update(Long projectId, Long ruleId, AutomationRuleRequest.UpdateDTO dto) {
        AutomationRule rule = automationRuleRepository
                .findByIdAndProject_Id(ruleId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (dto.getName() != null) {
            rule.setName(dto.getName().trim());
        }
        if (dto.getEnabled() != null) {
            rule.setEnabled(dto.getEnabled());
        }
        if (dto.getTriggerType() != null) {
            rule.setTriggerType(dto.getTriggerType());
        }
        if (dto.getConditionJson() != null) {
            rule.setConditionJson(trimToNull(dto.getConditionJson()));
        }
        if (dto.getActionType() != null) {
            rule.setActionType(dto.getActionType());
        }
        if (dto.getActionJson() != null) {
            rule.setActionJson(trimToNull(dto.getActionJson()));
        }
        if (dto.getSortOrder() != null) {
            rule.setSortOrder(dto.getSortOrder());
        }
        automationEngine.validatePayload(
                rule.getTriggerType(), rule.getActionType(), rule.getConditionJson(), rule.getActionJson());
        return AutomationRuleResponse.of(rule);
    }

    @Transactional
    public void delete(Long projectId, Long ruleId) {
        AutomationRule rule = automationRuleRepository
                .findByIdAndProject_Id(ruleId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        automationExecutionLogRepository.deleteByRule_Id(ruleId);
        automationRuleRepository.delete(rule);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
