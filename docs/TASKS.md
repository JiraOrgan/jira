# Project Control Hub - Task 목록

> **버전**: v1.2
> **작성일**: 2026-03-22
> **최종수정일**: 2026-04-10 (T-404 이슈 링크 CRUD API)
> **연결 문서**: [PHASE.md](PHASE.md) | [WORKFLOW.md](WORKFLOW.md) | [PRD.md](PRD.md)
> **스프린트·FR 정본**: `C:\workspace\phs-prj\documents\00-스케줄_v3.1.md`

---

## Task 상태 정의

| 상태 | 설명 |
|------|------|
| TODO | 아직 시작하지 않음 |
| IN_PROGRESS | 진행 중 |
| REVIEW | 코드 리뷰 중 |
| DONE | 완료 |
| BLOCKED | 선행 작업 대기 |

---

## Phase 0 — 초기 설정 Tasks [완료]

| ID | Task | 담당 | 상태 | 비고 |
|----|------|------|------|------|
| T-000 | Spring Boot 4.0.3 프로젝트 초기화 | Backend | DONE | Java 21, Gradle (`build.gradle` 검증) |
| T-001 | 공통 인프라 구성 | Backend | DONE | Security, Redis, Swagger, Exception |
| T-002 | PRD 문서 작성 | PM | DONE | `docs/PRD.md` |
| T-003 | 공유 Enum 9개 생성 | Backend | DONE | `global/enums/` 9파일 |
| T-004 | Entity 20개 생성 | Backend | DONE | `@Entity` 20 |
| T-005 | Repository 20개 생성 | Backend | DONE | Spring Data JPA |
| T-006 | DTO 17개 생성 | Backend | DONE | Request 7 + 도메인 Response 10 |
| T-007 | Service 7개 생성 | Backend | DONE | 도메인 Service 7 |
| T-008 | Controller 8개 생성 | Backend | DONE | `*ApiController` 8 |
| T-009 | Spring 규칙 문서 작성 | Backend | DONE | `rules/human`·`rules/ai` 각 7개 |

---

## Phase 1 — 기획 Tasks [완료]

| ID | Task | 담당 | 상태 | 산출물 | 선행 |
|----|------|------|------|--------|------|
| T-100 | 요구사항 상세 분석 (FR-001~033) | PM | DONE | [REQUIREMENTS-v2.md](REQUIREMENTS-v2.md) | - |
| T-101 | 비기능 요구사항 검증 (NFR-001~011) | PM | DONE | [NFR-VERIFICATION.md](NFR-VERIFICATION.md) | - |
| T-102 | 사용자 스토리 매핑 | PM + 전체 | DONE | [STORY-MAP.md](STORY-MAP.md) | T-100 |
| T-103 | 화면 와이어프레임 작성 (웹 14 + 모바일 흐름) | 디자이너 | DONE | [WIREFRAME-SPEC.md](WIREFRAME-SPEC.md); Figma 링크 합의 후 추가 | T-100 |
| T-104 | Sprint Backlog 초안 작성 (`00-스케줄` FR 매핑) | PM | DONE | [SPRINT-BACKLOG-DRAFT.md](SPRINT-BACKLOG-DRAFT.md) | T-102 |
| T-107 | DoR/DoD 체크리스트 확정 (M1.1) | PM + 전체 | DONE | [DOR-DOD.md](DOR-DOD.md) | T-104 |
| T-105 | 기술 스파이크: JQL 파서 설계 | Backend | DONE | [spikes/SPIKE-JQL-PARSER.md](spikes/SPIKE-JQL-PARSER.md) | - |
| T-106 | 기술 스파이크: 워크플로우 엔진 설계 | Backend | DONE | [spikes/SPIKE-WORKFLOW-ENGINE.md](spikes/SPIKE-WORKFLOW-ENGINE.md) | - |

---

## Phase 2 — 설계 Tasks [완료]

| ID | Task | 담당 | 상태 | 산출물 | 선행 |
|----|------|------|------|--------|------|
| T-200 | ERD 최종 확정 + DDL 생성 | Backend | DONE | [design/ERD.md](design/ERD.md), [design/DDL-mysql-v1.sql](design/DDL-mysql-v1.sql) | T-100 |
| T-201 | API 상세 스펙 작성 (Swagger) | Backend | DONE | [design/API-SPEC-v4-implementation.md](design/API-SPEC-v4-implementation.md) + `/v3/api-docs` | T-200 |
| T-202 | 시퀀스 다이어그램 작성 (주요 흐름 5개) | Backend | DONE | [design/SEQUENCES.md](design/SEQUENCES.md) | T-201 |
| T-203 | UI 디자인 시스템 구축 | 디자이너 | DONE | [design/DESIGN-SYSTEM.md](design/DESIGN-SYSTEM.md) (토큰·인벤토리 v0.1) | T-103 |
| T-204 | UI 화면 디자인 (SCR-001~014) | 디자이너 | DONE | [design/UI-SCREEN-DESIGN.md](design/UI-SCREEN-DESIGN.md); Figma 링크 TODO | T-203 |
| T-205 | 인프라 설계 (AWS 아키텍처) | Backend | DONE | [design/INFRA-AWS.md](design/INFRA-AWS.md) | - |
| T-206 | CI/CD 파이프라인 설계 | Backend | DONE | [design/CICD.md](design/CICD.md) | T-205 |

---

## Phase 3 — 개발 Sprint 1 Tasks [진행 중]

> **Sprint Goal (`00-스케줄_v3.1`)**: 이슈 CRUD + 워크플로우 기본 — **FR-001~007, FR-013~014** (인증·프로젝트·RBAC는 이슈 API 보호를 위해 동일 스프린트에서 선행)

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-300 | JWT 인증 구현 (로그인/회원가입/토큰 갱신) | Backend | DONE | `AuthController` `/api/auth/*` | T-200 |
| T-301 | CustomUserDetails + Security Filter Chain | Backend | DONE | `CustomUserDetails`, `JwtAuthenticationFilter` | T-300 |
| T-302 | 비밀번호 BCrypt 암호화 적용 | Backend | DONE | `BCryptPasswordEncoder(12)`, 가입 시 encode | T-300 |
| T-303 | 로그인 실패 잠금 (5회/30분) | Backend | DONE | `RedisLoginAttemptService` (`app.security.login.*`) | T-301 |
| T-304 | Refresh Token Redis 저장 | Backend | DONE | `RedisRefreshTokenStore` / 테스트 `InMemoryRefreshTokenStore` | T-300 |
| T-305 | RBAC 권한 검증 (@PreAuthorize) | Backend | DONE | `ProjectSecurityService`, `DashboardSecurityService`, FR-030 | T-301 |
| T-306 | 프로젝트 CRUD API 완성 | Backend | DONE | 멤버십 기반 목록·생성 시 창립자 ADMIN | T-305 |
| T-307 | 프로젝트 멤버 관리 API 완성 | Backend | DONE | ADMIN 전용 추가/삭제·`projectId` 검증 | T-306 |
| T-308 | 단위 테스트 (Auth, User, Project) | Backend | TODO | - | T-307 |
| T-400 | 이슈 CRUD API 완성 (5 타입; 키·계층·스프린트 검증) | Backend | IN_PROGRESS | FR-001, FR-002 | T-306 |
| T-401 | 이슈 키 자동 생성 로직 (PROJ-N) | Backend | DONE | FR-001 | T-400 |
| T-402 | 워크플로우 전환 규칙 엔진 구현 (`IssueWorkflowPolicy`) | Backend | DONE | FR-013, FR-014 | T-400 |
| T-403 | WorkflowTransition 자동 기록 | Backend | DONE | FR-013 | T-402 |
| T-404 | 이슈 링크 CRUD | Backend | DONE | FR-007 | T-400 |
| T-406 | 첨부파일 업로드 (S3 연동) | Backend | TODO | - | T-400 |
| T-408 | 이슈 레이블/컴포넌트 연결 | Backend | TODO | FR-006 | T-400 |
| T-409 | 단위 테스트 (Issue, Workflow) | Backend | TODO | - | T-408 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-310 | React 프로젝트 초기 설정 (Vite + Zustand + Tailwind) | Frontend | TODO | - | - |
| T-311 | 로그인 화면 구현 | Frontend | TODO | SCR-001 | T-310 |
| T-312 | 토큰 관리 (Axios Interceptor) | Frontend | TODO | - | T-311 |
| T-314 | 공통 레이아웃 (GNB, 사이드바) | Frontend | TODO | - | T-310 |
| T-410 | 이슈 생성 폼 (동적 필드) | Frontend | TODO | SCR-007 | T-312 |
| T-411 | 이슈 상세 화면 (첨부, 상태 전환 — 댓글은 Sprint 4) | Frontend | TODO | SCR-006 | T-410 |

---

## Phase 4 — 개발 Sprint 2 Tasks

> **Sprint Goal**: 보드 + 스프린트 + JQL + **모바일 이슈** — **FR-008~012, FR-016, FR-MOBILE-001**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-500 | 스프린트 lifecycle API (생성/시작/완료) | Backend | TODO | FR-011 | T-400 |
| T-501 | 백로그 관리 (순서 변경, 스프린트 배정) | Backend | TODO | FR-010 | T-500 |
| T-502 | WIP 제한 검증 로직 | Backend | TODO | FR-009 | T-500 |
| T-503 | 보드 데이터 조회 API (스윔레인 지원) | Backend | TODO | FR-008 | T-500 |
| T-504 | 보드 캐시 (Redis) | Backend | TODO | - | T-503 |
| T-505 | 단위 테스트 (Sprint, Board) | Backend | TODO | - | T-504 |
| T-602 | JQL 파서 구현 | Backend | TODO | FR-016 | T-400 |
| T-603 | JQL 검색 API (페이징, 필터 저장) | Backend | TODO | FR-016 | T-602 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-510 | 백로그 화면 (드래그앤드롭) | Frontend | TODO | SCR-003 | T-411 |
| T-511 | 스크럼 보드 (드래그앤드롭) | Frontend | TODO | SCR-004 | T-510 |
| T-512 | 칸반 보드 (WIP 표시) | Frontend | TODO | SCR-005 | T-511 |
| T-513 | 스프린트 관리 화면 | Frontend | TODO | SCR-010 | T-510 |
| T-611 | JQL 검색 화면 (자동완성) | Frontend | TODO | SCR-008 | T-512 |
| T-612 | 로드맵 (Epic 타임라인) | Frontend | TODO | SCR-009 | T-512 |

### Mobile (Flutter)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-620 | Flutter 앱 프로젝트 초기화 (Riverpod, Dio) | Mobile | TODO | FR-MOBILE-001 | T-300 |
| T-621 | 모바일 이슈 목록·생성·상세 | Mobile | TODO | FR-MOBILE-001 | T-620 |

---

## Phase 5 — 개발 Sprint 3 Tasks

> **Sprint Goal**: 대시보드·리포트·산정·권한·Audit·릴리즈·워치·모바일 보드/푸시 — **FR-017~022, FR-025, FR-028~031, FR-MOBILE-002~003**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-407 | AuditLog 자동 기록 (AOP/이벤트) | Backend | TODO | FR-028 | T-400 |
| T-600 | 대시보드 + 가젯 CRUD | Backend | TODO | FR-021 | T-500 |
| T-601 | 번다운/속도/CFD 차트 데이터 API | Backend | TODO | FR-022 | T-600 |
| T-604 | 릴리즈 노트 자동 생성 | Backend | TODO | FR-020 | T-408 |
| T-608 | 워치(Watch) 구독 API | Backend | TODO | FR-025 | T-400 |
| T-609 | 이슈 보안 레벨·정책 API 보강 | Backend | TODO | FR-031 | T-305 |
| T-607a | 단위 테스트 (Dashboard, Audit, Release) | Backend | TODO | - | T-601 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-313 | 프로젝트 설정 화면 구현 | Frontend | TODO | SCR-013 | T-312 |
| T-610 | 메인 대시보드 (역할별 가젯) | Frontend | TODO | SCR-002 | T-513 |
| T-613 | 릴리즈 관리 화면 | Frontend | TODO | SCR-011 | T-610 |
| T-614 | 리포트 화면 (차트) | Frontend | TODO | SCR-012 | T-610 |
| T-615 | Audit Log 화면 | Frontend | TODO | SCR-014 | T-610 |
| T-616 | Planning Poker UI (선택) | Frontend | TODO | SCR-006 | T-610 |

### Mobile (Flutter)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-622 | 모바일 보드·터치 기반 상태 전환 | Mobile | TODO | FR-MOBILE-002 | T-621 |
| T-623 | FCM/APNs 푸시 수신 연동 | Mobile | TODO | FR-MOBILE-003 | T-622 |

---

## Phase 6 — 개발 Sprint 4 Tasks

> **Sprint Goal**: 자동화·협업·아카이브·외부연동·모바일 오프라인 — **FR-015, FR-023~024, FR-026~027, FR-032~033, FR-MOBILE-004**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-405 | 댓글 CRUD + @멘션 파싱 | Backend | TODO | FR-023 | T-400 |
| T-605 | 알림 서비스 (이메일/Slack) | Backend | TODO | FR-024 | T-405 |
| T-606 | 자동화 엔진 (Trigger→Condition→Action) | Backend | TODO | FR-015 | T-402 |
| T-617 | 아카이브·자동 아카이브 규칙 API | Backend | TODO | FR-026, FR-027 | T-400 |
| T-625 | GitHub/GitLab 커밋·PR 연동 | Backend | TODO | FR-033 | T-400 |
| T-607 | 단위 테스트 (JQL 통합, Notification, Automation) | Backend | TODO | - | T-606 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-628 | 이슈 상세 댓글·@멘션 UI 보강 | Frontend | TODO | SCR-006 | T-615 |
| T-629 | 아카이브·연동 설정 UI | Frontend | TODO | SCR-013 | T-628 |

### Mobile (Flutter)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-624 | 오프라인 캐싱·재연결 동기화 | Mobile | TODO | FR-MOBILE-004 | T-623 |

---

## Phase 7 — QA Tasks

| ID | Task | 담당 | 상태 | 선행 |
|----|------|------|------|------|
| T-700 | 통합 테스트 시나리오 작성 | QA | TODO | T-607 |
| T-701 | API 통합 테스트 실행 | QA | TODO | T-700 |
| T-702 | UI E2E 테스트 (Playwright) | QA | TODO | T-700 |
| T-703 | 성능 테스트 (API P95 < 200ms) | QA | TODO | T-701 |
| T-704 | JQL 성능 테스트 (< 500ms) | QA | TODO | T-703 |
| T-705 | 보안 점검 (OWASP Top 10) | QA | TODO | T-701 |
| T-706 | 버그 수정 Sprint | Backend + Frontend | TODO | T-705 |
| T-707 | 회귀 테스트 | QA | TODO | T-706 |
| T-708 | UAT (User Acceptance Test) | PM + 전체 | TODO | T-707 |

---

## Phase 8 — 배포 Tasks

| ID | Task | 담당 | 상태 | 선행 |
|----|------|------|------|------|
| T-800 | AWS 인프라 프로비저닝 (Terraform) | Backend | TODO | T-205 |
| T-801 | CI/CD 파이프라인 구축 (GitHub Actions) | Backend | TODO | T-206 |
| T-802 | 스테이징 환경 배포 및 검증 | Backend | TODO | T-801 |
| T-803 | 운영 배포 (Blue-Green) | Backend | TODO | T-802 |
| T-804 | 모니터링 설정 (CloudWatch + Grafana) | Backend | TODO | T-803 |
| T-805 | 배포 완료 보고서 작성 | PM | TODO | T-804 |

---

## Task 통계

| Phase | 총 Task | Backend | Frontend | Mobile | QA | PM/Design |
|-------|---------|---------|----------|--------|-----|----------|
| Phase 0 | 10 | 8 | 0 | 0 | 0 | 2 |
| Phase 1 | 8 | 2 | 0 | 0 | 0 | 6 |
| Phase 2 | 7 | 4 | 0 | 0 | 0 | 3 |
| Phase 3 | 23 | 17 | 6 | 0 | 0 | 0 |
| Phase 4 | 16 | 8 | 6 | 2 | 0 | 0 |
| Phase 5 | 15 | 7 | 6 | 2 | 0 | 0 |
| Phase 6 | 9 | 6 | 2 | 1 | 0 | 0 |
| Phase 7 | 9 | 1 | 0 | 0 | 7 | 1 |
| Phase 8 | 6 | 5 | 0 | 0 | 0 | 1 |
| **합계** | **103** | **58** | **20** | **5** | **7** | **13** |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | PRD 기반 Task 목록 초안 작성 |
| v1.1 | 2026-04-09 | `00-스케줄_v3.1`에 맞춰 Sprint 1~4 Task 재배치, Flutter 태스크(T-620~624)·Git 연동(T-625)·워치(T-608) 등 추가, Phase 1 M1.1(T-107) 반영 |
| v1.2 | 2026-04-10 | T-404 이슈 링크 CRUD API 완료 반영 (DONE) |
