# Project Control Hub - 개발 워크플로우

> **버전**: v1.4
> **작성일**: 2026-03-22
> **최종수정일**: 2026-04-14 (본문 `develop(또는 dev)` 잔여 제거)
> **연결 문서**: [PHASE.md](PHASE.md) | [TASKS.md](TASKS.md) | [PRD.md](PRD.md)
> **Git 정본**: `C:\workspace\phs-prj\documents\08-Git규칙정의서_v3.0.md` — 본 파일은 코드 저장소용 요약이며, 세부·예외는 해당 문서를 따른다.

기능 완료·Task 상태·릴리즈 단위 진척은 **[TASKS.md](TASKS.md) 변경 이력** 및 Phase 로드맵의 **「일정 vs 저장소 진척」** 안내([PHASE.md](PHASE.md))를 본다.

---

## 1. Git 브랜치 전략

### 1.1 브랜치 구조 (Git 규칙 정의서 v3.0 정렬)

```
main            ── 운영 배포 (직접 커밋 금지)
  └── develop   ── 개발 통합
       ├── feature/{이슈키}-{설명}   ── 기능 개발
       ├── bugfix/{이슈키}-{설명}    ── 버그 수정
       ├── release/{버전}           ── 배포 준비
       └── docs/{설명}              ── 문서 작업 (선택 규칙)

hotfix/{이슈키}-{설명}  ── main에서 분기 → main·develop에 병합
```

> **본 저장소**: GitHub 통합 브랜치는 **`develop`**이다. 타 문서·도구에서 `dev`를 쓰는 경우 역할은 동일하게 이해한다.

### 1.2 브랜치 네이밍 규칙

| 종류 | 용도 | 예시 |
|------|------|------|
| feature/ | 기능 개발 | feature/PROJ-23-login |
| bugfix/ | 버그 수정 | bugfix/PROJ-45-wip-warning |
| hotfix/ | 긴급 수정 | hotfix/PROJ-99-security |
| release/ | 릴리즈 준비 | release/1.0 |
| docs/ | 문서 | docs/api-spec-update |
| refactor/ | 리팩토링 (로컬 합의 시) | refactor/issue-service |

Task ID만 알려진 경우: `feature/T-300-jwt-auth` 형태도 허용(팀 합의 하에).

### 1.3 브랜치 규칙

- **main**: 직접 push 금지, PR + 최소 1 Approve, 릴리즈·태그 정책은 `08-Git규칙정의서` §7
- **develop**: 기능 브랜치 머지 대상, CI 통과 필수
- **feature**/**bugfix**: develop에서 분기 → PR로 develop에 병합
- **hotfix**: main에서 분기 → main + develop 동기화

---

## 2. 커밋 컨벤션

### 2.1 Conventional Commits

```
{type}({scope}): {subject}

{body}

Refs: PROJ-123
```

Footer에 **이슈 키**(`Refs:` / `Closes:`)를 두는 방식은 `08-Git규칙정의서` §2.4와 동일하다. (Jira 미사용 시 Task ID로 대체 가능.)

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

`08-Git규칙정의서` §2.3 모듈 매핑을 권장: `issue`, `board`, `workflow`, `jql`, `auth`, `sprint`, `dashboard`, `audit`, `mobile` 등

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
2. develop에서 기능 브랜치 생성
   $ git checkout develop && git pull
   $ git checkout -b feature/T-300-jwt-auth

3. 코드 작성 (rules/ 규칙 준수)
   - Entity → Repository → DTO → Service → Controller 순서
   - 단위 테스트 작성

4. 커밋 (Conventional Commits)
   $ git commit -m "feat(auth): JWT 로그인 API 구현"

5. Push 및 PR 생성
   $ git push -u origin feature/T-300-jwt-auth
   - PR 제목: [T-300] JWT 로그인 API 구현
   - PR 본문: Task ID, 변경 사항, 테스트 방법

6. 코드 리뷰 (REVIEW 상태)
   - 최소 1인 Approve 필수
   - CI 통과 필수

7. develop로 머지 (Squash Merge 권장)

8. TASKS.md 상태 업데이트 (DONE)
```

---

## 4. PR (Pull Request) 규칙

### 4.1 PR 템플릿

`08-Git규칙정의서` §3.1과 호환되도록 다음을 포함한다.

```markdown
## 변경 사항
-

## 변경 사유
- Issue: PROJ-{번호} 또는 Task: T-{id}

## 테스트 결과
- [ ] 단위 테스트 통과
- [ ] 기능/API 수동 테스트

## 관련 이슈
- Closes PROJ-{번호}
```

### 4.2 PR 규칙

| 항목 | 규칙 |
|------|------|
| 제목 | `[T-{id}] {요약}` 형식 |
| 리뷰어 | 최소 1명 지정 |
| CI | 빌드 + 테스트 통과 필수 |
| 크기 | 파일 변경 300줄 이하 권장 (초과 시 분리) |
| 머지 | Squash Merge (develop), Merge Commit (main) — 팀 정책에 따름 |
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
- [ ] develop 브랜치 머지 완료, 빌드 성공
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
develop 머지 → 스테이징 자동 배포
  └── 수동 승인 → 운영 배포 (Canary 또는 Blue-Green — `12-배포가이드_v4.0` 기준)

main 머지 → 운영 배포 트리거
  ├── Docker Build → ECR Push
  ├── ECS Task Definition 업데이트
  ├── Health Check 통과 확인
  └── 배포 완료 알림
```

### 7.3 환경 구성

| 환경 | 브랜치 | DB | URL |
|------|--------|-----|-----|
| Local | feature/* | localhost:5432 (PostgreSQL) | localhost:8080 |
| Staging | develop | RDS (staging) | staging.example.com |
| Production | main | RDS (prod, Multi-AZ) | api.example.com |

---

## 8. Phase별 워크플로우 연결

| Phase | 브랜치 전략 | 주요 워크플로우 |
|-------|-----------|--------------|
| Phase 0 | develop 직접 커밋(예외) | 초기 설정, 팀 합의 하 빠른 진행 |
| Phase 1-2 | docs/ 또는 feature/docs-* | 문서 중심, 리뷰 간소화 |
| Phase 3-6 | feature/* · bugfix/* | 정규 Sprint, PR 필수, 코드 리뷰 |
| Phase 7 | bugfix/* | 버그 수정·회귀 테스트 |
| Phase 8 | release/* · hotfix/* | 배포 체크리스트, 롤백 계획 (`12-배포가이드`) |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | 개발 워크플로우 초안 작성 |
| v1.1 | 2026-04-09 | `08-Git규칙정의서_v3.0` 정렬(main/develop/feature/bugfix/release/hotfix), 커밋 Footer·스코프, CD·환경(PostgreSQL) 갱신 |
| v1.3 | 2026-04-14 | 본 저장소 통합 브랜치 **`develop`** 명시(§1.1 주석·§1.3), `dev` 혼동 완화 |
| v1.4 | 2026-04-14 | §3.2·DoD·§7.3 등 본문 잔여 **`develop(또는 dev)`** → **`develop`** 로 통일 |
