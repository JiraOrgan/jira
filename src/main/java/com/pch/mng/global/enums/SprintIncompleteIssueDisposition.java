package com.pch.mng.global.enums;

/**
 * 활성 스프린트 완료 시 DONE이 아닌 이슈를 어떻게 처리할지.
 */
public enum SprintIncompleteIssueDisposition {
    /** 제품 백로그로 되돌림(스프린트 해제, 상태 BACKLOG). */
    BACKLOG,
    /** 동일 프로젝트의 PLANNING 스프린트로 이관(워크플로 상태 유지). */
    NEXT_SPRINT
}
