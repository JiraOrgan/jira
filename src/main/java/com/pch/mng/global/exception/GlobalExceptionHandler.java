package com.pch.mng.global.exception;

import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.jql.JqlParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("AccessDenied: {}", e.getMessage());
        return ResponseEntity.status(403)
                .body(ApiResponse.fail(403, "접근 권한이 없습니다"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(400, "요청 본문(JSON)을 읽을 수 없습니다"));
    }

    @ExceptionHandler(JqlParseException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleJqlParse(JqlParseException e) {
        log.warn("JqlParseException: {}", e.getMessage());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("position", e.getPosition());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(400, e.getMessage(), detail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(400, "입력값 검증 실패", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(500, "서버 오류가 발생했습니다"));
    }
}
