# Project Control Hub - Phase 로드맵

> **버전**: v1.1
> **작성일**: 2026-03-22
> **최종수정일**: 2026-04-09 (Phase 1 산출물 완료, Phase 2 대기)
> **기준 문서**: [PRD.md](PRD.md) · `C:\workspace\phs-prj\documents\00-스케줄_v3.1.md` (스프린트·FR 매핑 정본)
> **연결 문서**: [TASKS.md](TASKS.md) | [WORKFLOW.md](WORKFLOW.md)

---

## Phase 전체 요약

| Phase | 구분 | 기간 | 상태 |
|-------|------|------|------|
| Phase 0 | 프로젝트 초기 설정 | 2026-03-22 | **완료** |
| Phase 1 | 기획 | 2026-04-01 ~ 04-14 (2주) | **완료** |
| Phase 2 | 설계 | 2026-04-15 ~ 05-05 (3주) | 대기 |
| Phase 3 | 개발 Sprint 1 — 이슈·워크플로우 (FR-001~007, 013~014) | 2026-05-06 ~ 05-19 (2주) | 대기 |
| Phase 4 | 개발 Sprint 2 — 보드·스프린트·백로그·JQL·모바일 이슈 (FR-008~012, 016, FR-MOBILE-001) | 2026-05-20 ~ 06-02 (2주) | 대기 |
| Phase 5 | 개발 Sprint 3 — 대시보드·산정·권한·Audit·릴리즈·워치·모바일 보드/푸시 | 2026-06-03 ~ 06-16 (2주) | 대기 |
| Phase 6 | 개발 Sprint 4 — 자동화·협업·아카이브·API·외부연동·모바일 오프라인 | 2026-06-17 ~ 06-30 (2주) | 대기 |
| Phase 7 | QA 및 버그 수정 | 2026-07-01 ~ 07-21 (3주) | 대기 |
| Phase 8 | 배포 | 2026-07-22 ~ 07-31 (1.5주) | 대기 |

---

## Phase 0 — 프로젝트 초기 설정 [완료]

> **기간**: 2026-03-22
> **산출물**: 프로젝트 scaffolding, Entity, Repository, DTO, Service, Controller
> **검증**: 2026-04-09 코드·문서 대조 (Enum 9, Entity 20, Repository 20, DTO 17, Service 7, Controller 8, PRD·규칙 14)

### 완료 항목

- [x] Spring Boot 4.0.3 프로젝트 초기화 (Java 21, Gradle)
- [x] 공통 인프라 구성 (SecurityConfig, RedisConfig, SwaggerConfig, GlobalExceptionHandler)
- [x] PRD 문서 작성 (jira-doc 기반 종합 → `documents`와 주기적 정합)
- [x] 컨벤션 정의 (.ai/spring-conventions.json)
- [x] 공유 Enum 9개 생성
- [x] Entity 20개 생성 (Phase A~E)
- [x] Repository 20개 + DTO 17개 + Service 7개 + Controller 8개 생성
- [x] Spring 개발 규칙 문서 14개 작성 (human/ai)

### 생성된 도메인 구조

```
com.pch.mng/
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

## Phase 1 — 기획 [완료]

> **기간**: 2026-04-01 ~ 04-14 (2주)
> **마일스톤**: M1. 기획 완료 · **M1.1. DoR/DoD** — [DOR-DOD.md](DOR-DOD.md)
> **완료일(저장소)**: 2026-04-09
> **Task**: [TASKS.md - Phase 1](TASKS.md#phase-1--기획-tasks)

### 반영된 산출물 (저장소)

| 문서 | 설명 |
|------|------|
| [REQUIREMENTS-v2.md](REQUIREMENTS-v2.md) | FR 상세·페이즈 RTM |
| [NFR-VERIFICATION.md](NFR-VERIFICATION.md) | NFR-001~011 검증 |
| [STORY-MAP.md](STORY-MAP.md) | Epic·사용자 스토리 |
| [WIREFRAME-SPEC.md](WIREFRAME-SPEC.md) | 웹 14·모바일 흐름 사양 (Figma는 별도 링크 예정) |
| [SPRINT-BACKLOG-DRAFT.md](SPRINT-BACKLOG-DRAFT.md) | FR ↔ Phase 백로그 초안 |
| [spikes/SPIKE-JQL-PARSER.md](spikes/SPIKE-JQL-PARSER.md) | JQL 파서 권장안 |
| [spikes/SPIKE-WORKFLOW-ENGINE.md](spikes/SPIKE-WORKFLOW-ENGINE.md) | 워크플로 FSM 설계 |

### 목표

- 요구사항 상세 분석 및 확정 (**FR-001 ~ FR-033**, **FR-MOBILE-001 ~ 004**)
- 비기능 요구사항 검증 (**NFR-001 ~ NFR-011**)
- 화면 와이어프레임 작성 (웹 SCR-001 ~ SCR-014, 모바일 SCR-MOB-* 는 07 정본)
- 사용자 스토리 매핑 및 우선순위 확정

### 주요 산출물

| 산출물 | 설명 | 담당 |
|--------|------|------|
| 요구사항 정의서 v2.3 | FR/NFR·RTM 정본 | PM |
| 화면 와이어프레임 | 14개 웹 화면 + 모바일 흐름 | 디자이너 |
| 사용자 스토리 맵 | Epic → Story 분해 | PM + 전체 |
| Sprint Backlog 초안 | Phase 3~6 ↔ 00-스케줄 스프린트 매핑 | PM |
| DoR/DoD 체크리스트 | M1.1 산출 | PM + 전체 |

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
| ERD v4.0 | BOARD 등 33 테이블 정본 | 백엔드 |
| API 정의서 v4.0 | 엔드포인트·스키마 정본 | 백엔드 |
| 아키텍처 정의서 v4.0 | 웹+모바일(Flutter) 포함 | 백엔드 |
| UI 디자인 시스템 | 컴포넌트 라이브러리, 스타일 가이드 | 디자이너 |

---

## Phase 3 — 개발 Sprint 1: 이슈·워크플로우 [대기]

> **기간**: 2026-05-06 ~ 05-19 (2주)
> **Sprint Goal (`00-스케줄_v3.1`)**: 이슈 CRUD + 워크플로우 기본 — **FR-001~007, FR-013~014**
> **Task**: [TASKS.md - Phase 3](TASKS.md#phase-3--개발-sprint-1-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| JWT·Security (API 보호 선행) | - | auth, global |
| 이슈 CRUD (5 타입) · 이슈 키 | FR-001, FR-002 | issue |
| 워크플로우 6단계 전환 · 규칙 엔진 | FR-013, FR-014 | issue, workflow |
| 이슈 링크 | FR-007 | issue |
| 우선순위·담당·레이블·컴포넌트 | FR-004~006 | issue, label |

### 프론트엔드 / 모바일

| 화면 | 화면 ID / 비고 |
|------|----------------|
| 로그인 (API 호출 전제) | SCR-001 |
| 이슈 상세·생성(최소) | SCR-006, SCR-007 |

---

## Phase 4 — 개발 Sprint 2: 보드·스프린트·JQL·모바일 이슈 [대기]

> **기간**: 2026-05-20 ~ 06-02 (2주)
> **Sprint Goal**: 보드 + 스프린트 관리 + JQL + **모바일 이슈 조회/생성** — **FR-008~012, FR-016, FR-MOBILE-001**
> **Task**: [TASKS.md - Phase 4](TASKS.md#phase-4--개발-sprint-2-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 스크럼/칸반 보드 데이터 · WIP | FR-008, FR-009 | board, project |
| 백로그 · 스프린트 lifecycle | FR-010, FR-011 | sprint, issue |
| 로드맵(Epic 타임라인) API | FR-012 | issue |
| JQL 파서·검색 API | FR-016 | search |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 백로그 | SCR-003 |
| 스크럼·칸반 보드 | SCR-004, SCR-005 |
| 스프린트 관리 | SCR-010 |
| JQL 검색 | SCR-008 |
| 로드맵 | SCR-009 |

### 모바일 (Flutter)

| 기능 | 관련 FR |
|------|---------|
| 이슈 목록·생성·상세 | FR-MOBILE-001 |

---

## Phase 5 — 개발 Sprint 3: 대시보드·산정·권한·Audit·릴리즈·워치·모바일 보드 [대기]

> **기간**: 2026-06-03 ~ 06-16 (2주)
> **Sprint Goal**: **FR-017~022, FR-025, FR-028~031, FR-MOBILE-002~003** (`00-스케줄_v3.1`)
> **Task**: [TASKS.md - Phase 5](TASKS.md#phase-5--개발-sprint-3-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 스토리 포인트 · Planning Poker | FR-017, FR-018 | issue |
| Fix Version · 릴리즈 노트 | FR-019, FR-020 | release |
| 대시보드·가젯 · 차트 데이터 | FR-021, FR-022 | dashboard |
| Audit Log ·보내기 | FR-028, FR-029 | audit |
| RBAC · 이슈 보안 레벨 | FR-030, FR-031 | project, issue |
| 워치 | FR-025 | issue |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 메인 대시보드 · 리포트 | SCR-002, SCR-012 |
| 릴리즈 관리 | SCR-011 |
| Audit Log | SCR-014 |
| 프로젝트 설정(RBAC 등) | SCR-013 |

### 모바일 (Flutter)

| 기능 | 관련 FR |
|------|---------|
| 보드 터치·상태 전환 | FR-MOBILE-002 |
| 푸시 알림(FCM/APNs) | FR-MOBILE-003 |

---

## Phase 6 — 개발 Sprint 4: 자동화·협업·아카이브·연동·모바일 오프라인 [대기]

> **기간**: 2026-06-17 ~ 06-30 (2주)
> **Sprint Goal**: **FR-015, FR-023~024, FR-026~027, FR-032~033, FR-MOBILE-004**
> **Task**: [TASKS.md - Phase 6](TASKS.md#phase-6--개발-sprint-4-tasks)

### 백엔드

| 기능 | 관련 FR | 도메인 |
|------|---------|--------|
| 자동화 엔진 | FR-015 | automation |
| 댓글·@멘션 · 이메일/Slack 알림 | FR-023, FR-024 | comment, notification |
| 아카이브·자동 아카이브 규칙 | FR-026, FR-027 | issue, project |
| REST API 마감 · Git 연동 | FR-032, FR-033 | integration |

### 프론트엔드

| 화면 | 화면 ID |
|------|---------|
| 이슈 상세(댓글·알림 UI 보강) | SCR-006 |
| 설정·아카이브 정책 UI | SCR-013 |

### 모바일 (Flutter)

| 기능 | 관련 FR |
|------|---------|
| 오프라인 캐싱·동기화 | FR-MOBILE-004 |

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
| v1.1 | 2026-04-09 | `00-스케줄_v3.1`과 스프린트·FR 매핑 정합, Phase 3~6 범위 재정의, M1.1·산출물 버전(ERD/API/아키텍처 v4) 반영 |
