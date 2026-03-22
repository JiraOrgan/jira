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
    EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다");

    private final int status;
    private final String message;
}
