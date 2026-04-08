# LearnFlow AI

> LLM 기반 적응형 학습 관리 시스템 (AI-LMS)

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Flutter](https://img.shields.io/badge/Flutter-3.x-02569B?style=flat-square&logo=flutter&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-3.x-231F20?style=flat-square&logo=apachekafka&logoColor=white)
![Claude](https://img.shields.io/badge/Claude_API-Anthropic-191919?style=flat-square)

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [API 경로](#api-경로)
- [ERD](#erd)
- [프로젝트 문서](#프로젝트-문서)
- [기여 가이드](#기여-가이드)

---

## 프로젝트 소개

기존 LMS의 일방향 학습 구조를 넘어, AI/LLM 기술로 학습자 개개인에게 맞춤형 학습 경험을 제공하는 차세대 학습 관리 시스템입니다.

| 항목 | 내용 |
|------|------|
| 개발 기간 | 2026-04-07 ~ 2026-09-27 (24주) |
| Phase | 6개 Phase, 116개 Task |
| 역할 | 학습자 (Learner), 강사 (Instructor), 관리자 (Admin) |
| 문서 | [PRD](docs/PRD.md) · [Phase 로드맵](docs/PHASE.md) · [Task 목록](docs/TASKS.md) · [ERD](docs/erd/learnflow-erd.md) |

---

## 주요 기능

- **AI 튜터**: Claude API 기반 실시간 질의응답 (SSE 스트리밍), 학습자 수준별 3단계 레벨링, 이중 메모리 (Short-term Redis + Long-term MySQL)
- **RAG 파이프라인**: Semantic Chunking, Hybrid Search (pgvector + ES BM25, RRF 융합), CrossEncoder Re-ranking, Context Compression
- **AI 자동 평가**: AI 퀴즈 생성, AI 채점 + Confidence Score 기반 Human-in-the-Loop (>= 0.8 자동 확정, < 0.8 강사 검토)
- **적응형 학습**: 온보딩 진단 테스트, 개념별 숙련도 추적, 취약점 분석, AI 추천
- **강의 관리**: 3계층 구조 (Course > Section > Lesson), 텍스트/영상/첨부 레슨 타입, 수강 진도 추적
- **AI 품질 관리**: 3층 평가 (사용자 피드백 + RAGAS/DeepEval + 전문가 리뷰), A/B 테스트, 프롬프트 버전 관리
- **FinOps**: Unit Economics 추적, 동적 모델 라우팅 (Haiku/Sonnet/Opus), Semantic Cache, Kill-switch
- **PII 보호**: Input + Output 양방향 마스킹 (Presidio + KoNLPy), Redis session-scoped
- **이벤트 아키텍처**: Transactional Outbox + Kafka, Consumer 멱등성, DLQ
- **커뮤니티**: 토론, Q&A, 피어 리뷰

---

## 기술 스택

### Backend

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java (Virtual Threads) | 21+ |
| Framework | Spring Boot | 4.x |
| Security | Spring Security + JWT | jjwt 0.12.6 |
| ORM | Spring Data JPA + QueryDSL | 5.x |
| Database | MySQL | 8.x |
| Cache | Redis | 7.x |
| Messaging | Apache Kafka | 3.x |
| CDC | Debezium | 2.x |
| Search | Elasticsearch | 8.x |
| Storage | MinIO (S3 호환) | - |
| Resilience | Resilience4j | - |
| Tracing | Micrometer + OpenTelemetry + Zipkin | - |
| Migration | Flyway | - |
| API Docs | springdoc-openapi (Swagger) | 3.x |
| Build | Gradle (Kotlin DSL) | 9.x |

### AI / LLM

| 분류 | 기술 | 용도 |
|------|------|------|
| LLM (Primary) | Claude API (Anthropic) | 튜터, 퀴즈 생성, 채점 |
| LLM (Fallback) | OpenAI GPT API | Circuit Breaker Fallback |
| Embedding | text-embedding-3-small | 콘텐츠 벡터화 |
| Vector DB | pgvector (Phase 1) → Qdrant (Phase 2) | RAG 벡터 검색 |
| Re-ranking | CrossEncoder (ms-marco-MiniLM) | 검색 결과 재정렬 |
| RAG 평가 | RAGAS + DeepEval | 품질 자동 평가 |
| PII | Presidio + KoNLPy | 개인정보 마스킹 |

### Frontend

| 분류 | 기술 | 버전 |
|------|------|------|
| Web | React + TypeScript | 18.x / 5.x |
| 상태관리 | Zustand (클라이언트) + TanStack Query (서버) | - |
| UI | shadcn/ui + Tailwind CSS | - |
| 에디터 | TipTap | 2.x |
| 차트 | Recharts | 2.x |
| Mobile | Flutter + Dart (Riverpod) | 3.x / 3.x |
| Build | Vite / pnpm | - |

### Infra

| 분류 | 기술 | 포트 |
|------|------|------|
| API Server | Spring Boot | 8080 |
| Web Dev Server | React (Vite) | 3000 |
| MySQL | 메인 DB | 3306 |
| Redis | 캐시/세션 | 6379 |
| Kafka + Zookeeper | 메시징 | 9092 |
| Debezium | CDC | 8083 |
| pgvector | 벡터 DB | 5433 |
| Elasticsearch | 검색 | 9200 |
| MinIO | 파일 스토리지 | 9000/9001 |
| Zipkin | 분산 추적 | 9411 |
| Prometheus | 메트릭 | 9090 |
| Grafana | 대시보드 | 3001 |

---

## 시스템 아키텍처

```
[React SPA]           [Flutter Mobile]
     │                       │
     └───────┬───────────────┘
             ▼
    [Spring Boot API Server]
             │
     ┌───────┼──────────────────────────────┐
     │       │                              │
     ▼       ▼                              ▼
  [MySQL]  [Redis]                    [AI Gateway]
  Source    Cache/Session              PII Masking
  of Truth  PII Mapping               Model Router
     │       │                     FinOps Guard
     │       │                         │
     │       │              ┌──────────┼──────────┐
     │       │              ▼          ▼          ▼
     │       │         [Claude]   [OpenAI]   [CrossEncoder]
     │       │          Primary    Fallback    Re-ranking
     │       │
     ▼       ▼
  [Outbox] → [Kafka] → [Workers]
  Events     Relay      Embedding / Grading / Analytics / Notification
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
               [pgvector] [Elasticsearch] [MinIO]
                Vector     BM25 Search    Files
```

### 핵심 아키텍처 원칙

1. **AI Gateway 패턴** -- 모든 AI 호출은 Gateway 경유 (PII Masking + FinOps + Circuit Breaker)
2. **Transactional Outbox** -- Kafka 직접 발행 금지, OutboxPublisher.publish()로 원자적 이벤트 발행
3. **PII 양방향 보호** -- Input + Output 모두 스캔, Redis session-scoped 매핑
4. **RAG course_id 격리** -- 수강 강의 콘텐츠 내에서만 검색, 다른 강의 데이터 노출 금지
5. **Confidence 기반 HITL** -- AI 채점 Confidence >= 0.8 자동 확정, < 0.8 강사 검토

---

## 시작하기

### 사전 요구사항

- Java 21+, Docker & Docker Compose, pnpm 8+, Flutter 3.x

### 빠른 시작

```bash
# 1. 클론
git clone https://github.com/Project-Control-Hub/phs.git
cd phs

# 2. 환경변수 설정
cp .env.example .env
# .env 파일에 DB, JWT, AI API Key 설정

# 3. 인프라 서비스 실행
docker compose up -d

# 4. Backend 실행
cd learnflow-api
./gradlew bootRun

# 5. Web 실행
cd learnflow-web
pnpm install && pnpm dev
```

> 상세한 환경 구성 방법은 [SETUP.md](docs/SETUP.md) 참조

---

## 프로젝트 구조

```
learnflow-api/src/main/java/com/learnflow/
├── global/               # 횡단 관심사
│   ├── config/           # Security, JPA, Redis, Kafka, Swagger, Resilience
│   ├── security/         # JWT, Filter, UserDetails
│   ├── common/           # ApiResponse, PageResponse, BaseTimeEntity
│   ├── exception/        # GlobalHandler, ErrorCode, BusinessException
│   ├── event/
│   │   ├── outbox/       # OutboxEvent, OutboxPublisher
│   │   └── events/       # 도메인 이벤트 클래스
│   ├── audit/            # AuditLog, AuditAspect
│   └── tracing/          # OTel TraceContextPropagator
│
├── domain/               # 핵심 도메인
│   ├── user/             # 사용자 + 프로필 + 학습 선호
│   ├── course/           # 강의 + 섹션 + 레슨 + 수강
│   ├── quiz/             # 퀴즈 + 문제 + 시도 + Appeal
│   ├── assignment/       # 과제 + 제출 + Appeal
│   └── community/        # 토론, Q&A
│
├── ai/                   # AI 전체 (이 패키지 안에서만 LLM 코드 작성)
│   ├── gateway/          # AI Gateway, Model Router, PII, FinOps Guard
│   ├── tutor/            # AI 튜터, 레벨링, 메모리
│   ├── rag/              # RAG 파이프라인
│   ├── evaluation/       # AI 퀴즈 생성, 채점, Confidence
│   ├── quality/          # 피드백, A/B 테스트, 프롬프트 버전
│   ├── finops/           # 비용 추적, Kill-switch
│   ├── client/           # LLM API 클라이언트
│   ├── prompt/           # 프롬프트 템플릿 엔진
│   └── cache/            # Embedding, RAG, Semantic 캐시
│
├── worker/               # Kafka Consumer
├── onboarding/           # 진단 테스트
└── analytics/            # 학습 분석

learnflow-web/src/         # React
├── components/            # shadcn/ui 기반 공통 컴포넌트
├── pages/                 # 강의, 학습, AI 튜터, 퀴즈, 분석, 관리자
├── hooks/                 # TanStack Query 커스텀 훅
├── stores/                # Zustand 스토어
└── lib/                   # API 클라이언트, SSE 핸들러, utils
```

---

## API 경로

> Swagger UI: http://localhost:8080/swagger-ui.html

```
인증:        /api/v1/auth/*                    → PUBLIC
사용자:      /api/v1/users/*                   → AUTHENTICATED
강의:        /api/v1/courses/*                 → PUBLIC(조회) / INSTRUCTOR(쓰기)
             /api/v1/courses/{id}/sections/*
             /api/v1/courses/{id}/sections/{id}/lessons/*
AI 튜터:     /api/v1/ai/chat/*                 → LEARNER (SSE 스트리밍)
퀴즈:        /api/v1/quizzes/*                 → LEARNER(제출) / INSTRUCTOR(출제)
과제:        /api/v1/assignments/*             → LEARNER(제출) / INSTRUCTOR(출제)
학습 분석:   /api/v1/analytics/*               → LEARNER / INSTRUCTOR
온보딩:      /api/v1/onboarding/*              → LEARNER
AI 품질:     /api/v1/admin/ai/quality/*        → ADMIN
FinOps:      /api/v1/admin/finops/*            → ADMIN
강사 검토:   /api/v1/instructor/review-queue/* → INSTRUCTOR
```

---

## ERD

주요 테이블 20+개로 구성됩니다. 상세 ERD는 [learnflow-erd.md](docs/erd/learnflow-erd.md) 참조.

```
users ──1:N── courses (instructs)
users ──1:N── enrollments ──N:1── courses
courses ──1:N── sections ──1:N── lessons
users ──1:N── ai_chat_sessions ──1:N── ai_chat_messages
courses ──1:N── quizzes ──1:N── quiz_questions
quizzes ──1:N── quiz_attempts ──N:1── users
assignments ──1:N── assignment_submissions ──N:1── users
users ──1:N── concept_mastery ──N:1── courses
lessons ──1:N── content_embeddings (RAG vector)
outbox_events (Transactional Outbox → Kafka)
ai_cost_logs / cost_thresholds (FinOps)
prompt_versions / ragas_evaluations (AI 품질)
posts ──1:N── comments (커뮤니티)
```

---

## 프로젝트 문서

| 문서 | 설명 |
|------|------|
| [PRD.md](docs/PRD.md) | 제품 요구사항 (76 FR + 27 NFR) |
| [PHASE.md](docs/PHASE.md) | 6 Phase 24주 로드맵 |
| [TASKS.md](docs/TASKS.md) | 116개 세부 Task 목록 |
| [SETUP.md](docs/SETUP.md) | 개발 환경 구성 가이드 |
| [ERD](docs/erd/learnflow-erd.md) | 데이터베이스 ERD (Mermaid) |
| [WORKFLOW.md](docs/WORKFLOW.md) | 개발 워크플로우 |
| [workflows/](docs/workflows/) | Phase별 세부 워크플로우 |
| [rules/human/](rules/human/) | 개발 규칙 (상세, 7개) |
| [rules/ai/](rules/ai/) | 개발 규칙 (AI 최적화, 7개) |

---

## 기여 가이드

### 브랜치 전략

```
master           ← 운영 배포 (직접 push 금지)
  └── develop    ← 개발 통합
       ├── feature/T-{id}-{설명}   ← 기능 개발
       ├── fix/T-{id}-{설명}       ← 버그 수정
       └── docs/{설명}             ← 문서 작업
```

### 커밋 컨벤션

```
feat(course): 강의 CRUD API 구현
fix(auth): 토큰 갱신 실패 수정
refactor(rag): SemanticChunking 분리
test(grading): Confidence 계산 단위 테스트
docs: API 스펙 문서 갱신
perf: Semantic Cache 히트율 개선
chore: Gradle 의존성 업데이트
```

### PR 규칙

1. PR 제목: `[LF-{이슈번호}] {타입}: {설명}`
2. 변경 사유, 테스트 방법, AI 관련 변경 시 비용 영향 여부 포함
3. 최소 서비스 레이어 단위 테스트 포함
4. CI (빌드 + 테스트) 통과 필수

자세한 내용은 [WORKFLOW.md](docs/WORKFLOW.md) 참조

---

## 라이선스

This project is licensed under the MIT License.
