# Exception Rules

## Exception Hierarchy
```
RuntimeException
  └── BusinessException
        └── ErrorCode (enum: status + message)
```

## ErrorCode Enum
```java
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

// Course
COURSE_NOT_FOUND(404, "강의를 찾을 수 없습니다"),
ENROLLMENT_NOT_FOUND(404, "수강 정보를 찾을 수 없습니다"),

// Quiz / Assignment
QUIZ_NOT_FOUND(404, "퀴즈를 찾을 수 없습니다"),
SUBMISSION_NOT_FOUND(404, "제출물을 찾을 수 없습니다"),

// AI
CONFIDENCE_TOO_LOW(422, "AI 채점 신뢰도가 기준 미달입니다"),
PII_LEAK_DETECTED(500, "PII 유출이 감지되었습니다"),
AI_GATEWAY_ERROR(502, "AI Gateway 호출 실패"),
AI_BUDGET_EXCEEDED(429, "AI 비용 한도를 초과했습니다"),
```

## Usage
```java
throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
throw new BusinessException(ErrorCode.CONFIDENCE_TOO_LOW);
```

## Forbidden
- RuntimeException / IllegalArgumentException 직접 throw 금지
- try-catch로 예외 삼키기 금지
- 에러 응답에 stacktrace 노출 금지 (운영)
- null 반환 후 null 체크 패턴 금지
