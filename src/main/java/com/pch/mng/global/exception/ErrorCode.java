package com.pch.mng.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다"),
    ENTITY_NOT_FOUND(404, "리소스를 찾을 수 없습니다"),
    DUPLICATE_RESOURCE(409, "이미 존재하는 리소스입니다"),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다"),

    // Auth
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다"),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다"),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다"),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다"),
    ACCOUNT_LOCKED(423, "로그인 시도 횟수 초과로 일시 잠금되었습니다. 30분 후 다시 시도해 주세요"),

    // User
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다"),

    // Issue
    INVALID_ISSUE_HIERARCHY(400, "이슈 타입과 부모 이슈 조합이 PRD 계층(Epic→Story/Task/Bug→Sub-task)에 맞지 않습니다"),
    EPIC_DATE_NOT_ALLOWED(400, "Epic 기간(epicStartDate/epicEndDate)은 Epic 타입 이슈에만 설정할 수 있습니다"),
    EPIC_DATE_RANGE_INVALID(400, "Epic 종료일은 시작일 이상이어야 합니다"),
    ISSUE_PARENT_PROJECT_MISMATCH(400, "부모 이슈가 같은 프로젝트에 있어야 합니다"),
    SPRINT_PROJECT_MISMATCH(400, "스프린트가 해당 프로젝트에 속하지 않습니다"),
    COMPONENT_PROJECT_MISMATCH(400, "컴포넌트는 이슈와 같은 프로젝트에 속해야 합니다"),
    WORKFLOW_VIOLATION(409, "허용되지 않는 워크플로 전환입니다"),
    WIP_LIMIT_EXCEEDED(409, "해당 워크플로 상태 열의 WIP 한도를 초과했습니다"),

    // Sprint (FR-011)
    SPRINT_INVALID_TRANSITION(409, "허용되지 않는 스프린트 상태 전환입니다"),
    SPRINT_ACTIVE_ALREADY_EXISTS(409, "해당 프로젝트에 이미 진행 중인 스프린트가 있습니다"),
    SPRINT_DELETE_FORBIDDEN(409, "진행 중이거나 이슈가 배정된 스프린트는 삭제할 수 없습니다"),
    SPRINT_NOT_ASSIGNABLE(409, "완료된 스프린트에는 이슈를 배정할 수 없습니다"),

    // Backlog (FR-010)
    BACKLOG_REORDER_INVALID(400, "백로그 순서가 올바르지 않습니다. 스프린트에 미배정인 이슈 ID 집합과 일치해야 합니다"),

    // Kanban / WIP (FR-009)
    WIP_LIMITS_KANBAN_ONLY(400, "WIP 제한은 칸반(KANBAN) 프로젝트에서만 설정할 수 있습니다"),
    ISSUE_LINK_SELF(400, "동일 이슈에는 링크를 걸 수 없습니다"),
    ISSUE_LINK_PROJECT_MISMATCH(400, "링크 대상 이슈는 같은 프로젝트에 있어야 합니다"),
    ISSUE_LINK_DUPLICATE(409, "동일한 이슈 링크가 이미 존재합니다"),
    ISSUE_SECURITY_LEVEL_FORBIDDEN(403, "해당 역할로는 요청한 이슈 보안 레벨을 설정할 수 없습니다"),

    // JQL (FR-016)
    JQL_INVALID_VALUE(400, "JQL 값이 올바르지 않습니다"),
    JQL_UNSUPPORTED_CLAUSE(400, "JQL에서 해당 필드와 연산 조합을 지원하지 않습니다"),
    JQL_PROJECT_KEY_MISMATCH(400, "JQL의 project 조건이 현재 프로젝트와 일치하지 않습니다"),

    // Dashboard (FR-021)
    DASHBOARD_GADGET_MISMATCH(400, "해당 대시보드에 속하지 않는 가젯입니다"),

    // Attachment
    FILE_REQUIRED(400, "업로드할 파일이 필요합니다"),
    FILE_TOO_LARGE(413, "파일 크기 제한(20MB)을 초과했습니다"),
    FILE_UPLOAD_FAILED(500, "파일 저장에 실패했습니다");

    private final int status;
    private final String message;
}
