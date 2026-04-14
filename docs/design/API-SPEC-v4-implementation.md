# API 정의서 v4 — 현재 구현 매핑

> **작성일**: 2026-04-09  
> **최종 갱신**: 2026-04-14 (JQL 검색·아카이브 동작 주석)  
> **Task**: T-201  
> **OpenAPI**: 앱 기동 후 `/v3/api-docs`, Swagger UI `/swagger-ui.html`  
> **외부 정본**: `04-API정의서_v4.0.md` (Jira 스타일 `/rest/api/3` 등) — 점진 정렬 예정

## 공통

| 항목 | 값 |
|------|-----|
| Base path (현재) | `/api/v1` (인증은 `/api/auth`) |
| 성공 래퍼 | `ApiResponse<T>` (`success`, `data`, `error`) |
| 인증 | Bearer JWT (`JwtAuthenticationFilter`) |
| 권한 | `@PreAuthorize` + `projectSecurity` / `dashboardSecurity` (프로젝트 멤버·역할) |

## 인증 `AuthController`

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/register` | `UserAccountRequest.JoinDTO` → 사용자 생성 (비밀번호 BCrypt) |
| POST | `/api/auth/login` | `AuthRequest.LoginDTO` → `TokenDTO` |
| POST | `/api/auth/refresh` | `AuthRequest.RefreshDTO` → 새 Access+Refresh (리프레시 로테이션) |

## 엔드포인트 요약

### 사용자 `UserAccountApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/users` | 목록 |
| GET | `/api/v1/users/{id}` | 단건 |
| POST | `/api/v1/users` | 생성 (`UserAccountRequest`) |
| PUT | `/api/v1/users/{id}` | 수정 |
| DELETE | `/api/v1/users/{id}` | 삭제 |

### 프로젝트 `ProjectApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/projects` | 목록 |
| GET | `/api/v1/projects/by-key/{key}` | 단건(멤버만, 아카이브 포함) |
| GET | `/api/v1/projects/{id}` | 단건 |
| POST | `/api/v1/projects` | 생성 |
| PUT | `/api/v1/projects/{id}` | 수정 (`UpdateDTO`: `name`, `description`, `leadId`, `archived`, `autoArchiveDoneAfterDays` 선택; ≤0 또는 생략 시 자동 아카이브 비활성) |
| POST | `/api/v1/projects/{projectId}/issues/auto-archive-done` | 관리자: 설정된 일수 기준 DONE 이슈 일괄 아카이브(건수 반환); 규칙 미설정 시 `AUTO_ARCHIVE_NOT_CONFIGURED` |
| DELETE | `/api/v1/projects/{id}` | 삭제 |
| GET | `/api/v1/projects/{projectId}/members` | 멤버 목록 |
| POST | `/api/v1/projects/{projectId}/members` | 멤버 추가 |
| DELETE | `/api/v1/projects/{projectId}/members/{memberId}` | 멤버 제거 |
| GET | `/api/v1/projects/{projectId}/roadmap/epics` | 로드맵 Epic 목록 (FR-012, `RoadmapEpicResponse`) |

### 이슈 `IssueApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/issues/project/{projectId}` | 페이징 목록 (`MinDTO.archived`; 비아카이브만 집계) |
| GET | `/api/v1/issues/project/{projectId}/backlog` | 백로그 (비아카이브만) |
| GET | `/api/v1/issues/{issueKey}` | 상세 (`DetailDTO.archived`) |
| POST | `/api/v1/issues` | 생성 (`IssueRequest.SaveDTO`; Epic만 `epicStartDate`/`epicEndDate` 선택) |
| PUT | `/api/v1/issues/{issueKey}` | 수정 (`IssueRequest.UpdateDTO`: `archived` 선택; Epic만 `patchEpicDates`+기간·`clearEpicDates`) |
| DELETE | `/api/v1/issues/{issueKey}` | 삭제 |
| POST | `/api/v1/issues/{issueKey}/transitions` | 상태 전환 (`IssueRequest.TransitionDTO`) |
| GET | `/api/v1/issues/{issueKey}/watchers` | 워처 목록 + 현재 사용자 구독 여부 (`IssueWatcherResponse.ListDTO`, FR-025) |
| POST | `/api/v1/issues/{issueKey}/watchers/me` | 현재 사용자 워치 구독 (멱등) |
| DELETE | `/api/v1/issues/{issueKey}/watchers/me` | 현재 사용자 워치 해제 (멱등) |

### 스프린트 `SprintApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/sprints/project/{projectId}` | 프로젝트별 목록 |
| GET | `/api/v1/sprints/{id}` | 단건 |
| POST | `/api/v1/sprints` | 생성 |
| POST | `/api/v1/sprints/{id}/start` | 시작 |
| POST | `/api/v1/sprints/{id}/complete` | 완료 |
| DELETE | `/api/v1/sprints/{id}` | 삭제 |

### 릴리즈 버전 `ReleaseVersionApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/versions/project/{projectId}` | 목록 |
| GET | `/api/v1/versions/{id}` | 단건 |
| GET | `/api/v1/versions/{id}/release-notes` | Fix 버전 연결 이슈 집계 릴리즈 노트 초안 (마크다운, FR-020) |
| POST | `/api/v1/versions` | 생성 |
| POST | `/api/v1/versions/{id}/release` | 릴리즈 처리 |
| DELETE | `/api/v1/versions/{id}` | 삭제 |

### 댓글 `CommentApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/comments/issue/{issueId}` | 이슈별 목록 (`DetailDTO`: `mentionedUsers` — 본문 `@토큰`이 프로젝트 멤버로 해석된 사용자) |
| POST | `/api/v1/comments` | 생성 (저장 시 멘션 동기화) |
| PUT | `/api/v1/comments/{id}` | 수정 (멘션 재동기화) |
| DELETE | `/api/v1/comments/{id}` | 삭제 |

### 알림 (FR-024, REST 엔드포인트 없음)

| 구성요소 | 설명 |
|----------|------|
| 댓글 멘션 | 댓글 저장·수정 커밋 **이후** 비동기 전송: Slack Incoming Webhook (`app.notification.slack.webhook-url`), 멘션 대상(작성자 제외)에게 **이메일** (`JavaMailSender` 빈 존재 시 + `spring.mail.*` + `app.notification.mail.from`) |
| 스위치 | `app.notification.enabled` (테스트는 `application-test.yml`에서 `false`) |

### 감사 로그 `AuditLogApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/audit-logs/issue/{issueId}` | 이슈별 로그 |

### 대시보드 `DashboardApiController`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/dashboards` | (소유자 기준) 목록 |
| GET | `/api/v1/dashboards/{id}` | 단건(인증 필수; 없으면 404, 비공개·타인 소유면 403) |
| POST | `/api/v1/dashboards` | 생성 |
| PUT | `/api/v1/dashboards/{id}` | 수정 |
| DELETE | `/api/v1/dashboards/{id}` | 삭제 |
| POST | `/api/v1/dashboards/{dashboardId}/gadgets` | 가젯 추가 (`gadgetType`은 `DashboardGadgetType` enum 이름) |
| PUT | `/api/v1/dashboards/{dashboardId}/gadgets/{gadgetId}` | 가젯 수정(타입·위치·configJson 부분 갱신) |
| PUT | `/api/v1/dashboards/{dashboardId}/gadgets/reorder` | 가젯 순서 일괄 변경(본문에 대시보드 소속 가젯 ID 전체) |
| DELETE | `/api/v1/dashboards/{dashboardId}/gadgets/{gadgetId}` | 가젯 삭제 |

### 리포트 `ProjectReportApiController` (FR-022)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/projects/{projectId}/reports/sprints/{sprintId}/burndown` | 스프린트 번다운(전환 이력 기준 일별 잔량·이상선) |
| GET | `/api/v1/projects/{projectId}/reports/velocity` | 완료 스프린트별 DONE 스토리 포인트 (`limit` 기본 6, 최대 24) |
| GET | `/api/v1/projects/{projectId}/reports/cfd` | 누적 흐름용 일별 상태별 이슈 수 (`days` 기본 30, 7~90; `sprintId` 선택) |

### JQL 검색·저장 필터 `JqlSearchApiController` (FR-016, T-603)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/projects/{projectId}/jql/search` | JQL 검색(페이징). 응답 `IssueResponse.MinDTO`에 `archived` 포함 |
| GET | `/api/v1/projects/{projectId}/jql/filters` | 저장 필터 목록(소유자별) |
| POST | `/api/v1/projects/{projectId}/jql/filters` | 저장 필터 생성 |
| DELETE | `/api/v1/projects/{projectId}/jql/filters/{filterId}` | 저장 필터 삭제 |

**아카이브와 JQL**: `JqlSearchService`는 파싱된 조건에 더해 **항상 비아카이브**(`issue.archived = false`)를 적용한다.  
MVP 파서의 `JqlField`에는 **`archived` 필드가 없음** — 사용자 JQL에서 `archived = true` 등을 쓰는 것은 현재 미지원(후속 확장 시 스파이크·필드 enum 정합 필요).

## 다음 작업 (구현 대비)

- DTO 필드·검증 어노테이션을 OpenAPI `description`/`example`과 동기화.  
- `/auth/login`, `/auth/refresh` 문서화 (Phase 3).  
- 표준 오류 코드(PR드 §6.4)와 `GlobalExceptionHandler` 응답 스키마 통일.
