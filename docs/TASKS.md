# Project Control Hub - Task 목록

> **버전**: v1.0
> **작성일**: 2026-03-22
> **연결 문서**: [PHASE.md](PHASE.md) | [WORKFLOW.md](WORKFLOW.md) | [PRD.md](PRD.md)

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

## Phase 0 — 초기 설정 Tasks [DONE]

| ID | Task | 담당 | 상태 | 비고 |
|----|------|------|------|------|
| T-000 | Spring Boot 4.0.3 프로젝트 초기화 | Backend | DONE | Java 21, Gradle |
| T-001 | 공통 인프라 구성 | Backend | DONE | Security, Redis, Swagger, Exception |
| T-002 | PRD 문서 작성 | PM | DONE | jira-doc 기반 |
| T-003 | 공유 Enum 9개 생성 | Backend | DONE | global/enums/ |
| T-004 | Entity 20개 생성 | Backend | DONE | Phase A~E |
| T-005 | Repository 20개 생성 | Backend | DONE | Spring Data JPA |
| T-006 | DTO 17개 생성 | Backend | DONE | Request/Response |
| T-007 | Service 7개 생성 | Backend | DONE | 비즈니스 로직 |
| T-008 | Controller 8개 생성 | Backend | DONE | REST API |
| T-009 | Spring 규칙 문서 작성 | Backend | DONE | human/ai 14개 |

---

## Phase 1 — 기획 Tasks

| ID | Task | 담당 | 상태 | 산출물 | 선행 |
|----|------|------|------|--------|------|
| T-100 | 요구사항 상세 분석 (FR-001~033) | PM | TODO | 요구사항 정의서 v2.0 | - |
| T-101 | 비기능 요구사항 검증 (NFR-001~009) | PM | TODO | NFR 검증 보고서 | - |
| T-102 | 사용자 스토리 매핑 | PM + 전체 | TODO | 스토리 맵 | T-100 |
| T-103 | 화면 와이어프레임 작성 (14 화면) | 디자이너 | TODO | Figma 와이어프레임 | T-100 |
| T-104 | Sprint Backlog 초안 작성 | PM | TODO | Sprint 계획서 | T-102 |
| T-105 | 기술 스파이크: JQL 파서 설계 | Backend | TODO | 기술 조사 보고서 | - |
| T-106 | 기술 스파이크: 워크플로우 엔진 설계 | Backend | TODO | 기술 조사 보고서 | - |

---

## Phase 2 — 설계 Tasks

| ID | Task | 담당 | 상태 | 산출물 | 선행 |
|----|------|------|------|--------|------|
| T-200 | ERD 최종 확정 + DDL 생성 | Backend | TODO | ERD v3.0, DDL 스크립트 | T-100 |
| T-201 | API 상세 스펙 작성 (Swagger) | Backend | TODO | API 정의서 v3.0 | T-200 |
| T-202 | 시퀀스 다이어그램 작성 (주요 흐름 5개) | Backend | TODO | 시퀀스 다이어그램 | T-201 |
| T-203 | UI 디자인 시스템 구축 | 디자이너 | TODO | 컴포넌트 라이브러리 | T-103 |
| T-204 | UI 화면 디자인 (SCR-001~014) | 디자이너 | TODO | Figma 시안 | T-203 |
| T-205 | 인프라 설계 (AWS 아키텍처) | Backend | TODO | 인프라 설계서 | - |
| T-206 | CI/CD 파이프라인 설계 | Backend | TODO | CI/CD 설계서 | T-205 |

---

## Phase 3 — 개발 Sprint 1 Tasks

> **Sprint Goal**: 사용자 인증 및 프로젝트 관리 기본 기능

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-300 | JWT 인증 구현 (로그인/회원가입/토큰 갱신) | Backend | TODO | - | T-200 |
| T-301 | CustomUserDetails + Security Filter Chain | Backend | TODO | - | T-300 |
| T-302 | 비밀번호 BCrypt 암호화 적용 | Backend | TODO | NFR-005 | T-300 |
| T-303 | 로그인 실패 잠금 (5회/30분) | Backend | TODO | NFR-007 | T-301 |
| T-304 | Refresh Token Redis 저장 | Backend | TODO | - | T-300 |
| T-305 | RBAC 권한 검증 (@PreAuthorize) | Backend | TODO | FR-030 | T-301 |
| T-306 | 프로젝트 CRUD API 완성 | Backend | TODO | - | T-305 |
| T-307 | 프로젝트 멤버 관리 API 완성 | Backend | TODO | FR-030 | T-306 |
| T-308 | 단위 테스트 (Auth, User, Project) | Backend | TODO | - | T-307 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-310 | React 프로젝트 초기 설정 (Vite + Zustand + Tailwind) | Frontend | TODO | - | - |
| T-311 | 로그인 화면 구현 | Frontend | TODO | SCR-001 | T-310 |
| T-312 | 토큰 관리 (Axios Interceptor) | Frontend | TODO | - | T-311 |
| T-313 | 프로젝트 설정 화면 구현 | Frontend | TODO | SCR-013 | T-312 |
| T-314 | 공통 레이아웃 (GNB, 사이드바) | Frontend | TODO | - | T-310 |

---

## Phase 4 — 개발 Sprint 2 Tasks

> **Sprint Goal**: 이슈 CRUD 및 워크플로우 전환

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-400 | 이슈 CRUD API 완성 (5 타입) | Backend | TODO | FR-001, FR-002 | T-306 |
| T-401 | 이슈 키 자동 생성 로직 (PROJ-N) | Backend | TODO | FR-001 | T-400 |
| T-402 | 워크플로우 전환 규칙 엔진 구현 | Backend | TODO | FR-013, FR-014 | T-400 |
| T-403 | WorkflowTransition 자동 기록 | Backend | TODO | FR-013 | T-402 |
| T-404 | 이슈 링크 CRUD | Backend | TODO | FR-007 | T-400 |
| T-405 | 댓글 CRUD + @멘션 파싱 | Backend | TODO | FR-023 | T-400 |
| T-406 | 첨부파일 업로드 (S3 연동) | Backend | TODO | - | T-400 |
| T-407 | AuditLog 자동 기록 (AOP/이벤트) | Backend | TODO | FR-028 | T-400 |
| T-408 | 이슈 레이블/컴포넌트/버전 관리 | Backend | TODO | FR-006, FR-019 | T-400 |
| T-409 | 단위 테스트 (Issue, Workflow, Comment) | Backend | TODO | - | T-408 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-410 | 이슈 생성 폼 (동적 필드) | Frontend | TODO | SCR-007 | T-314 |
| T-411 | 이슈 상세 화면 (댓글, 첨부, 전환) | Frontend | TODO | SCR-006 | T-410 |

---

## Phase 5 — 개발 Sprint 3 Tasks

> **Sprint Goal**: 보드, 스프린트, 백로그 관리

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-500 | 스프린트 lifecycle API (생성/시작/완료) | Backend | TODO | FR-011 | T-400 |
| T-501 | 백로그 관리 (순서 변경, 스프린트 배정) | Backend | TODO | FR-010 | T-500 |
| T-502 | WIP 제한 검증 로직 | Backend | TODO | FR-009 | T-500 |
| T-503 | 보드 데이터 조회 API (스윔레인 지원) | Backend | TODO | FR-008 | T-500 |
| T-504 | 보드 캐시 (Redis) | Backend | TODO | - | T-503 |
| T-505 | 단위 테스트 (Sprint, Board) | Backend | TODO | - | T-504 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-510 | 백로그 화면 (드래그앤드롭) | Frontend | TODO | SCR-003 | T-411 |
| T-511 | 스크럼 보드 (칸반 스타일, 드래그앤드롭) | Frontend | TODO | SCR-004 | T-510 |
| T-512 | 칸반 보드 (WIP 표시) | Frontend | TODO | SCR-005 | T-511 |
| T-513 | 스프린트 관리 화면 | Frontend | TODO | SCR-010 | T-510 |

---

## Phase 6 — 개발 Sprint 4 Tasks

> **Sprint Goal**: 대시보드, 릴리즈, JQL 검색

### Backend

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-600 | 대시보드 + 가젯 CRUD | Backend | TODO | FR-021 | T-500 |
| T-601 | 번다운/속도/CFD 차트 데이터 API | Backend | TODO | FR-022 | T-600 |
| T-602 | JQL 파서 구현 | Backend | TODO | FR-016 | T-400 |
| T-603 | JQL 검색 API (페이징, 필터 저장) | Backend | TODO | FR-016 | T-602 |
| T-604 | 릴리즈 노트 자동 생성 | Backend | TODO | FR-020 | T-408 |
| T-605 | 알림 서비스 (이메일/Slack) | Backend | TODO | FR-024 | T-400 |
| T-606 | 자동화 엔진 (Trigger→Condition→Action) | Backend | TODO | FR-015 | T-402 |
| T-607 | 단위 테스트 (Dashboard, JQL, Notification) | Backend | TODO | - | T-606 |

### Frontend

| ID | Task | 담당 | 상태 | 화면 ID | 선행 |
|----|------|------|------|---------|------|
| T-610 | 메인 대시보드 (역할별 가젯) | Frontend | TODO | SCR-002 | T-513 |
| T-611 | JQL 검색 화면 (자동완성) | Frontend | TODO | SCR-008 | T-610 |
| T-612 | 로드맵 (Epic 타임라인) | Frontend | TODO | SCR-009 | T-610 |
| T-613 | 릴리즈 관리 화면 | Frontend | TODO | SCR-011 | T-610 |
| T-614 | 리포트 화면 (차트) | Frontend | TODO | SCR-012 | T-610 |
| T-615 | Audit Log 화면 | Frontend | TODO | SCR-014 | T-610 |

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

| Phase | 총 Task | Backend | Frontend | QA | PM/Design |
|-------|---------|---------|----------|-----|----------|
| Phase 0 | 10 | 8 | 0 | 0 | 2 |
| Phase 1 | 7 | 2 | 0 | 0 | 5 |
| Phase 2 | 7 | 4 | 0 | 0 | 3 |
| Phase 3 | 14 | 9 | 5 | 0 | 0 |
| Phase 4 | 12 | 10 | 2 | 0 | 0 |
| Phase 5 | 10 | 6 | 4 | 0 | 0 |
| Phase 6 | 14 | 8 | 6 | 0 | 0 |
| Phase 7 | 9 | 1 | 0 | 7 | 1 |
| Phase 8 | 6 | 5 | 0 | 0 | 1 |
| **합계** | **89** | **53** | **17** | **7** | **12** |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | PRD 기반 Task 목록 초안 작성 |
