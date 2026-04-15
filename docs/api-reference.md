# REST API 요약

> 상세 계약·스키마는 Swagger UI에서 확인: `http://localhost:8080/swagger-ui.html` (개발 프로필 기준)

## 인증 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/auth/register` | 회원가입 (JWT 없음) | - |
| `POST` | `/api/auth/login` | 로그인 (Access + Refresh JWT) | - |
| `POST` | `/api/auth/refresh` | 리프레시 토큰으로 재발급 (로테이션) | - |

이후 API는 헤더 `Authorization: Bearer {accessToken}` 필요.

## 사용자 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/users` | 사용자 목록 | ✅ |
| `GET` | `/api/v1/users/{id}` | 사용자 상세 | ✅ |
| `POST` | `/api/v1/users` | 회원가입 (레거시, `register`와 동일) | - |
| `PUT` | `/api/v1/users/{id}` | 사용자 수정 | ✅ |
| `DELETE` | `/api/v1/users/{id}` | 사용자 삭제 | ✅ |

## 프로젝트 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/projects` | 내가 멤버인 프로젝트 목록 | ✅ |
| `POST` | `/api/v1/projects` | 프로젝트 생성 | ✅ |
| `GET` | `/api/v1/projects/{id}` | 프로젝트 상세 | ✅ |
| `PUT` | `/api/v1/projects/{id}` | 프로젝트 수정 | ✅ |
| `DELETE` | `/api/v1/projects/{id}` | 프로젝트 삭제 | ✅ |
| `GET` | `/api/v1/projects/{id}/members` | 멤버 목록 | ✅ |
| `POST` | `/api/v1/projects/{id}/members` | 멤버 추가 | ✅ |
| `DELETE` | `/api/v1/projects/{id}/members/{memberId}` | 멤버 제거 | ✅ |
| `GET` | `/api/v1/projects/{projectId}/roadmap/epics` | 로드맵용 Epic 목록 (`effectiveStart`/`effectiveEnd` 등, FR-012) | ✅ |
| `GET` | `/api/v1/projects/{projectId}/wip-limits` | 칸반 WIP 한도 목록 | ✅ |
| `PUT` | `/api/v1/projects/{projectId}/wip-limits` | WIP 한도 전체 교체 (`limits`: `[{status,maxIssues}]`, KANBAN만) | ✅ |

## 이슈 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/issues/project/{projectId}` | 이슈 목록 (페이징) | ✅ |
| `GET` | `/api/v1/issues/project/{projectId}/backlog` | 백로그 (`backlogRank` 오름차순) | ✅ |
| `PUT` | `/api/v1/issues/project/{projectId}/backlog/order` | 백로그 순서 일괄 저장 (`orderedIssueIds`: 스프린트 미배정 이슈 전체) | ✅ |
| `POST` | `/api/v1/issues/project/{projectId}/sprint-assignment` | 다수 이슈 스프린트 배정 (`issueIds`, `sprintId` null이면 백로그로) | ✅ |
| `GET` | `/api/v1/issues/{issueKey}` | 이슈 상세 (`labels`·`components` 배열 포함) | ✅ |
| `POST` | `/api/v1/issues` | 이슈 생성 (Epic만 `epicStartDate`·`epicEndDate` 선택, ISO 날짜) | ✅ |
| `PUT` | `/api/v1/issues/{issueKey}` | 이슈 수정 (Epic: `patchEpicDates`·기간, `clearEpicDates`) | ✅ |
| `DELETE` | `/api/v1/issues/{issueKey}` | 이슈 삭제 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/transitions` | 상태 전환 (KANBAN + WIP 설정 시 한도 초과 시 409) | ✅ |
| `GET` | `/api/v1/issues/{issueKey}/transitions` | 전환 이력 | ✅ |
| `GET` | `/api/v1/issues/{issueKey}/links` | 이슈 링크 목록 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/links` | 이슈 링크 생성 (`targetIssueKey`, `linkType`) | ✅ |
| `PUT` | `/api/v1/issues/links/{linkId}` | 링크 유형 변경 | ✅ |
| `DELETE` | `/api/v1/issues/links/{linkId}` | 링크 삭제 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/labels` | 레이블 연결 (`labelId`) | ✅ |
| `DELETE` | `/api/v1/issues/{issueKey}/labels/{labelId}` | 레이블 해제 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/components` | 컴포넌트 연결 (`componentId`, 동일 프로젝트만) | ✅ |
| `DELETE` | `/api/v1/issues/{issueKey}/components/{componentId}` | 컴포넌트 해제 | ✅ |
| `GET` | `/api/v1/issues/{issueKey}/attachments` | 이슈 첨부 목록 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/attachments` | 첨부 업로드 (`multipart/form-data`, 파트명 `file`, 최대 20MB) | ✅ |
| `GET` | `/api/v1/attachments/{id}/file` | 첨부 파일 다운로드 (바이너리) | ✅ |
| `DELETE` | `/api/v1/attachments/{id}` | 첨부 삭제 | ✅ |

## JQL (FR-016)

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/v1/projects/{projectId}/jql/search` | JQL 검색 (`jql`, `startAt`, `maxResults`; `app.jql.max-results-cap` 상한) | ✅ |
| `POST` | `/api/v1/projects/{projectId}/jql/filters` | 저장 필터 생성 (`name`, `jql`) | ✅ |
| `GET` | `/api/v1/projects/{projectId}/jql/filters` | 현재 사용자 저장 필터 목록 | ✅ |
| `DELETE` | `/api/v1/projects/{projectId}/jql/filters/{filterId}` | 저장 필터 삭제 (소유자만) | ✅ |

## 스프린트 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/sprints/project/{projectId}` | 스프린트 목록 | ✅ |
| `GET` | `/api/v1/sprints/{id}` | 스프린트 상세 | ✅ |
| `POST` | `/api/v1/sprints` | 스프린트 생성 (`PLANNING`) | ✅ |
| `POST` | `/api/v1/sprints/{id}/start` | 스프린트 시작 (`PLANNING`→`ACTIVE`, 프로젝트당 `ACTIVE` 1개) | ✅ |
| `POST` | `/api/v1/sprints/{id}/complete` | 스프린트 완료 (`ACTIVE`→`COMPLETED`). 선택 JSON: `disposition`=`BACKLOG`\|`NEXT_SPRINT`, `NEXT_SPRINT` 시 `nextSprintId`(동일 프로젝트·`PLANNING`) | ✅ |
| `DELETE` | `/api/v1/sprints/{id}` | 스프린트 삭제 (`ACTIVE`·이슈 배정 시 409) | ✅ |
| `GET` | `/api/v1/sprints/{id}/board` | 스프린트 보드 (상태별 컬럼, `swimlane=NONE`\|`ASSIGNEE`) | ✅ |

**FR-008**: 보드는 `IssueStatus` 6단계 컬럼 + 스윔레인. 응답은 Redis에 JSON으로 캐시됨 (`app.board.cache.enabled` / `ttl-seconds`; 테스트 프로필에서는 기본 비활성). 이슈 생성·수정·전환·삭제·스프린트 배정 및 스프린트 시작·완료·삭제 시 해당 스프린트 캐시 무효화. **FR-011**: 잘못된 전환이나 동시 진행·삭제 제한 위반 시 HTTP 409 (`SPRINT_*` 오류 코드).

## 릴리즈·댓글·대시보드·감사 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/versions/project/{projectId}` | 프로젝트별 버전 목록 | ✅ |
| `GET` | `/api/v1/versions/{id}` | 버전 상세 | ✅ |
| `POST` | `/api/v1/versions` | 버전 생성 | ✅ |
| `POST` | `/api/v1/versions/{id}/release` | 릴리즈 | ✅ |
| `DELETE` | `/api/v1/versions/{id}` | 버전 삭제 | ✅ |
| `GET` | `/api/v1/comments/issue/{issueId}` | 댓글 목록 | ✅ |
| `POST` | `/api/v1/comments` | 댓글 작성 | ✅ |
| `GET` | `/api/v1/dashboards` | 대시보드 목록 | ✅ |
| `GET` | `/api/v1/audit-logs/issue/{issueId}` | 감사 로그 | ✅ |

GitHub 연동·자동화·VCS 링크 등 추가 엔드포인트는 Swagger에서 확인하세요.
