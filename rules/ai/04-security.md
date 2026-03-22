# Security Rules

## Sensitive Values
- 환경 변수로 주입: ${DB_PASSWORD}, ${JWT_SECRET}
- application.yml 하드코딩 금지
- .env 파일 .gitignore 포함

## JWT
- Access Token: 1시간, Refresh Token: 7일
- Algorithm: HS256+, Secret 32자 이상
- Header: Authorization: Bearer {token}

## RBAC (5 Roles)
| Role | Create | Edit | Delete | Transition | Sprint | Settings |
|------|:------:|:----:|:------:|:----------:|:------:|:--------:|
| ADMIN | O | O | O | O | O | O |
| DEVELOPER | O | O | O | O | O | X |
| QA | O | O | X | O | X | X |
| REPORTER | O | own | X | X | X | X |
| VIEWER | X | X | X | X | X | X |

## Password
- BCrypt hash 저장
- 평문 저장/로깅 금지
- 5회 실패 → 30분 잠금

## CORS
- 환경별 오리진 명시적 관리
- allowCredentials(true) + "*" 금지
