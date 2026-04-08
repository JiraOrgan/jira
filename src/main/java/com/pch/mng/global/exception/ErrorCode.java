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

    // User
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다"),

    // Auth - Login
    ACCOUNT_LOCKED(423, "계정이 잠겨 있습니다. 30분 후 다시 시도해주세요"),
    PASSWORD_MISMATCH(401, "비밀번호가 일치하지 않습니다"),
    REFRESH_TOKEN_NOT_FOUND(401, "리프레시 토큰을 찾을 수 없습니다"),
    REFRESH_TOKEN_EXPIRED(401, "리프레시 토큰이 만료되었습니다"),

    // Course
    COURSE_NOT_FOUND(404, "강의를 찾을 수 없습니다"),
    SECTION_NOT_FOUND(404, "섹션을 찾을 수 없습니다"),
    LESSON_NOT_FOUND(404, "레슨을 찾을 수 없습니다"),
    COURSE_ACCESS_DENIED(403, "해당 강의에 대한 권한이 없습니다"),

    // Enrollment
    ENROLLMENT_NOT_FOUND(404, "수강 정보를 찾을 수 없습니다"),
    ALREADY_ENROLLED(409, "이미 수강 중인 강의입니다"),
    INVALID_PROGRESS(400, "진도율은 0~100 사이여야 합니다"),

    // File
    FILE_TOO_LARGE(413, "파일 크기가 제한을 초과합니다"),
    UNSUPPORTED_FILE_TYPE(415, "지원하지 않는 파일 형식입니다"),
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다");

    private final int status;
    private final String message;
}
