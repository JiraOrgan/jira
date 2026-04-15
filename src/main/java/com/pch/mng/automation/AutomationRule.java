package com.pch.mng.automation;

import com.pch.mng.global.enums.AutomationActionType;
import com.pch.mng.global.enums.AutomationTriggerType;
import com.pch.mng.project.Project;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "automation_rule_tb")
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AutomationTriggerType triggerType;

    /** JSON: fromStatus, toStatus, issueTypes(배열) 등 — 미설정이면 해당 트리거에서 항상 매칭. */
    @Column(columnDefinition = "TEXT")
    private String conditionJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AutomationActionType actionType;

    /** 액션별 페이로드 (예: SET_PRIORITY 시 priority). */
    @Column(columnDefinition = "TEXT")
    private String actionJson;

    /** 낮을수록 먼저 실행. */
    @Column(nullable = false)
    private int sortOrder;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public AutomationRule(
            Long id,
            Project project,
            String name,
            boolean enabled,
            AutomationTriggerType triggerType,
            String conditionJson,
            AutomationActionType actionType,
            String actionJson,
            int sortOrder) {
        this.id = id;
        this.project = project;
        this.name = name;
        this.enabled = enabled;
        this.triggerType = triggerType;
        this.conditionJson = conditionJson;
        this.actionType = actionType;
        this.actionJson = actionJson;
        this.sortOrder = sortOrder;
    }
}
