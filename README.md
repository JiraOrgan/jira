# Jira 프로젝트 관리 시스템

> 애자일 팀을 위한 이슈 트래킹, 스프린트 관리, 워크플로우 자동화 통합 협업 플랫폼

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Flutter](https://img.shields.io/badge/Flutter-3.41-02569B?style=flat-square&logo=flutter&logoColor=white)
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
- **스프린트 관리**: 생성/시작/완료 라이프사이클, 백로그 우선순위 관리, 스토리 포인트 산정
- **대시보드**: 역할별 가젯 구성 (번다운/속도/CFD 차트), 커스터마이징 가능
- **JQL 검색**: Jira Query Language 검색 엔진, 자동완성, 필터 저장
- **릴리즈 관리**: Fix Version, 릴리즈 노트 자동 생성, Semantic Versioning
- **RBAC 권한**: Admin/Developer/QA/Reporter/Viewer 5단계 역할 기반 접근 제어
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
| Framework | React | 18.x |
| 상태관리 | Zustand | 최신 |
| 스타일링 | Tailwind CSS | 최신 |
| 빌드 | Vite | 최신 |
| 차트 | Recharts | 최신 |

### Frontend (Mobile)

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | Flutter | 3.41.x |
| 언어 | Dart | 3.11.x |
| 상태관리 | Riverpod | 최신 |
| HTTP | Dio | 최신 |

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
- Gradle 9.x

### 백엔드 실행

```bash
# 1. 레포지토리 클론
git clone https://github.com/JiraOrgan/spring-react-flutter-jira-mng.git
cd spring-react-flutter-jira-mng

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
```

---

## 📁 프로젝트 구조

```
spring-react-flutter-jira-mng/
├── src/main/java/com/jira/mng/
│   ├── global/                    # 공통 인프라
│   │   ├── config/                #   Security, Redis, Swagger, Gson
│   │   ├── exception/             #   BusinessException, ErrorCode, Handler
│   │   ├── response/              #   ApiResponse<T>
│   │   ├── enums/                 #   9개 공유 Enum
│   │   ├── filter/                #   MDC Logging Filter
│   │   └── aop/                   #   Logging, ExecutionTime
│   ├── user/                      # 사용자 도메인
│   ├── project/                   # 프로젝트 + 멤버 + 컴포넌트 + WIP
│   ├── issue/                     # 이슈 (핵심) + N:M 관계 테이블
│   ├── sprint/                    # 스프린트 lifecycle
│   ├── release/                   # 릴리즈 버전 관리
│   ├── comment/                   # 댓글
│   ├── attachment/                # 첨부파일
│   ├── workflow/                  # 워크플로우 전환 이력
│   ├── audit/                     # 감사 로그
│   ├── dashboard/                 # 대시보드 + 가젯
│   └── label/                     # 레이블
├── src/main/resources/
│   ├── application.yml            # 공통 설정
│   ├── application-dev.yml        # 개발 환경
│   └── application-prod.yml       # 운영 환경
├── docs/                          # 프로젝트 문서
│   ├── PRD.md                     #   제품 요구사항
│   ├── PHASE.md                   #   Phase 로드맵
│   ├── TASKS.md                   #   Task 목록 (89개)
│   └── WORKFLOW.md                #   개발 워크플로우
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
| `POST` | `/auth/login` | 로그인 (JWT 발급) | - |
| `POST` | `/auth/refresh` | 토큰 갱신 | - |

### 사용자 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/users` | 사용자 목록 | ✅ |
| `GET` | `/api/v1/users/{id}` | 사용자 상세 | ✅ |
| `POST` | `/api/v1/users` | 회원가입 | - |
| `PUT` | `/api/v1/users/{id}` | 사용자 수정 | ✅ |
| `DELETE` | `/api/v1/users/{id}` | 사용자 삭제 | ✅ |

### 프로젝트 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/projects` | 프로젝트 목록 | ✅ |
| `POST` | `/api/v1/projects` | 프로젝트 생성 | ✅ |
| `GET` | `/api/v1/projects/{id}` | 프로젝트 상세 | ✅ |
| `PUT` | `/api/v1/projects/{id}` | 프로젝트 수정 | ✅ |
| `DELETE` | `/api/v1/projects/{id}` | 프로젝트 삭제 | ✅ |
| `GET` | `/api/v1/projects/{id}/members` | 멤버 목록 | ✅ |
| `POST` | `/api/v1/projects/{id}/members` | 멤버 추가 | ✅ |
| `DELETE` | `/api/v1/projects/{id}/members/{memberId}` | 멤버 제거 | ✅ |

### 이슈 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/issues/project/{projectId}` | 이슈 목록 (페이징) | ✅ |
| `GET` | `/api/v1/issues/project/{projectId}/backlog` | 백로그 | ✅ |
| `GET` | `/api/v1/issues/{issueKey}` | 이슈 상세 | ✅ |
| `POST` | `/api/v1/issues` | 이슈 생성 | ✅ |
| `PUT` | `/api/v1/issues/{issueKey}` | 이슈 수정 | ✅ |
| `DELETE` | `/api/v1/issues/{issueKey}` | 이슈 삭제 | ✅ |
| `POST` | `/api/v1/issues/{issueKey}/transitions` | 상태 전환 | ✅ |

### 스프린트 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/v1/sprints/project/{projectId}` | 스프린트 목록 | ✅ |
| `GET` | `/api/v1/sprints/{id}` | 스프린트 상세 | ✅ |
| `POST` | `/api/v1/sprints` | 스프린트 생성 | ✅ |
| `POST` | `/api/v1/sprints/{id}/start` | 스프린트 시작 | ✅ |
| `POST` | `/api/v1/sprints/{id}/complete` | 스프린트 완료 | ✅ |

### 릴리즈/댓글/대시보드/감사 API

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/v1/versions` | 버전 생성 | ✅ |
| `POST` | `/api/v1/versions/{id}/release` | 릴리즈 | ✅ |
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
master        ← 운영 배포 (직접 push 금지)
  └── dev     ← 개발 통합
       ├── feat/T-{id}-{설명}   ← 기능 개발
       ├── fix/T-{id}-{설명}    ← 버그 수정
       └── docs/{설명}          ← 문서 작업
```

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

1. `dev` 브랜치로 PR 생성
2. 제목: `[T-{id}] {요약}` 형식
3. 리뷰어 1명 이상 승인 후 Squash Merge
4. CI (빌드 + 테스트) 통과 필수

자세한 내용은 [WORKFLOW.md](docs/WORKFLOW.md) 참조

---

## 📚 프로젝트 문서

| 문서 | 설명 |
|------|------|
| [PRD.md](docs/PRD.md) | 제품 요구사항 (기능/비기능, 데이터 모델, API) |
| [PHASE.md](docs/PHASE.md) | Phase 0~8 개발 로드맵 |
| [TASKS.md](docs/TASKS.md) | 89개 세부 Task 목록 (Phase별 분류) |
| [WORKFLOW.md](docs/WORKFLOW.md) | Git, Sprint, CI/CD, 코드 리뷰 프로세스 |
| [rules/human/](rules/human/) | 사람용 Spring 개발 규칙 (7개) |
| [rules/ai/](rules/ai/) | AI용 간결 개발 규칙 (7개) |

---

## 📄 라이선스

This project is licensed under the MIT License.
