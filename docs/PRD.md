# Jira 프로젝트 관리 시스템 - PRD (Product Requirements Document)

> **버전**: v1.0
> **작성일**: 2026-03-22
> **원본 문서**: [jira-doc](https://github.com/JiraOrgan/documents) 기반 종합
> **상태**: 초안

---

## 1. 프로젝트 개요

### 1.1 배경 및 목적

소프트웨어 개발팀의 애자일(Agile) 방법론을 지원하는 이슈 트래킹 및 프로젝트 관리 시스템을 구축한다. 단순한 이슈 트래커를 넘어 팀 워크플로우 설계, 릴리즈 관리, 자동화, 보고까지 통합하는 협업 플랫폼을 목표로 한다.

### 1.2 프로젝트 범위

| 구분 | 내용 |
|------|------|
| **In-Scope** | 이슈(Issue) CRUD, 워크플로우 엔진, 스크럼/칸반 보드, 백로그, 스프린트 관리, 대시보드, JQL 검색, REST API, RBAC 권한 관리, Audit Log, 릴리즈/버전 관리 |
| **Out-of-Scope** | Confluence 연동, Marketplace 앱 개발, 온프레미스 Data Center 배포 |

### 1.3 성공 기준

| 목표 | 측정 지표 | 목표치 |
|------|-----------|--------|
| 이슈 관리 효율화 | 이슈 처리 평균 시간 | 기존 대비 30% 단축 |
| 워크플로우 표준화 | 표준 워크플로우 적용률 | 100% |
| 스프린트 가시성 | 번다운 차트 정확도 | 95% 이상 |
| API 안정성 | API 응답 성공률 | 99.9% |

---

## 2. 기술 스택

### 2.1 Frontend (Web)

| 항목 | 기술 | 버전 |
|------|------|------|
| Framework | React | 18.x |
| 상태 관리 | Zustand | 최신 |
| 스타일링 | Tailwind CSS | 최신 |
| 빌드 도구 | Vite | 최신 |
| 차트 | Recharts | 최신 |

### 2.2 Frontend (Mobile)

| 항목 | 기술 | 버전 |
|------|------|------|
| Framework | Flutter | 3.41.x |
| 언어 | Dart | 3.11.x |
| 상태 관리 | Riverpod | 최신 |
| HTTP | Dio | 최신 |

### 2.3 Backend

| 항목 | 기술 | 버전 |
|------|------|------|
| Framework | Spring Boot | 3.x (4.0.3 적용) |
| ORM | JPA/Hibernate | - |
| 보안 | Spring Security + JWT | - |
| 빌드 | Gradle | - |

### 2.4 Infrastructure

| 항목 | 기술 | 비고 |
|------|------|------|
| Database | PostgreSQL | 16.x, Multi-AZ |
| Cache | Redis (ElastiCache) | 7.x, Cluster Mode |
| Storage | AWS S3 | 첨부파일, 정적 리소스 |
| Message Queue | AWS SQS | 알림/자동화 큐 + DLQ |
| Container | ECS Fargate | Auto Scaling |
| CI/CD | GitHub Actions | - |
| CDN | CloudFront | 정적 리소스 |
| Monitoring | CloudWatch + Grafana | APM 대시보드 |

---

## 3. 핵심 기능 요구사항

### 3.1 이슈 관리

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-001 | 이슈 CRUD (Epic/Story/Task/Bug/Sub-task) | 필수 |
| FR-002 | 이슈 타입별 생성 및 계층 구조 (Epic → Story/Task/Bug → Sub-task) | 필수 |
| FR-003 | 이슈 상태 관리 (6단계 워크플로우) | 필수 |
| FR-004 | 우선순위 설정 (Highest/High/Medium/Low/Lowest) | 필수 |
| FR-005 | 담당자 지정 | 필수 |
| FR-006 | 레이블 & 컴포넌트 분류 | 선택 |
| FR-007 | 이슈 간 링크 (Blocks/Duplicates/Relates to) | 필수 |

**이슈 타입 계층**:
```
Epic (Level 1) ─── 2주 이상 소요되는 큰 기능 단위
  ├── Story (Level 2) ─── 사용자 기능 단위, SP ≤ 8 권장
  ├── Task (Level 2) ──── 기술적 작업 단위
  ├── Bug (Level 2) ───── 결함 처리 (재현절차/기대결과/실제결과/환경 필수)
  └── Sub-task (Level 3) ─ 세부 병렬 작업
```

### 3.2 프로젝트 관리

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-008 | 스크럼 보드 (스프린트 단위 작업 관리, 번다운 차트) | 필수 |
| FR-009 | 칸반 보드 + WIP 제한 (컬럼별 최대 이슈 수 설정) | 필수 |
| FR-010 | 백로그 관리 (우선순위 기반 드래그앤드롭 정렬) | 필수 |
| FR-011 | 스프린트 생성/시작/완료 | 필수 |
| FR-012 | 로드맵 (Epic 타임라인) | 선택 |

### 3.3 워크플로우

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-013 | 표준 워크플로우 6단계 | 필수 |
| FR-014 | 전환 규칙 (조건부 상태 전환) | 필수 |
| FR-015 | 자동화 (Trigger → Condition → Action) | 선택 |

**표준 워크플로우 6단계**:
```
Backlog → Selected for Sprint → In Progress → Code Review → QA → Done
```

**전환 규칙**:
| 전환 경로 | 조건 |
|-----------|------|
| In Progress → Code Review | PR 생성 완료 |
| Code Review → In Progress | 리뷰어 변경 요청 |
| Code Review → QA | 리뷰어 승인 완료 |
| QA → In Progress | 테스트 실패 |
| QA → Done | DoD 체크리스트 전체 충족 |

### 3.4 검색 & 산정

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-016 | JQL 검색 엔진 (자동완성, 필터 저장) | 필수 |
| FR-017 | 스토리 포인트 (피보나치: 1,2,3,5,8,13) | 필수 |
| FR-018 | Planning Poker 지원 | 선택 |

### 3.5 릴리즈 관리

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-019 | Fix Version 관리 (Semantic Versioning: MAJOR.MINOR.PATCH) | 필수 |
| FR-020 | 릴리즈 노트 자동 생성 | 선택 |

### 3.6 대시보드 & 리포트

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-021 | 역할별 가젯 구성 (개발자/PM/QA별 맞춤) | 필수 |
| FR-022 | 번다운/속도/CFD 차트 | 필수 |

**역할별 대시보드 가젯**:
| 대상 | 가젯 |
|------|------|
| Developer | 내 담당 이슈, Sprint Burndown |
| PM/PO | Velocity Chart, Sprint Report, Epic 로드맵, 이슈 타입별 Pie Chart |
| QA | QA 대기 목록, Created vs Resolved |
| 공통 | 누적 흐름 다이어그램 (CFD) |

### 3.7 협업

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-023 | 댓글 & @멘션 | 필수 |
| FR-024 | 이메일/Slack 알림 | 선택 |
| FR-025 | 워치(Watch) 기능 | 선택 |

### 3.8 아카이브 & 감사

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-026 | 이슈/프로젝트 아카이브 | 선택 |
| FR-027 | 자동 아카이브 규칙 (6개월 미수정) | 선택 |
| FR-028 | Audit Log (필드 변경 이력, 변경자, 시각) | 필수 |
| FR-029 | Audit Log CSV/JSON 내보내기 | 선택 |

### 3.9 권한 관리 (RBAC)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-030 | RBAC 5단계 역할 | 필수 |
| FR-031 | 이슈 보안 레벨 (Public/Internal/Confidential) | 선택 |

**권한 매트릭스**:
| 권한 | Admin | Developer | QA | Reporter | Viewer |
|------|:-----:|:---------:|:--:|:--------:|:------:|
| 이슈 생성 | O | O | O | O | X |
| 이슈 수정 | O | O | O | 본인만 | X |
| 이슈 삭제 | O | O | X | X | X |
| 상태 전환 | O | O | O | X | X |
| Sprint 관리 | O | O | X | X | X |
| 프로젝트 설정 | O | X | X | X | X |
| Audit Log 조회 | O | X | X | X | X |

### 3.10 API & 연동

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-032 | REST API (Issue CRUD, Search, Transition) | 필수 |
| FR-033 | GitHub/GitLab 커밋/PR 연결 | 선택 |

---

## 4. 비기능 요구사항

| ID | 분류 | 요구사항 | 목표치 | 우선순위 |
|----|------|----------|--------|----------|
| NFR-001 | 성능 | API 응답 시간 | P95 < 200ms | 필수 |
| NFR-002 | 성능 | JQL 검색 응답 | < 500ms | 필수 |
| NFR-003 | 가용성 | 시스템 가동률 | 99.9% | 필수 |
| NFR-004 | 확장성 | 동시 접속자 | 500명 | 필수 |
| NFR-005 | 보안 | 비밀번호 암호화 | bcrypt | 필수 |
| NFR-006 | 보안 | 통신 암호화 | HTTPS TLS 1.3 | 필수 |
| NFR-007 | 보안 | 로그인 실패 잠금 | 5회 실패 시 30분 잠금 | 필수 |
| NFR-008 | 감사 | 변경 이력 보존 | 전체 필드 변경 추적 | 필수 |
| NFR-009 | 아카이브 | 자동 아카이브 | 6개월 미수정 이슈 | 선택 |

---

## 5. 데이터 모델 (주요 엔티티)

| 엔티티 | 설명 | 주요 필드 |
|--------|------|-----------|
| `PROJECT` | 프로젝트 | key(UK), name, board_type(SCRUM/KANBAN), lead_id, archived |
| `USER_ACCOUNT` | 사용자 | email(UK), password(해시), name |
| `PROJECT_MEMBER` | 프로젝트 멤버십 | project_id, user_id, role(ADMIN/DEVELOPER/QA/REPORTER/VIEWER) |
| `ISSUE` | 이슈 | issue_key(UK), project_id, issue_type, summary, status, priority, story_points, assignee_id, reporter_id, parent_id, sprint_id, security_level |
| `SPRINT` | 스프린트 | project_id, name, status(PLANNING/ACTIVE/COMPLETED), start_date, end_date, goal_points |
| `RELEASE_VERSION` | 릴리즈 버전 | project_id, name(v1.0.0), release_date, status(UNRELEASED/RELEASED) |
| `WORKFLOW_TRANSITION` | 워크플로우 전환 | issue_id, from_status, to_status, changed_by, condition_note |
| `COMMENT` | 댓글 | issue_id, author_id, body |
| `ISSUE_LINK` | 이슈 관계 | source_issue_id, target_issue_id, link_type(BLOCKS/DUPLICATES/RELATES_TO) |
| `AUDIT_LOG` | 감사 로그 | issue_id, changed_by, field_name, old_value, new_value |
| `LABEL` | 레이블 | name(UK) |
| `COMPONENT` | 컴포넌트 | project_id, name, lead_id |
| `ATTACHMENT` | 첨부파일 | issue_id, file_name, file_path, file_size, mime_type |
| `WIP_LIMIT` | WIP 제한 | project_id, status, max_issues |
| `DASHBOARD` | 대시보드 | owner_id, name, is_shared |
| `DASHBOARD_GADGET` | 대시보드 가젯 | dashboard_id, gadget_type, position, config_json |

**N:M 중간 테이블**: `ISSUE_LABEL`, `ISSUE_COMPONENT`, `ISSUE_FIX_VERSION`, `ISSUE_WATCHER`

---

## 6. API 설계 요약

### 6.1 기본 정보

| 항목 | 내용 |
|------|------|
| Base URL | `/rest/api/3` |
| Agile URL | `/rest/agile/1.0` |
| 인증 | Bearer Token (JWT) - Access(1h) + Refresh(7d) |
| Content-Type | application/json (UTF-8) |

### 6.2 주요 엔드포인트

| 도메인 | Method | Endpoint | 설명 |
|--------|--------|----------|------|
| **인증** | POST | `/auth/login` | 로그인 (JWT 발급) |
| **인증** | POST | `/auth/refresh` | 토큰 갱신 |
| **이슈** | POST | `/rest/api/3/issue` | 이슈 생성 |
| **이슈** | GET | `/rest/api/3/issue/{issueKey}` | 이슈 조회 |
| **이슈** | PUT | `/rest/api/3/issue/{issueKey}` | 이슈 수정 |
| **이슈** | DELETE | `/rest/api/3/issue/{issueKey}` | 이슈 삭제 |
| **검색** | POST | `/rest/api/3/search` | JQL 검색 |
| **전환** | POST | `/rest/api/3/issue/{issueKey}/transitions` | 상태 전환 |
| **스프린트** | POST | `/rest/agile/1.0/sprint` | 스프린트 생성 |
| **버전** | POST | `/rest/api/3/version` | 릴리즈 버전 생성 |
| **댓글** | POST | `/rest/api/3/issue/{issueKey}/comments` | 댓글 추가 |
| **첨부** | POST | `/rest/api/3/issue/{issueKey}/attachments` | 첨부파일 업로드 |
| **보드** | GET | `/rest/agile/1.0/board/{boardId}` | 보드 조회 |
| **대시보드** | GET | `/dashboard/gadgets` | 대시보드 가젯 목록 |
| **감사** | GET | `/rest/api/3/audit` | Audit Log 조회 |

### 6.3 Rate Limiting

| 엔드포인트 | 제한 |
|-----------|------|
| 검색 (`/search`) | 100 req/min |
| 이슈 CRUD (`/issue`) | 300 req/min |
| 상태 전환 (`/transitions`) | 200 req/min |
| 첨부파일 (`/attachments`) | 30 req/min (최대 10MB) |
| 인증 (`/auth/login`) | 10 req/min (IP 기준) |

### 6.4 공통 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| UNAUTHORIZED | 401 | 인증 실패 |
| FORBIDDEN | 403 | 권한 없음 |
| NOT_FOUND | 404 | 리소스 없음 |
| WORKFLOW_VIOLATION | 409 | 허용되지 않는 상태 전환 |
| WIP_LIMIT_EXCEEDED | 409 | WIP 제한 초과 |
| VERSION_CONFLICT | 409 | 동시 편집 충돌 (낙관적 락) |
| ACCOUNT_LOCKED | 423 | 계정 잠금 |

---

## 7. 화면 구성

### 7.1 화면 목록

| 화면 ID | 화면명 | URL | 권한 |
|---------|--------|-----|------|
| SCR-001 | 로그인 | `/login` | ALL |
| SCR-002 | 메인 대시보드 | `/dashboard` | ALL |
| SCR-003 | 백로그 | `/project/{key}/backlog` | DEVELOPER+ |
| SCR-004 | 스크럼 보드 | `/project/{key}/board` | DEVELOPER+ |
| SCR-005 | 칸반 보드 | `/project/{key}/kanban` | DEVELOPER+ |
| SCR-006 | 이슈 상세 | `/issue/{issueKey}` | REPORTER+ |
| SCR-007 | 이슈 생성 | `/project/{key}/issue/new` | REPORTER+ |
| SCR-008 | JQL 검색 | `/search` | ALL |
| SCR-009 | 로드맵 | `/project/{key}/roadmap` | ALL |
| SCR-010 | 스프린트 관리 | `/project/{key}/sprints` | DEVELOPER+ |
| SCR-011 | 릴리즈 관리 | `/project/{key}/releases` | ADMIN |
| SCR-012 | 리포트 | `/project/{key}/reports` | ALL |
| SCR-013 | 프로젝트 설정 | `/project/{key}/settings` | ADMIN |
| SCR-014 | Audit Log | `/project/{key}/audit` | ADMIN |

### 7.2 전체 화면 흐름

```
로그인 → 메인 대시보드
  ├── 백로그 ──→ 이슈 생성 / 이슈 상세
  ├── 보드 (스크럼/칸반) ──→ 이슈 상세 → 댓글/첨부 / 상태 전환
  ├── 스프린트 관리 ──→ 플래닝 / 리뷰
  ├── 릴리즈 관리
  ├── 로드맵
  ├── 리포트 (번다운/속도/CFD)
  ├── JQL 검색
  ├── Audit Log
  ├── 대시보드 가젯
  └── 프로젝트 설정 → 워크플로우 / 권한 / 자동화
```

---

## 8. 아키텍처 개요

### 8.1 레이어드 아키텍처

```
[프레젠테이션] React SPA / Flutter Mobile
       ↓
[API Gateway] Rate Limiting, JWT 검증, CORS
       ↓
[애플리케이션] Spring MVC Controller, Bean Validation
       ↓
[도메인] WorkflowEngine, JqlParser, AutomationEngine, NotificationService
       ↓
[인프라] PostgreSQL, Redis, S3, SQS
```

### 8.2 이벤트 기반 아키텍처

이슈 생성/상태 전환 시 이벤트 발행 → SQS를 통한 비동기 처리:
- **알림 서비스**: 이메일/Slack 알림 비동기 발송
- **자동화 엔진**: Trigger → Condition → Action 규칙 평가 및 실행

### 8.3 캐싱 전략

Redis를 활용한 캐싱:
- 보드 데이터 캐시 (`board:{projectId}`)
- WIP 카운트 캐시
- 세션 캐시

### 8.4 물리 배포 (AWS)

```
Internet → Route 53 → CloudFront (CDN) / WAF → ALB
  → ECS Fargate (Spring Boot, Auto Scaling)
  → RDS PostgreSQL (Multi-AZ + Read Replica)
  → ElastiCache Redis (Cluster Mode)
  → S3 (첨부파일) / SQS (알림/자동화 큐)
  → CloudWatch + Grafana (모니터링)
```

---

## 9. 스프린트 운영 프로세스

### 9.1 Sprint 사이클 (2주)

| 이벤트 | 시점 | 소요 시간 | 목적 |
|--------|------|-----------|------|
| Sprint Planning | 1일차 오전 | 2~4시간 | 목표 설정, 백로그 선택, SP 산정 |
| Daily Scrum | 매일 고정 | 15분 | 진행 공유, 블로커 식별 |
| Sprint Review | 마지막 날 오후 | 1~2시간 | 완성 기능 데모 |
| Retrospective | 마지막 날 말 | 1시간 | Keep / Problem / Try |

### 9.2 Definition of Ready (DoR)

- 요구사항 명확 (Acceptance Criteria 명문화)
- UI/UX 산출물 존재
- API 스펙 확정
- Story Point 합의 (SP ≤ 8 권장)
- 의존성 파악 완료

### 9.3 Definition of Done (DoD)

- 코드 구현 완료 (AC 전체 충족)
- 코드 리뷰 완료 (최소 1인 Approve)
- 단위 테스트 통과 (커버리지 80%+)
- QA 테스트 통과
- 문서 업데이트 완료
- main/develop 머지 완료, 빌드 성공
- 회귀 테스트 확인

---

## 10. 프로젝트 일정

| 단계 | 기간 | 산출물 |
|------|------|--------|
| 기획 | 2주 (2026-04-01 ~ 04-14) | 요구사항 정의서, 화면 설계서 |
| 설계 | 3주 (2026-04-15 ~ 05-05) | ERD, API 정의서, 아키텍처 정의서 |
| 개발 | 8주 (2026-05-06 ~ 06-30) | 소스코드, 단위 테스트 |
| QA | 3주 (2026-07-01 ~ 07-21) | 테스트 리포트 |
| 배포 | 1.5주 (2026-07-22 ~ 07-31) | 운영 시스템 |

### 마일스톤

| 마일스톤 | 목표일 | 산출물 |
|----------|--------|--------|
| M1. 기획 완료 | 2026-04-14 | 요구사항 정의서, 화면 설계서 |
| M2. 설계 완료 | 2026-05-05 | ERD, API 정의서, 아키텍처 정의서 |
| M3. 개발 완료 | 2026-06-30 | 소스코드, 단위 테스트 결과 |
| M4. QA 완료 | 2026-07-21 | 테스트 리포트 |
| M5. 배포 | 2026-07-31 | 배포 완료 보고서 |

---

## 11. 팀 구성

| 역할 | 책임 |
|------|------|
| PM | 프로젝트 관리, 일정 조율, 이해관계자 소통 |
| 백엔드 개발 | REST API, 워크플로우 엔진, DB 설계 및 구현 |
| 프론트엔드 개발 | 보드, 대시보드, 이슈 관리 UI/UX 구현 |
| QA | 테스트 계획 및 실행, 결함 관리 |
| 디자이너 | UI/UX 디자인 (보드, 대시보드, 이슈 화면) |

---

## 12. 리스크 관리

| 리스크 | 영향도 | 발생확률 | 대응 방안 |
|--------|--------|----------|-----------|
| 워크플로우 엔진 복잡도 증가 | 높음 | 중간 | 표준 워크플로우 6단계로 제한 |
| API 성능 병목 | 중간 | 중간 | Redis 캐싱, JQL 쿼리 최적화, 인덱스 튜닝 |
| 권한 체계 설계 오류 | 높음 | 낮음 | 5단계 역할 사전 정의 |

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|-----------|
| v1.0 | 2026-03-22 | - | jira-doc 기반 PRD 초안 작성 |
