# 최종 테스트 시나리오 검증 결과 보고서

> **작성일**: 2026-04-15  
> **검증 대상**: FINAL-TEST-SCENARIOS.md (v1.1) 기준 Part A~E  
> **검증 방법**: 백엔드 단위/통합 테스트 실행(Gradle), 소스 코드 정적 분석, 프론트엔드 빌드 검증, **브라우저 기반 UI 테스트**  
> **백엔드 테스트**: 38개 테스트 클래스, **185개 테스트 전체 통과** (실패 0, 에러 0, 스킵 0)

---

## 종합 요약

| 구분 | 전체 항목 | 통과 | 미구현/미비 | 비고 |
|------|-----------|------|-------------|------|
| Part A (E2E·UAT) | 12 | 10 | 2 | A-11 스프린트 완료 미완료 이슈 처리 누락, 프론트엔드 테스트 부재 |
| Part B (API·계약) | 6 | 6 | 0 | 전체 준수 |
| Part C (보안·RBAC) | 4 | 3 | 1 | CORS 설정 누락 |
| Part D (예외·검증) | 6 | 6 | 0 | 전체 준수 |
| Part E (NFR) | 5 | 2 | 3 | 부하 테스트 스크립트 부재, 모바일 NFR 미구현 |
| **총계** | **33** | **27** | **6** | UI 테스트 추가 이슈 1건 (ISSUE-007) |

---

## 발견된 문제점 목록

### ISSUE-001 [Critical/P0] 스프린트 완료 시 미완료 이슈 처리 로직 누락

**관련 시나리오**: A-11 (S-11 스프린트 완료·미완료 처리·로그아웃)  
**관련 TC**: TC-080~TC-085  
**파일**: `src/main/java/com/pch/mng/sprint/SprintService.java` — `complete()` 메서드

**현상**: `SprintService.complete()` 메서드가 단순히 스프린트 상태를 COMPLETED로 변경하고 Redis 캐시를 제거하는 것만 수행한다. 스프린트 내 IN_PROGRESS, SELECTED 등 미완료 상태의 이슈에 대한 처리 로직이 전혀 없다.

**현재 코드**:
```java
@Transactional
public SprintResponse.DetailDTO complete(Long id) {
    Sprint sprint = sprintRepository.findByIdWithProject(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    if (sprint.getStatus() != SprintStatus.ACTIVE) {
        throw new BusinessException(ErrorCode.SPRINT_INVALID_TRANSITION);
    }
    sprint.setStatus(SprintStatus.COMPLETED);
    sprintBoardRedisCache.evictSprint(id);
    return SprintResponse.DetailDTO.of(sprint);
}
```

**필요한 조치**:
1. 스프린트 완료 시 미완료 이슈를 백로그로 이동하거나 다음 스프린트로 이관하는 로직 추가
2. 완료 전 미완료 이슈 존재 여부를 경고하거나, 이슈 처리 방법(백로그 이동/다음 스프린트 이관)을 선택할 수 있는 API 파라미터 제공
3. 관련 프론트엔드 UI에서도 미완료 이슈 처리 모달/확인 절차 구현

**심각도**: Critical — 스크럼 핵심 워크플로의 필수 기능이며 P0 항목이다.

---

### ISSUE-002 [Critical/P0] CORS 설정 완전 누락

**관련 시나리오**: C-04 (CORS)  
**관련 규칙**: `rules/human/04-security.md` — "운영/stage에서 허용 오리진 명시; credentials 사용 시 와일드카드 금지"  
**파일**: `src/main/java/com/pch/mng/global/config/SecurityConfig.java`

**현상**: 프로젝트 전체에서 CORS 관련 설정이 전혀 존재하지 않는다. `CorsConfigurer`, `WebMvcConfigurer.addCorsMappings()`, `@CrossOrigin`, Spring Security의 `.cors()` 메서드 호출 모두 부재하다.

**영향**:
1. 웹 프론트엔드(`apps/web`)와 백엔드가 다른 포트/도메인에서 구동 시 브라우저의 CORS 정책에 의해 모든 API 호출이 차단됨
2. 모바일 앱에서의 API 접근에는 영향 없음 (CORS는 브라우저 전용 정책)
3. 스테이징/프로덕션 환경에서의 프론트엔드-백엔드 통합 불가

**필요한 조치**:
```java
// SecurityConfig.java 또는 별도 WebConfig.java에 추가
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("${허용_도메인}")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```
- `rules/human/04-security.md`에 따라 와일드카드(`*`) 사용 금지, 명시적 오리진만 허용
- 환경별(dev/prod) 오리진 분리 적용 필요

**심각도**: Critical — 웹 프론트엔드 통합이 불가능하며 보안 규칙 위반이다.

---

### ISSUE-003 [Major] 부하 테스트 스크립트 부재

**관련 시나리오**: Part E — NFR-001~002, NFR-004  
**관련 TC**: 10-테스트전략서 §9 k6 SLA  
**검색 경로**: `scripts/`, `docs/`, 프로젝트 전체

**현상**: k6, Gatling, JMeter 등 부하 테스트 도구의 스크립트 파일이 프로젝트 내에 전혀 존재하지 않는다. `scripts/` 디렉토리에는 `run-gradlew.cjs` 하나만 존재하며, `package.json`에도 부하 테스트 관련 의존성이 없다.

**영향**: 다음 NFR 항목의 검증이 불가능하다:
- NFR-001: API P95 응답시간 < 200ms
- NFR-002: JQL 검색 P95 < 500ms
- NFR-004: 동시 500 사용자 처리

**필요한 조치**:
1. k6 또는 Gatling 기반 부하 테스트 시나리오 파일 작성 (로그인, 이슈 CRUD, JQL 검색 등 핵심 API)
2. CI/CD 파이프라인에 부하 테스트 게이트 통합
3. 실행 결과를 13-테스트보고서에 기록할 수 있는 출력 형식 표준화

**심각도**: Major — 릴리즈 판정을 위한 성능 수치 확보가 불가능하다.

---

### ISSUE-004 [Major] 프론트엔드(Web) 테스트 완전 부재

**관련 시나리오**: Part A 전체 (E2E 검증), 10-테스트전략서 §7 테스트 피라미드  
**파일**: `apps/web/`

**현상**: React(Vite + TypeScript) 웹 애플리케이션에 테스트 파일이 단 하나도 존재하지 않는다. Jest, Vitest 등 테스트 러너도 `package.json`의 devDependencies에 설정되어 있지 않다.

**영향**:
1. UI 회귀 테스트 불가 — 기능 변경 시 프론트엔드 사이드 이펙트를 자동으로 감지할 수 없음
2. 테스트 피라미드의 UI 계층이 완전히 공백
3. Part A E2E 시나리오가 수동 테스트에만 의존

**필요한 조치**:
1. Vitest + React Testing Library 설정 추가
2. 최소한 핵심 페이지(로그인, 이슈 생성, 보드, 스프린트)의 컴포넌트 테스트 작성
3. E2E 테스트 (Playwright/Cypress) 도입 검토

**심각도**: Major — 테스트 자동화 전략의 핵심 계층이 누락되어 있다.

---

### ISSUE-005 [Major] 모바일(Flutter) 테스트 완전 부재

**관련 시나리오**: Part A 모바일, Part E — NFR-010~011  
**파일**: `apps/mobile/`

**현상**: Flutter 앱에 `test/` 디렉토리가 존재하지 않으며 단위/위젯 테스트가 전무하다. `flutter_test`가 `dev_dependencies`에 선언되어 있으나 실제 테스트 파일은 없다.

**추가 문제 (NFR-010~011)**:
- 콜드 스타트 성능 프로파일링 코드 없음 (NFR-010: < 3초)
- 오프라인 캐시 메커니즘 미구현 (NFR-011: 데이터 손실 0)
- SharedPreferences는 토큰 저장용으로만 사용 중이며 Hive, Drift 등 로컬 DB 미도입

**필요한 조치**:
1. Widget 테스트 기본 구조 생성 (`test/` 디렉토리)
2. 핵심 화면(이슈 목록, 이슈 상세, 보드)의 위젯 테스트 작성
3. Dio 인터셉터 기반 오프라인 캐시 전략 수립 (또는 Hive 도입)
4. Flutter DevTools를 활용한 콜드 스타트 프로파일링

**심각도**: Major — 모바일 관련 NFR 및 기능 검증이 완전히 불가능하다.

---

### ISSUE-006 [Minor] 웹 프론트엔드 빌드 실패

**검증 환경**: 샌드박스 (Node.js v22.22.0)  
**파일**: `apps/web/package.json`

**현상**: `npm run build:web` (tsc + vite build) 실행 시 `rolldown` 네이티브 바이너리 모듈 로드 실패로 빌드가 중단된다. Vite 8.x가 내부적으로 사용하는 `rolldown` 번들러의 플랫폼별 바이너리가 현재 환경에 맞게 설치되지 않았다.

**에러 메시지**:
```
requireNative (rolldown/dist/shared/binding-s-V_wTpj.mjs)
code: 'MODULE_NOT_FOUND'
```

**영향**: CI/CD 환경에서도 동일한 문제가 발생할 수 있으므로 Node.js 버전 및 플랫폼 호환성 확인 필요하다.

**필요한 조치**:
1. `node_modules` 삭제 후 재설치 (`npm ci`)
2. CI 환경의 Node.js 버전과 OS 아키텍처 확인
3. `package-lock.json`에 플랫폼별 optional dependencies가 올바르게 포함되어 있는지 검증

**심각도**: Minor — 환경 의존적 문제이며 `npm ci`로 해결 가능성 높음.

---

### ISSUE-007 [Minor] 프론트엔드 409 에러 시 원시 메시지 노출

**관련 시나리오**: A-02 (프로젝트 생성·키 중복), D-03 (중복 409)  
**발견 경로**: 브라우저 UI 테스트 — 프로젝트 키 중복 생성 시도

**현상**: 이미 존재하는 프로젝트 키로 프로젝트를 생성하면 백엔드는 `409 Conflict` + `ApiResponse(success=false, error={code, message})`를 정상 반환하지만, 프론트엔드에서 `"Request failed with status code 409"`라는 Axios 기본 에러 메시지를 그대로 사용자에게 표시한다. 백엔드가 보내주는 비즈니스 에러 메시지(예: "이미 사용 중인 프로젝트 키입니다")를 파싱하여 보여주지 않는다.

**영향**: 사용자가 어떤 입력이 잘못되었는지 알 수 없어 UX가 저하된다. 409뿐 아니라 400, 403 등 다른 비정상 응답에서도 동일한 패턴이 존재할 가능성이 있다.

**필요한 조치**:
1. Axios 인터셉터 또는 에러 핸들러에서 `error.response.data.error.message`를 파싱하여 사용자에게 표시
2. 모든 API 에러 응답에 대해 백엔드 메시지를 우선 사용하고, 파싱 불가 시에만 기본 메시지를 fallback으로 사용

**심각도**: Minor — 기능 동작에는 영향 없으나 사용자 경험이 저하된다.

---

## Part별 상세 검증 결과

### Part A — 엔드투엔드·UAT

| ID | 시나리오 | 결과 | 비고 |
|----|----------|------|------|
| A-01 | 로그인·세션·보호 라우트 | PASS | JWT 인증, refresh 토큰 순환, 무효 자격증명 처리, 로그인 잠금 모두 구현 |
| A-02 | 프로젝트 생성·키 중복 | PASS | `existsByKey()` 중복 체크, 생성자 자동 ADMIN 배정 |
| A-03 | 멤버·역할(2계정) | PASS | 5개 역할(ADMIN/DEVELOPER/QA/REPORTER/VIEWER), ProjectSecurityService SpEL 기반 권한 검증 |
| A-04 | 이슈 생성·백로그·순서 | PASS | 필수 필드 검증, backlogRank 자동 배정, 순서 재정렬 API |
| A-05 | 스프린트 생성·시작·배정 | PASS | PLANNING→ACTIVE 전이, 활성 스프린트 1개 제한 |
| A-06 | 워크플로·WIP 제한 | PASS | 6단계 FSM + 리워크, 칸반 WIP 제한 검증 |
| A-07 | 댓글·멘션·첨부 | PASS | 멘션 파서 + 이벤트, 첨부 업로드/다운로드/삭제 |
| A-08 | JQL·아카이브·로드맵 | PASS | 완전한 AST 파서, 저장 필터, 가시성 필터 |
| A-09 | 리포트·릴리즈 노트 | PASS | 번다운/벨로시티/CFD, 릴리즈 버전 CRUD + 릴리즈 노트 생성 |
| A-10 | 설정·아카이브·Audit | PASS | 필드 수준 감사 추적(old→new), 자동 아카이브 |
| **A-11** | **스프린트 완료·미완료 처리** | **FAIL** | **→ ISSUE-001** |
| A-R | 회귀 R-01~R-04 | PASS | RBAC 통합 테스트, 타 프로젝트 접근 거부 |

### Part B — API·계약

| ID | 시나리오 | 결과 | 비고 |
|----|----------|------|------|
| B-01 | ApiResponse 래퍼 | PASS | 바이너리 다운로드·OAuth 콜백·웹훅은 적절히 예외 처리 |
| B-02 | URI 규칙 | PASS | `/api/v1/`, 복수형, kebab-case, POST+액션 일관 적용 |
| B-03 | HTTP 상태 코드 | PASS | POST 201, DELETE 200+noContent, GET/PUT 200 |
| B-04 | Bearer 인증 | PASS | 모든 보호 엔드포인트에 @PreAuthorize 적용 |
| B-05 | 페이징 | PASS | Page<T> + Pageable, 기본 size 20 |
| B-06 | OpenAPI 대조 | PASS | SpringDoc 3.0.0, JWT bearer 설정, 프로덕션 비활성화 |

### Part C — 보안·RBAC

| ID | 시나리오 | 결과 | 비고 |
|----|----------|------|------|
| C-01 | JWT | PASS | Access 1시간/Refresh 7일, 환경변수 주입, Redis/InMemory 토큰 저장소 |
| C-02 | RBAC 행렬 | PASS | 5역할 세분화, ProjectSecurityService 빈으로 SpEL 검증 |
| C-03 | BCrypt | PASS | BCryptPasswordEncoder(12) 적용 |
| **C-04** | **CORS** | **FAIL** | **→ ISSUE-002** |

### Part D — 예외·검증

| ID | 시나리오 | 결과 | 비고 |
|----|----------|------|------|
| D-01 | 잘못된 입력 (400) | PASS | MethodArgumentNotValidException 핸들링, 필드 에러 맵 반환 |
| D-02 | 리소스 미존재 (404) | PASS | ENTITY_NOT_FOUND ErrorCode |
| D-03 | 중복 (409) | PASS | DUPLICATE_RESOURCE, EMAIL_ALREADY_EXISTS 등 |
| D-04 | 권한 없음 (403) | PASS | FORBIDDEN ErrorCode + AccessDeniedException 핸들링 |
| D-05 | 워크플로 위반 (409) | PASS | WORKFLOW_VIOLATION, WIP_LIMIT_EXCEEDED |
| D-06 | 전역 핸들러 | PASS | RestControllerAdvice, 스택트레이스 미노출, 운영 Swagger 비활성화 |

### Part E — NFR 스팟 체크

| NFR | 결과 | 비고 |
|-----|------|------|
| NFR-001~002,004 | **미측정** | → ISSUE-003 (부하 테스트 스크립트 부재) |
| NFR-005 | PASS | BCrypt cost = 12 확인 |
| NFR-007 | PASS | RedisLoginAttemptService: 5회/30분 잠금, 설정값 검증 완료 |
| NFR-008 | PASS | IssueAuditService 필드 수준 기록 (fieldName, oldValue, newValue) |
| NFR-010~011 | **미구현** | → ISSUE-005 (모바일 콜드 스타트/오프라인 미구현) |

---

## 백엔드 테스트 실행 결과 상세

**실행 환경**: JDK 21.0.2, Gradle 9.4.0, H2 Database (테스트 프로필)  
**결과**: BUILD SUCCESSFUL — 185 tests, 0 failures, 0 errors, 0 skipped

| 테스트 클래스 | 테스트 수 | 결과 |
|--------------|-----------|------|
| DashboardServiceTest | 22 | PASS |
| IssueIntegrationTest | 19 | PASS |
| JqlParserTest | 13 | PASS |
| IssueWorkflowPolicyTest | 12 | PASS |
| SprintServiceTest | 12 | PASS |
| ReleaseVersionServiceTest | 9 | PASS |
| ProjectServiceTest | 9 | PASS |
| IssueAuditServiceTest | 8 | PASS |
| SprintIntegrationTest | 6 | PASS |
| BoardServiceTest | 6 | PASS |
| IssueSecurityPolicyTest | 5 | PASS |
| AutomationEngineTest | 5 | PASS |
| ReportServiceTest | 4 | PASS |
| JqlSearchIntegrationTest | 4 | PASS |
| IssueWatcherServiceTest | 4 | PASS |
| 기타 23개 클래스 | 47 | PASS |

---

## 권장 우선순위

1. **긴급(릴리즈 차단)**: ISSUE-001 (스프린트 미완료 이슈 처리), ISSUE-002 (CORS 설정)
2. **높음(다음 스프린트)**: ISSUE-003 (부하 테스트), ISSUE-004 (웹 테스트)
3. **보통(로드맵)**: ISSUE-005 (모바일 테스트/NFR), ISSUE-006 (빌드 환경), ISSUE-007 (프론트엔드 에러 메시지)

---

## 브라우저 기반 UI 테스트 결과

> **테스트 환경**: Chrome (localhost:5173), 시드 계정 `admin@local.test` / `dev123`  
> **테스트 도구**: Chrome DevTools + API 직접 호출

### 테스트된 페이지 및 결과

| 페이지 | URL | 결과 | 비고 |
|--------|-----|------|------|
| 로그인 | `/login` | PASS | 이메일/비밀번호 입력 → JWT 발급 → 대시보드 리다이렉트 정상 |
| 대시보드 | `/dashboard` | PASS | 프로젝트 목록 표시, 프로젝트 생성 폼 동작 |
| 프로젝트 개요 | `/project/TEST/board` | PASS | 스크럼 보드 정상 렌더링 |
| 스크럼 보드 | `/project/TEST/scrum` | PASS | 스프린트별 이슈 표시, 드래그앤드롭 UI 존재 |
| 칸반 보드 | `/project/TEST/kanban` | PASS | 6컬럼(BACKLOG~DONE) 정상 표시, 이슈 카드 배치 |
| 백로그 | `/project/TEST/backlog` | PASS | 이슈 목록, 스프린트 배정 기능 |
| 스프린트 관리 | `/project/TEST/sprints` | PASS | 스프린트 CRUD, 활성화 기능 |
| 새 이슈 | `/project/TEST/issues/new` | PASS | 이슈 생성 폼, 필수 필드 검증 |
| JQL 검색 | `/project/TEST/jql` | PASS | 자동완성, 저장 필터, 검색 결과 |
| 로드맵 | `/project/TEST/roadmap` | PASS | Epic 기반 타임라인, 빈 상태 메시지 |
| 릴리즈 | `/project/TEST/releases` | PASS | 버전 등록 폼, 버전 목록 |
| 리포트 | `/project/TEST/reports` | PASS | 번다운/속도/CFD 3가지 차트, 스프린트 선택 |
| 설정 | `/project/TEST/settings` | PASS | 프로젝트 정보 수정, 아카이브, 담당자 변경 |
| 감사 로그 | `/project/TEST/audit` | PASS | 필드 변경 이력 12건 표시, ADMIN 전용 안내 |

### UI 보안 테스트 결과

| 테스트 항목 | 결과 | 비고 |
|-------------|------|------|
| 로그아웃 후 보호 경로 접근 | PASS | `/project/TEST/board` → `/login` 자동 리다이렉트 |
| 무효 토큰 API 호출 | PASS | `Authorization: Bearer invalid-token` → 401 Unauthorized |
| JWT 응답 구조 | PASS | `accessToken`, `refreshToken` 반환, 민감 정보 미노출 |
| localStorage 토큰 저장 | PASS | `pch.auth.v1` 키에 JSON 형태로 저장 |

### UI 워크플로 테스트 결과

| 테스트 항목 | 결과 | 비고 |
|-------------|------|------|
| 이슈 생성 → 백로그 등록 | PASS | 이슈 생성 후 백로그에 정상 표시 |
| 이슈 스프린트 배정 | PASS | API 통해 `sprintId: 100` 배정 확인 |
| 워크플로 전이 (BACKLOG→SELECTED→IN_PROGRESS→CODE_REVIEW) | PASS | API `POST /api/v1/issues/{key}/transition` 정상 |
| 프로젝트 키 중복 생성 | PASS (기능) | 409 반환은 정상이나 UI 에러 메시지가 원시 형태 → **ISSUE-007** |

---

## 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| v1.0 | 2026-04-15 | FINAL-TEST-SCENARIOS.md v1.1 기준 전체 검증 결과 초안 |
| v1.1 | 2026-04-15 | 브라우저 기반 UI 테스트 결과 추가, ISSUE-007 추가 |
