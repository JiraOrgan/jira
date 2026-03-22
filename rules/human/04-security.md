# 4. Security RULE -- 보안, JWT 인증, RBAC [MUST]

> **기준**: Spring Security, JWT (jjwt 0.12.6), RBAC 5단계

### 4.1 환경 변수 관리 [MUST]

- DB 비밀번호, JWT Secret, API Key 등 민감 정보는 **환경 변수**로 주입한다
- application.yml에 하드코딩 **절대 금지**
- .env 파일은 .gitignore에 포함한다

```yaml
# Good
spring:
  datasource:
    password: ${DB_PASSWORD:root}
jwt:
  secret: ${JWT_SECRET:dev-secret-key-must-be-at-least-32-characters-long}

# Bad
spring:
  datasource:
    password: myp@ssw0rd123
```

### 4.2 JWT 토큰 관리 [MUST]

| 항목 | 값 | 비고 |
|------|---|------|
| Access Token 만료 | 1시간 | 짧게 유지 |
| Refresh Token 만료 | 7일 | Redis 저장 권장 |
| 알고리즘 | HS256 이상 | Secret 32자 이상 |
| 저장 위치 | Authorization Bearer | 쿠키 저장 시 HttpOnly + Secure 필수 |

### 4.3 RBAC 권한 체계 [MUST]

5단계 프로젝트 역할 기반 접근 제어:

| 역할 | 이슈 생성 | 이슈 수정 | 이슈 삭제 | 상태 전환 | Sprint 관리 | 프로젝트 설정 |
|------|:-------:|:-------:|:-------:|:-------:|:---------:|:----------:|
| ADMIN | O | O | O | O | O | O |
| DEVELOPER | O | O | O | O | O | X |
| QA | O | O | X | O | X | X |
| REPORTER | O | 본인만 | X | X | X | X |
| VIEWER | X | X | X | X | X | X |

### 4.4 비밀번호 정책 [MUST]

- **BCrypt** 알고리즘으로 해시 저장
- 평문 비밀번호 로깅 절대 금지
- 로그인 5회 연속 실패 시 30분 잠금

### 4.5 CORS 설정 [MUST]

- 환경별 허용 오리진을 명시적으로 관리한다
- allowCredentials(true) 사용 시 와일드카드(*) 오리진 금지

### 4.6 금지 사항

- 비밀번호 평문 저장 금지
- JWT Secret 하드코딩 금지
- 민감 정보(비밀번호, 토큰) 로그 출력 금지
- CORS allowedOrigins에 "*" + allowCredentials(true) 동시 사용 금지
- csrf를 비활성화한 상태에서 쿠키 기반 인증 사용 금지 (JWT Bearer 사용)

---

## 참고 문서

- [01-architecture.md](01-architecture.md) -- SecurityConfig 위치
- [06-exception.md](06-exception.md) -- 인증/인가 예외 처리
