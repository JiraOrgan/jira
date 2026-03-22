# Project Control Hub - 개발 워크플로우

> **버전**: v1.0
> **작성일**: 2026-03-22
> **연결 문서**: [PHASE.md](PHASE.md) | [TASKS.md](TASKS.md) | [PRD.md](PRD.md)

---

## 1. Git 브랜치 전략

### 1.1 브랜치 구조

```
master          ── 운영 배포 브랜치 (직접 커밋 금지)
  └── dev       ── 개발 통합 브랜치
       ├── feat/{task-id}-{설명}   ── 기능 개발
       ├── fix/{task-id}-{설명}    ── 버그 수정
       ├── hotfix/{설명}           ── 긴급 수정 (master에서 분기)
       └── docs/{설명}             ── 문서 작업
```

### 1.2 브랜치 네이밍 규칙

| 접두사 | 용도 | 예시 |
|--------|------|------|
| feat/ | 기능 개발 | feat/T-300-jwt-auth |
| fix/ | 버그 수정 | fix/T-706-login-error |
| hotfix/ | 긴급 수정 | hotfix/security-patch |
| docs/ | 문서 | docs/api-spec-update |
| refactor/ | 리팩토링 | refactor/issue-service |

### 1.3 브랜치 규칙

- master: 직접 push 금지, PR + 최소 1 Approve 필요
- dev: 기능 브랜치 머지 대상, CI 통과 필수
- 기능 브랜치: dev에서 분기, dev로 머지

---

## 2. 커밋 컨벤션

### 2.1 Conventional Commits

```
{type}({scope}): {subject}

{body}

Co-Authored-By: {author}
```

### 2.2 타입

| 타입 | 용도 | 예시 |
|------|------|------|
| feat | 기능 추가 | feat(issue): 이슈 CRUD API 구현 |
| fix | 버그 수정 | fix(auth): 토큰 갱신 실패 수정 |
| docs | 문서 | docs: PRD 문서 작성 |
| refactor | 리팩토링 | refactor(project): 서비스 로직 분리 |
| test | 테스트 | test(user): 회원가입 단위 테스트 |
| chore | 빌드/설정 | chore: build.gradle 의존성 업데이트 |
| style | 포맷팅 | style: 코드 포맷팅 정리 |

### 2.3 스코프

도메인명을 스코프로 사용: `user`, `project`, `issue`, `sprint`, `release`, `comment`, `dashboard`, `auth`, `global`

---

## 3. Task 작업 흐름

### 3.1 Task 라이프사이클

```
TODO → IN_PROGRESS → REVIEW → DONE
  │                    │
  └── BLOCKED          └── IN_PROGRESS (리뷰 반려 시)
```

### 3.2 개발자 작업 순서

```
1. TASKS.md에서 할당된 Task 확인 (TODO → IN_PROGRESS)
2. dev에서 기능 브랜치 생성
   $ git checkout dev && git pull
   $ git checkout -b feat/T-300-jwt-auth

3. 코드 작성 (rules/ 규칙 준수)
   - Entity → Repository → DTO → Service → Controller 순서
   - 단위 테스트 작성

4. 커밋 (Conventional Commits)
   $ git commit -m "feat(auth): JWT 로그인 API 구현"

5. Push 및 PR 생성
   $ git push -u origin feat/T-300-jwt-auth
   - PR 제목: [T-300] JWT 로그인 API 구현
   - PR 본문: Task ID, 변경 사항, 테스트 방법

6. 코드 리뷰 (REVIEW 상태)
   - 최소 1인 Approve 필수
   - CI 통과 필수

7. dev로 머지 (Squash Merge 권장)

8. TASKS.md 상태 업데이트 (DONE)
```

---

## 4. PR (Pull Request) 규칙

### 4.1 PR 템플릿

```markdown
## Summary
- [T-{id}] {Task 제목}
- {변경 사항 1~3줄 요약}

## Changes
- [ ] {구체적 변경 내용 1}
- [ ] {구체적 변경 내용 2}

## Test Plan
- [ ] 단위 테스트 통과
- [ ] API 수동 테스트 (Swagger)
- [ ] 기존 기능 회귀 확인

## Related
- Phase: {Phase N}
- Task: T-{id}
- Related PRs: #{PR 번호}
```

### 4.2 PR 규칙

| 항목 | 규칙 |
|------|------|
| 제목 | `[T-{id}] {요약}` 형식 |
| 리뷰어 | 최소 1명 지정 |
| CI | 빌드 + 테스트 통과 필수 |
| 크기 | 파일 변경 300줄 이하 권장 (초과 시 분리) |
| 머지 | Squash Merge (dev), Merge Commit (master) |
| 삭제 | 머지 후 기능 브랜치 자동 삭제 |

---

## 5. 코드 리뷰 기준

### 5.1 체크리스트

| 카테고리 | 확인 항목 |
|---------|----------|
| 아키텍처 | 레이어 책임 분리 준수 (Controller ← Service ← Repository) |
| 아키텍처 | 순환 참조 없음 |
| Entity | @Data + @NoArgsConstructor + @Entity + @Table 순서 |
| Entity | FetchType.LAZY 사용, EAGER 없음 |
| Entity | @Builder 생성자 레벨 |
| API | ApiResponse 래퍼 사용 |
| API | @Valid 입력 검증 |
| Service | @Transactional(readOnly=true) 클래스 레벨 |
| Service | BusinessException + ErrorCode 사용 |
| DTO | 정적 팩토리 of() 패턴 |
| DTO | Entity 직접 반환 없음 |
| 보안 | 민감 정보 하드코딩 없음 |
| 보안 | 비밀번호 평문 로깅 없음 |
| 테스트 | 신규 코드 단위 테스트 포함 |

### 5.2 리뷰 응답 규칙

| 표기 | 의미 | 반영 필수 |
|------|------|----------|
| **[MUST]** | 반드시 수정 | 수정 전 Approve 불가 |
| **[SHOULD]** | 권장 수정 | Approve 가능, 후속 대응 |
| **[NIT]** | 사소한 의견 | 무시 가능 |
| **[Q]** | 질문 | 답변 필요 |

---

## 6. Sprint 운영

### 6.1 Sprint 사이클 (2주)

| 일차 | 이벤트 | 소요 | 참여 |
|------|--------|------|------|
| 1일차 오전 | Sprint Planning | 2~4시간 | 전체 |
| 매일 | Daily Scrum | 15분 | 전체 |
| 10일차 오후 | Sprint Review | 1~2시간 | 전체 + 이해관계자 |
| 10일차 말 | Retrospective | 1시간 | 전체 |

### 6.2 Sprint Planning 절차

```
1. PM: Sprint Goal 제시
2. 전체: 백로그에서 이슈 선택 (DoR 충족 확인)
3. 전체: Planning Poker로 SP 산정
4. PM: Sprint에 이슈 배정 (팀 Velocity 기준)
5. 개발자: Task 분해 및 담당자 지정
```

### 6.3 Definition of Ready (DoR)

Sprint에 이슈를 포함하기 위한 최소 조건:

- [ ] Acceptance Criteria 명문화
- [ ] UI/UX 산출물 존재
- [ ] API 스펙 확정
- [ ] Story Point 합의 (SP <= 8)
- [ ] 의존성 파악 완료

### 6.4 Definition of Done (DoD)

이슈를 Done으로 전환하기 위한 조건:

- [ ] 코드 구현 완료 (AC 전체 충족)
- [ ] 코드 리뷰 완료 (최소 1 Approve)
- [ ] 단위 테스트 통과 (커버리지 80%+)
- [ ] QA 테스트 통과
- [ ] 문서 업데이트 완료
- [ ] dev 브랜치 머지 완료, 빌드 성공
- [ ] 회귀 테스트 확인

---

## 7. CI/CD 파이프라인

### 7.1 CI (Continuous Integration)

```
Push/PR → GitHub Actions
  ├── Build (Gradle compileJava)
  ├── Test (./gradlew test)
  ├── Lint (Checkstyle)
  └── Security Scan (OWASP Dependency Check)
```

### 7.2 CD (Continuous Deployment)

```
dev 머지 → 스테이징 자동 배포
  └── 수동 승인 → 운영 배포 (Blue-Green)

master 머지 → 운영 배포 트리거
  ├── Docker Build → ECR Push
  ├── ECS Task Definition 업데이트
  ├── Health Check 통과 확인
  └── 배포 완료 Slack 알림
```

### 7.3 환경 구성

| 환경 | 브랜치 | DB | URL |
|------|--------|-----|-----|
| Local | feat/* | localhost:3306 | localhost:8080 |
| Staging | dev | RDS (staging) | staging.jira-pm.example.com |
| Production | master | RDS (prod, Multi-AZ) | api.jira-pm.example.com |

---

## 8. Phase별 워크플로우 연결

| Phase | 브랜치 전략 | 주요 워크플로우 |
|-------|-----------|--------------|
| Phase 0 | dev 직접 커밋 | 초기 설정, 규칙 없이 빠르게 진행 |
| Phase 1-2 | docs/ 브랜치 | 문서 중심, 리뷰 간소화 |
| Phase 3-6 | feat/T-{id} 브랜치 | 정규 Sprint 운영, PR 필수, 코드 리뷰 |
| Phase 7 | fix/T-{id} 브랜치 | 버그 수정 집중, 회귀 테스트 |
| Phase 8 | hotfix/ (필요 시) | 배포 체크리스트, 롤백 계획 |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | 개발 워크플로우 초안 작성 |
