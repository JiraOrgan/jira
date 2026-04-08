# 1. Architecture RULE -- 레이어 아키텍처 및 패키지 구조 [MUST]

> 이 프로젝트는 도메인별 flat 패키지 구조를 사용한다.
> **기준**: Spring Boot 4.0.3 / Java 21

### 1.1 패키지 구조 [MUST]

#### 1.1.1 도메인별 패키지 원칙

도메인 단위로 패키지를 구성한다. 기술 레이어(controller/, service/, repository/)가 아닌 비즈니스 도메인으로 분류한다.

```text
com.learnflow/
├── global/               # 횡단 관심사
│   ├── config/           # Security, JPA, Redis, Kafka, Swagger, Resilience
│   ├── security/         # JWT, Filter, UserDetails
│   ├── common/           # ApiResponse, PageResponse, BaseTimeEntity
│   ├── exception/        # GlobalHandler, ErrorCode, BusinessException
│   ├── event/
│   │   ├── outbox/       # OutboxEvent, OutboxPublisher — Kafka 발행 담당
│   │   └── events/       # ContentCreated, QuizSubmitted, GradingAppeal 등
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
│   ├── gateway/          # AiGatewayController, ModelRouter, PiiMaskingService
│   ├── tutor/            # AiTutorService, ChatSession, Memory, Leveling
│   ├── rag/              # RagOrchestrator, HybridSearch, Reranking
│   ├── evaluation/       # AiQuizGenerator, AiGrading, ConfidenceScorer
│   ├── quality/          # FeedbackService, AbTestService, PromptVersionService
│   ├── finops/           # CostTracking, KillSwitch, UnitEconomics
│   ├── client/           # LlmClient, ClaudeApiClient, OpenAiApiClient
│   ├── prompt/           # TemplateEngine, Registry, templates/
│   └── cache/            # EmbeddingCache, RagResultCache, SemanticResponseCache
│
├── worker/               # Kafka Consumer (Embedding, AiGrading, Analytics, Notification)
├── onboarding/           # DiagnosticTest, SelfAssessment
└── analytics/            # LearningAnalytics, ConceptMastery, WeaknessDetection
```

```java
// Good -- 도메인별 패키지
com.learnflow.domain.course.Course
com.learnflow.domain.course.CourseService
com.learnflow.domain.course.CourseApiController

// Bad -- 기술 레이어별 패키지
com.learnflow.entity.Course
com.learnflow.service.CourseService
com.learnflow.controller.CourseApiController
```

#### 1.1.2 AI 코드 격리 원칙

- **LLM 호출, 프롬프트, 임베딩 관련 코드는 반드시 `ai/` 패키지 안에 작성**한다
- domain/ 패키지에서 AI 기능이 필요하면 이벤트(Outbox) 또는 인터페이스를 통해 `ai/` 패키지와 연동한다

#### 1.1.3 금지

- 기술 레이어 기준 패키지 분류 금지 (entity/, service/, controller/ 등)
- 도메인 간 순환 참조 금지
- global 패키지에 비즈니스 로직 배치 금지
- `ai/` 패키지 밖에서 LLM API 호출 금지

### 1.2 레이어 책임 분리 [MUST]

| 레이어 | 클래스 패턴 | 책임 | 금지 사항 |
|--------|-----------|------|----------|
| Controller | {Domain}ApiController | 요청 수신, 입력 검증, 응답 직렬화 | 비즈니스 로직 금지 |
| Service | {Domain}Service | 트랜잭션, 도메인 로직, DTO 변환, Outbox 발행 | HTTP 객체 사용 금지, LLM 직접 호출 금지 |
| Repository | {Domain}Repository | DB 접근, 쿼리 | 비즈니스 로직 금지 |
| Entity | {Domain} | 데이터 모델, 비즈니스 메서드로 상태 변경 | @Setter 금지, Controller에 직접 노출 금지 |
| DTO | {Domain}Request / {Domain}Response | 입출력 데이터 전달 (Java record) | Entity 상속/포함 금지 |

### 1.3 의존 방향 [MUST]

```text
Controller → Service → Repository → Entity
    ↓            ↓
  Request      Response
  (record)     (record)
                 ↓
         OutboxPublisher → outbox_events (Kafka 직접 발행 금지)
```

- 상위 레이어만 하위 레이어를 참조한다
- Repository가 Service를 참조하거나 Controller가 Repository를 직접 참조하면 안 된다
- 예외: 조회 전용 API(AuditLog 등)에서 Controller가 Repository를 직접 사용 가능 [SHOULD]
- **모든 AI 호출은 AI Gateway 경유** — Service에서 LLM API를 직접 호출하지 않는다
- **Kafka 발행은 OutboxPublisher.publish() 경유** — KafkaTemplate 직접 호출 금지

---

## 참고 문서

- [02-entity-jpa.md](02-entity-jpa.md) -- Entity 설계 규칙
- [05-service-layer.md](05-service-layer.md) -- Service 레이어 상세
