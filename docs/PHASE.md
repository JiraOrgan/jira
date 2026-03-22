# Project Control Hub - Phase 로드맵

> **버전**: v1.0
> **작성일**: 2026-03-22
> **기준 문서**: [PRD.md](PRD.md)
> **연결 문서**: [TASKS.md](TASKS.md) | [WORKFLOW.md](WORKFLOW.md)

---

## Phase 전체 요약

| Phase | 구분 | 기간 | 상태 |
|-------|------|------|------|
| Phase 0 | 프로젝트 초기 설정 | 2026-03-22 | **완료** |
| Phase 1 | 기획 | 2026-04-01 ~ 04-14 (2주) | 대기 |
| Phase 2 | 설계 | 2026-04-15 ~ 05-05 (3주) | 대기 |
| Phase 3 | 개발 Sprint 1 — 인증/사용자/프로젝트 | 2026-05-06 ~ 05-19 (2주) | 대기 |
| Phase 4 | 개발 Sprint 2 — 이슈 핵심 기능 | 2026-05-20 ~ 06-02 (2주) | 대기 |
| Phase 5 | 개발 Sprint 3 — 보드/스프린트/백로그 | 2026-06-03 ~ 06-16 (2주) | 대기 |
| Phase 6 | 개발 Sprint 4 — 대시보드/릴리즈/검색 | 2026-06-17 ~ 06-30 (2주) | 대기 |
| Phase 7 | QA 및 버그 수정 | 2026-07-01 ~ 07-21 (3주) | 대기 |
| Phase 8 | 배포 | 2026-07-22 ~ 07-31 (1.5주) | 대기 |

---

## Phase 0 — 프로젝트 초기 설정 [완료]

> **기간**: 2026-03-22
> **산출물**: 프로젝트 scaffolding, Entity, Repository, DTO, Service, Controller

### 완료 항목

- [x] Spring Boot 4.0.3 프로젝트 초기화 (Java 21, Gradle)
- [x] 공통 인프라 구성 (SecurityConfig, RedisConfig, SwaggerConfig, GlobalExceptionHandler)
- [x] PRD 문서 작성 (jira-doc 기반 종합)
- [x] 컨벤션 정의 (.ai/spring-conventions.json)
- [x] 공유 Enum 9개 생성
- [x] Entity 20개 생성 (Phase A~E)
- [x] Repository 20개 + DTO 17개 + Service 7개 + Controller 8개 생성
- [x] Spring 개발 규칙 문서 14개 작성 (human/ai)

### 생성된 도메인 구조

```
com.jira.mng/
├── global/    ── config, exception, response, enums, filter, aop
├── user/      ── UserAccount (CRUD)
├── project/   ── Project, ProjectMember, ProjectComponent, WipLimit
├── issue/     ── Issue, IssueLabel, IssueComponent, IssueFixVersion, IssueWatcher, IssueLink
├── sprint/    ── Sprint (lifecycle)
├── release/   ── ReleaseVersion
├── comment/   ── Comment (CRUD)
├── attachment/ ── Attachment (Repository/Response)
├── workflow/  ── WorkflowTransition (조회)
├── audit/     ── AuditLog (페이징 조회)
├── dashboard/ ── Dashboard, DashboardGadget (CRUD)
└── label/     ── Label (Repository)
```

---

## Phase 1 — 기획 [대기]

> **기간**: 2026-04-01 ~ 04-14 (2주)
> **마일스톤**: M1. 기획 완료
> **산출물**: 요구사항 정의서, 화면 설계서
> **Task**: [TASKS.md - Phase 1](TASKS.md#phase-1--기획-tasks)

### 목표

- 요구사항 상세 분석 및 확정 (FR-001 ~ FR-033)
- 비기능 요구사항 검증 (NFR-001 ~ NFR-009)
- 화면 와이어프레임 작성 (SCR-001 ~ SCR-014)
- 사용자 스토리 매핑 및 우선순위 확정

### 주요 산출물

| 산출물 | 설명 | 담당 |
|--------|------|------|
| 요구사항 정의서 v2.0 | FR/NFR 상세 확정 | PM |
| 화면 와이어프레임 | 14개 화면 Figma | 디자이너 |
| 사용자 스토리 맵 | Epic → Story 분해 | PM + 전체 |
| Sprint Backlog 초안 | Phase 3~6 작업 배분 | PM |

---

## Phase 2 — 설계 [대기]

> **기간**: 2026-04-15 ~ 05-05 (3주)
> **마일스톤**: M2. 설계 완료
> **산출물**: ERD, API 정의서, 아키텍처 정의서
> **Task**: [TASKS.md - Phase 2](TASKS.md#phase-2--설계-tasks)

### 목표

- ERD 최종 확정 및 DDL 생성
- REST API 상세 스펙 확정 (Request/Response 스키마)
- 아키텍처 상세 설계 (시퀀스 다이어그램, 컴포넌트 다이어그램)
- UI/UX 디자인 시스템 확정

### 주요 산출물

| 산출물 | 설명 | 담당 |
|--------|------|------|
| ERD v3.0 | 인덱스, 제약조건 포함 최종 | 백엔드 |
| API 정의서 v3.0 | Swagger 기반 상세 스펙 | 백엔드 |
| 아키텍처 정의서 v3.0 | 시퀀스/컴포넌트 다이어그램 | 백엔드 |
| UI 디자인 시스템 | 컴포넌트 라이브러리, 스타일 가이드 | 디자이너 |

---

## Phase 3 — 개발 Sprint 1: 인증/사용자/프로젝트 [대기]

> **기간**: 2026-05-06 ~ 05-19 (2주)
> **Sprint Goal**: 사용자 인증 및 프로젝트 관리 기본 기능 완성
> **Task**: [TASKS.md - Phase 3](TASKS.md#phase-3--개발-sprint-1-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| JWT 로그인/회원가입/토큰 갱신 | - | auth |
| 사용자 CRUD + 비밀번호 BCrypt | - | user |
| 프로젝트 CRUD | - | project |
| 프로젝트 멤버 관리 (RBAC) | FR-030 | project |
| Spring Security Filter Chain | - | global/config |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 로그인 | SCR-001 |
| 프로젝트 설정 | SCR-013 |

---

## Phase 4 — 개발 Sprint 2: 이슈 핵심 기능 [대기]

> **기간**: 2026-05-20 ~ 06-02 (2주)
> **Sprint Goal**: 이슈 CRUD 및 워크플로우 전환 완성
> **Task**: [TASKS.md - Phase 4](TASKS.md#phase-4--개발-sprint-2-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 이슈 CRUD (5 타입) | FR-001, FR-002 | issue |
| 이슈 키 자동 생성 (PROJ-N) | FR-001 | issue |
| 워크플로우 6단계 전환 | FR-013, FR-014 | issue, workflow |
| 전환 규칙 검증 엔진 | FR-014 | workflow |
| 이슈 링크 (Blocks/Duplicates/Relates) | FR-007 | issue |
| 댓글 CRUD + @멘션 | FR-023 | comment |
| 첨부파일 업로드 (S3) | - | attachment |
| Audit Log 자동 기록 | FR-028 | audit |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 이슈 상세 | SCR-006 |
| 이슈 생성 | SCR-007 |

---

## Phase 5 — 개발 Sprint 3: 보드/스프린트/백로그 [대기]

> **기간**: 2026-06-03 ~ 06-16 (2주)
> **Sprint Goal**: 스크럼/칸반 보드 및 스프린트 관리 완성
> **Task**: [TASKS.md - Phase 5](TASKS.md#phase-5--개발-sprint-3-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 스프린트 생성/시작/완료 lifecycle | FR-011 | sprint |
| 백로그 관리 (순서, 스프린트 배정) | FR-010 | issue, sprint |
| WIP 제한 설정 및 검증 | FR-009 | project |
| 스토리 포인트 산정 | FR-017 | issue |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 백로그 | SCR-003 |
| 스크럼 보드 | SCR-004 |
| 칸반 보드 | SCR-005 |
| 스프린트 관리 | SCR-010 |

---

## Phase 6 — 개발 Sprint 4: 대시보드/릴리즈/검색 [대기]

> **기간**: 2026-06-17 ~ 06-30 (2주)
> **Sprint Goal**: 대시보드, 릴리즈, JQL 검색 완성
> **Task**: [TASKS.md - Phase 6](TASKS.md#phase-6--개발-sprint-4-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 대시보드 + 가젯 CRUD | FR-021 | dashboard |
| 번다운/속도/CFD 차트 데이터 API | FR-022 | dashboard |
| 릴리즈 버전 관리 + 릴리즈 노트 | FR-019, FR-020 | release |
| JQL 검색 엔진 | FR-016 | search (신규) |
| 이메일/Slack 알림 | FR-024 | notification (신규) |
| 자동화 엔진 (Trigger→Condition→Action) | FR-015 | automation (신규) |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 메인 대시보드 | SCR-002 |
| JQL 검색 | SCR-008 |
| 로드맵 | SCR-009 |
| 릴리즈 관리 | SCR-011 |
| 리포트 | SCR-012 |
| Audit Log | SCR-014 |

---

## Phase 7 — QA 및 버그 수정 [대기]

> **기간**: 2026-07-01 ~ 07-21 (3주)
> **마일스톤**: M4. QA 완료
> **산출물**: 테스트 리포트
> **Task**: [TASKS.md - Phase 7](TASKS.md#phase-7--qa-tasks)

### 목표

- 통합 테스트 전체 실행
- 성능 테스트 (API P95 < 200ms, JQL < 500ms)
- 보안 점검 (OWASP Top 10)
- 버그 수정 및 회귀 테스트
- UAT (User Acceptance Test)

---

## Phase 8 — 배포 [대기]

> **기간**: 2026-07-22 ~ 07-31 (1.5주)
> **마일스톤**: M5. 배포
> **산출물**: 운영 시스템, 배포 완료 보고서
> **Task**: [TASKS.md - Phase 8](TASKS.md#phase-8--배포-tasks)

### 목표

- AWS 인프라 프로비저닝 (ECS, RDS, ElastiCache, S3)
- CI/CD 파이프라인 구축 (GitHub Actions)
- 스테이징 환경 검증
- 운영 배포 및 모니터링 설정
- 배포 완료 보고서 작성

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | PRD 기반 Phase 로드맵 초안 작성 |
