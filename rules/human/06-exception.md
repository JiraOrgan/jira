# 6. Exception RULE -- 예외 처리 및 에러 응답 [MUST]

> **기준**: GlobalExceptionHandler, BusinessException, ErrorCode enum

### 6.1 예외 계층 구조 [MUST]

```text
RuntimeException
  └── BusinessException (커스텀)
        └── ErrorCode enum (상태코드 + 메시지)
```

### 6.2 ErrorCode 정의 [MUST]

| 카테고리 | 코드 | HTTP | 메시지 |
|---------|------|------|--------|
| Common | INVALID_INPUT_VALUE | 400 | 잘못된 입력값입니다 |
| Common | ENTITY_NOT_FOUND | 404 | 리소스를 찾을 수 없습니다 |
| Common | DUPLICATE_RESOURCE | 409 | 이미 존재하는 리소스입니다 |
| Common | INTERNAL_SERVER_ERROR | 500 | 서버 오류가 발생했습니다 |
| Auth | UNAUTHORIZED | 401 | 인증이 필요합니다 |
| Auth | FORBIDDEN | 403 | 접근 권한이 없습니다 |
| Auth | EXPIRED_TOKEN | 401 | 토큰이 만료되었습니다 |
| Auth | INVALID_TOKEN | 401 | 유효하지 않은 토큰입니다 |
| User | USER_NOT_FOUND | 404 | 사용자를 찾을 수 없습니다 |
| User | EMAIL_ALREADY_EXISTS | 409 | 이미 사용 중인 이메일입니다 |
| Course | COURSE_NOT_FOUND | 404 | 강의를 찾을 수 없습니다 |
| Course | ENROLLMENT_NOT_FOUND | 404 | 수강 정보를 찾을 수 없습니다 |
| Quiz | QUIZ_NOT_FOUND | 404 | 퀴즈를 찾을 수 없습니다 |
| Assignment | SUBMISSION_NOT_FOUND | 404 | 제출물을 찾을 수 없습니다 |
| AI | CONFIDENCE_TOO_LOW | 422 | AI 채점 신뢰도가 기준 미달입니다 |
| AI | PII_LEAK_DETECTED | 500 | PII 유출이 감지되었습니다 |
| AI | AI_GATEWAY_ERROR | 502 | AI Gateway 호출 실패 |
| AI | AI_BUDGET_EXCEEDED | 429 | AI 비용 한도를 초과했습니다 |

도메인별 ErrorCode 추가 시 카테고리를 주석으로 구분한다.

### 6.3 사용 패턴 [MUST]

```java
// Good
throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
throw new BusinessException(ErrorCode.CONFIDENCE_TOO_LOW);

// Bad -- RuntimeException 직접 사용
throw new RuntimeException("강의를 찾을 수 없습니다");
throw new IllegalArgumentException("잘못된 입력");
```

### 6.4 GlobalExceptionHandler [MUST]

- @RestControllerAdvice로 전역 처리
- BusinessException, MethodArgumentNotValidException, 기타 Exception 구분 처리
- 에러 응답도 ApiResponse.fail() 래퍼 사용

### 6.5 금지 사항

- RuntimeException, IllegalArgumentException 직접 throw 금지 (BusinessException 사용)
- try-catch로 예외 삼키기 금지 (최소 로깅 필수)
- 에러 응답에 stacktrace 노출 금지 (운영 환경)
- null 반환 후 호출부에서 null 체크 패턴 금지 (orElseThrow 사용)

---

## 참고 문서

- [05-service-layer.md](05-service-layer.md) -- Service에서 예외 사용
- [03-api-design.md](03-api-design.md) -- HTTP 상태 코드 매핑
