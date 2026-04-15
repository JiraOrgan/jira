# Project Control Hub

> 애자일 팀을 위한 이슈 트래킹, 스프린트 관리, 워크플로우 자동화 통합 협업 플랫폼

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-6-646CFF?style=flat-square&logo=vite&logoColor=white)
![Flutter](https://img.shields.io/badge/Flutter_SDK-3.10+-02569B?style=flat-square&logo=flutter&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.4-02303A?style=flat-square&logo=gradle&logoColor=white)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#️-기술-스택)
- [시스템 아키텍처](#️-시스템-아키텍처)
- [시작하기](#-시작하기)
- [환경변수 설정](#-환경변수-설정)
- [CORS (브라우저 통신)](#-cors-브라우저-통신)
- [테스트 및 품질](#-테스트-및-품질)
- [프로젝트 구조](#-프로젝트-구조)
- [API 명세](#-api-명세)
- [ERD](#️-erd)
- [기여 가이드](#-기여-가이드)
- [프로젝트 문서](#-프로젝트-문서)

---

## 📌 프로젝트 소개

소프트웨어 개발팀의 애자일(Agile) 방법론을 지원하는 이슈 트래킹 및 프로젝트 관리 시스템입니다. 이슈 관리, 워크플로우 엔진, 스크럼/칸반 보드, 스프린트 관리, JQL 검색, 대시보드, REST API를 포함하는 통합 협업 플랫폼을 목표로 합니다.

| 항목 | 내용 |
|------|------|
| 개발 기간 | 2026-04-01 ~ 2026-07-31 (18주) |
| 팀 구성 | PM, 백엔드, 프론트엔드, QA, 디자이너 |
| 문서 | [PRD](docs/PRD.md) &#124; [Phase 로드맵](docs/PHASE.md) &#124; [Task 목록](docs/TASKS.md) |

---

## ✨ 주요 기능

- **이슈 관리**: Epic/Story/Task/Bug/Sub-task 5가지 타입 CRUD, 이슈 간 링크, 레이블/컴포넌트 분류
- **워크플로우**: 6단계 표준 워크플로우 (Backlog → Selected → In Progress → Code Review → QA → Done), 조건부 전환 규칙
- **스크럼/칸반 보드**: 드래그앤드롭 상태 전환, WIP 제한, 스윔레인, 스프린트 번다운 차트
- **스프린트 관리**: 생성/시작/완료 라이프사이클, 백로그 우선순위 관리, 스토리 포인트 산정; 완료 시 **미완료(DONE 아님) 이슈**는 제품 백로그로 되돌리거나 **동일 프로젝트의 PLANNING 스프린트**로 이관(API·웹 모달)
- **대시보드**: 역할별 가젯 구성 (번다운/속도/CFD 차트), 커스터마이징 가능
- **JQL 검색**: PCH Query Language 검색 엔진, 자동완성, 필터 저장
- **릴리즈 관리**: Fix Version, 릴리즈 노트 자동 생성, Semantic Versioning
- **RBAC 권한**: Admin/Developer/QA/Reporter/Viewer 5단계 역할 기반 접근 제어
- **웹 UX**: API 오류 시 Axios 기본 문구 대신 서버 `ApiResponse.message`(및 `error.message`)를 우선 표시
- **GitHub 연동**(선택): OAuth·웹훅·이슈 VCS 링크 (프로젝트 설정 / 이슈 상세)
- **감사 로그**: 전체 필드 변경 추적, CSV/JSON 내보내기

---

## 🛠️ 기술 스택

### Backend

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.3 |
| Security | Spring Security + JWT (jjwt) | 0.12.6 |
| ORM | Spring Data JPA, Hibernate, QueryDSL | 5.1.0 |
| Database | MySQL | 8.0 |
| Cache | Redis | 7.x |
| API 문서 | springdoc-openapi (Swagger) | 3.0.0 |
| Build | Gradle | 9.4 |

### Frontend (Web)

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | React | 19.x |
| 라우팅 | React Router | 7.x |
| 상태관리 | Zustand | 5.x |
| 스타일링 | Tailwind CSS | 4.x |
| 빌드 | Vite | 6.x (모노레포 `overrides`로 버전 정렬) |
| HTTP | Axios | 1.x |
| 테스트 | Vitest, React Testing Library, jsdom | 최신 |
| 리포트 UI | SVG 기반 차트 (번다운·속도·CFD) | — |

### Frontend (Mobile)

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | Flutter | `pubspec` SDK ≥ 3.10 |
| 언어 | Dart | 3.10+ |
| 상태관리 | Riverpod | 3.3.x |
| HTTP | Dio | 5.x |
| 로컬 저장 | shared_preferences | 2.x |
| 테스트 | flutter_test, 위젯 테스트 | — |

### Infra

| 분류 | 기술 |
|------|------|
| Cloud | AWS (ECS Fargate, RDS, ElastiCache, S3, SQS) |
| CI/CD | GitHub Actions |
| CDN | CloudFront |
| 모니터링 | CloudWatch + Grafana |

---

## 🏗️ 시스템 아키텍처

```
[Browser / React SPA]     [Flutter Mobile App]
          │                       │
          │    브라우저→API: CORS (허용 Origin은 APP_CORS_ORIGINS·application.yml)
          └───────┬───────────────┘
                  ▼
        [Route 53 → CloudFront / WAF]
                  │
                  ▼
       [ALB (HTTPS, Health Check)]
                  │
      ┌───────────┼───────────┐
      ▼           ▼           ▼
 [ECS Task 1] [ECS Task 2] [ECS Task N]
 Spring Boot   Spring Boot   Auto Scaling
      │           │           │
      ├───────────┼───────────┤
      ▼           ▼           ▼
   [RDS MySQL]  [Redis]    [S3]
   Multi-AZ     Cluster    첨부파일
      │
   [SQS]
   알림/자동화 큐
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0
- Redis 7.x
- Gradle Wrapper (`./gradlew` / Windows `gradlew.bat`) — 백엔드는 **Maven(`pom.xml`) 미사용**, 별도 Maven 설치 불필요
- Node.js 20 이상 권장 (웹 앱; LTS·CI와 맞춤)
- [k6](https://k6.io/) (선택, 부하 스모크 `npm run test:load:k6`)
- Flutter SDK 3.x (모바일 앱, 선택)

### 모노레포 레이아웃

- **백엔드**: 저장소 루트 **Gradle 전용** Spring Boot (`build.gradle`, Wrapper만 사용)
- **웹**: `apps/web` — React 19, Vite 6, Zustand, Tailwind CSS 4, Vitest (`npm` workspaces)
- **모바일**: `apps/mobile` — Flutter, `flutter_riverpod`, Dio

### 백엔드 실행

```bash
# 1. 레포지토리 클론
git clone https://github.com/Project-Control-Hub/phs.git
cd phs

# 2. 환경변수 설정
cp .env.example .env
# .env 파일 편집 (DB, Redis, JWT 설정)

# 3. 데이터베이스 생성
mysql -u root -p -e "CREATE DATABASE mng_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 4. 빌드 및 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 5. 접속 확인
# API:     http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

### 웹 앱 실행 (React)

저장소 루트에서:

```bash
npm install
npm run dev:web
```

브라우저: `http://localhost:5173` — 개발 시 `/api` 요청은 Vite가 `http://localhost:8080` 으로 프록시합니다. 백엔드가 먼저 떠 있어야 합니다. 로그인은 `/login` (`POST /api/auth/login`), 액세스 만료 시 `POST /api/auth/refresh` 로 자동 갱신됩니다.

주요 웹 경로: `/`(맞춤 대시보드·프로젝트 목록), `/dashboard/{id}`, `/project/{key}`, `/project/{key}/settings`, `/project/{key}/board`, `/project/{key}/backlog`, `/project/{key}/kanban`, `/project/{key}/sprints`, `/project/{key}/jql`, `/project/{key}/roadmap`, `/project/{key}/releases`, `/project/{key}/issues/new`, `/issue/{issueKey}`.

### 백엔드 테스트·빌드 (npm → Gradle Wrapper)

루트에서 웹과 동일하게 `npm`으로 Wrapper를 호출할 수 있습니다(Maven `pom.xml` 없음).

```bash
npm run test:api    # ./gradlew test 와 동등
npm run build:api   # bootJar
npm run boot:api    # bootRun (프로필 등 추가 인자는 아래 참고)
```

Spring 프로필을 넘기려면 예:  
`node scripts/run-gradlew.cjs bootRun --args='--spring.profiles.active=dev'`

### 웹 테스트·프로덕션 빌드

```bash
npm run test:web     # Vitest (단위·컴포넌트)
npm run build:web    # tsc + vite build
npm run lint:web     # ESLint
```

### 부하 테스트 (k6)

[k6](https://k6.io/) 설치 후, API 서버가 떠 있는 상태에서 저장소 루트에서:

```bash
# 기본 BASE_URL=http://localhost:8080, 매 VU마다 회원가입·로그인 후 프로젝트 생성·JQL 검색
npm run test:load:k6

# 또는
BASE_URL=http://localhost:8080 k6 run scripts/load/k6-api-smoke.js

# 시드 계정으로만 부하를 줄 때 (선택)
EMAIL=admin@local.test PASSWORD=... BASE_URL=http://localhost:8080 k6 run scripts/load/k6-api-smoke.js
```

### 모바일 앱 (Flutter)

- **Dart SDK**: `apps/mobile/pubspec.yaml` 기준 **3.10+** (Riverpod 3.3·`riverpod_lint` 3.1과 맞춤).
- **기본(권장)**: PC에 Flutter SDK를 설치하고 **PATH에 `flutter`만** 두면 됩니다. 아래 명령은 **`fvm` 없이** 그대로 사용하세요.
- **FVM**(완전 선택): 팀이 Flutter 버전을 고정할 때만 씁니다. `apps/mobile/.fvm/fvm_config.json`은 있어도 **FVM을 설치하지 않으면 무시**해도 됩니다. PowerShell에서 `fvm`을 찾을 수 없다면 → **FVM 설치를 건너뛰고 `flutter` / `dart`만 사용**하면 됩니다.
  - **Windows에서 FVM을 쓰고 싶을 때**: 먼저 [Flutter](https://docs.flutter.dev/get-started/install/windows)로 `flutter`·`dart`를 PATH에 둔 뒤,
    ```powershell
    dart pub global activate fvm
    ```
    실행 후 **Pub 전역 bin**을 PATH에 추가합니다. (일반적으로 `%LOCALAPPDATA%\Pub\Cache\bin` — 탐색기 주소창에 붙여 넣어 확인.) 새 터미널에서 `fvm --version`이 나오면 `cd apps/mobile` 후 `fvm install`, `fvm flutter pub get` 순서로 사용합니다.
  - 자세한 내용: [fvm.app — 설치](https://fvm.app/documentation/getting-started/installation).
- **상태·HTTP**: Flutter Riverpod **3.x**, Dio, 디버그 빌드에서만 요청/응답 로그(`pretty_dio_logger`, `kDebugMode`).

`apps/mobile`에 `android/`·`ios/` 등이 없으면 Flutter SDK로 한 번 생성합니다.

```bash
cd apps/mobile
flutter create .
flutter pub get
flutter run
```

FVM을 쓰는 경우에만 위 세 줄 앞에 `fvm`을 붙이면 됩니다 (`fvm flutter pub get` 등).

에뮬레이터에서 호스트 백엔드 접근 시 기본 `API_BASE_URL`은 Android 에뮬 `10.0.2.2:8080` 입니다. 필요 시 `flutter run --dart-define=API_BASE_URL=http://...` 로 바꿉니다.

**오프라인(스테일) 응답**: 모바일 Dio에 GET `/api/v1/**` 성공 본문을 `SharedPreferences`에 캐시하는 인터셉터가 있으며, 연결·타임아웃 오류 시 마지막 성공 응답을 반환할 수 있습니다(완전 오프라인 DB는 아님).

```bash
cd apps/mobile
flutter test
```

---

## 🔐 환경변수 설정

```dotenv
# Database
DB_NAME=mng_dev
DB_USERNAME=root
DB_PASSWORD=root

# Redis
REDIS_PASSWORD=

# JWT
JWT_SECRET=dev-secret-key-must-be-at-least-32-characters-long

# Spring
SPRING_PROFILES_ACTIVE=dev

# CORS: 브라우저에서 API와 다른 Origin일 때 (쉼표 구분, credentials 사용 시 * 불가)
# APP_CORS_ORIGINS=https://app.example.com,https://admin.example.com
```

---

## 🌐 CORS (브라우저 통신)

- Spring Security에 **`/api/**` CORS**가 등록되어 있으며, 허용 Origin은 설정으로만 지정합니다(와일드카드 Origin 미사용).
- 기본값은 `application.yml`의 `app.security.cors.allowed-origins`이며, **`APP_CORS_ORIGINS` 환경 변수**로 덮어쓸 수 있습니다.
- 로컬 웹(`http://localhost:5173`, `http://127.0.0.1:5173`)이 기본 포함됩니다. Vite 개발 서버는 `apps/web/vite.config.ts`에서 `/api`를 백엔드로 **프록시**하므로, 같은 Origin으로만 호출할 때는 CORS가 개입하지 않을 수 있습니다.

---

## ✅ 테스트 및 품질

| 구분 | 명령 | 설명 |
|------|------|------|
| 백엔드 단위·통합 | `./gradlew test` 또는 `npm run test:api` | JUnit, `@SpringBootTest`, MockMvc 등 |
| 웹 | `npm run test:web` | Vitest + Testing Library (`apps/web/src/**/*.test.ts(x)`) |
| 모바일 | `cd apps/mobile && flutter test` | 위젯 테스트 등 |
| 부하(k6) | `npm run test:load:k6` | `scripts/load/k6-api-smoke.js` 스모크 시나리오 |

검증 시나리오·알려진 이슈 요약은 [docs/FINAL-TEST-SCENARIOS.md](docs/FINAL-TEST-SCENARIOS.md), [docs/TEST-ISSUES-REPORT.md](docs/TEST-ISSUES-REPORT.md)를 참고합니다.

---

## 📁 프로젝트 구조

```
phs/
├── apps/
│   ├── web/                       # React SPA (Vite 6, Vitest, Zustand, Tailwind 4)
│   └── mobile/                    # Flutter 앱 (Riverpod, Dio)
├── package.json                   # npm workspaces + test:api / test:web / test:load:k6
├── scripts/
│   ├── run-gradlew.cjs            # npm에서 Gradle Wrapper 호출
│   └── load/
│       └── k6-api-smoke.js      # k6 부하 스모크
├── src/main/java/com/pch/mng/
│   ├── global/                    # 공통 인프라
│   │   ├── config/                #   Security, CORS, Redis, Swagger, Gson
│   │   ├── exception/             #   BusinessException, ErrorCode, Handler
│   │   ├── response/              #   ApiResponse<T>
│   │   ├── enums/                 #   공유 Enum (스프린트 미완료 이슈 처리 방식 등)
│   │   ├── filter/                #   MDC Logging Filter
│   │   └── aop/                   #   Logging, ExecutionTime
│   ├── user/                      # 사용자 도메인
│   ├── project/                   # 프로젝트 + 멤버 + 컴포넌트 + WIP
│   ├── issue/                     # 이슈 (핵심) + N:M 관계 테이블
│   ├── sprint/                    # 스프린트 lifecycle
│   ├── board/                     # 스프린트 보드 조회 (FR-008)
│   ├── jql/                       # PCH JQL 파서·검색·저장 필터 (FR-016, T-602·T-603)
│   ├── release/                   # 릴리즈 버전 관리
│   ├── comment/                   # 댓글
│   ├── attachment/                # 첨부파일
│   ├── workflow/                  # 워크플로우 전환 이력
│   ├── audit/                     # 감사 로그
│   ├── dashboard/                 # 대시보드 + 가젯
│   ├── label/                     # 레이블
│   ├── integration/github/        # GitHub OAuth·웹훅 (FR-033)
│   ├── automation/                # 자동화 규칙 엔진 (FR-015)
│   └── notification/              # 멘션 등 알림 (FR-024)
├── src/main/resources/
│   ├── application.yml            # 공통 설정
│   ├── application-dev.yml        # 개발 환경
│   └── application-prod.yml       # 운영 환경
├── docs/                          # 프로젝트 문서
│   ├── FINAL-TEST-SCENARIOS.md    #   최종 테스트 시나리오
│   ├── TEST-ISSUES-REPORT.md      #   검증 결과·이슈 보고
│   ├── PRD.md                     #   제품 요구사항
│   ├── PHASE.md                   #   Phase 로드맵
│   ├── TASKS.md                   #   Task 목록 (89개)
│   ├── WORKFLOW.md                #   개발 워크플로우
│   ├── REQUIREMENTS-v2.md         #   FR 상세·RTM (Phase 1)
│   ├── NFR-VERIFICATION.md        #   NFR 검증
│   ├── STORY-MAP.md               #   스토리 맵
│   ├── WIREFRAME-SPEC.md          #   화면 와이어 사양
│   ├── SPRINT-BACKLOG-DRAFT.md    #   스프린트 백로그 초안
│   ├── DOR-DOD.md                 #   DoR/DoD
│   ├── spikes/                    #   기술 스파이크 (JQL, 워크플로)
│   └── design/                    #   Phase 2 설계 (ERD, DDL, API, 인프라, CI/CD)
├── rules/                         # 개발 규칙
│   ├── human/                     #   사람용 상세 규칙 (7개)
│   └── ai/                        #   AI용 간결 규칙 (7개)
├── .ai/spring-conventions.json    # 코드 생성 컨벤션
├── build.gradle
└── README.md
```

---

## 📡 API 명세

> Swagger UI: `http://localhost:8080/swagger-ui.html`

### 인증 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/auth/register` | 회원가입 (JWT 없음) | - |
| `POST` | `/api/auth/login` | 로그인 (Access + Refresh JWT) | - |
| `POST` | `/api/auth/refresh` | 리프레시 토큰으로 재발급 (로테이션) | - |

이후 API는 헤더 `Authorization: Bearer {accessToken}` 필요.

### 사용자 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/users` | 사용자 목록 | ✅ |
| `GET` | `/api/v1/users/{id}` | 사용자 상세 | ✅ |
| `POST` | `/api/v1/users` | 회원가입 (레거시, `register`와 동일) | - |
| `PUT` | `/api/v1/users/{id}` | 사용자 수정 | ✅ |
| `DELETE` | `/api/v1/users/{id}` | 사용자 삭제 | ✅ |

### 프로젝트 API

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

### 이슈 API

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

### JQL (FR-016)

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/v1/projects/{projectId}/jql/search` | JQL 검색 (`jql`, `startAt`, `maxResults`; `app.jql.max-results-cap` 상한) | ✅ |
| `POST` | `/api/v1/projects/{projectId}/jql/filters` | 저장 필터 생성 (`name`, `jql`) | ✅ |
| `GET` | `/api/v1/projects/{projectId}/jql/filters` | 현재 사용자 저장 필터 목록 | ✅ |
| `DELETE` | `/api/v1/projects/{projectId}/jql/filters/{filterId}` | 저장 필터 삭제 (소유자만) | ✅ |

### 스프린트 API

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

### 릴리즈/댓글/대시보드/감사 API

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

---

## 🗃️ ERD

주요 엔티티 20개, N:M 중간 테이블 4개로 구성됩니다.

```
PROJECT ──1:N── PROJECT_MEMBER ──N:1── USER_ACCOUNT
   │                                       │
   ├──1:N── SPRINT                         │
   ├──1:N── RELEASE_VERSION                │
   ├──1:N── COMPONENT                      │
   └──1:N── WIP_LIMIT                     │
                                           │
ISSUE ──N:1── PROJECT          ISSUE ──N:1── USER_ACCOUNT (assignee/reporter)
  │                               │
  ├──1:N── COMMENT               ├──N:1── SPRINT
  ├──1:N── ATTACHMENT            ├──N:1── ISSUE (parent, 자기참조)
  ├──1:N── WORKFLOW_TRANSITION   │
  ├──1:N── AUDIT_LOG             ├──N:M── LABEL (via ISSUE_LABEL)
  ├──1:N── ISSUE_LINK            ├──N:M── COMPONENT (via ISSUE_COMPONENT)
  └──1:N── ISSUE_WATCHER         └──N:M── RELEASE_VERSION (via ISSUE_FIX_VERSION)

DASHBOARD ──N:1── USER_ACCOUNT
  └──1:N── DASHBOARD_GADGET
```

---

## 🤝 기여 가이드

### 브랜치 전략

```
master           ← 운영 배포 (직접 push 금지)
  └── develop    ← 개발 통합 (기본 PR 타깃)
       ├── feature/{주제}       ← 기능·품질 묶음 (예: feature/test-issues-remediation)
       ├── feat/T-{id}-{설명}   ← 기능 개발
       ├── fix/T-{id}-{설명}    ← 버그 수정
       └── docs/{설명}          ← 문서 작업
```

테스트 보고서·CORS·스프린트 완료 시 미완료 이슈 처리·Vitest·k6·모바일 오프라인 캐시 등은 **`feature/test-issues-remediation`** 등 기능 브랜치에서 작업한 뒤 `develop`으로 PR·머지합니다.

### 커밋 컨벤션

```
feat(issue): 이슈 CRUD API 구현
fix(auth):   토큰 갱신 실패 수정
docs:        PRD 문서 작성
refactor:    서비스 로직 분리
test:        회원가입 단위 테스트
chore:       build.gradle 의존성 업데이트
```

### PR 규칙

1. `develop` 브랜치로 PR 생성
2. 제목: `[T-{id}] {요약}` 형식
3. 리뷰어 1명 이상 승인 후 Squash Merge
4. CI (빌드 + 테스트) 통과 필수
5. 스프린트 단위 DoR/DoD는 [DOR-DOD.md](docs/DOR-DOD.md)를 본다 (Sprint 맥락은 [WORKFLOW.md](docs/WORKFLOW.md) §6)

### Cursor 에이전트 스킬

Cursor 등에서 Flutter·React 환경 작업 시 참고용 스킬이 `.cursor/skills/`에 있습니다 (`flutter-setup`, `react-setup`).

자세한 내용은 [WORKFLOW.md](docs/WORKFLOW.md) 참조

---

## 📚 프로젝트 문서

### 내부 문서

| 문서 | 설명 |
|------|------|
| [PRD.md](docs/PRD.md) | 제품 요구사항 (기능/비기능, 데이터 모델, API) |
| [PHASE.md](docs/PHASE.md) | Phase 0~8 개발 로드맵 |
| [TASKS.md](docs/TASKS.md) | 89개 세부 Task 목록 (Phase별 분류) |
| [WORKFLOW.md](docs/WORKFLOW.md) | Git, Sprint, CI/CD, 코드 리뷰 프로세스 |
| [REQUIREMENTS-v2.md](docs/REQUIREMENTS-v2.md) | FR-001~033·모바일 RTM (Phase 1) |
| [NFR-VERIFICATION.md](docs/NFR-VERIFICATION.md) | NFR-001~011 검증 보고 |
| [STORY-MAP.md](docs/STORY-MAP.md) | Epic·사용자 스토리 맵 |
| [WIREFRAME-SPEC.md](docs/WIREFRAME-SPEC.md) | 웹 14화면·모바일 흐름 사양 |
| [SPRINT-BACKLOG-DRAFT.md](docs/SPRINT-BACKLOG-DRAFT.md) | FR ↔ 개발 Phase 백로그 초안 |
| [DOR-DOD.md](docs/DOR-DOD.md) | Definition of Ready / Done |
| [E2E-LIFECYCLE-SCENARIOS.md](docs/E2E-LIFECYCLE-SCENARIOS.md) | 프로젝트 생성~관리 종료 E2E·수동 테스트 시나리오 |
| [FINAL-TEST-SCENARIOS.md](docs/FINAL-TEST-SCENARIOS.md) | 최종 테스트 시나리오(Part A~E) |
| [TEST-ISSUES-REPORT.md](docs/TEST-ISSUES-REPORT.md) | 검증 결과·이슈 목록 및 조치 요약 |
| [docs/spikes/](docs/spikes/) | JQL·워크플로 기술 스파이크 |
| [docs/design/](docs/design/) | Phase 2: ERD, DDL, API, 시퀀스, 인프라, CI/CD, UI 시스템 |
| [rules/human/](rules/human/) | 사람용 Spring 개발 규칙 (7개) |
| [rules/ai/](rules/ai/) | AI용 간결 개발 규칙 (7개) |

### 외부 참고 문서

본 프로젝트의 기획/설계 원본 문서는 별도 레포지토리에서 관리됩니다.

> [Project-Control-Hub/documents](https://github.com/Project-Control-Hub/documents)

| 문서 | 설명 |
|------|------|
| 00-스케줄 | 프로젝트 마일스톤 및 주차별 일정 (Gantt) |
| 01-프로젝트계획서 | 배경, 목표, 팀 구성, 기술 스택, 리스크 관리 |
| 02-ERD | 데이터베이스 ERD 및 테이블 명세 (20개 엔티티) |
| 03-아키텍처정의서 | 레이어드/물리 배포/데이터 흐름 아키텍처 |
| 04-API정의서 | REST API 엔드포인트 상세 스펙 (18개 도메인) |
| 05-화면흐름시퀀스 | 로그인, 이슈 생성, 워크플로우 전환 시퀀스 다이어그램 |
| 06-화면기능정의서 | 14개 화면 UI 요소, 기능 정의, 상태 처리 |
| 07-요구사항정의서 | 기능(FR-001~033) / 비기능(NFR-001~009) 요구사항 |
| 08-Git규칙정의서 | 브랜치 전략, 커밋 컨벤션, PR 규칙 |
| 09-스토리보드 | 화면별 사용자 시나리오 스토리보드 |
| 10-테스트전략서 | 단위/통합/E2E/성능/보안 테스트 전략 |
| 11-코드리뷰규칙 | 리뷰 체크리스트, 응답 규칙 |
| 12-배포가이드 | AWS 인프라, CI/CD, 모니터링 배포 절차 |

---

## 📄 라이선스

This project is licensed under the MIT License.
