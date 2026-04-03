# Phase 3 — 이벤트 인프라 + AI 기반 (Week 9~12) Task Workflows

> **기간**: 2026-06-02 ~ 06-29
> **마일스톤**: M3 — 인프라
> **Task 수**: 20개 (T-301 ~ T-320)
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [README.md](README.md)

---

## Week 9: Outbox + Kafka + Consumer

---

### T-301: OutboxPublisher 구현 + outbox_events 테이블

> **담당**: Backend | **선행**: T-102 | **관련 FR**: FR-EVENT-01

#### 서브 스텝

1. **Flyway 마이그레이션 — outbox_events 테이블 생성**
   - 파일: `learnflow-api/src/main/resources/db/migration/V3_001__create_outbox_events.sql`
   - 컬럼: `id`(BIGINT PK), `event_type`(VARCHAR 100), `destination_topic`(VARCHAR 200), `payload`(TEXT/JSON), `dedup_key`(VARCHAR 255 UNIQUE), `status`(ENUM: PENDING, SENT, DEAD_LETTER), `retry_count`(INT DEFAULT 0), `created_at`, `sent_at`
   - `dedup_key`에 UNIQUE INDEX, `status + created_at`에 복합 INDEX (Relay 폴링용)

2. **OutboxEvent 엔티티 구현**
   - 파일: `global/event/outbox/OutboxEvent.java`
   - `@Entity`, `@Table(name = "outbox_events")`, `@Setter` 금지
   - 상태 변경 비즈니스 메서드: `markAsSent()`, `incrementRetry()`, `markAsDeadLetter()`
   - `status` 필드는 `OutboxStatus` enum (PENDING, SENT, DEAD_LETTER)

3. **OutboxPublisher 서비스 구현**
   - 파일: `global/event/outbox/OutboxPublisher.java`
   - `publish(Object event, String destinationTopic)` 메서드: 이벤트 객체를 JSON 직렬화 → `outbox_events` INSERT
   - `dedup_key` 생성 전략: `{eventType}:{entityId}:{timestamp}` 또는 UUID
   - 이 메서드는 호출 측의 `@Transactional` 안에서 실행되어야 함 (별도 트랜잭션 시작 금지)

4. **OutboxEventRepository 구현**
   - 파일: `global/event/outbox/OutboxEventRepository.java`
   - `findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before)` — Relay 폴링용
   - `findByDedupKey(String dedupKey)` — 중복 체크용

5. **이벤트 클래스 기반 인터페이스 정의**
   - 파일: `global/event/events/DomainEvent.java`
   - 모든 도메인 이벤트가 구현해야 할 마커 인터페이스: `getEventType()`, `getAggregateId()`, `getOccurredAt()`

#### 완료 기준

- [ ] Flyway 마이그레이션 실행 후 `outbox_events` 테이블 정상 생성
- [ ] `OutboxPublisher.publish()` 호출 시 `outbox_events`에 PENDING 상태로 INSERT 확인
- [ ] `dedup_key` UNIQUE 제약 조건으로 중복 이벤트 방지 확인
- [ ] 비즈니스 데이터 + outbox INSERT가 단일 트랜잭션에서 원자적 실행 확인

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지
- [ ] `destination_topic` 누락 없음

---

### T-302: Outbox Relay (Polling + ShedLock)

> **담당**: Backend | **선행**: T-301 | **관련 FR**: FR-EVENT-02

#### 서브 스텝

1. **ShedLock 의존성 + 설정**
   - 파일: `learnflow-api/build.gradle.kts` — `net.javacrumbs.shedlock:shedlock-spring`, `shedlock-provider-jdbc-template` 추가
   - 파일: `global/config/ShedLockConfig.java` — `@EnableSchedulerLock`, `LockProvider` Bean (JDBC 기반)
   - Flyway: `V3_002__create_shedlock.sql` — `shedlock` 테이블 생성

2. **OutboxRelay 스케줄러 구현**
   - 파일: `global/event/outbox/OutboxRelay.java`
   - `@Scheduled(fixedDelay = 1000)` + `@SchedulerLock(name = "outbox-relay", lockAtMostFor = "30s")`
   - 로직: PENDING 상태 이벤트 조회 (배치 크기 100) → KafkaTemplate으로 `destination_topic`에 발행 → 성공 시 `markAsSent()` → 실패 시 `incrementRetry()`
   - **주의**: OutboxRelay만 KafkaTemplate 직접 사용 허용 (유일한 예외)

3. **재시도 + DEAD_LETTER 처리**
   - `retry_count >= 5` → `markAsDeadLetter()` → 알림 로그 기록
   - Relay 실패 시에도 다른 이벤트 처리 계속 (개별 try-catch)

4. **Kafka 설정**
   - 파일: `global/config/KafkaConfig.java`
   - Producer 설정: `acks=all`, `retries=3`, `enable.idempotence=true`
   - 토픽 자동 생성 또는 토픽 Bean 등록 (content, quiz, assignment, analytics, notification, dlq)

#### 완료 기준

- [ ] PENDING 상태 이벤트가 1초 간격으로 Kafka에 발행되는지 확인
- [ ] ShedLock으로 다중 인스턴스 환경에서 Relay 중복 실행 방지 확인
- [ ] retry_count 5회 초과 시 DEAD_LETTER 상태 전환 확인
- [ ] Kafka Producer `acks=all` + idempotence 설정 확인

#### 규칙 체크리스트

- [ ] 서비스 코드에서 KafkaTemplate 직접 호출 금지 (OutboxRelay만 예외)
- [ ] `destination_topic` 기반 토픽 라우팅
- [ ] Flyway 마이그레이션 파일 추가 (ShedLock 테이블)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-303: Kafka Consumer 프레임워크 + dedup_key 멱등성

> **담당**: Backend | **선행**: T-302 | **관련 FR**: FR-EVENT-03

#### 서브 스텝

1. **Consumer 추상 베이스 클래스 구현**
   - 파일: `worker/AbstractIdempotentConsumer.java`
   - Template Method 패턴: `consume(ConsumerRecord)` → `isDuplicate(dedupKey)` → `processEvent(payload)` → `markProcessed(dedupKey)`
   - `@KafkaListener` 공통 설정: `groupId`, `concurrency`, `containerFactory`

2. **Flyway 마이그레이션 — consumed_events 테이블**
   - 파일: `V3_003__create_consumed_events.sql`
   - 컬럼: `id`(BIGINT PK), `dedup_key`(VARCHAR 255 UNIQUE), `consumer_group`(VARCHAR 100), `processed_at`(TIMESTAMP)
   - `dedup_key + consumer_group` 복합 UNIQUE INDEX

3. **멱등성 체크 서비스**
   - 파일: `worker/IdempotencyService.java`
   - `isDuplicate(String dedupKey, String consumerGroup)`: consumed_events 조회
   - `markProcessed(String dedupKey, String consumerGroup)`: consumed_events INSERT
   - 동시성 대비: INSERT 시 `ON DUPLICATE KEY` 또는 try-catch `DuplicateKeyException`

4. **Kafka Consumer 공통 설정**
   - 파일: `global/config/KafkaConsumerConfig.java`
   - `ConcurrentKafkaListenerContainerFactory` Bean: `enable.auto.commit=false`, `AckMode.MANUAL_IMMEDIATE`
   - 에러 핸들러: `DefaultErrorHandler` + `FixedBackOff(1000, 3)` → 3회 실패 시 DLQ 토픽 전송
   - JSON Deserializer 설정 (`trusted.packages`)

#### 완료 기준

- [ ] 동일 `dedup_key` 메시지 재처리 시 스킵 확인 (멱등성)
- [ ] `consumed_events` 테이블에 처리된 이벤트 기록 확인
- [ ] Consumer 에러 시 3회 재시도 후 DLQ 전송 확인
- [ ] Manual Acknowledge 모드 동작 확인

#### 규칙 체크리스트

- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] Flyway 마이그레이션 파일 추가
- [ ] 엔티티에 `@Setter` 금지

---

### T-304: DLQ 처리 (Outbox DEAD_LETTER + Consumer DLQ 토픽)

> **담당**: Backend | **선행**: T-303 | **관련 FR**: FR-EVENT-04

#### 서브 스텝

1. **Outbox DEAD_LETTER 모니터링 API**
   - 파일: `global/event/outbox/OutboxAdminController.java`
   - `GET /api/v1/admin/outbox/dead-letters` — DEAD_LETTER 상태 이벤트 목록 조회 (ADMIN 권한)
   - `POST /api/v1/admin/outbox/dead-letters/{id}/retry` — 수동 재시도 (상태를 PENDING으로 리셋, retry_count 초기화)

2. **Consumer DLQ 토픽 설정**
   - 파일: `global/config/KafkaConsumerConfig.java` (기존 수정)
   - `DeadLetterPublishingRecoverer` 설정: 실패 메시지를 `{원본토픽}.dlq` 토픽으로 전송
   - DLQ 메시지에 원본 토픽, 파티션, 오프셋, 에러 메시지 헤더 포함

3. **DLQ Consumer 구현**
   - 파일: `worker/DlqConsumer.java`
   - 모든 `.dlq` 토픽을 구독하여 `dlq_events` 테이블에 기록 (감사 추적용)
   - Flyway: `V3_004__create_dlq_events.sql` — `id`, `original_topic`, `dedup_key`, `payload`, `error_message`, `created_at`

4. **DLQ 관리 API**
   - 파일: `worker/DlqAdminController.java`
   - `GET /api/v1/admin/dlq` — DLQ 이벤트 목록 (페이징, 필터)
   - `POST /api/v1/admin/dlq/{id}/replay` — 원본 토픽으로 재발행

#### 완료 기준

- [ ] Outbox 5회 실패 → DEAD_LETTER 상태 + 관리자 API에서 조회 가능
- [ ] Consumer 3회 실패 → DLQ 토픽 전송 + `dlq_events` 테이블 기록
- [ ] 관리자 수동 재시도(replay) 정상 동작 확인
- [ ] DLQ 이벤트에 원본 토픽, 에러 메시지 포함 확인

#### 규칙 체크리스트

- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가
- [ ] `@Audited` 감사 로그 필요 여부 확인 (DLQ replay는 감사 대상)

---

### T-305: EmbeddingWorker, AiGradingWorker, AnalyticsWorker, NotificationWorker 스캐폴딩

> **담당**: Backend | **선행**: T-303 | **관련 FR**: FR-EVENT-05

#### 서브 스텝

1. **EmbeddingWorker 스캐폴딩**
   - 파일: `worker/EmbeddingWorker.java`
   - `@KafkaListener(topics = "content-events")` — `ContentCreated`, `ContentUpdated` 이벤트 소비
   - `AbstractIdempotentConsumer` 상속, `processEvent()` 메서드 stub (Phase 4에서 구현)
   - 로그: "EmbeddingWorker received event: {eventType}, contentId: {id}"

2. **AiGradingWorker 스캐폴딩**
   - 파일: `worker/AiGradingWorker.java`
   - `@KafkaListener(topics = "grading-events")` — `QuizSubmitted`, `AssignmentSubmitted` 이벤트 소비
   - stub 구현: 이벤트 수신 확인 로그만 출력

3. **AnalyticsWorker 스캐폴딩**
   - 파일: `worker/AnalyticsWorker.java`
   - `@KafkaListener(topics = "analytics-events")` — `LessonCompleted`, `QuizCompleted` 이벤트 소비
   - stub 구현: 이벤트 수신 확인 로그만 출력

4. **NotificationWorker 스캐폴딩**
   - 파일: `worker/NotificationWorker.java`
   - `@KafkaListener(topics = "notification-events")` — 알림 관련 이벤트 소비
   - stub 구현: 이벤트 수신 확인 로그만 출력

5. **이벤트 클래스 정의 (Phase 4 선행)**
   - 파일: `global/event/events/ContentCreated.java`, `ContentUpdated.java`, `QuizSubmitted.java`, `AssignmentSubmitted.java`, `LessonCompleted.java` 등
   - 모든 이벤트는 `DomainEvent` 인터페이스 구현, Java record 사용

#### 완료 기준

- [ ] 4개 Worker 모두 Kafka 토픽 구독 + 이벤트 수신 로그 출력 확인
- [ ] 각 Worker가 `AbstractIdempotentConsumer` 상속하여 멱등성 프레임워크 적용
- [ ] 이벤트 클래스가 `DomainEvent` 인터페이스를 구현하고 record로 정의

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용 (이벤트 클래스 포함)
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음

---

## Week 10: AI Gateway + PII

---

### T-306: AiGatewayController + ModelRouter

> **담당**: Backend (AI) | **선행**: T-301 | **관련 FR**: -

#### 서브 스텝

1. **AiGatewayController 구현**
   - 파일: `ai/gateway/AiGatewayController.java`
   - `POST /api/v1/ai/gateway/chat` — 범용 AI 요청 엔드포인트
   - Request DTO (record): `AiRequest(String sessionId, String userMessage, String serviceType, Map<String,Object> metadata)`
   - Response DTO (record): `AiResponse(String content, String modelUsed, double costUsd, boolean cacheHit)`
   - 전체 파이프라인 오케스트레이션: PII Masking → Model Routing → LLM 호출 → PII Demasking → Output PII 스캔 → 응답

2. **ModelRouter 구현**
   - 파일: `ai/gateway/ModelRouter.java`
   - `route(AiRequest request)` → `ModelTier` 결정
   - 라우팅 기준: `serviceType` (QUICK_QA → Tier1/Haiku, CODE_EXPLAIN → Tier2/Sonnet, CONCEPT → Tier3/Sonnet, ARCHITECTURE → Tier4/Opus)
   - 예산 기반 동적 다운그레이드: `FinOpsGuard`에서 현재 잔여 예산 비율 조회 → 라우팅 조정

3. **AiGatewayService 구현**
   - 파일: `ai/gateway/AiGatewayService.java`
   - 파이프라인 실행 순서: `piiMaskingService.mask()` → `modelRouter.route()` → `llmClient.call()` → `piiDemaskingService.demask()` → `piiOutputScanner.scan()` → `costTracker.record()`
   - 각 단계별 Span 추가 (OTel, T-312에서 연동)

4. **ModelTier enum + 설정**
   - 파일: `ai/gateway/ModelTier.java`
   - `TIER_1(haiku, 0.001)`, `TIER_2(sonnet, 0.005)`, `TIER_3(sonnet, 0.010)`, `TIER_4(opus, 0.050)`
   - 각 Tier별 모델 ID, 예상 비용, max_tokens 설정

5. **AiRateLimiter 구현**
   - 파일: `ai/gateway/AiRateLimiter.java`
   - Redis 기반: `rate:ai:{userId}` 키로 분당/시간당 요청 수 제한
   - 초과 시 `ErrorCode.AI_RATE_LIMIT_EXCEEDED` 예외

#### 완료 기준

- [ ] AI Gateway 엔드포인트가 요청을 받아 파이프라인 순서대로 처리
- [ ] ModelRouter가 serviceType + 예산 상태에 따라 올바른 Tier 선택
- [ ] Rate Limiting이 Redis 기반으로 동작하며 초과 시 429 응답
- [ ] `ApiResponse<T>` 래핑 응답 반환

#### 규칙 체크리스트

- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] 프롬프트 하드코딩 금지

---

### T-307: PiiMaskingService (Regex + NER)

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-PII-01

#### 서브 스텝

1. **PiiMaskingService 구현**
   - 파일: `ai/gateway/PiiMaskingService.java`
   - `mask(String input, String sessionId)` → `MaskingResult(String maskedText, Map<String,String> tokenMap)`
   - 2단계 파이프라인: Regex 패턴 매칭 → NER 엔진 호출

2. **한국 특화 Regex 패턴 정의**
   - 파일: `ai/gateway/PiiPatterns.java`
   - 패턴: 주민등록번호 (`\d{6}-[1-4]\d{6}`), 전화번호 (`01[0-9]-\d{3,4}-\d{4}`), 이메일, 카드번호, 계좌번호, 여권번호
   - 각 패턴별 PII 타입 enum: `PiiType(NAME, PHONE, EMAIL, SSN, CARD, ACCOUNT)`

3. **NER 엔진 연동 (Presidio + KoNLPy)**
   - 파일: `ai/gateway/NerPiiDetector.java`
   - Presidio Analyzer REST API 호출 (또는 내장 라이브러리) + KoNLPy 한국어 NER
   - Regex로 잡지 못한 이름, 주소 등 자연어 PII 감지
   - Circuit Breaker 적용: NER 엔진 장애 시 Regex만으로 동작 (graceful degradation)

4. **토큰 치환 + Redis 매핑 저장**
   - 감지된 PII를 `<NAME_1>`, `<PHONE_1>` 형태로 순차 치환
   - Redis 키: `pii:mapping:{sessionId}` → Hash 타입으로 토큰-원본 매핑 저장
   - TTL: 세션 종료 시 자동 삭제 (또는 24시간 기본 TTL)

#### 완료 기준

- [ ] 한국어 주민번호, 전화번호, 이메일 등이 정확히 마스킹되는지 확인
- [ ] NER로 이름, 주소 등 자연어 PII 감지 확인
- [ ] 토큰-원본 매핑이 Redis `pii:mapping:{sessionId}` 키에 저장 확인
- [ ] NER 장애 시 Regex만으로 fallback 동작 확인

#### 규칙 체크리스트

- [ ] PII 처리 없이 LLM에 사용자 데이터를 보내는 코드 작성 금지
- [ ] PII 매핑은 Redis session-scoped, 세션 종료 시 자동 삭제
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] Layer 2: PII Masking (Input + Output 양방향)

---

### T-308: PiiDemaskingService + Redis session-scoped 매핑

> **담당**: Backend (AI) | **선행**: T-307 | **관련 FR**: FR-PII-02

#### 서브 스텝

1. **PiiDemaskingService 구현**
   - 파일: `ai/gateway/PiiDemaskingService.java`
   - `demask(String maskedResponse, String sessionId)` → 원본 텍스트 복원
   - Redis `pii:mapping:{sessionId}` Hash에서 토큰-원본 매핑 조회
   - 역치환: `<NAME_1>` → 원본 이름, `<PHONE_1>` → 원본 전화번호

2. **세션 종료 시 매핑 자동 삭제**
   - 파일: `ai/gateway/PiiSessionManager.java`
   - `createSession(String sessionId)`: Redis 키 생성 + TTL 설정 (24시간)
   - `endSession(String sessionId)`: Redis 키 명시적 삭제
   - Spring Event 연동: 채팅 세션 종료 이벤트 수신 시 자동 삭제

3. **매핑 조회 실패 처리**
   - 토큰이 Redis에 없을 경우 (TTL 만료 등): 토큰을 그대로 유지하고 경고 로그 기록
   - 민감 정보 노출보다 토큰 노출이 안전한 방향으로 처리

#### 완료 기준

- [ ] LLM 응답의 `<NAME_1>` 등 토큰이 원본으로 정확히 복원
- [ ] 세션 종료 시 Redis PII 매핑 자동 삭제 확인
- [ ] TTL 만료 후에도 안전하게 동작 (토큰 유지, 경고 로그)

#### 규칙 체크리스트

- [ ] PII 매핑은 Redis session-scoped, 세션 종료 시 자동 삭제
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] Layer 2: PII Masking (Input + Output 양방향)

---

### T-309: PiiOutputScanner (v4.0 Output PII 스캔)

> **담당**: Backend (AI) | **선행**: T-307 | **관련 FR**: FR-PII-03

#### 서브 스텝

1. **PiiOutputScanner 구현**
   - 파일: `ai/gateway/PiiOutputScanner.java`
   - `scan(String demaskdOutput, String sessionId)` → `ScanResult(String sanitizedOutput, List<PiiDetection> detections)`
   - Demasking 후 LLM 응답에 대해 PiiMaskingService와 동일한 Regex + NER 파이프라인 재적용
   - LLM이 새로 생성한 PII 감지 (기존 매핑에 없는 새 PII)

2. **새 PII 감지 시 처리**
   - 감지된 새 PII를 마스킹 처리 (`[REDACTED]` 또는 일반적 대체 텍스트)
   - 감사 로그 기록: `AuditLog` 테이블에 `event_type=PII_OUTPUT_DETECTED`, 감지된 PII 타입, 세션 ID

3. **감사 로그 연동**
   - 파일: `ai/gateway/PiiAuditLogger.java`
   - `@Audited` AOP 또는 명시적 `AuditLogRepository.save()` 호출
   - 로그 내용: sessionId, userId, piiType, detectionSource(OUTPUT_SCAN), timestamp

4. **PiiOutputScanner를 Gateway 파이프라인에 통합**
   - `AiGatewayService`의 파이프라인에 Demasking 후 Output Scan 단계 추가
   - 스캔 결과에 PII 감지가 있으면 sanitized 버전을 최종 응답으로 사용

#### 완료 기준

- [ ] LLM이 새로 생성한 PII (입력에 없던 전화번호 등)가 감지되고 마스킹
- [ ] 감사 로그에 Output PII 감지 이벤트 기록
- [ ] Gateway 파이프라인에서 Output Scan이 Demasking 직후 실행
- [ ] 기존 매핑된 PII는 정상 복원, 새 PII만 마스킹 처리

#### 규칙 체크리스트

- [ ] PII 처리 없이 LLM에 사용자 데이터를 보내는 코드 작성 금지
- [ ] Layer 2: PII Masking (Input + Output 양방향)
- [ ] `@Audited` 감사 로그 필요 여부 확인
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성

---

### T-310: LlmClient 추상화 + ClaudeApiClient + OpenAiApiClient

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: -

#### 서브 스텝

1. **LlmClient 인터페이스 정의**
   - 파일: `ai/client/LlmClient.java`
   - `LlmResponse call(LlmRequest request)` — 동기 호출
   - `Flux<LlmChunk> stream(LlmRequest request)` — SSE 스트리밍 호출
   - `LlmRequest` record: `systemPrompt`, `messages(List<Message>)`, `model`, `maxTokens`, `temperature`
   - `LlmResponse` record: `content`, `model`, `inputTokens`, `outputTokens`, `finishReason`

2. **ClaudeApiClient 구현**
   - 파일: `ai/client/ClaudeApiClient.java`
   - Anthropic Messages API 호출 (`/v1/messages`)
   - HTTP Client: `WebClient` (Spring WebFlux) 또는 `RestClient`
   - API Key: `${CLAUDE_API_KEY}` — application.yml의 환경변수 참조
   - 스트리밍: SSE `text/event-stream` 파싱 → `Flux<LlmChunk>` 변환

3. **OpenAiApiClient 구현**
   - 파일: `ai/client/OpenAiApiClient.java`
   - OpenAI Chat Completions API 호출 (`/v1/chat/completions`)
   - Request/Response 매핑: Claude 형식 ↔ OpenAI 형식 변환
   - API Key: `${OPENAI_API_KEY}` — application.yml의 환경변수 참조

4. **LlmClientFactory 구현**
   - 파일: `ai/client/LlmClientFactory.java`
   - `getClient(ModelTier tier)` → Claude 또는 OpenAI Client 반환
   - 기본: Claude, Fallback: OpenAI (Circuit Breaker와 연동, T-311)

#### 완료 기준

- [ ] ClaudeApiClient로 Anthropic Messages API 정상 호출 및 응답 파싱
- [ ] OpenAiApiClient로 OpenAI Chat Completions API 정상 호출 및 응답 파싱
- [ ] 스트리밍(SSE) 응답이 `Flux<LlmChunk>`로 정상 변환
- [ ] API Key가 환경변수로 관리되고 코드에 하드코딩되지 않음
- [ ] LlmClientFactory가 Tier에 따라 올바른 Client 반환

#### 규칙 체크리스트

- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] DTO는 Java record 사용
- [ ] System Prompt을 사용자에게 노출하는 코드 금지
- [ ] API Key 등 민감 정보 `.env` 관리 (Git 미추적)

---

### T-311: Circuit Breaker (Resilience4j) — Claude → OpenAI Fallback

> **담당**: Backend (AI) | **선행**: T-310 | **관련 FR**: NFR-AVAIL-02

#### 서브 스텝

1. **Resilience4j 의존성 + 설정**
   - 파일: `learnflow-api/build.gradle.kts` — `io.github.resilience4j:resilience4j-spring-boot3` 추가
   - 파일: `application.yml` — Circuit Breaker 인스턴스 설정
   - 설정값: `slidingWindowSize=10`, `failureRateThreshold=50`, `waitDurationInOpenState=30s`, `permittedNumberOfCallsInHalfOpenState=3`

2. **Resilience4j 설정 클래스**
   - 파일: `global/config/ResilienceConfig.java`
   - Circuit Breaker 인스턴스: `claudeApiBreaker`
   - Retry 설정: `maxAttempts=2`, `waitDuration=500ms` (Circuit Breaker 내부 재시도)

3. **LlmClient에 Circuit Breaker 적용**
   - 파일: `ai/client/ResilientLlmClient.java` (데코레이터 패턴)
   - `@CircuitBreaker(name = "claudeApi", fallbackMethod = "fallbackToOpenAi")`
   - Fallback 메서드: Claude 실패 시 OpenAiApiClient로 동일 요청 전송
   - Fallback 시 모델명을 OpenAI 대응 모델로 매핑 (Sonnet → GPT-4o-mini 등)

4. **Circuit Breaker 상태 모니터링**
   - Actuator 엔드포인트 노출: `/actuator/circuitbreakers`
   - 상태 변경 이벤트 로깅: CLOSED → OPEN → HALF_OPEN 전환 시 WARN 로그

5. **Fallback 비용 기록**
   - Fallback 발생 시 `ai_cost_logs`에 `model=openai-fallback` 기록 (T-315와 연동)
   - 메트릭: fallback 횟수 카운터 (Prometheus, T-314와 연동)

#### 완료 기준

- [ ] Claude API 장애 시 자동으로 OpenAI Fallback 전환 확인
- [ ] 실패율 50% 초과 시 Circuit Breaker OPEN 상태 전환 확인
- [ ] 30초 후 HALF_OPEN → 성공 시 CLOSED 복귀 확인
- [ ] Actuator에서 Circuit Breaker 상태 모니터링 가능

#### 규칙 체크리스트

- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성

---

## Week 11: Distributed Tracing (OTel)

---

### T-312: OTel + Zipkin 연동 + Sampling 설정 (env별 차등)

> **담당**: Backend (Infra) | **선행**: T-306 | **관련 FR**: FR-OBS-01

#### 서브 스텝

1. **OTel 의존성 추가**
   - 파일: `learnflow-api/build.gradle.kts`
   - `io.micrometer:micrometer-tracing-bridge-otel`, `io.opentelemetry:opentelemetry-exporter-zipkin`
   - Spring Boot Actuator + Micrometer Tracing 자동 설정 활용

2. **Zipkin 연동 설정**
   - 파일: `application.yml` (프로파일별)
   - `management.tracing.sampling.probability`: dev=1.0, staging=0.3, prod=0.1
   - Zipkin endpoint: `http://zipkin:9411/api/v2/spans`
   - 에러 발생 시 100% 샘플링: 커스텀 `Sampler` 구현

3. **커스텀 에러 우선 Sampler**
   - 파일: `global/tracing/ErrorPrioritySampler.java`
   - 정상 요청: 환경별 확률 샘플링
   - 에러 응답(4xx/5xx) 또는 예외 발생: 100% 샘플링
   - AI 요청: 별도 샘플링 비율 (prod에서도 30%)

4. **TraceContextPropagator 설정**
   - 파일: `global/tracing/TraceContextPropagator.java`
   - HTTP 헤더, Kafka 메시지 헤더에 Trace ID 전파
   - Kafka Producer/Consumer Interceptor에 Trace Context 주입/추출

#### 완료 기준

- [ ] Zipkin UI에서 API 요청 트레이스 조회 가능
- [ ] dev=100%, staging=30%, prod=10% 샘플링 비율 적용 확인
- [ ] 에러 발생 시 100% 샘플링 확인
- [ ] Kafka 메시지에 Trace ID가 전파되어 Consumer 측에서도 트레이스 연결

#### 규칙 체크리스트

- [ ] Trace ID 전파 확인 (OTel)
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성 (Tracing은 `global/tracing/`에 위치)
- [ ] 환경별 설정은 `application-{profile}.yml`로 관리

---

### T-313: Business Context Span Attributes (user.id, ai.cost_usd, rag.* 등)

> **담당**: Backend (Infra) | **선행**: T-312 | **관련 FR**: FR-OBS-02

#### 서브 스텝

1. **비즈니스 Span Attribute 정의**
   - 파일: `global/tracing/SpanAttributes.java`
   - 상수 정의: `USER_ID = "user.id"`, `AI_MODEL = "ai.model"`, `AI_COST_USD = "ai.cost_usd"`, `AI_TOKENS_INPUT = "ai.tokens.input"`, `AI_TOKENS_OUTPUT = "ai.tokens.output"`, `AI_CACHE_HIT = "ai.cache_hit"`, `RAG_CHUNKS_RETRIEVED = "rag.chunks_retrieved"`, `RAG_COURSE_ID = "rag.course_id"`, `PII_TOKENS_MASKED = "pii.tokens_masked"`

2. **AiGateway Span 주입**
   - 파일: `ai/gateway/AiGatewayService.java` (기존 수정)
   - 각 파이프라인 단계에 child span 생성: `pii-masking`, `model-routing`, `llm-call`, `pii-demasking`, `output-scan`
   - LLM 응답 후 Span에 `ai.cost_usd`, `ai.model`, 토큰 수 등 attribute 추가

3. **인증 필터 Span 주입**
   - 파일: `global/security/JwtAuthenticationFilter.java` (기존 수정)
   - 인증 성공 시 현재 Span에 `user.id`, `user.role` attribute 추가

4. **Kafka Consumer Span 주입**
   - 파일: `worker/AbstractIdempotentConsumer.java` (기존 수정)
   - Consumer 처리 시 `event.type`, `event.dedup_key`, 처리 시간 attribute 추가

#### 완료 기준

- [ ] Zipkin에서 `ai.cost_usd`, `ai.model` 등 비즈니스 속성으로 트레이스 검색 가능
- [ ] AI Gateway 파이프라인 각 단계가 개별 Span으로 표시
- [ ] 사용자 ID 기반 트레이스 필터링 가능
- [ ] Kafka Consumer 처리도 동일 Trace에 연결

#### 규칙 체크리스트

- [ ] Trace ID 전파 확인 (OTel)
- [ ] FinOps 비용 기록과 Span attribute 일관성 유지
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성

---

### T-314: Kafka Consumer Lag Prometheus 메트릭

> **담당**: Backend (Infra) | **선행**: T-303 | **관련 FR**: FR-OBS-04

#### 서브 스텝

1. **Micrometer + Prometheus 의존성 확인**
   - 파일: `learnflow-api/build.gradle.kts` — `io.micrometer:micrometer-registry-prometheus` 추가 (이미 있을 수 있음)
   - Actuator 설정: `management.endpoints.web.exposure.include=prometheus,health,info,circuitbreakers`

2. **Kafka Consumer 메트릭 노출**
   - 파일: `global/config/KafkaMetricsConfig.java`
   - Spring Kafka의 내장 Micrometer 메트릭 활성화: `spring.kafka.listener.observation-enabled=true`
   - Consumer lag 메트릭: `kafka_consumer_records_lag_max`, `kafka_consumer_fetch_manager_records_consumed_total`

3. **커스텀 비즈니스 메트릭 추가**
   - 파일: `worker/WorkerMetrics.java`
   - `Counter`: 각 Worker별 처리 성공/실패 카운트 (`worker.events.processed`, `worker.events.failed`)
   - `Timer`: 이벤트 처리 소요 시간 (`worker.event.processing.duration`)
   - `Gauge`: DLQ 이벤트 수 (`worker.dlq.pending.count`)
   - 태그: `worker_type` (embedding, grading, analytics, notification)

4. **Grafana 대시보드 JSON (참고용)**
   - 파일: `infra/grafana/dashboards/kafka-consumer-lag.json` (선택 사항)
   - Consumer Lag, 처리량, 에러율, DLQ 적체 패널 포함

#### 완료 기준

- [ ] `/actuator/prometheus` 엔드포인트에서 Kafka consumer lag 메트릭 노출
- [ ] Worker별 처리 성공/실패 카운터 메트릭 확인
- [ ] Prometheus에서 메트릭 수집 + Grafana 시각화 가능
- [ ] DLQ 적체 수 실시간 모니터링 가능

#### 규칙 체크리스트

- [ ] Actuator 엔드포인트 보안 설정 확인 (ADMIN 전용 또는 내부 네트워크)
- [ ] 메트릭 네이밍 Micrometer 컨벤션 준수 (`.` 구분자)

---

## Week 12: FinOps

---

### T-315: CostTracking + ai_cost_logs 테이블

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-FINOPS-01

#### 서브 스텝

1. **Flyway 마이그레이션 — ai_cost_logs 테이블**
   - 파일: `V3_005__create_ai_cost_logs.sql`
   - 컬럼: `id`(BIGINT PK), `user_id`(BIGINT FK), `session_id`(VARCHAR), `service`(VARCHAR 50, e.g. TUTOR/QUIZ_GEN/GRADING), `model`(VARCHAR 50), `input_tokens`(INT), `output_tokens`(INT), `cost_usd`(DECIMAL 10,6), `cache_hit`(BOOLEAN), `trace_id`(VARCHAR 64), `created_at`
   - INDEX: `user_id + created_at`, `service + created_at`, `created_at` (일별 집계용)

2. **AiCostLog 엔티티**
   - 파일: `ai/finops/AiCostLog.java`
   - `@Entity`, `@Setter` 금지, 생성 시점에 모든 값 설정 (immutable 스타일)
   - 정적 팩토리 메서드: `AiCostLog.create(userId, sessionId, service, model, inputTokens, outputTokens, costUsd, cacheHit, traceId)`

3. **CostTracker 서비스**
   - 파일: `ai/finops/CostTracker.java`
   - `record(LlmResponse response, AiRequest request, boolean cacheHit)` — ai_cost_logs INSERT + Redis `finops:daily_cost` INCRBYFLOAT
   - 비용 계산: 모델별 토큰당 단가 테이블 (`ModelPricing` enum 또는 설정 파일)
   - Redis 당일 누적 비용 캐시: `finops:daily_cost` (TTL = 자정까지 남은 시간)

4. **CostTracker를 Gateway 파이프라인에 통합**
   - `AiGatewayService`에서 LLM 응답 수신 후 `costTracker.record()` 호출
   - Cache hit 시에도 비용 $0 + `cache_hit=true`로 기록 (통계용)

#### 완료 기준

- [ ] 모든 LLM 호출 시 `ai_cost_logs` 테이블에 비용 기록
- [ ] Redis `finops:daily_cost`에 당일 누적 비용 실시간 반영
- [ ] Cache hit 요청도 `cache_hit=true`로 기록
- [ ] Trace ID가 비용 로그에 포함되어 트레이스 연결 가능

#### 규칙 체크리스트

- [ ] AI 비용 미기록 금지 — 모든 LLM 호출은 `ai_cost_logs` 기록 (cache_hit 포함)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Flyway 마이그레이션 파일 추가
- [ ] 엔티티에 `@Setter` 금지
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성

---

### T-316: Unit Economics 산출 API

> **담당**: Backend (AI) | **선행**: T-315 | **관련 FR**: FR-FINOPS-02

#### 서브 스텝

1. **UnitEconomics 서비스**
   - 파일: `ai/finops/UnitEconomicsService.java`
   - `calculateByService(String service, LocalDate from, LocalDate to)` → 서비스별 평균 비용 산출
   - 서비스별 목표 대비 현황: `TUTOR` < $0.15/세션, `QUIZ_GEN` < $0.05/건, `GRADING` < $0.03/건
   - 집계 쿼리: `ai_cost_logs` GROUP BY service, 기간별 SUM/AVG/COUNT

2. **UnitEconomicsController**
   - 파일: `ai/finops/UnitEconomicsController.java`
   - `GET /api/v1/admin/finops/unit-economics` — 서비스별 단위 비용 현황 (ADMIN 권한)
   - `GET /api/v1/admin/finops/unit-economics/daily` — 일별 추이
   - Response DTO (record): `UnitEconomicsResponse(String service, double avgCostUsd, double targetCostUsd, boolean withinTarget, long totalRequests, double totalCostUsd)`

3. **모델별 비용 분석**
   - `GET /api/v1/admin/finops/cost-by-model` — 모델별 사용량 + 비용 비율
   - Cache hit 비율 산출: `totalCacheHit / totalRequests`
   - 일별/주별/월별 트렌드 데이터

4. **알림 연동 (선택)**
   - Unit Economics 목표 초과 시 관리자 알림 (OutboxPublisher로 notification-events 발행)

#### 완료 기준

- [ ] 서비스별 단위 비용이 목표 대비 정상 범위 내인지 API로 확인 가능
- [ ] 일별 비용 추이 데이터 제공
- [ ] Cache hit 비율 산출 정상 동작
- [ ] ADMIN 권한만 접근 가능

#### 규칙 체크리스트

- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] DTO는 Java record 사용
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-317: KillSwitch (Soft/Hard Limit + 동적 라우팅)

> **담당**: Backend (AI) | **선행**: T-315 | **관련 FR**: FR-FINOPS-03~04

#### 서브 스텝

1. **Flyway 마이그레이션 — cost_thresholds 테이블**
   - 파일: `V3_006__create_cost_thresholds.sql`
   - 컬럼: `id`(BIGINT PK), `period`(VARCHAR 20, e.g. DAILY/MONTHLY), `soft_limit`(DECIMAL 10,2), `hard_limit`(DECIMAL 10,2), `is_killed`(BOOLEAN DEFAULT FALSE), `updated_at`
   - 초기 데이터 INSERT: DAILY soft=$50/hard=$100, MONTHLY soft=$1000/hard=$2000

2. **KillSwitch 서비스**
   - 파일: `ai/finops/KillSwitch.java`
   - `checkBudget()` → 현재 비용 vs 한도 비교 → `BudgetStatus` 반환
   - `BudgetStatus` enum: `NORMAL`(잔여 > 70%), `CAUTION`(50~70%), `WARNING`(30~50%), `CRITICAL`(< 30%), `KILLED`(hard limit 초과)
   - Hard limit 초과 시: `is_killed=true` 설정 → 모든 AI 요청 차단 (ErrorCode.AI_SERVICE_KILLED)

3. **동적 라우팅 연동**
   - 파일: `ai/gateway/ModelRouter.java` (기존 수정)
   - `BudgetStatus`에 따른 라우팅 조정:
     - `NORMAL`: 정상 4 Tier 분류
     - `CAUTION`: Opus(Tier 4) 비활성 → Sonnet으로 다운그레이드
     - `WARNING`: Haiku 우선 (Tier 2~4 → Tier 1로 강제)
     - `CRITICAL`: Haiku 전용 + 배치 작업 중단
     - `KILLED`: 모든 AI 요청 거부

4. **FinOpsGuard (Gateway 통합)**
   - 파일: `ai/gateway/FinOpsGuard.java`
   - `AiGatewayService`에서 LLM 호출 전 `finOpsGuard.check()` 호출
   - Kill 상태 시 즉시 예외 발생, 라우팅 다운그레이드 시 ModelRouter에 힌트 전달

5. **관리자 Kill-switch 수동 제어 API**
   - `POST /api/v1/admin/finops/kill-switch/toggle` — 수동 ON/OFF (ADMIN)
   - `GET /api/v1/admin/finops/budget-status` — 현재 예산 상태 조회

#### 완료 기준

- [ ] Soft limit 초과 시 모델 다운그레이드 자동 적용
- [ ] Hard limit 초과 시 모든 AI 요청 차단 (`is_killed=true`)
- [ ] 관리자가 수동으로 Kill-switch ON/OFF 가능
- [ ] 예산 상태별 동적 라우팅 정상 동작 (NORMAL → CAUTION → WARNING → CRITICAL → KILLED)

#### 규칙 체크리스트

- [ ] Layer 7: FinOps Kill-switch
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] Flyway 마이그레이션 파일 추가
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `@Audited` 감사 로그 필요 여부 확인 (Kill-switch 토글은 감사 대상)

---

### T-318: SemanticResponseCache (유사도 > 0.95 캐시)

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-FINOPS-05

#### 서브 스텝

1. **SemanticResponseCache 서비스**
   - 파일: `ai/cache/SemanticResponseCache.java`
   - `get(String query, String courseId)` → 유사 질문 캐시 조회
   - `put(String query, String courseId, String response, float[] embedding)` → 캐시 저장
   - 유사도 기준: cosine similarity > 0.95 → 캐시 히트

2. **쿼리 임베딩 + 유사도 검색**
   - 쿼리를 임베딩 벡터로 변환 (text-embedding-3-small)
   - Redis 또는 pgvector에서 기존 캐시 임베딩과 cosine similarity 계산
   - `cache:semantic:{queryHash}` Redis 키 또는 `semantic_cache` 테이블 (pgvector)

3. **캐시 무효화 정책**
   - TTL: 24시간 (기본)
   - course_id별 격리: 다른 강의의 캐시 절대 반환 금지
   - 콘텐츠 업데이트 시 해당 course_id 캐시 무효화 (`ContentUpdated` 이벤트 수신)

4. **Gateway 파이프라인 통합**
   - `AiGatewayService`에서 PII Masking 후, LLM 호출 전 캐시 조회
   - 캐시 히트 시 LLM 호출 스킵 → 캐시 응답 반환 + `cache_hit=true` 비용 기록
   - 캐시 미스 시 LLM 호출 → 응답 캐시 저장

#### 완료 기준

- [ ] 유사도 0.95 이상의 질문에 대해 캐시 히트 확인 (LLM 호출 스킵)
- [ ] course_id별 캐시 격리 확인
- [ ] 캐시 히트 시 `ai_cost_logs`에 `cache_hit=true` 기록
- [ ] 콘텐츠 업데이트 시 관련 캐시 무효화

#### 규칙 체크리스트

- [ ] Semantic Cache 적용 가능 여부 검토
- [ ] `course_id` 기반 격리 (다른 강의 데이터 노출 금지)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성

---

### T-319: AnomalyDetection (이상 패턴 감지)

> **담당**: Backend (AI) | **선행**: T-315 | **관련 FR**: FR-FINOPS-06

#### 서브 스텝

1. **AnomalyDetection 서비스**
   - 파일: `ai/finops/AnomalyDetection.java`
   - `detect()` → 이상 패턴 목록 반환
   - 감지 규칙:
     - 단일 사용자 비용 급증: 시간당 비용 > 이전 7일 평균의 3배
     - 서비스별 비용 급증: 시간당 서비스 비용 > 이전 7일 평균의 5배
     - 비정상 요청 패턴: 분당 요청 수 > Rate Limit의 80%
     - 높은 토큰 소비: 단일 요청 output_tokens > 4000 (비정상 긴 응답)

2. **스케줄러 기반 주기적 감지**
   - `@Scheduled(fixedDelay = 300_000)` — 5분마다 실행
   - `ai_cost_logs` 테이블에서 최근 1시간 데이터 집계 + 이전 7일 평균 대비 비교

3. **이상 감지 시 알림**
   - 이상 감지 → `CostThreshold` 이벤트 발행 (OutboxPublisher 경유)
   - 관리자 알림: notification-events 토픽으로 전달
   - 감사 로그: `AuditLog`에 이상 패턴 기록

4. **이상 감지 조회 API**
   - 파일: `ai/finops/AnomalyDetectionController.java`
   - `GET /api/v1/admin/finops/anomalies` — 최근 이상 감지 목록 (ADMIN)
   - `GET /api/v1/admin/finops/anomalies/stats` — 기간별 이상 감지 통계

#### 완료 기준

- [ ] 비용 급증 패턴 감지 시 관리자 알림 발생
- [ ] 비정상 요청 패턴 감지 동작 확인
- [ ] 이상 감지 이력 API 조회 가능
- [ ] OutboxPublisher 경유 이벤트 발행 확인

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] `@Audited` 감사 로그 필요 여부 확인
- [ ] AI 관련 코드는 `ai/` 패키지 안에서만 작성
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] DTO는 Java record 사용

---

### T-320: 단위 테스트 (Outbox, PII, FinOps, Gateway)

> **담당**: Backend | **선행**: T-319 | **관련 FR**: NFR-MAINT-03

#### 서브 스텝

1. **Outbox 단위 테스트**
   - 파일: `test/.../global/event/outbox/OutboxPublisherTest.java`
   - 테스트 케이스:
     - `publish()` 호출 시 outbox_events에 PENDING 상태 INSERT
     - 동일 `dedup_key` 중복 발행 시 예외 또는 스킵
     - OutboxRelay가 PENDING → SENT 상태 전환
     - retry_count 5회 초과 시 DEAD_LETTER 전환
   - `@DataJpaTest` + Testcontainers (MySQL)

2. **PII 단위 테스트**
   - 파일: `test/.../ai/gateway/PiiMaskingServiceTest.java`, `PiiOutputScannerTest.java`
   - 테스트 케이스:
     - 주민번호, 전화번호, 이메일 마스킹 정확성
     - NER fallback (NER 장애 시 Regex만으로 동작)
     - Demasking 정확성 (토큰 → 원본 복원)
     - Output PII 스캔: LLM이 새로 생성한 PII 감지
     - 세션 종료 후 Redis 매핑 삭제
   - `@SpringBootTest` + Embedded Redis 또는 Testcontainers (Redis)

3. **FinOps 단위 테스트**
   - 파일: `test/.../ai/finops/CostTrackerTest.java`, `KillSwitchTest.java`
   - 테스트 케이스:
     - 비용 기록 정상 동작 (ai_cost_logs INSERT)
     - KillSwitch 예산 상태 계산 정확성 (NORMAL/CAUTION/WARNING/CRITICAL/KILLED)
     - Hard limit 초과 시 AI 요청 차단
     - 동적 라우팅 다운그레이드 적용
   - `@DataJpaTest` + Mockito

4. **AI Gateway 통합 테스트**
   - 파일: `test/.../ai/gateway/AiGatewayServiceTest.java`
   - 테스트 케이스:
     - 전체 파이프라인 실행 순서 검증 (PII Mask → Route → LLM → Demask → Output Scan → Cost Record)
     - Circuit Breaker Fallback 동작
     - Rate Limiting 초과 시 429 응답
     - Semantic Cache hit 시 LLM 호출 스킵
   - `@SpringBootTest` + WireMock (LLM API Mock)

5. **Consumer 멱등성 테스트**
   - 파일: `test/.../worker/IdempotencyServiceTest.java`
   - 테스트 케이스:
     - 동일 dedup_key 메시지 중복 처리 방지
     - DLQ 전송 동작 확인
   - `@DataJpaTest` + Testcontainers

6. **테스트 커버리지 확인**
   - `./gradlew test jacocoTestReport`
   - Outbox, PII, FinOps, Gateway 패키지 라인 커버리지 80% 이상 목표

#### 완료 기준

- [ ] Outbox 관련 테스트 전체 통과 (publish, relay, dedup, dead_letter)
- [ ] PII 관련 테스트 전체 통과 (masking, demasking, output scan, session cleanup)
- [ ] FinOps 관련 테스트 전체 통과 (cost tracking, kill-switch, unit economics)
- [ ] Gateway 통합 테스트 전체 통과 (파이프라인, circuit breaker, rate limit, cache)
- [ ] Consumer 멱등성 테스트 전체 통과

#### 규칙 체크리스트

- [ ] `@WebMvcTest` (컨트롤러) + `@DataJpaTest` (리포지토리) + `@SpringBootTest` (통합)
- [ ] 테스트 없는 PR 금지 — 최소 서비스 레이어 단위 테스트 포함
- [ ] Testcontainers 사용 시 Docker 환경 확인

---

## Phase 3 완료 기준 (M3 마일스톤)

Phase 3의 모든 Task (T-301 ~ T-320) 완료 후 다음 항목을 검증한다:

- [ ] Outbox 패턴으로 이벤트 발행 → Kafka → Consumer 수신 → 멱등성 처리 전체 흐름 동작
- [ ] DLQ 처리 (Outbox DEAD_LETTER + Consumer DLQ 토픽) 정상 동작
- [ ] AI Gateway 파이프라인 (PII Masking → Model Routing → LLM 호출 → PII Demasking → Output PII Scan) 전체 흐름 동작
- [ ] Circuit Breaker: Claude 장애 시 OpenAI Fallback 자동 전환
- [ ] OTel + Zipkin 분산 추적: API → Kafka → Consumer 전체 Trace 연결
- [ ] FinOps: 비용 추적 + Kill-switch + 동적 라우팅 + 이상 감지 동작
- [ ] Semantic Cache: 유사 질문 캐시 히트 → LLM 비용 절감 효과 확인
- [ ] 단위 테스트 전체 통과 (`./gradlew test`)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-04-03 | Phase 3 워크플로우 초안 작성 (T-301 ~ T-320, 20개 Task) |
