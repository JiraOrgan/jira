# 4. Security RULE -- 보안, JWT 인증, RBAC [MUST]

> **기준**: Spring Security, JWT (jjwt 0.12.6), RBAC 3단계

### 4.1 환경 변수 관리 [MUST]

- DB 비밀번호, JWT Secret, AI API Key 등 민감 정보는 **환경 변수**로 주입한다
- application.yml에 하드코딩 **절대 금지**
- .env 파일은 .gitignore에 포함한다

```yaml
# Good
spring:
  datasource:
    password: ${DB_PASSWORD:root}
jwt:
  secret: ${JWT_SECRET:dev-secret-key-must-be-at-least-32-characters-long}
ai:
  claude:
    api-key: ${CLAUDE_API_KEY}

# Bad
spring:
  datasource:
    password: myp@ssw0rd123
```

### 4.2 JWT 토큰 관리 [MUST]

| 항목 | 값 | 비고 |
|------|---|------|
| Access Token 만료 | 15분 | 짧게 유지 (보안 강화) |
| Refresh Token 만료 | 7일 | Redis 저장 |
| 알고리즘 | HS256 이상 | Secret 32자 이상 |
| 저장 위치 | Authorization Bearer | 쿠키 저장 시 HttpOnly + Secure 필수 |

### 4.3 RBAC 권한 체계 [MUST]

3단계 역할 기반 접근 제어:

| 역할 | 강의 관리 | 수강/학습 | AI 튜터 | 퀴즈/과제 출제 | 채점 검토 | 시스템 설정 |
|------|:--------:|:--------:|:------:|:------------:|:--------:|:----------:|
| ADMIN | O | O | O | O | O | O |
| INSTRUCTOR | O | X | X | O | O (Manual Review) | X |
| LEARNER | X | O | O | X (제출만) | X (이의 제기만) | X |

### 4.4 비밀번호 정책 [MUST]

- **BCrypt(cost 12+)** 알고리즘으로 해시 저장
- 평문 비밀번호 로깅 절대 금지
- 로그인 5회 연속 실패 시 30분 잠금

### 4.5 보안 7 Layer [MUST]

AI 코드 작성 시 반드시 준수하는 보안 계층:

```text
Layer 1: 입력 필터링     — 길이 제한 + 위험 패턴 감지
Layer 2: PII Masking     — Input + Output 양방향 (Presidio + KoNLPy)
Layer 3: System Prompt   — 절대 노출 금지, 사용자 입력과 격리
Layer 4: Output Validation — 점수 범위 / JSON 스키마 검증
Layer 5: 데이터 격리     — course_id 기반 RAG 범위 제한
Layer 6: Tool 제한       — DB 직접 조회 / 외부 URL / 파일 접근 차단
Layer 7: FinOps Kill-switch — Soft/Hard 한도 + 자동 다운그레이드
```

### 4.6 CORS 설정 [MUST]

- 환경별 허용 오리진을 명시적으로 관리한다
- allowCredentials(true) 사용 시 와일드카드(*) 오리진 금지

### 4.7 금지 사항

- 비밀번호 평문 저장 금지
- JWT Secret / AI API Key 하드코딩 금지
- 민감 정보(비밀번호, 토큰) 로그 출력 금지
- CORS allowedOrigins에 "*" + allowCredentials(true) 동시 사용 금지
- PII 처리 없이 LLM에 사용자 데이터 전송 금지
- System Prompt을 사용자에게 노출하는 코드 금지

---

## 참고 문서

- [01-architecture.md](01-architecture.md) -- SecurityConfig 위치
- [06-exception.md](06-exception.md) -- 인증/인가 예외 처리
