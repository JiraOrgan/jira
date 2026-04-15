package com.pch.mng.global.enums;

/** FR-015: 자동화 액션 (MVP). actionJson 스키마는 API 문서 참고. */
public enum AutomationActionType {
    /** {@code {"priority":"HIGH"}} — {@link com.pch.mng.global.enums.Priority} 이름. */
    SET_PRIORITY,
    /** 담당자를 리포터와 동일하게 설정. */
    ASSIGN_TO_REPORTER,
    /** 담당자 해제. */
    UNASSIGN
}
