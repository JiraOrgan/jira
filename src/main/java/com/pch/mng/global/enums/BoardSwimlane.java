package com.pch.mng.global.enums;

/**
 * 스프린트 보드 스윔레인 모드 (FR-008).
 */
public enum BoardSwimlane {
    /** 컬럼당 이슈를 한 덩어리로 반환 */
    NONE,
    /** 담당자(assignee)별로 버킷 분리 (미배정은 assigneeId null) */
    ASSIGNEE
}
