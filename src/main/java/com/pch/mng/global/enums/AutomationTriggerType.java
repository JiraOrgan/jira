package com.pch.mng.global.enums;

/** FR-015: 자동화 트리거 (MVP). */
public enum AutomationTriggerType {
    /** 이슈 최초 저장 직후 (상태는 보통 BACKLOG). */
    ISSUE_CREATED,
    /** 워크플로 상태 전환 성공 직후. */
    ISSUE_STATUS_CHANGED
}
