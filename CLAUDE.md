# CLAUDE.md — LearnFlow AI

> AI 에이전트가 이 프로젝트에서 작업할 때 반드시 참고해야 하는 프로젝트 컨텍스트.  
> 이 문서에 없는 정보는 "존재하지 않는 것"으로 간주하라.

---

## 프로젝트 개요

LearnFlow AI는 LLM 기반 적응형 학습 관리 시스템(AI-LMS)이다.
- **백엔드**: Spring Boot 4 / Java 21+ (Virtual Threads)
- **웹**: React 18 + TypeScript (Zustand, TanStack Query, shadcn/ui, TipTap)
- **모바일**: Flutter 3.x + Dart 3.x (Riverpod)
- **AI/LLM**: Claude API (메인) + OpenAI GPT (Fallback) + text-embedding-3-small
- **Vector DB**: pgvector (Phase 1) → Qdrant (Phase 2)
- **인프라**: MySQL 8 · Redis 7 · Kafka · Elasticsearch 8 · MinIO · Zipkin · Prometheus + Grafana
- **빌드**: Gradle Kotlin DSL (백엔드), pnpm (웹)

---

## 아키텍처 핵심 원칙

### 1. AI Gateway 패턴 — 모든 AI 호출은 Gateway 경유
- 서비스 코드에서 **LLM API를 직접 호출하지 않는다**
- 모든 AI 요청은 `ai/gateway/` → PII Masking → 모델 라우팅 → LLM 호출 → PII Demasking → Output PII 스캔
- Circuit Breaker (Resilience4j): Claude 장애 시 OpenAI Fallback 자동 전환
- FinOps Guard: 예산 초과 시 모델 다운그레이드 또는 Kill-switch

### 2. Transactional Outbox — Kafka 직접 발행 금지
- 비즈니스 데이터 + `outbox_events` INSERT가 **단일 @Transactional** 안에서 원자적 실행
- Outbox Relay(Polling/ShedLock → Phase 2: Debezium CDC)가 Kafka로 발행
- **서비스 코드에서 KafkaTemplate 직접 호출 금지** — 반드시 `OutboxPublisher.publish()` 사용
- Consumer 멱등성 필수: dedup_key 체크 / Version OCC
- DLQ: Relay 5회 실패 → DEAD_LETTER, Consumer 3회 실패 → DLQ 토픽

### 3. PII 양방향 보호 — Input + Output 모두 스캔
- **Input**: Regex(한국 특화) → NER(Presidio+KoNLPy) → 토큰 치환(`<NAME_1>`)
- **Output**: Demasking 후 Output PII 스캔 → LLM이 새로 생성한 PII 감지 시 마스킹 + 감사 로그
- PII 매핑은 Redis session-scoped, 세션 종료 시 자동 삭제
- PII 처리 없이 LLM에 사용자 데이터를 보내는 코드 작성 금지

### 4. RAG — course_id 기반 격리
- 수강 중인 강의 콘텐츠 내에서만 검색 (다른 강의 데이터 절대 노출 금지)
- 파이프라인: Query Rewrite → Hybrid Search (pgvector + ES BM25, RRF 융합) → Re-ranking (CrossEncoder) → Context Compression → LLM 응답
- 임베딩은 비동기: ContentCreated/Updated 이벤트 → Embedding Worker (Outbox 경유)
- `chunk_hash`(SHA-256)로 동일 내용 재임베딩 스킵

### 5. AI 채점 — Confidence 기반 Human-in-the-Loop
- AI 채점 후 Confidence Score 계산 (rubric_match 0.3 + determinism 0.25 + consistency 0.25 + rubric_coverage 0.2)
- Confidence ≥ 0.8 → 자동 확정 (CONFIRMED)
- Confidence < 0.8 → 강사 Manual Review Queue 자동 이관
- 학습자 이의 제기(Appeal) → GradingAppeal 이벤트 → 강사 최종 확정
- **AI 채점을 Confidence 체크 없이 자동 확정하는 코드 금지**

---

## 디렉토리 구조

```
learnflow-api/src/main/java/com/learnflow/
├── global/               # 횡단 관심사
│   ├── config/           # Security, JPA, Redis, Kafka, Debezium, Zipkin, Resilience, Swagger
│   ├── security/         # JWT, Filter, UserDetails
│   ├── common/           # ApiResponse, PageResponse, BaseTimeEntity
│   ├── exception/        # GlobalHandler, ErrorCode, BusinessException
│   ├── event/
│   │   ├── outbox/       # OutboxEvent, OutboxPublisher — 여기만 Kafka 발행 담당
│   │   └── events/       # ContentCreated, QuizSubmitted, GradingAppeal, CostThreshold 등
│   ├── audit/            # AuditLog, AuditAspect (@Audited)
│   └── tracing/          # OTel TraceContextPropagator
│
├── domain/               # 핵심 도메인 (entity, repo, service, controller, dto)
│   ├── user/             # 사용자 + 프로필 + 학습 선호
│   ├── course/           # 강의 + 섹션 + 레슨 + 수강
│   ├── quiz/             # 퀴즈 + 문제 + 시도 + Appeal
│   ├── assignment/       # 과제 + 제출 + Appeal
│   └── community/        # 토론, Q&A
│
├── ai/                   # ★ AI 전체 — 이 패키지 안에서만 LLM 관련 코드 작성
│   ├── gateway/          # AiGatewayController, ModelRouter, PiiMaskingService,
│   │                     #   PiiDemaskingService, PiiOutputScanner, FinOpsGuard, AiRateLimiter
│   ├── tutor/            # AiTutorService, ChatSession, ShortTermMemory, LongTermMemory,
│   │                     #   LevelingService (3단계), SuggestedQuestion
│   ├── rag/              # RagOrchestrator, QueryRewrite, HybridSearch, Reranking,
│   │                     #   ContextCompression, SemanticChunking, ChunkVersioning
│   ├── evaluation/       # AiQuizGenerator, AiGrading, ConfidenceScorer, AppealService,
│   │                     #   ManualReviewQueue, OutputValidator
│   ├── quality/          # FeedbackService, AbTestService, PromptVersionService,
│   │                     #   RagasEvaluation, DeepEvalService, LlmJudge
│   ├── finops/           # CostTracking, KillSwitch, UnitEconomics, AnomalyDetection
│   ├── client/           # LlmClient(추상), ClaudeApiClient, OpenAiApiClient, CrossEncoderClient
│   ├── prompt/           # TemplateEngine, Registry, templates/ (level1~3, quiz, grading ...)
│   └── cache/            # EmbeddingCache, RagResultCache, SemanticResponseCache
│
├── worker/               # Kafka Consumer (EmbeddingWorker, AiGradingWorker, AnalyticsWorker, NotificationWorker)
├── onboarding/           # DiagnosticTest (Bloom's 배분), SelfAssessment
├── analytics/            # LearningAnalytics, ConceptMastery, WeaknessDetection

learnflow-web/src/         # React
├── components/            # shadcn/ui 기반 공통 컴포넌트
├── pages/                 # 강의, 학습, AI 튜터, 퀴즈, 분석, 관리자
├── hooks/                 # TanStack Query 커스텀 훅
├── stores/                # Zustand 스토어
└── lib/                   # API 클라이언트, SSE 핸들러, utils
```

---

## 빌드 & 테스트 명령어

```bash
# Backend
cd learnflow-api
./gradlew build                     # 전체 빌드
./gradlew test                      # 단위 + 통합 테스트
./gradlew bootRun                   # 로컬 실행

# Web
cd learnflow-web
pnpm install && pnpm dev            # 개발 서버
pnpm build                          # 프로덕션 빌드
pnpm test                           # Vitest

# Mobile
cd learnflow-mobile
flutter pub get && flutter run
flutter test

# 인프라
docker compose up -d                # MySQL, Redis, Kafka, pgvector, ES, MinIO, Zipkin, Prometheus, Grafana
docker compose down -v              # 완전 정리
```

---

## 코딩 컨벤션

### Java (Backend)
- **DTO는 Java record** 사용 (불변, 간결)
- **엔티티에 `@Setter` 금지** → 비즈니스 메서드로 상태 변경 (예: `submission.applyAiGrade(score, confidence)`)
- **공통 응답**: `ApiResponse<T>` 래핑 (`{ success, data, error }`)
- **예외**: `GlobalExceptionHandler`에서 일괄 처리, `ErrorCode` enum으로 관리
- **감사 로그**: AOP 기반 (`@Audited`) — before/after JSON 기록
- **DB 마이그레이션**: Flyway (`V{번호}__{설명}.sql`), 수동 DDL 금지
- **테스트**: `@WebMvcTest`(컨트롤러) + `@DataJpaTest`(리포지토리) + `@SpringBootTest`(통합)
- **AI 관련 코드는 `ai/` 패키지 밖에 작성하지 않는다** (LLM 호출, 프롬프트, 임베딩 등)

### TypeScript (React)
- **상태 관리**: 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- **폼**: React Hook Form + Zod 스키마 검증
- **컴포넌트**: shadcn/ui 기반, Tailwind 유틸리티 클래스
- **에디터**: TipTap (마크다운/리치텍스트)
- **AI 튜터 채팅**: SSE(Server-Sent Events) 스트리밍

---

## 이벤트 흐름 (에이전트가 코드를 쓸 때 반드시 따라야 하는 패턴)

```
[새 이벤트 추가 시 체크리스트]

1. 이벤트 클래스 정의: global/event/events/
2. 서비스에서 같은 @Transactional 안에서:
   a. 비즈니스 데이터 저장
   b. OutboxPublisher.publish(event, destinationTopic) → outbox_events INSERT
3. Outbox Relay가 Kafka로 발행 (자동)
4. Consumer 구현 (worker/ 패키지):
   - EmbeddingWorker: 콘텐츠 → 청킹 + 임베딩
   - AiGradingWorker: 퀴즈/과제 → AI 채점 + Confidence
   - AnalyticsWorker: 학습 완료 → 진도/취약점 갱신
   - NotificationWorker: 알림 발송
5. Consumer는 반드시 멱등성 보장 (dedup_key 체크)
6. DLQ 처리 로직 확인
```

```
[AI 기능 추가 시 체크리스트]

1. AI Gateway 경유 확인 (직접 LLM 호출 금지)
2. PII Masking 파이프라인 통과 확인 (Input + Output)
3. FinOps 비용 기록 (ai_cost_logs INSERT, cache_hit 여부)
4. Trace ID 전파 확인 (OTel)
5. 에러 시 Fallback 동작 확인 (Circuit Breaker)
6. Semantic Cache 적용 가능 여부 검토
```

---

## AI 튜터 프롬프트 구성 순서

```
System Prompt (역할 정의 + 레벨별 지시)
→ Long-term Memory (concept_mastery + 오답 패턴 + 학습 스타일)
→ RAG Context (압축된 강의 콘텐츠, course_id 격리)
→ Short-term Memory (최근 10턴 대화)
→ User Message

레벨 결정: concept_mastery 평균 기반
  Level 1 (< 0.4):  비유, 그림, 쉬운 설명
  Level 2 (0.4~0.7): 개념 + 코드 예시, 원리 + 실무 실수
  Level 3 (≥ 0.7):  내부 구현, 소스 코드, 트레이드오프
```

---

## LLM 라우팅 규칙

```
Tier 1 (Haiku):   짧은 Q&A, OX, 용어 정의           ~$0.001/요청
Tier 2 (Sonnet):  코드 설명, 디버깅, 리팩토링         ~$0.005/요청
Tier 3 (Sonnet):  개념 설명, 비교 분석, 원리           ~$0.010/요청
Tier 4 (Opus):    복잡한 설계, 아키텍처 질문           ~$0.050/요청

예산 기반 동적 라우팅:
  잔여 > 70%: 정상 (4분류)
  50~70%:     Opus 비활성
  30~50%:     Haiku 우선
  < 30%:      Haiku 전용 + 배치 중단

Unit Economics 목표:
  cost_per_tutor_session < $0.15
  cost_per_quiz_generation < $0.05
  cost_per_grading < $0.03
```

---

## 주요 Redis 키 패턴

```
session:chat:{sessionId}          → List (Short-term Memory, 최근 10턴, TTL 24h)
pii:mapping:{sessionId}           → Hash (PII 토큰 ↔ 원본 매핑, session-scoped)
cache:embedding:{hash}            → String (임베딩 캐시)
cache:rag:{queryEmbeddingHash}    → String (RAG 결과 캐시)
cache:semantic:{queryHash}        → String (Semantic Response 캐시, similarity > 0.95)
rate:ai:{userId}                  → Counter (Rate Limiting)
finops:daily_cost                 → String (당일 누적 비용)
```

---

## DB 스키마 요약 (핵심 테이블)

| 테이블 | 역할 | 핵심 컬럼 |
|--------|------|-----------|
| `courses / sections / lessons` | 강의 구조 | course → section → lesson 계층 |
| `enrollments` | 수강 | user_id, course_id, progress, status |
| `ai_chat_sessions / messages` | AI 튜터 | session_id, role(USER/ASSISTANT), model_used, feedback |
| `quiz_attempts` | 퀴즈 시도 | score, ai_feedback, answers(JSON) |
| `assignment_submissions` | 과제 제출 | **ai_confidence**, status(SUBMITTED→AI_GRADED→CONFIRMED/APPEALED/MANUAL_REVIEW) |
| `concept_mastery` | 개념별 숙련도 | mastery_score, **confidence**, source(DIAGNOSTIC/QUIZ/MANUAL) |
| `content_embeddings` | RAG 벡터 | embedding(VECTOR 1536), **chunk_hash**, status(ACTIVE/INACTIVE), version |
| `outbox_events` | Outbox | event_type, **destination_topic**, **dedup_key**(UNIQUE), status |
| `ai_cost_logs` | FinOps 비용 | service, model, tokens, cost_usd, **cache_hit** |
| `cost_thresholds` | Kill-switch | soft_limit, hard_limit, **is_killed** |
| `ragas_evaluations` | RAG 품질 | faithfulness, context_precision, **run_number** (3회 중앙값) |
| `prompt_versions` | 프롬프트 관리 | name, version, template, **is_active** |
| `diagnostic_results` | 온보딩 | diagnosed_level, concept_scores, **confidence_weight** |

---

## API 경로 규칙

```
인증:        /api/v1/auth/*                    → PUBLIC
사용자:      /api/v1/users/*                   → AUTHENTICATED
강의 (CRUD): /api/v1/courses/*, sections/*, lessons/* → PUBLIC(조회) / INSTRUCTOR(쓰기)
AI 튜터:     /api/v1/ai/chat/*                 → LEARNER (SSE 스트리밍)
퀴즈/과제:   /api/v1/quizzes/*, assignments/*  → LEARNER(제출) / INSTRUCTOR(출제)
학습 분석:   /api/v1/analytics/*               → LEARNER / INSTRUCTOR
온보딩:      /api/v1/onboarding/*              → LEARNER
AI 요약:     /api/v1/ai/summarize/*, flashcards/* → LEARNER
AI 품질:     /api/v1/admin/ai/quality/*        → ADMIN
FinOps:      /api/v1/admin/finops/*            → ADMIN
강사 검토:   /api/v1/instructor/review-queue/* → INSTRUCTOR
```

---

## Workflow 확인 필수 규칙

> **Phase/Week 단위 작업 진행 시 반드시 `docs/workflows/` 문서를 먼저 읽고 따라야 한다.**

```
[Task 착수 전 필수 절차]

1. 해당 Phase의 workflow 문서 읽기: docs/workflows/phase-{N}.md
2. 대상 Task의 서브 스텝, 완료 기준, 규칙 체크리스트 확인
3. TASKS.md에서 해당 Task 상태를 IN_PROGRESS로 변경
4. 서브 스텝 순서대로 구현 진행
5. 완료 기준 체크박스를 하나씩 체크 (phase-{N}.md 갱신)
6. 모든 완료 기준 충족 시 TASKS.md 상태를 DONE으로 변경
```

**위반 시**: workflow 문서를 읽지 않고 구현을 시작하면 누락/불일치 발생 위험.
에이전트는 **코드 작성 전에 반드시 workflow 문서를 참조**하고, 완료 후 체크박스를 갱신해야 한다.

---

## 주의사항 (에이전트가 흔히 실수하는 것)

1. **LLM API 직접 호출 금지** — 반드시 AI Gateway 경유 (PII + FinOps + Circuit Breaker 필수)
2. **KafkaTemplate 직접 호출 금지** — 반드시 `OutboxPublisher.publish()` 사용
3. **PII 없이 LLM 호출 금지** — 사용자 입력은 반드시 PII Masking 통과 후 LLM 전송
4. **course_id 격리 누락 금지** — RAG 검색 시 반드시 수강 강의 범위로 필터링
5. **AI 채점 자동 확정 금지** — Confidence < 0.8이면 반드시 Manual Review Queue로 이관
6. **AI 비용 미기록 금지** — 모든 LLM 호출은 `ai_cost_logs` 기록 (cache_hit 포함)
7. **chunk_hash 미적용 금지** — 콘텐츠 업데이트 시 동일 내용은 재임베딩 스킵
8. **Outbox 이벤트에 destination_topic 누락 금지** — 토픽 라우팅에 필수
9. **테스트 없는 PR 금지** — 최소 서비스 레이어 단위 테스트 포함
10. **Flyway 마이그레이션 파일 수정 금지** — 새 버전 파일만 추가
11. **System Prompt을 사용자에게 노출하는 코드 금지** — Layer 3 격리 원칙
12. **프롬프트 하드코딩 금지** — `ai/prompt/templates/`에서 관리, `prompt_versions` 테이블과 연동

---

## 보안 7 Layer (에이전트가 AI 코드 작성 시 검증 체크리스트)

```
Layer 1: 입력 필터링     — 길이 제한 + 위험 패턴 감지
Layer 2: PII Masking     — Input + Output 양방향 (Presidio + KoNLPy)
Layer 3: System Prompt   — 절대 노출 금지, 사용자 입력과 격리
Layer 4: Output Validation — 점수 범위 / JSON 스키마 검증
Layer 5: 데이터 격리     — course_id 기반 RAG 범위 제한
Layer 6: Tool 제한       — DB 직접 조회 / 외부 URL / 파일 접근 차단
Layer 7: FinOps Kill-switch — Soft/Hard 한도 + 자동 다운그레이드
```

---

## Docker Compose 서비스 (로컬 개발)

```
api (8080) · web (3000) · mysql (3306) · redis (6379)
kafka (9092) + zookeeper · debezium (8083)
pgvector (5433) · elasticsearch (9200)
minio (9000/9001) · zipkin (9411)
prometheus (9090) · grafana (3001)
```

환경변수: `CLAUDE_API_KEY`, `DB_ROOT_PASSWORD` → `.env` 파일 관리 (Git 미추적)

---

## 커밋 & PR 규칙

```
feat: AI 튜터 3단계 레벨링 구현
fix: PII Output 스캔 누락 수정
refactor: RAG 파이프라인 SemanticChunking 분리
test: AiGrading Confidence 계산 단위 테스트
docs: API 스펙 문서 갱신
perf: Semantic Cache 히트율 개선
chore: Gradle 의존성 업데이트
```

PR 제목: `[LF-{이슈번호}] {타입}: {설명}`  
PR에 반드시 포함: 변경 사유, 테스트 방법, AI 관련 변경 시 비용 영향 여부
