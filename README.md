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

## 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#️-기술-스택)
- [시작하기](#-시작하기)
- [환경변수 설정](#-환경변수-설정)
- [프로젝트 구조](#-프로젝트-구조)
- [더 보기 (부가 문서)](#-더-보기-부가-문서)

---

## 📌 프로젝트 소개

소프트웨어 개발팀의 애자일(Agile) 방법론을 지원하는 이슈 트래킹 및 프로젝트 관리 시스템입니다. 이슈 관리, 워크플로우 엔진, 스크럼/칸반 보드, 스프린트 관리, JQL 검색, 대시보드, REST API를 포함하는 통합 협업 플랫폼을 목표로 합니다.

| 항목 | 내용 |
|------|------|
| 개발 기간 | 2026-04-01 ~ 2026-07-31 (18주) |
| 팀 구성 | PM, 백엔드, 프론트엔드, QA, 디자이너 |
| 문서 | [PRD](docs/PRD.md) · [Phase 로드맵](docs/PHASE.md) · [Task 목록](docs/TASKS.md) · [문서 색인](docs/documents-index.md) |

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
- **감사 로그**: 전체 필드 변경 추적, CSV 또는 JSON으로 보내기

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

자세한 CORS 동작은 [docs/cors.md](docs/cors.md)를 참고하세요.

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
├── docs/                          # 프로젝트 문서 (PRD, 설계, 아래 부가 md)
│   ├── architecture.md            # 시스템 아키텍처 (README에서 분리)
│   ├── cors.md
│   ├── testing.md
│   ├── api-reference.md
│   ├── erd-overview.md
│   ├── contributing.md
│   ├── documents-index.md         # PRD·TASKS 등 색인 + 외부 문서 링크
│   ├── LICENSE-MIT.md
│   ├── PRD.md
│   ├── FINAL-TEST-SCENARIOS.md
│   ├── TEST-ISSUES-REPORT.md
│   ├── PHASE.md
│   ├── TASKS.md
│   ├── WORKFLOW.md
│   ├── REQUIREMENTS-v2.md
│   ├── NFR-VERIFICATION.md
│   ├── STORY-MAP.md
│   ├── WIREFRAME-SPEC.md
│   ├── SPRINT-BACKLOG-DRAFT.md
│   ├── DOR-DOD.md
│   ├── spikes/
│   └── design/
├── rules/
│   ├── human/
│   └── ai/
├── .ai/spring-conventions.json
├── build.gradle
└── README.md
```

---

## 📎 더 보기 (부가 문서)

| 문서 | 내용 |
|------|------|
| [docs/documents-index.md](docs/documents-index.md) | PRD·Phase·Task 등 **내부 문서 색인** 및 외부(documents 레포) 목록 |
| [docs/architecture.md](docs/architecture.md) | 운영 관점 시스템 아키텍처 다이어그램 |
| [docs/cors.md](docs/cors.md) | 브라우저 CORS 설정 요약 |
| [docs/testing.md](docs/testing.md) | 테스트 명령·품질 관련 링크 |
| [docs/api-reference.md](docs/api-reference.md) | REST API 엔드포인트 표 (Swagger 보조) |
| [docs/erd-overview.md](docs/erd-overview.md) | 엔티티 관계 요약 |
| [docs/contributing.md](docs/contributing.md) | 브랜치·커밋·PR·Cursor 스킬 |
| [docs/LICENSE-MIT.md](docs/LICENSE-MIT.md) | MIT 라이선스 전문 |

This project is licensed under the MIT License — [전문](docs/LICENSE-MIT.md).
