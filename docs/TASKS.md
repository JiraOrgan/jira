# Project Control Hub - Task 목록

> **버전**: v1.46
> **작성일**: 2026-03-22
> **최종수정일**: 2026-04-14 (이슈 상세 아카이브 시 상태 전환 UI 비표시)
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
| T-308 | 단위 테스트 (Auth, User, Project) | Backend | DONE | - | T-307 |
| T-400 | 이슈 CRUD API 완성 (5 타입; 키·계층·스프린트 검증) | Backend | DONE | FR-001, FR-002; `IssueHierarchyPolicy` PRD §3.1 | T-306 |
| T-401 | 이슈 키 자동 생성 로직 (PROJ-N) | Backend | DONE | FR-001 | T-400 |
| T-402 | 워크플로우 전환 규칙 엔진 구현 (`IssueWorkflowPolicy`) | Backend | DONE | FR-013, FR-014 | T-400 |
| T-403 | WorkflowTransition 자동 기록 | Backend | DONE | FR-013 | T-402 |
| T-404 | 이슈 링크 CRUD | Backend | DONE | FR-007 | T-400 |
| T-406 | 첨부파일 업로드 (S3 연동) | Backend | DONE | 로컬 기본·`app.storage.type=s3` | T-400 |
| T-408 | 이슈 레이블/컴포넌트 연결 | Backend | DONE | FR-006 | T-400 |
| T-409 | 단위 테스트 (Issue, Workflow) | Backend | DONE | - | T-408 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-310 | React 프로젝트 초기 설정 (Vite + Zustand + Tailwind) | Frontend | DONE | `apps/web`, 루트 npm workspaces | - |
| T-311 | 로그인 화면 구현 | Frontend | DONE | SCR-001; `LoginPage`, `/login` | T-310 |
| T-312 | 토큰 관리 (Axios Interceptor) | Frontend | DONE | `api`·`bareApi`, localStorage, 401→refresh 큐 | T-311 |
| T-314 | 공통 레이아웃 (GNB, 사이드바) | Frontend | DONE | `AppLayout`, `ProjectsProvider` | T-310 |
| T-410 | 이슈 생성 폼 (동적 필드) | Frontend | DONE | SCR-007; `/project/:key/issues/new`, Bug 안내 | T-312 |
| T-411 | 이슈 상세 화면 (첨부, 상태 전환 — 댓글은 Sprint 4) | Frontend | DONE | SCR-006; `/issue/:issueKey`, 전환·첨부·이력 | T-410 |

---

## Phase 4 — 개발 Sprint 2 Tasks

> **Sprint Goal**: 보드 + 스프린트 + JQL + **모바일 이슈** — **FR-008~012, FR-016, FR-MOBILE-001**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-500 | 스프린트 lifecycle API (생성/시작/완료) | Backend | DONE | FR-011 전이·동시 ACTIVE 1개·삭제 제한 | T-400 |
| T-501 | 백로그 관리 (순서 변경, 스프린트 배정) | Backend | DONE | `backlog_rank`, reorder API, 일괄 배정 | T-500 |
| T-502 | WIP 제한 검증 로직 | Backend | DONE | KANBAN 전환 시 검증·`/wip-limits` API | T-500 |
| T-503 | 보드 데이터 조회 API (스윔레인 지원) | Backend | DONE | `GET /sprints/{id}/board`, `NONE`/`ASSIGNEE` | T-500 |
| T-504 | 보드 캐시 (Redis) | Backend | DONE | `app.board.cache`, 무효화: 이슈·스프린트 변경 | T-503 |
| T-505 | 단위 테스트 (Sprint, Board) | Backend | DONE | `SprintServiceTest`, `BoardServiceTest` 캐시 히트/미스 | T-504 |
| T-506 | Epic 기간 필드·로드맵 조회 API | Backend | DONE | FR-012; `epic_start_date`/`epic_end_date`, `GET .../roadmap/epics` | T-400 |
| T-602 | JQL 파서 구현 | Backend | DONE | `jql/JqlParser`, AST, SPIKE MVP 문법 | T-400 |
| T-603 | JQL 검색 API (페이징, 필터 저장) | Backend | DONE | FR-016; `JqlSearchService`, `saved_jql_filter_tb` | T-602 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-510 | 백로그 화면 (드래그앤드롭) | Frontend | DONE | SCR-003; `/project/:key/backlog`, @dnd-kit | T-411 |
| T-511 | 스크럼 보드 (드래그앤드롭) | Frontend | DONE | SCR-004; `/project/:key/board`, 컬럼→`transition` | T-510 |
| T-512 | 칸반 보드 (WIP 표시) | Frontend | DONE | SCR-005; `/project/:key/kanban`, DnD·`wip-limits` | T-511 |
| T-513 | 스프린트 관리 화면 | Frontend | DONE | SCR-010; `/project/:key/sprints`, 생성·시작·완료·삭제 | T-510 |
| T-611 | JQL 검색 화면 (자동완성) | Frontend | DONE | SCR-008; `/project/:key/jql`, 제안 토큰·저장 필터 | T-512 |
| T-612 | 로드맵 (Epic 타임라인) | Frontend | DONE | SCR-009; `/project/:key/roadmap`, `GET .../roadmap/epics`·effective·줌 | T-512 |

### Mobile (Flutter)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-620 | Flutter 앱 프로젝트 초기화 (Riverpod, Dio) | Mobile | DONE | `apps/mobile`, `flutter create .` 로 플랫폼 생성 | T-300 |
| T-621 | 모바일 이슈 목록·생성·상세 | Mobile | DONE | FR-MOBILE-001; Riverpod·Dio·SharedPreferences, 로그인·프로젝트 선택·이슈 CRUD UI | T-620 |

---

## Phase 5 — 개발 Sprint 3 Tasks

> **Sprint Goal**: 대시보드·리포트·산정·권한·Audit·릴리즈·워치·모바일 보드/푸시 — **FR-017~022, FR-025, FR-028~031, FR-MOBILE-002~003**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-407 | AuditLog 자동 기록 (AOP/이벤트) | Backend | DONE | FR-028; `IssueAuditService`·이슈 생성/수정/전환/라벨·컴포넌트·스프린트 배정·삭제 전 정리 | T-400 |
| T-600 | 대시보드 + 가젯 CRUD | Backend | DONE | FR-021; 타입 검증·가젯 수정·reorder·소속 검증·`DashboardServiceTest` | T-500 |
| T-601 | 번다운/속도/CFD 차트 데이터 API | Backend | DONE | FR-022; `ReportService`·`/projects/{id}/reports/*`·전환 이력 기반 일별 상태·`ReportServiceTest` | T-600 |
| T-604 | 릴리즈 노트 자동 생성 | Backend | DONE | FR-020; `GET .../versions/{id}/release-notes`, Fix 연결 이슈 집계·마크다운·`ReleaseVersionServiceReleaseNotesTest` | T-408 |
| T-608 | 워치(Watch) 구독 API | Backend | DONE | FR-025; `GET/POST/DELETE .../issues/{key}/watchers`, `IssueWatcherService`, `IssueWatcherServiceTest` | T-400 |
| T-609 | 이슈 보안 레벨·정책 API 보강 | Backend | DONE | FR-031 | T-305 |
| T-607a | 단위 테스트 (Dashboard, Audit, Release) | Backend | DONE | - | T-601 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-313 | 프로젝트 설정 화면 구현 | Frontend | DONE | SCR-013 | T-312 |
| T-610 | 메인 대시보드 (역할별 가젯) | Frontend | DONE | SCR-002 | T-513 |
| T-613 | 릴리즈 관리 화면 | Frontend | DONE | SCR-011 | T-610 |
| T-614 | 리포트 화면 (차트) | Frontend | DONE | SCR-012; `/project/:key/reports`, `reportApi`, 번다운·속도·CFD 탭(SVG) | T-601 |
| T-615 | Audit Log 화면 | Frontend | DONE | SCR-014; `/project/:key/audit`, `GET /api/v1/audit-logs/project/{id}` (ADMIN) | T-610 |
| T-616 | Planning Poker UI (선택) | Frontend | DONE | SCR-006; `IssueDetailPage` 피보나치 카드·localStorage·`storyPoints` 반영, `planningPokerStorage` | T-610 |

### Mobile (Flutter)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-622 | 모바일 보드·터치 기반 상태 전환 | Mobile | DONE | FR-MOBILE-002; `SprintBoardScreen`, `SprintRepository`, `workflow_edges`, 이슈 목록 앱바 보드 진입 | T-621 |
| T-623 | FCM/APNs 푸시 수신 연동 | Mobile | TODO | FR-MOBILE-003 | T-622 |

---

## Phase 6 — 개발 Sprint 4 Tasks

> **Sprint Goal**: 자동화·협업·아카이브·외부연동·모바일 오프라인 — **FR-015, FR-023~024, FR-026~027, FR-032~033, FR-MOBILE-004**

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-405 | 댓글 CRUD + @멘션 파싱 | Backend | DONE | FR-023; `CommentMention`·`CommentMentionResolver`(프로젝트 멤버 매칭)·`DetailDTO.mentionedUsers`, 웹 멘션 요약 표시 | T-400 |
| T-605 | 알림 서비스 (이메일/Slack) | Backend | DONE | FR-024; `NotificationService`·Slack Webhook·`JavaMailSender`(선택)·댓글 멘션 `AFTER_COMMIT` 비동기 | T-405 |
| T-606 | 자동화 엔진 (Trigger→Condition→Action) | Backend | TODO | FR-015 | T-402 |
| T-617 | 아카이브·자동 아카이브 규칙 API | Backend | DONE | FR-026, FR-027; `Issue.archived`·집계/JQL 제외·`POST .../auto-archive-done`·`Project.autoArchiveDoneAfterDays` | T-400 |
| T-625 | GitHub/GitLab 커밋·PR 연동 | Backend | TODO | FR-033 | T-400 |
| T-607 | 단위 테스트 (JQL 통합, Notification, Automation) | Backend | TODO | - | T-606 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-628 | 이슈 상세 댓글·@멘션 UI 보강 | Frontend | DONE | SCR-006; `commentApi`, `IssueDetailPage` 댓글 CRUD·프로젝트 멤버 멘션 삽입·`CommentBody` @강조 (알림·저장 파싱은 T-405) | T-615 |
| T-629 | 아카이브·연동 설정 UI | Frontend | DONE | SCR-013; 아카이브 토글·`autoArchiveDoneAfterDays`·즉시 실행 버튼·`GET /projects/by-key/{key}`·외부 연동(T-625) 안내; 이슈 상세 아카이브 표시·해제(T-617) | T-628 |

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
| v1.2 | 2026-04-10 | T-408 이슈 레이블·컴포넌트 연결 API 완료 반영 (DONE) |
| v1.3 | 2026-04-10 | T-404 이슈 링크 CRUD API 완료 반영 (DONE), develop 병합 충돌 정리 |
| v1.4 | 2026-04-10 | T-409 `IssueWorkflowPolicy`·`IssueResponse` 매핑 단위 테스트 추가 (DONE) |
| v1.5 | 2026-04-10 | T-308 JWT·AuthService·UserAccountService·ProjectService Mockito 단위 테스트 (DONE) |
| v1.11 | 2026-04-10 | T-504 스프린트 보드 JSON 캐시(Redis)·이슈/스프린트 변경 시 무효화·`SprintBoardRedisCacheTest` (DONE) |
| v1.12 | 2026-04-10 | T-505 `SprintServiceTest`(Mockito)·`BoardServiceTest` 보강 (DONE) |
| v1.13 | 2026-04-10 | T-602 PCH JQL MVP 파서(`JqlParser`·sealed AST·`JqlParserTest`) FR-016 (DONE) |
| v1.14 | 2026-04-10 | T-400 PRD §3.1 이슈 타입 계층 검증(`IssueHierarchyPolicy`)·통합 테스트·FR-002 (DONE) |
| v1.15 | 2026-04-10 | T-603 JQL 검색 API(`POST .../jql/search`)·저장 필터 CRUD·QueryDSL·`DDL` `saved_jql_filter_tb` (DONE) |
| v1.16 | 2026-04-13 | 모노레포: `apps/web`(Vite·React·Zustand·Tailwind·axios·API 프록시), `apps/mobile`(pubspec·Riverpod·Dio·`api_client`), 루트 `package.json` workspaces — T-310·T-620 DONE |
| v1.17 | 2026-04-13 | 웹: `react-router-dom`, 로그인(SCR-001)·보호 라우트, Zustand+localStorage 토큰, `api` 인터셉터(Authorization·401 refresh 큐·실패 시 `/login`) — T-311·T-312 DONE |
| v1.18 | 2026-04-13 | 웹: 공통 레이아웃·프로젝트 컨텍스트, 이슈 생성/상세(워크플로 전이·첨부·전환 이력), API 클라이언트(`issueApi` 등) — T-314·T-410·T-411 DONE |
| v1.19 | 2026-04-13 | 웹: 백로그 화면(`fetchBacklog`·`reorderBacklog`·`assignSprintToIssues`), @dnd-kit 정렬+스프린트 드롭 — T-510 DONE |
| v1.20 | 2026-04-13 | 웹: 스크럼 보드(`fetchSprintBoard`, 스윔레인 NONE/ASSIGNEE, 컬럼 드롭→`transitionIssue`) — T-511 DONE |
| v1.21 | 2026-04-13 | 웹: 칸반·스프린트(`KanbanPage`·`SprintsPage`·WIP·`/kanban`·`/sprints`) — T-512·T-513 DONE |
| v1.22 | 2026-04-13 | 웹: JQL 검색(`JqlSearchPage`·`jqlApi`·토큰 제안·저장 필터) — T-611 DONE |
| v1.23 | 2026-04-13 | 웹: Epic 로드맵(`RoadmapPage`·`/roadmap`·JQL+상세 타임라인·줌) — T-612 DONE |
| v1.24 | 2026-04-13 | Epic 기간 컬럼·`GET .../roadmap/epics`·이슈 DTO·`RoadmapEpicIntegrationTest`; 웹 로드맵·이슈 폼 연동 — T-506·T-612 보강 |
| v1.26 | 2026-04-13 | 모바일: 로그인·프로젝트 선택·이슈 목록/상세/생성(Riverpod·Dio·SharedPreferences, `API_BASE_URL`) — T-621 FR-MOBILE-001 DONE |
| v1.27 | 2026-04-13 | 감사 로그: `GET /api/v1/audit-logs/project/{projectId}`(ADMIN)·`AuditLogResponse.issueKey`·웹 `/project/:key/audit` — T-615 SCR-014 DONE |
| v1.28 | 2026-04-13 | 이슈 도메인 감사 자동 기록(`IssueAuditService`)·삭제 시 `audit_log_tb` 선삭제·통합 테스트 — T-407 FR-028 DONE |
| v1.29 | 2026-04-13 | T-600 대시보드 가젯: `DashboardGadgetType`·PUT 수정·PUT reorder·삭제 시 대시보드 소속 검증·상세 응답 가젯 position 정렬·API-SPEC 반영 |
| v1.30 | 2026-04-13 | T-601 리포트 API: 번다운·velocity·CFD, `WorkflowTransitionRepository`·`IssueRepository` 집계 쿼리·TASKS/API-SPEC·T-614 선행 T-601 |
| v1.31 | 2026-04-13 | 웹: 리포트 페이지(`ReportsPage`·`/reports`)·`reportApi`·사이드바/개요 링크 — T-614 SCR-012 DONE |
| v1.32 | 2026-04-13 | T-604 `GET /api/v1/versions/{id}/release-notes`·웹 릴리즈 화면 미리보기·API-SPEC — FR-020 DONE |
| v1.33 | 2026-04-13 | T-608 이슈 워치 API(`watchers/me`)·`IssueWatcherService`·API-SPEC — FR-025 DONE |
| v1.34 | 2026-04-14 | T-609 FR-031: `IssueSecurityPolicy`·가시성(목록·보드·JQL·리포트)·Reporter Confidential 설정 제한·`ISSUE_SECURITY_LEVEL_FORBIDDEN` |
| v1.35 | 2026-04-14 | T-607a: `IssueAuditServiceTest`·`ReleaseVersionServiceTest`·`DashboardServiceTest` 보강(save/update/delete·소유자 조회·reorder 빈 케이스 등) |
| v1.36 | 2026-04-14 | T-622 Flutter: 스프린트 보드 API·가로 컬럼·탭 시 전환 바텀시트·`IssueRepository.transition`·롱프레스 상세 — FR-MOBILE-002 |
| v1.37 | 2026-04-14 | T-616: 웹 이슈 상세 Planning Poker UI (`planningPokerStorage`, 피보나치 선택·로컬 저장·SP 반영) — FR-018 보조 |
| v1.38 | 2026-04-14 | T-628: 웹 이슈 상세 댓글 목록·등록·편집·삭제, 멘션 토큰 삽입·표시 — FR-023 UI (백엔드 멘션 파싱 T-405) |
| v1.39 | 2026-04-14 | T-629: 프로젝트 `UpdateDTO.archived`·`GET /api/v1/projects/by-key/{key}`, 웹 설정 아카이브·연동 안내 — FR-026 UI (자동 규칙 T-617) |
| v1.40 | 2026-04-14 | T-405: 댓글 `@토큰` 파싱·`comment_mention_tb`·API `mentionedUsers` — FR-023 (알림 T-605) |
| v1.41 | 2026-04-14 | T-605: 댓글 멘션 알림(Slack Webhook·선택 이메일)·`app.notification` — FR-024 |
| v1.42 | 2026-04-14 | T-617: 이슈 `archived`·프로젝트 자동 아카이브 일수·일괄 실행 API; 웹 타입·설정·이슈 상세; API-SPEC·DDL·H2 시드 반영 — FR-026~027 |
| v1.43 | 2026-04-14 | 웹: 백로그·칸반·스프린트 보드 카드에 아카이브 뱃지·드래그 비활성 — T-617 UX 보강 |
| v1.44 | 2026-04-14 | H2 dev 시드: `admin@local.test`(DEMO ADMIN)·`DevSeedAdminLoginTest` 검증 |
| v1.45 | 2026-04-14 | 웹 JQL 검색 결과 테이블에 아카이브 이슈 뱃지·행 스타일 — T-617 UX |
| v1.46 | 2026-04-14 | 웹 이슈 상세: 아카이브 시 상태 전환 폼 숨김·안내 문구 — T-617 UX |
