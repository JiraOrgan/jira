# Security Rules

## Sensitive Values
- 환경 변수로 주입: ${DB_PASSWORD}, ${JWT_SECRET}, ${CLAUDE_API_KEY}
- application.yml 하드코딩 금지
- .env 파일 .gitignore 포함

## JWT
- Access Token: 15분, Refresh Token: 7일
- Algorithm: HS256+, Secret 32자 이상
- Header: Authorization: Bearer {token}

## RBAC (3 Roles)
| Role | 강의 관리 | 수강/학습 | AI 튜터 | 퀴즈/과제 출제 | 채점 검토 | 시스템 설정 |
|------|:--------:|:--------:|:------:|:------------:|:--------:|:----------:|
| ADMIN | O | O | O | O | O | O |
| INSTRUCTOR | O | X | X | O | O | X |
| LEARNER | X | O | O | X (제출만) | X (이의만) | X |

## Password
- BCrypt(cost 12+) hash 저장
- 평문 저장/로깅 금지
- 5회 실패 → 30분 잠금

## 보안 7 Layer
```
Layer 1: 입력 필터링     — 길이 제한 + 위험 패턴 감지
Layer 2: PII Masking     — Input + Output 양방향 (Presidio + KoNLPy)
Layer 3: System Prompt   — 절대 노출 금지, 사용자 입력과 격리
Layer 4: Output Validation — 점수 범위 / JSON 스키마 검증
Layer 5: 데이터 격리     — course_id 기반 RAG 범위 제한
Layer 6: Tool 제한       — DB 직접 조회 / 외부 URL / 파일 접근 차단
Layer 7: FinOps Kill-switch — Soft/Hard 한도 + 자동 다운그레이드
```

## CORS
- 환경별 오리진 명시적 관리
- allowCredentials(true) + "*" 금지
