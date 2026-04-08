# Architecture Rules

## Package Structure
- 베이스 패키지: `com.learnflow/`
- 공통 인프라: `com.learnflow.global/` (config, security, common, exception, event/outbox, audit, tracing)
- 도메인: `com.learnflow.domain/{domain}/` (user, course, quiz, assignment, community)
- AI 전용: `com.learnflow.ai/` (gateway, tutor, rag, evaluation, quality, finops, client, prompt, cache)
- 워커: `com.learnflow.worker/` (EmbeddingWorker, AiGradingWorker, AnalyticsWorker, NotificationWorker)
- 온보딩: `com.learnflow.onboarding/`
- 분석: `com.learnflow.analytics/`
- **AI 관련 코드(LLM 호출, 프롬프트, 임베딩)는 `ai/` 패키지 밖에 작성 금지**

## File Naming per Domain
```
{Domain}.java              -- Entity (extends BaseTimeEntity)
{Domain}Repository.java    -- Repository (Spring Data JPA interface)
{Domain}Request.java       -- Request DTOs (Java record)
{Domain}Response.java      -- Response DTOs (Java record)
{Domain}Service.java       -- Service
{Domain}ApiController.java -- REST Controller
```

## Layer Dependencies
```
Controller → Service → Repository → Entity
                ↓
        OutboxPublisher → outbox_events (Kafka 직접 발행 금지)
```
- 상위 레이어만 하위 참조
- Controller → Repository 직접 참조 금지 (조회 전용 API 예외 가능)
- 순환 참조 금지
- **모든 AI 호출은 AI Gateway 경유** (직접 LLM API 호출 금지)
- **Kafka 발행은 OutboxPublisher.publish() 경유** (KafkaTemplate 직접 호출 금지)

## Layer Responsibilities
| Layer | Does | Does NOT |
|-------|------|----------|
| Controller | 요청 수신, @Valid 검증, ApiResponse 래핑 | 비즈니스 로직 |
| Service | 트랜잭션, 도메인 로직, DTO 변환, OutboxPublisher 호출 | HTTP 객체 사용, LLM 직접 호출 |
| Repository | DB 접근, JPQL/QueryDSL | 비즈니스 로직 |
| Entity | 데이터 모델, 비즈니스 메서드로 상태 변경 | @Setter 사용, 직접 API 노출 |
