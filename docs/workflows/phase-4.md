# Phase 4 — RAG + AI 튜터 (Week 13~16) Task Workflows

> **기간**: 2026-06-30 ~ 07-27
> **마일스톤**: M4 — AI 통합
> **Task 수**: 24개 (T-401 ~ T-424)
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [README.md](README.md)

---

## 목차

- [Week 13: Semantic Chunking + Hybrid Search](#week-13-semantic-chunking--hybrid-search)
  - [T-401: SemanticChunking](#t-401-semanticchunking-recursive--semantic-boundary-detection)
  - [T-402: chunk_hash 중복 스킵](#t-402-chunk_hash-sha-256-중복-스킵-로직)
  - [T-403: content_embeddings + pgvector](#t-403-content_embeddings-테이블--pgvector-연동)
  - [T-404: EmbeddingWorker](#t-404-embeddingworker-구현)
  - [T-405: HybridSearch](#t-405-hybridsearch-pgvector--es-bm25--rrf)
- [Week 14: Re-ranking + Query Rewrite + Compression](#week-14-re-ranking--query-rewrite--compression)
  - [T-406: CrossEncoder Re-ranking](#t-406-crossencoder-re-ranking)
  - [T-407: QueryRewrite](#t-407-queryrewrite)
  - [T-408: ContextCompression](#t-408-contextcompression)
  - [T-409: ChunkVersioning](#t-409-chunkversioning--soft-delete)
- [Week 15: AI 튜터](#week-15-ai-튜터)
  - [T-410: AiTutorService](#t-410-aitutorservice--채팅-세션-관리)
  - [T-411: SSE 스트리밍](#t-411-sse-스트리밍-응답)
  - [T-412: LevelingService](#t-412-3단계-levelingservice)
  - [T-413: ShortTermMemory + LongTermMemory](#t-413-shorttermmemory--longtermmemory)
  - [T-414: RAG course_id 격리](#t-414-rag-course_id-격리-검색-통합)
  - [T-415: 응답 피드백 API](#t-415-응답-피드백-api)
  - [T-416: 추천 질문 생성](#t-416-추천-질문-생성)
  - [T-417: AI 튜터 채팅 UI](#t-417-ai-튜터-채팅-ui)
- [Week 16: AI 퀴즈 + AI 채점](#week-16-ai-퀴즈--ai-채점)
  - [T-418: AiQuizGenerator](#t-418-aiquizgenerator)
  - [T-419: AiGrading](#t-419-aigrading)
  - [T-420: ConfidenceScorer](#t-420-confidencescorer)
  - [T-421: AiGradingWorker](#t-421-aigradingworker)
  - [T-422: OutputValidator](#t-422-outputvalidator)
  - [T-423: AI 요약 + 플래시카드](#t-423-ai-요약--플래시카드-api)
  - [T-424: 단위 테스트](#t-424-단위-테스트-rag-tutor-grading)

---

## Week 13: Semantic Chunking + Hybrid Search

### T-401: SemanticChunking (Recursive + Semantic Boundary Detection)

> **담당**: Backend (AI) | **선행**: T-305 | **관련 FR**: FR-RAG-01

#### 서브 스텝

1. **SemanticChunking 서비스 구현**
   - 파일: `ai/rag/SemanticChunking.java`
   - Recursive Character Text Splitter를 기본으로 구현 (max_chunk_size=512 tokens, overlap=50 tokens)
   - 문단/섹션 경계(heading, 빈 줄, 코드 블록)를 우선 분할 지점으로 사용

2. **Semantic Boundary Detection 구현**
   - 파일: `ai/rag/SemanticChunking.java`
   - 인접 문장 임베딩 코사인 유사도를 계산하여 급격한 의미 전환 지점(similarity < 0.5)에서 청크 분리
   - text-embedding-3-small 모델 사용 (AI Gateway 경유)

3. **ChunkResult DTO 정의**
   - 파일: `ai/rag/dto/ChunkResult.java`
   - Java record: `chunkIndex`, `content`, `chunkHash`, `tokenCount`, `metadata`(lesson_id, section_title 등)

4. **콘텐츠 타입별 전략 분기**
   - 파일: `ai/rag/SemanticChunking.java`
   - 마크다운: heading 기반 분할 → 코드 블록: 코드 경계 유지 → 일반 텍스트: 문단 기반 분할
   - Strategy Pattern 적용으로 타입별 `ChunkingStrategy` 인터페이스 구현

#### 완료 기준

- [ ] 512 토큰 이하 청크로 분할되며, 의미 경계가 유지됨
- [ ] 코드 블록이 중간에 잘리지 않음
- [ ] ChunkResult에 lesson_id, section_title 메타데이터 포함
- [ ] AI Gateway 경유하여 임베딩 호출 (직접 호출 없음)

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] DTO는 Java record 사용

---

### T-402: chunk_hash (SHA-256) 중복 스킵 로직

> **담당**: Backend (AI) | **선행**: T-401 | **관련 FR**: FR-RAG-02

#### 서브 스텝

1. **chunk_hash 계산 유틸리티**
   - 파일: `ai/rag/ChunkHashUtil.java`
   - 청크 content를 정규화(trim, 연속 공백 제거) 후 SHA-256 해시 생성
   - `MessageDigest.getInstance("SHA-256")` 사용, HEX 문자열 반환

2. **중복 체크 로직 통합**
   - 파일: `ai/rag/SemanticChunking.java`
   - 청크 생성 후 `content_embeddings` 테이블에서 동일 `chunk_hash` 존재 여부 조회
   - 이미 ACTIVE 상태로 존재하면 재임베딩 스킵, 로그 기록

3. **ChunkHashUtil 단위 테스트**
   - 파일: `ai/rag/ChunkHashUtilTest.java`
   - 동일 내용 → 동일 해시, 공백만 다른 내용 → 동일 해시, 다른 내용 → 다른 해시 검증

#### 완료 기준

- [ ] 동일 콘텐츠 재업로드 시 임베딩 API 호출이 스킵됨
- [ ] 공백/줄바꿈만 다른 경우에도 동일 해시로 판정
- [ ] 스킵 시 로그에 `chunk_hash duplicate skipped` 기록
- [ ] 단위 테스트 통과

#### 규칙 체크리스트

- [ ] `chunk_hash` (SHA-256) 동일 내용 재임베딩 스킵
- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경

---

### T-403: content_embeddings 테이블 + pgvector 연동

> **담당**: Backend (AI) | **선행**: T-401 | **관련 FR**: FR-RAG-01

#### 서브 스텝

1. **Flyway 마이그레이션 작성**
   - 파일: `resources/db/migration/V13__content_embeddings.sql`
   - `content_embeddings` 테이블: `id`, `lesson_id`, `course_id`, `chunk_index`, `content`, `embedding`(VECTOR(1536)), `chunk_hash`(VARCHAR(64) UNIQUE), `status`(ENUM: ACTIVE/INACTIVE), `version`, `token_count`, `metadata`(JSON), `created_at`, `updated_at`
   - pgvector 확장 활성화: `CREATE EXTENSION IF NOT EXISTS vector`
   - IVFFlat 인덱스 생성: `CREATE INDEX ON content_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100)`

2. **ContentEmbedding 엔티티 구현**
   - 파일: `ai/rag/entity/ContentEmbedding.java`
   - `@Entity`, `@Setter` 없이 비즈니스 메서드 제공: `activate()`, `deactivate()`, `updateEmbedding(float[] embedding)`
   - `course_id` 필드 필수 — RAG 격리의 핵심

3. **ContentEmbeddingRepository 구현**
   - 파일: `ai/rag/repository/ContentEmbeddingRepository.java`
   - `findByCourseIdAndStatus(Long courseId, Status status)` — course_id 격리 조회
   - `findByChunkHash(String chunkHash)` — 중복 체크용
   - Native Query: `findNearestByCourseId(Long courseId, float[] queryEmbedding, int limit)` — pgvector `<=>` 연산자로 코사인 유사도 Top-K

4. **pgvector JPA 타입 변환기**
   - 파일: `global/config/PgVectorType.java`
   - `float[]` ↔ pgvector `vector` 타입 간 변환 `AttributeConverter` 구현

#### 완료 기준

- [ ] Flyway 마이그레이션 정상 실행, pgvector 확장 + IVFFlat 인덱스 생성 확인
- [ ] `findNearestByCourseId`에서 course_id 필터링이 반드시 포함됨
- [ ] `chunk_hash` UNIQUE 제약 조건 동작 확인
- [ ] `@Setter` 없이 상태 변경 메서드만 사용

#### 규칙 체크리스트

- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지
- [ ] `course_id` 기반 격리 (다른 강의 데이터 노출 금지)
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] DTO는 Java record 사용

---

### T-404: EmbeddingWorker 구현 (ContentCreated/Updated 이벤트 소비)

> **담당**: Backend (AI) | **선행**: T-403 | **관련 FR**: FR-COURSE-03

#### 서브 스텝

1. **ContentCreated / ContentUpdated 이벤트 정의**
   - 파일: `global/event/events/ContentCreatedEvent.java`, `ContentUpdatedEvent.java`
   - Java record: `lessonId`, `courseId`, `contentType`, `content`, `timestamp`

2. **EmbeddingWorker Kafka Consumer 구현**
   - 파일: `worker/EmbeddingWorker.java`
   - `@KafkaListener(topics = "content-events")` — ContentCreated/Updated 이벤트 소비
   - 처리 흐름: 이벤트 수신 → SemanticChunking → chunk_hash 중복 체크 → AI Gateway 경유 임베딩 생성 → content_embeddings 저장

3. **멱등성 보장 (dedup_key 체크)**
   - 파일: `worker/EmbeddingWorker.java`
   - `dedup_key` = `{lessonId}:{eventType}:{timestamp}` 로 구성
   - 처리 전 `outbox_events` 테이블에서 동일 `dedup_key` 존재 여부 확인, 중복 시 스킵

4. **DLQ 처리**
   - 파일: `worker/EmbeddingWorker.java`
   - 임베딩 실패 시 3회 재시도 후 `content-events-dlq` 토픽으로 전송
   - `@RetryableTopic(attempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))`

5. **콘텐츠 서비스에서 Outbox 이벤트 발행**
   - 파일: `domain/course/service/LessonService.java`
   - 레슨 생성/수정 시 `OutboxPublisher.publish(contentCreatedEvent, "content-events")` 호출
   - 비즈니스 데이터 저장 + outbox_events INSERT가 단일 `@Transactional` 내에서 실행

#### 완료 기준

- [ ] 레슨 생성/수정 시 자동으로 임베딩 생성됨
- [ ] 동일 이벤트 재수신 시 중복 처리되지 않음 (dedup_key 체크)
- [ ] 3회 실패 시 DLQ 토픽에 메시지 전달됨
- [ ] AI Gateway 경유하여 임베딩 API 호출
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 없음)

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음
- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] 임베딩은 비동기 (Outbox 경유)

---

### T-405: HybridSearch (pgvector Top 20 + ES BM25 Top 20 + RRF)

> **담당**: Backend (AI) | **선행**: T-403 | **관련 FR**: FR-RAG-04

#### 서브 스텝

1. **HybridSearch 서비스 구현**
   - 파일: `ai/rag/HybridSearch.java`
   - 두 검색 결과를 병합하는 오케스트레이터 역할
   - 입력: 쿼리 임베딩(`float[]`), 쿼리 텍스트(`String`), `courseId`(`Long`)

2. **Vector Search 구현 (pgvector)**
   - 파일: `ai/rag/HybridSearch.java`
   - `ContentEmbeddingRepository.findNearestByCourseId(courseId, queryEmbedding, 20)` 호출
   - 반드시 `course_id` 필터링 포함, ACTIVE 상태만 조회
   - 결과: Top 20 청크 + 코사인 유사도 점수

3. **BM25 Search 구현 (Elasticsearch)**
   - 파일: `ai/rag/HybridSearch.java`
   - ES `content_embeddings` 인덱스에 대해 BM25 쿼리 실행
   - `bool` 쿼리: `must`(match query text) + `filter`(term course_id) + `filter`(term status=ACTIVE)
   - 결과: Top 20 청크 + BM25 점수

4. **RRF (Reciprocal Rank Fusion) 구현**
   - 파일: `ai/rag/HybridSearch.java`
   - RRF 공식: `score = SUM(1 / (k + rank_i))`, k=60 (표준 상수)
   - Vector 결과와 BM25 결과의 chunk_id 기준 합산 후 상위 정렬
   - 최종 결과: Top 20 통합 랭킹

5. **HybridSearchResult DTO 정의**
   - 파일: `ai/rag/dto/HybridSearchResult.java`
   - Java record: `chunkId`, `content`, `vectorScore`, `bm25Score`, `rrfScore`, `metadata`

#### 완료 기준

- [ ] Vector Search + BM25 결과가 RRF로 올바르게 합산됨
- [ ] 검색 결과에 다른 course_id의 청크가 포함되지 않음
- [ ] INACTIVE 청크가 검색 결과에서 제외됨
- [ ] RRF 결과가 Top 20으로 제한됨
- [ ] Trace ID가 검색 요청에 전파됨

#### 규칙 체크리스트

- [ ] `course_id` 기반 격리 (다른 강의 데이터 노출 금지)
- [ ] DTO는 Java record 사용
- [ ] Trace ID 전파 확인 (OTel)
- [ ] Semantic Cache 적용 가능 여부 검토

---

## Week 14: Re-ranking + Query Rewrite + Compression

### T-406: CrossEncoder Re-ranking

> **담당**: Backend (AI) | **선행**: T-405 | **관련 FR**: FR-RAG-06

#### 서브 스텝

1. **CrossEncoderClient 구현**
   - 파일: `ai/client/CrossEncoderClient.java`
   - ms-marco-MiniLM-L-6-v2 모델 호출 (로컬 Python 서비스 또는 외부 API)
   - 입력: (query, passage) 쌍 리스트 → 출력: relevance score 리스트

2. **Reranking 서비스 구현**
   - 파일: `ai/rag/Reranking.java`
   - HybridSearch Top 20 결과를 CrossEncoder로 재평가
   - 모든 (query, chunk.content) 쌍에 대해 relevance score 계산
   - score 기준 내림차순 정렬 후 Top 5 반환

3. **RerankResult DTO 정의**
   - 파일: `ai/rag/dto/RerankResult.java`
   - Java record: `chunkId`, `content`, `rerankScore`, `originalRrfRank`, `metadata`

#### 완료 기준

- [ ] Top 20 → Top 5로 필터링됨
- [ ] CrossEncoder score가 RRF score보다 의미적 관련성 반영에 우수
- [ ] CrossEncoder 서비스 장애 시 RRF 결과 Top 5를 Fallback으로 반환
- [ ] Trace ID 전파 확인

#### 규칙 체크리스트

- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] DTO는 Java record 사용

---

### T-407: QueryRewrite

> **담당**: Backend (AI) | **선행**: T-405 | **관련 FR**: FR-RAG-05

#### 서브 스텝

1. **QueryRewrite 서비스 구현**
   - 파일: `ai/rag/QueryRewrite.java`
   - 사용자 원본 질문을 검색에 최적화된 쿼리로 변환
   - AI Gateway 경유하여 LLM 호출 (Tier 1 Haiku 사용 — 비용 효율)

2. **QueryRewrite 프롬프트 템플릿 정의**
   - 파일: `ai/prompt/templates/query_rewrite.mustache`
   - 지시: "사용자 질문을 검색에 최적화된 키워드 기반 쿼리로 변환. 핵심 개념 추출, 동의어 확장, 불필요한 수식어 제거"
   - 입력 변수: `{{userQuery}}`, `{{courseContext}}`
   - `prompt_versions` 테이블에 등록 (`name=query_rewrite`, `is_active=true`)

3. **PII 처리 통합**
   - 파일: `ai/rag/QueryRewrite.java`
   - 사용자 쿼리를 PII Masking 후 LLM에 전송, 결과는 Demasking 불필요 (검색 쿼리이므로)
   - Input PII 스캔만 적용

4. **Semantic Cache 적용**
   - 파일: `ai/rag/QueryRewrite.java`
   - 동일/유사 쿼리(similarity > 0.95) 캐시 히트 시 LLM 호출 스킵
   - Redis 키: `cache:semantic:qr:{queryHash}`, TTL 1h

#### 완료 기준

- [ ] 구어체 질문이 검색 키워드로 변환됨 (예: "스프링 부트에서 빈 주입이 뭐야?" → "Spring Boot Bean Injection DI 개념 설명")
- [ ] PII Masking 통과 후 LLM 전송
- [ ] 프롬프트가 `ai/prompt/templates/`에서 관리됨 (하드코딩 아님)
- [ ] Semantic Cache 히트 시 LLM 호출 스킵 확인

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Semantic Cache 적용 가능 여부 검토
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Trace ID 전파 확인 (OTel)

---

### T-408: ContextCompression

> **담당**: Backend (AI) | **선행**: T-406 | **관련 FR**: FR-RAG-07

#### 서브 스텝

1. **ContextCompression 서비스 구현**
   - 파일: `ai/rag/ContextCompression.java`
   - Re-ranking Top 5 청크에서 질문과 관련된 핵심 문장만 추출
   - AI Gateway 경유 LLM 호출 (Tier 1 Haiku — 비용 절감)

2. **압축 프롬프트 템플릿 정의**
   - 파일: `ai/prompt/templates/context_compression.mustache`
   - 지시: "주어진 컨텍스트에서 질문에 답하는 데 필요한 핵심 문장만 추출. 원문 변형 금지, 불필요한 부분 제거"
   - 입력 변수: `{{userQuery}}`, `{{contexts}}`

3. **토큰 절감 측정**
   - 파일: `ai/rag/ContextCompression.java`
   - 압축 전/후 토큰 수를 비교하여 절감률 계산
   - 절감률과 함께 `ai_cost_logs`에 기록 (cache_hit=false, compression_ratio 필드)

4. **CompressedContext DTO 정의**
   - 파일: `ai/rag/dto/CompressedContext.java`
   - Java record: `compressedContent`, `originalTokenCount`, `compressedTokenCount`, `compressionRatio`, `sourceChunkIds`

#### 완료 기준

- [ ] 압축 후 토큰 수가 원본 대비 30% 이상 절감됨
- [ ] 핵심 정보가 손실되지 않음 (원문 변형 없이 추출)
- [ ] 토큰 절감률이 비용 로그에 기록됨
- [ ] PII Masking 적용 후 LLM 전송

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] DTO는 Java record 사용

---

### T-409: ChunkVersioning + Soft Delete

> **담당**: Backend (AI) | **선행**: T-403 | **관련 FR**: FR-RAG-03

#### 서브 스텝

1. **ChunkVersioning 서비스 구현**
   - 파일: `ai/rag/ChunkVersioning.java`
   - 콘텐츠 업데이트 시 기존 청크를 INACTIVE로 Soft Delete, 새 청크를 ACTIVE로 생성
   - `version` 필드 자동 증가 (이전 버전 +1)

2. **버전 전환 로직**
   - 파일: `ai/rag/ChunkVersioning.java`
   - 새 청크 임베딩 완료 후에만 이전 버전을 INACTIVE로 전환 (원자적 전환)
   - `@Transactional` 내에서: 새 청크 ACTIVE 저장 → 이전 버전 INACTIVE 변경

3. **이전 버전 조회 API**
   - 파일: `ai/rag/ChunkVersioning.java`
   - `getChunkHistory(lessonId)` — 특정 레슨의 청크 버전 이력 조회 (디버깅/감사용)
   - INACTIVE 포함 전체 버전 반환

4. **Soft Delete 정리 스케줄러**
   - 파일: `ai/rag/ChunkVersioning.java`
   - `@Scheduled(cron = "0 0 3 * * *")` — 매일 새벽 3시 30일 이상 INACTIVE 청크 물리 삭제
   - 삭제 전 카운트 로깅

#### 완료 기준

- [ ] 콘텐츠 업데이트 시 기존 청크가 INACTIVE로 전환됨
- [ ] 새 청크 임베딩 완료 전까지 이전 청크가 검색 가능 (무중단)
- [ ] INACTIVE 청크가 HybridSearch 결과에서 제외됨
- [ ] 30일 이상 INACTIVE 청크가 자동 물리 삭제됨

#### 규칙 체크리스트

- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `course_id` 기반 격리 유지
- [ ] `chunk_hash` (SHA-256) 동일 내용 재임베딩 스킵

---

## Week 15: AI 튜터

### T-410: AiTutorService + 채팅 세션 관리

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-TUTOR-01

#### 서브 스텝

1. **ChatSession 엔티티 구현**
   - 파일: `ai/tutor/entity/ChatSession.java`
   - 컬럼: `id`, `userId`, `courseId`, `lessonId`, `status`(ACTIVE/CLOSED), `messageCount`, `modelUsed`, `totalCostUsd`, `createdAt`, `closedAt`
   - `@Setter` 없이 비즈니스 메서드: `addMessage()`, `close()`, `addCost(BigDecimal)`

2. **ChatMessage 엔티티 구현**
   - 파일: `ai/tutor/entity/ChatMessage.java`
   - 컬럼: `id`, `sessionId`, `role`(USER/ASSISTANT/SYSTEM), `content`, `modelUsed`, `tokenCount`, `feedback`(NONE/THUMBS_UP/THUMBS_DOWN), `createdAt`

3. **AiTutorService 핵심 로직**
   - 파일: `ai/tutor/AiTutorService.java`
   - 프롬프트 구성 순서: System Prompt → Long-term Memory → RAG Context (course_id 격리) → Short-term Memory (최근 10턴) → User Message
   - AI Gateway 경유 LLM 호출, 모델 티어는 질문 복잡도에 따라 ModelRouter가 결정

4. **세션 관리 API (생성/조회/종료)**
   - 파일: `ai/tutor/AiTutorController.java`
   - `POST /api/v1/ai/chat/sessions` — 세션 생성 (courseId, lessonId 필수)
   - `GET /api/v1/ai/chat/sessions/{sessionId}/messages` — 대화 이력 조회
   - `PATCH /api/v1/ai/chat/sessions/{sessionId}/close` — 세션 종료 (PII 매핑 삭제 트리거)

5. **Flyway 마이그레이션**
   - 파일: `resources/db/migration/V14__ai_chat_sessions.sql`
   - `ai_chat_sessions`, `ai_chat_messages` 테이블 생성

#### 완료 기준

- [ ] 세션 생성 시 courseId 기반 수강 여부 검증
- [ ] 프롬프트 구성 순서가 정확히 준수됨 (System → LongTerm → RAG → ShortTerm → User)
- [ ] 세션 종료 시 Redis PII 매핑 삭제 (`pii:mapping:{sessionId}`)
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] AI Gateway 경유 LLM 호출

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] Flyway 마이그레이션 파일 추가

---

### T-411: SSE 스트리밍 응답

> **담당**: Backend (AI) | **선행**: T-410 | **관련 FR**: FR-TUTOR-02

#### 서브 스텝

1. **SSE 엔드포인트 구현**
   - 파일: `ai/tutor/AiTutorController.java`
   - `POST /api/v1/ai/chat/sessions/{sessionId}/messages` → `SseEmitter` 반환
   - `MediaType.TEXT_EVENT_STREAM_VALUE` 설정, timeout 120초

2. **스트리밍 LLM 호출 통합**
   - 파일: `ai/tutor/AiTutorService.java`
   - AI Gateway의 스트리밍 API 호출 → 토큰 단위로 `SseEmitter.send()` 실행
   - 이벤트 타입: `data`(토큰), `done`(완료), `error`(에러)

3. **스트리밍 응답 PII Output 스캔**
   - 파일: `ai/tutor/AiTutorService.java`
   - 스트리밍 완료 후 전체 응답을 PII Output Scanner로 검사
   - LLM이 새로 생성한 PII 감지 시 마스킹 + 감사 로그 기록
   - 스트리밍 중에는 버퍼링하여 전체 응답 수집

4. **응답 저장 + 비용 기록**
   - 파일: `ai/tutor/AiTutorService.java`
   - 스트리밍 완료 후 `ai_chat_messages`에 ASSISTANT 메시지 저장
   - `ai_cost_logs`에 토큰 수, 비용, 모델명, cache_hit 기록

#### 완료 기준

- [ ] 토큰 단위 실시간 스트리밍이 프론트엔드에서 표시됨
- [ ] 스트리밍 중 연결 끊김 시 정상 정리(cleanup)
- [ ] 스트리밍 완료 후 PII Output 스캔이 실행됨
- [ ] 응답 완료 후 메시지 + 비용 로그 저장

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Layer 3: System Prompt 격리 (노출 금지)

---

### T-412: 3단계 LevelingService

> **담당**: Backend (AI) | **선행**: T-410 | **관련 FR**: FR-TUTOR-03

#### 서브 스텝

1. **LevelingService 구현**
   - 파일: `ai/tutor/LevelingService.java`
   - `concept_mastery` 테이블에서 해당 과목의 평균 mastery_score 조회
   - 레벨 결정 로직:
     - Level 1 (< 0.4): 비유, 그림, 쉬운 설명
     - Level 2 (0.4 ~ 0.7): 개념 + 코드 예시, 원리 + 실무 실수
     - Level 3 (>= 0.7): 내부 구현, 소스 코드, 트레이드오프

2. **레벨별 System Prompt 템플릿 정의**
   - 파일: `ai/prompt/templates/tutor_level1.mustache`, `tutor_level2.mustache`, `tutor_level3.mustache`
   - 각 레벨에 맞는 응답 스타일, 용어 수준, 예시 유형을 지시
   - `prompt_versions` 테이블에 등록 (name=tutor_level1/2/3, is_active=true)

3. **동적 레벨 전환**
   - 파일: `ai/tutor/LevelingService.java`
   - 세션 중에도 mastery_score 변동 시 레벨 재계산 (퀴즈 완료 이벤트 등)
   - 레벨 변경 시 System Prompt만 교체, 대화 컨텍스트는 유지

#### 완료 기준

- [ ] mastery < 0.4일 때 Level 1 프롬프트가 선택됨
- [ ] mastery 0.4~0.7일 때 Level 2 프롬프트가 선택됨
- [ ] mastery >= 0.7일 때 Level 3 프롬프트가 선택됨
- [ ] 프롬프트가 `ai/prompt/templates/`에서 관리됨
- [ ] `prompt_versions` 테이블과 연동

#### 규칙 체크리스트

- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Layer 3: System Prompt 격리 (노출 금지)
- [ ] DTO는 Java record 사용

---

### T-413: ShortTermMemory + LongTermMemory

> **담당**: Backend (AI) | **선행**: T-410 | **관련 FR**: FR-TUTOR-04

#### 서브 스텝

1. **ShortTermMemory 구현 (Redis)**
   - 파일: `ai/tutor/ShortTermMemory.java`
   - Redis 키: `session:chat:{sessionId}` → List 타입
   - 최근 10턴(USER+ASSISTANT 쌍) 유지, 초과 시 가장 오래된 턴 삭제
   - TTL 24h 자동 만료

2. **LongTermMemory 구현 (MySQL)**
   - 파일: `ai/tutor/LongTermMemory.java`
   - `concept_mastery` 테이블에서 해당 사용자의 개념별 숙련도 조회
   - 오답 패턴 분석: `quiz_attempts`에서 최근 오답 유형/빈도 집계
   - 학습 스타일: `users` 프로필의 학습 선호도 조회

3. **메모리 포맷팅**
   - 파일: `ai/tutor/LongTermMemory.java`
   - LLM에 전달할 Long-term Memory를 구조화된 텍스트로 변환
   - 형식: `[개념 숙련도] {concept}: {mastery_score} / [오답 패턴] {pattern} / [학습 스타일] {style}`

4. **AiTutorService에 메모리 통합**
   - 파일: `ai/tutor/AiTutorService.java`
   - 프롬프트 구성 시 Long-term → RAG Context → Short-term 순서로 결합
   - Short-term에서 PII가 마스킹된 상태로 저장/조회

#### 완료 기준

- [ ] Redis에 최근 10턴만 유지됨 (11번째 메시지 추가 시 1번째 삭제)
- [ ] Redis TTL 24h 후 자동 만료됨
- [ ] Long-term Memory에 concept_mastery + 오답 패턴 + 학습 스타일 포함
- [ ] 프롬프트 구성 순서: System → LongTerm → RAG → ShortTerm → User

#### 규칙 체크리스트

- [ ] PII Masking된 상태로 Redis 저장
- [ ] `course_id` 기반 격리 (mastery 조회 시 해당 과목만)
- [ ] Layer 5: 데이터 격리 확인

---

### T-414: RAG course_id 격리 검색 통합

> **담당**: Backend (AI) | **선행**: T-405 | **관련 FR**: FR-TUTOR-05

#### 서브 스텝

1. **RagOrchestrator 구현**
   - 파일: `ai/rag/RagOrchestrator.java`
   - 전체 RAG 파이프라인 오케스트레이션:
     QueryRewrite → HybridSearch → Reranking → ContextCompression → 결과 반환
   - 모든 단계에서 `courseId` 파라미터를 전파

2. **수강 검증 로직**
   - 파일: `ai/rag/RagOrchestrator.java`
   - RAG 검색 전 `enrollments` 테이블에서 사용자의 해당 courseId 수강 여부 확인
   - 미수강 과목 접근 시 `BusinessException(ErrorCode.COURSE_NOT_ENROLLED)` 발생

3. **RAG 결과 캐시**
   - 파일: `ai/cache/RagResultCache.java`
   - Redis 키: `cache:rag:{queryEmbeddingHash}:{courseId}` — course_id 포함하여 캐시 격리
   - TTL 1h, 캐시 히트 시 RAG 파이프라인 전체 스킵

4. **AiTutorService에 RAG 통합**
   - 파일: `ai/tutor/AiTutorService.java`
   - 사용자 질문 수신 → RagOrchestrator.search(query, courseId) → 압축된 컨텍스트를 프롬프트에 삽입

#### 완료 기준

- [ ] 수강하지 않은 강의의 콘텐츠가 검색 결과에 절대 포함되지 않음
- [ ] 수강 검증 실패 시 적절한 에러 응답 반환
- [ ] RAG 캐시 키에 course_id가 포함되어 캐시 격리됨
- [ ] RAG 결과가 AI 튜터 프롬프트에 올바르게 삽입됨

#### 규칙 체크리스트

- [ ] `course_id` 기반 격리 (다른 강의 데이터 노출 금지)
- [ ] Semantic Cache 적용 가능 여부 검토
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Layer 5: 데이터 격리 (`course_id` 기반)

---

### T-415: 응답 피드백 API

> **담당**: Backend (AI) | **선행**: T-410 | **관련 FR**: FR-TUTOR-06

#### 서브 스텝

1. **피드백 API 엔드포인트**
   - 파일: `ai/tutor/AiTutorController.java`
   - `PATCH /api/v1/ai/chat/messages/{messageId}/feedback`
   - Request Body: `FeedbackRequest` record (`feedbackType`: THUMBS_UP / THUMBS_DOWN, `reason`: optional String)

2. **피드백 저장 로직**
   - 파일: `ai/tutor/AiTutorService.java`
   - `ChatMessage.applyFeedback(feedbackType)` 비즈니스 메서드로 상태 변경
   - ASSISTANT 역할 메시지에만 피드백 가능 (USER 메시지에 피드백 시 에러)

3. **피드백 이벤트 발행 (Outbox)**
   - 파일: `ai/tutor/AiTutorService.java`
   - THUMBS_DOWN 피드백 시 `TutorFeedbackEvent` 발행 → 품질 모니터링용
   - `OutboxPublisher.publish(event, "tutor-feedback-events")`

#### 완료 기준

- [ ] THUMBS_UP/THUMBS_DOWN 피드백이 정상 저장됨
- [ ] USER 메시지에 피드백 시도 시 적절한 에러 반환
- [ ] THUMBS_DOWN 시 Outbox 이벤트 발행
- [ ] `ApiResponse<T>` 래핑 응답

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] DTO는 Java record 사용

---

### T-416: 추천 질문 생성

> **담당**: Backend (AI) | **선행**: T-410 | **관련 FR**: FR-TUTOR-07

#### 서브 스텝

1. **SuggestedQuestion 서비스 구현**
   - 파일: `ai/tutor/SuggestedQuestion.java`
   - 레슨 콘텐츠 기반으로 학습자에게 유용한 질문 3개 생성
   - AI Gateway 경유 LLM 호출 (Tier 1 Haiku — 비용 절감)

2. **추천 질문 프롬프트 템플릿**
   - 파일: `ai/prompt/templates/suggested_questions.mustache`
   - 지시: "레슨 콘텐츠와 학습자 레벨에 맞는 심화 질문 3개 생성. JSON 배열로 반환"
   - 입력 변수: `{{lessonContent}}`, `{{learnerLevel}}`, `{{conceptMastery}}`

3. **API 엔드포인트**
   - 파일: `ai/tutor/AiTutorController.java`
   - `GET /api/v1/ai/chat/lessons/{lessonId}/suggested-questions`
   - 응답: `ApiResponse<List<SuggestedQuestionDto>>` (question, difficulty, relatedConcept)

4. **캐시 적용**
   - 파일: `ai/tutor/SuggestedQuestion.java`
   - Redis 키: `cache:suggested:{lessonId}:{level}`, TTL 6h
   - 동일 레슨 + 동일 레벨에서 반복 호출 시 캐시 반환

#### 완료 기준

- [ ] 레슨별 3개 추천 질문이 생성됨
- [ ] 학습자 레벨에 따라 질문 난이도가 조절됨
- [ ] 캐시 히트 시 LLM 호출이 스킵됨
- [ ] `ApiResponse<T>` 래핑 응답

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Layer 4: Output Validation (JSON 스키마 검증)

---

### T-417: AI 튜터 채팅 UI

> **담당**: Frontend | **선행**: T-119 | **관련 FR**: FR-TUTOR-02

#### 서브 스텝

1. **AI 채팅 페이지 구현**
   - 파일: `learnflow-web/src/pages/AiTutorPage.tsx`
   - shadcn/ui 기반 채팅 레이아웃: 좌측 레슨 콘텐츠, 우측 채팅 패널
   - 메시지 버블(USER/ASSISTANT), 타이핑 인디케이터, 추천 질문 칩

2. **SSE 스트리밍 핸들러**
   - 파일: `learnflow-web/src/lib/sseHandler.ts`
   - `EventSource` 또는 `fetch` + `ReadableStream`으로 SSE 수신
   - 토큰 도착 시 실시간 UI 업데이트, 에러 이벤트 시 재시도/에러 표시

3. **채팅 상태 관리**
   - 파일: `learnflow-web/src/stores/chatStore.ts`
   - Zustand 스토어: `sessions`, `messages`, `isStreaming`, `currentSessionId`
   - 서버 상태(세션 목록, 메시지 이력)는 TanStack Query 커스텀 훅으로 관리

4. **TanStack Query 훅**
   - 파일: `learnflow-web/src/hooks/useAiChat.ts`
   - `useChatSessions(courseId)` — 세션 목록 조회
   - `useChatMessages(sessionId)` — 메시지 이력 조회
   - `useSuggestedQuestions(lessonId)` — 추천 질문 조회
   - `useSendMessage(sessionId)` — 메시지 전송 mutation (SSE 시작 트리거)

5. **피드백 UI 컴포넌트**
   - 파일: `learnflow-web/src/components/chat/MessageFeedback.tsx`
   - ASSISTANT 메시지 하단에 thumbs-up/down 아이콘 버튼
   - 클릭 시 `PATCH /api/v1/ai/chat/messages/{id}/feedback` 호출

#### 완료 기준

- [ ] SSE 토큰 스트리밍이 실시간으로 화면에 표시됨
- [ ] 추천 질문 클릭 시 해당 질문이 채팅 입력란에 자동 입력됨
- [ ] 피드백(thumbs-up/down) 동작 확인
- [ ] 네트워크 에러 시 사용자에게 재시도 안내 표시
- [ ] Zustand(클라이언트) + TanStack Query(서버) 역할 분리

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스
- [ ] AI 튜터 채팅: SSE 스트리밍

---

## Week 16: AI 퀴즈 + AI 채점

### T-418: AiQuizGenerator

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-QUIZ-01

#### 서브 스텝

1. **AiQuizGenerator 서비스 구현**
   - 파일: `ai/evaluation/AiQuizGenerator.java`
   - 레슨/섹션 콘텐츠 기반으로 퀴즈 문제 자동 생성
   - Bloom's Taxonomy 레벨(`bloom_level`: REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE) 배분

2. **퀴즈 생성 프롬프트 템플릿**
   - 파일: `ai/prompt/templates/quiz_generation.mustache`
   - 지시: "레슨 콘텐츠를 기반으로 bloom_level 배분에 따라 퀴즈 문제 생성. JSON 배열로 반환"
   - 입력 변수: `{{lessonContent}}`, `{{bloomDistribution}}`, `{{questionCount}}`, `{{difficulty}}`
   - 출력 형식: `[{ question, options, correctAnswer, bloomLevel, explanation }]`

3. **RAG 컨텍스트 연동**
   - 파일: `ai/evaluation/AiQuizGenerator.java`
   - 퀴즈 생성 시 RagOrchestrator로 관련 콘텐츠 검색 후 프롬프트에 포함
   - `course_id` 격리 적용

4. **Output Validation**
   - 파일: `ai/evaluation/AiQuizGenerator.java`
   - LLM 응답을 JSON 파싱 후 필수 필드 검증 (question, correctAnswer 존재 여부)
   - 검증 실패 시 1회 재시도, 2회 실패 시 에러 반환

5. **API 엔드포인트**
   - 파일: `ai/evaluation/AiQuizController.java`
   - `POST /api/v1/ai/quizzes/generate` — 퀴즈 자동 생성 (INSTRUCTOR 권한)
   - Request Body: `QuizGenerateRequest` record (lessonId, questionCount, bloomDistribution)

#### 완료 기준

- [ ] bloom_level 배분에 따라 문제가 생성됨
- [ ] 생성된 퀴즈가 JSON 스키마 검증을 통과함
- [ ] course_id 격리가 적용된 RAG 컨텍스트 사용
- [ ] 프롬프트가 `ai/prompt/templates/`에서 관리됨
- [ ] INSTRUCTOR 권한 검증

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Layer 4: Output Validation (JSON 스키마 검증)
- [ ] `course_id` 기반 격리

---

### T-419: AiGrading

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-QUIZ-03

#### 서브 스텝

1. **AiGrading 서비스 구현**
   - 파일: `ai/evaluation/AiGrading.java`
   - 학습자 답안 + 루브릭 기준으로 AI 채점 수행
   - AI Gateway 경유 LLM 호출 (Tier 2~3 Sonnet — 정확성 중요)

2. **채점 프롬프트 템플릿**
   - 파일: `ai/prompt/templates/grading.mustache`
   - 지시: "루브릭 기준에 따라 답안을 채점. 점수, 피드백, rubric_coverage를 JSON으로 반환"
   - 입력 변수: `{{question}}`, `{{rubric}}`, `{{studentAnswer}}`, `{{maxScore}}`
   - 출력 형식: `{ score, maxScore, feedback, rubricCoverage, detailedScores[] }`

3. **Determinism 측정 (3회 호출)**
   - 파일: `ai/evaluation/AiGrading.java`
   - 동일 답안에 대해 3회 독립 채점 수행 (temperature=0)
   - 3회 점수의 표준편차를 determinism 지표로 활용 (낮을수록 높은 결정성)

4. **PII 처리**
   - 파일: `ai/evaluation/AiGrading.java`
   - 학생 답안 Input PII Masking → LLM 채점 → Output PII 스캔 + Demasking
   - 피드백에 학생 개인정보가 포함되지 않도록 Output 검사

5. **GradingResult DTO 정의**
   - 파일: `ai/evaluation/dto/GradingResult.java`
   - Java record: `score`, `maxScore`, `feedback`, `rubricCoverage`, `determinismScore`, `consistencyScore`, `rawScores[]`(3회), `modelUsed`

#### 완료 기준

- [ ] 루브릭 기준에 따라 점수 + 피드백이 생성됨
- [ ] 3회 독립 채점 결과의 표준편차가 계산됨
- [ ] PII Input + Output 양방향 처리 적용
- [ ] 점수가 0~maxScore 범위 내인지 Output Validation
- [ ] AI Gateway 경유 LLM 호출

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Layer 4: Output Validation (점수 범위 검증)

---

### T-420: ConfidenceScorer

> **담당**: Backend (AI) | **선행**: T-419 | **관련 FR**: FR-QUIZ-04

#### 서브 스텝

1. **ConfidenceScorer 구현**
   - 파일: `ai/evaluation/ConfidenceScorer.java`
   - Confidence Score 계산 공식:
     - `rubric_match` * 0.3 (루브릭 항목 매칭률)
     - `determinism` * 0.25 (3회 채점 일관성, 1 - normalized_stddev)
     - `consistency` * 0.25 (3회 점수 중 최대-최소 차이의 역수)
     - `rubric_coverage` * 0.2 (루브릭 항목 중 AI가 평가한 비율)

2. **자동 확정 / Manual Review 분기**
   - 파일: `ai/evaluation/ConfidenceScorer.java`
   - Confidence >= 0.8 → `submission.confirm()` 호출 → 상태: CONFIRMED
   - Confidence < 0.8 → `ManualReviewQueue.enqueue(submission)` 호출 → 상태: MANUAL_REVIEW

3. **ManualReviewQueue 구현**
   - 파일: `ai/evaluation/ManualReviewQueue.java`
   - 강사 Manual Review 대기열에 제출물 추가
   - `GET /api/v1/instructor/review-queue` — 강사가 검토 대기 목록 조회
   - `PATCH /api/v1/instructor/review-queue/{submissionId}/confirm` — 강사 최종 확정

4. **Outbox 이벤트 발행**
   - 파일: `ai/evaluation/ConfidenceScorer.java`
   - CONFIRMED → `GradingConfirmedEvent` 발행 (알림용)
   - MANUAL_REVIEW → `ManualReviewRequiredEvent` 발행 (강사 알림용)

#### 완료 기준

- [ ] Confidence Score가 4개 가중치 합산으로 정확히 계산됨
- [ ] Confidence >= 0.8일 때만 자동 확정됨
- [ ] Confidence < 0.8일 때 Manual Review Queue에 이관됨
- [ ] 강사가 Manual Review Queue에서 검토/확정 가능
- [ ] AI 채점을 Confidence 체크 없이 자동 확정하는 경로 없음

#### 규칙 체크리스트

- [ ] Confidence Score 계산 (rubric_match 0.3 + determinism 0.25 + consistency 0.25 + rubric_coverage 0.2)
- [ ] Confidence >= 0.8 → 자동 확정 (CONFIRMED)
- [ ] Confidence < 0.8 → Manual Review Queue 이관
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용

---

### T-421: AiGradingWorker

> **담당**: Backend (AI) | **선행**: T-420 | **관련 FR**: FR-QUIZ-02

#### 서브 스텝

1. **QuizSubmitted / AssignmentSubmitted 이벤트 정의**
   - 파일: `global/event/events/QuizSubmittedEvent.java`, `AssignmentSubmittedEvent.java`
   - Java record: `submissionId`, `userId`, `courseId`, `questionId`/`assignmentId`, `answer`, `timestamp`

2. **AiGradingWorker Kafka Consumer 구현**
   - 파일: `worker/AiGradingWorker.java`
   - `@KafkaListener(topics = {"quiz-submitted", "assignment-submitted"})` 이벤트 소비
   - 처리 흐름: 이벤트 수신 → 제출물 조회 → AiGrading 채점 → ConfidenceScorer 판정 → 결과 저장

3. **멱등성 보장**
   - 파일: `worker/AiGradingWorker.java`
   - `dedup_key` = `{submissionId}:{eventType}:{timestamp}`
   - 이미 채점된 제출물(상태 != SUBMITTED)은 스킵

4. **DLQ 처리**
   - 파일: `worker/AiGradingWorker.java`
   - 채점 실패 시 3회 재시도 후 DLQ 토픽으로 전송
   - DLQ 전송 시 실패 사유 + submissionId 로깅

5. **제출 서비스에서 Outbox 이벤트 발행**
   - 파일: `domain/quiz/service/QuizAttemptService.java`, `domain/assignment/service/AssignmentSubmissionService.java`
   - 퀴즈/과제 제출 시 `OutboxPublisher.publish(event, "quiz-submitted"/"assignment-submitted")`
   - 비즈니스 데이터 저장 + outbox_events INSERT가 단일 `@Transactional`

#### 완료 기준

- [ ] 퀴즈/과제 제출 시 자동으로 AI 채점이 트리거됨
- [ ] 채점 결과에 Confidence Score가 포함됨
- [ ] 동일 제출물 중복 채점 방지 (dedup_key + 상태 체크)
- [ ] 3회 실패 시 DLQ 전송
- [ ] Outbox 경유 이벤트 발행 (KafkaTemplate 직접 호출 없음)

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음
- [ ] AI Gateway 경유 확인
- [ ] FinOps 비용 기록

---

### T-422: OutputValidator

> **담당**: Backend (AI) | **선행**: T-419 | **관련 FR**: NFR-SEC-07

#### 서브 스텝

1. **OutputValidator 서비스 구현**
   - 파일: `ai/evaluation/OutputValidator.java`
   - LLM 응답의 구조적 유효성 검증을 담당하는 공통 검증기

2. **점수 범위 검증**
   - 파일: `ai/evaluation/OutputValidator.java`
   - `validateScoreRange(score, minScore, maxScore)` — 점수가 유효 범위 내인지 확인
   - 범위 초과 시 `OutputValidationException` 발생

3. **JSON 스키마 검증**
   - 파일: `ai/evaluation/OutputValidator.java`
   - `validateJsonSchema(jsonString, schemaClass)` — LLM JSON 응답을 지정 DTO 클래스로 파싱 + 필수 필드 검증
   - Jackson ObjectMapper 사용, 파싱 실패 시 1회 재시도 (LLM 재호출)

4. **퀴즈/채점 응답 전용 검증기**
   - 파일: `ai/evaluation/OutputValidator.java`
   - `validateQuizOutput(quizJson)` — question, correctAnswer 필수 필드 검증
   - `validateGradingOutput(gradingJson)` — score, feedback, rubricCoverage 필수 필드 검증
   - bloom_level이 유효한 enum 값인지 검증

#### 완료 기준

- [ ] 유효하지 않은 점수(음수, maxScore 초과)가 거부됨
- [ ] 필수 필드 누락 JSON이 감지됨
- [ ] 검증 실패 시 1회 LLM 재호출 후 최종 실패 시 에러 반환
- [ ] 모든 AI 채점/퀴즈 생성 응답이 OutputValidator를 통과

#### 규칙 체크리스트

- [ ] Layer 4: Output Validation (점수 범위 / JSON 스키마)
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] DTO는 Java record 사용

---

### T-423: AI 요약 + 플래시카드 API

> **담당**: Backend (AI) | **선행**: T-405 | **관련 FR**: FR-SUMMARY-01~02

#### 서브 스텝

1. **AI 요약 서비스 구현**
   - 파일: `ai/tutor/AiSummaryService.java`
   - 레슨/섹션 콘텐츠를 RAG 파이프라인으로 검색 후 요약 생성
   - AI Gateway 경유 LLM 호출 (Tier 2 Sonnet)

2. **요약 프롬프트 템플릿**
   - 파일: `ai/prompt/templates/summarize.mustache`
   - 지시: "핵심 개념 3~5개 + 요약 텍스트 생성. 학습자 레벨에 맞춤"
   - 입력 변수: `{{content}}`, `{{learnerLevel}}`, `{{maxLength}}`

3. **플래시카드 생성 서비스**
   - 파일: `ai/tutor/AiFlashcardService.java`
   - 레슨 콘텐츠 기반 Q&A 형태 플래시카드 5~10개 자동 생성
   - 프롬프트 템플릿: `ai/prompt/templates/flashcard.mustache`
   - 출력 형식: `[{ front, back, concept, difficulty }]`

4. **API 엔드포인트**
   - 파일: `ai/tutor/AiSummaryController.java`
   - `POST /api/v1/ai/summarize/lessons/{lessonId}` — 레슨 요약 생성
   - `POST /api/v1/ai/flashcards/lessons/{lessonId}` — 플래시카드 생성
   - LEARNER 권한, course_id 격리 적용

5. **캐시 적용**
   - 파일: `ai/cache/SemanticResponseCache.java`
   - Redis 키: `cache:summary:{lessonId}:{level}`, `cache:flashcard:{lessonId}`, TTL 6h
   - 캐시 히트 시 LLM 호출 스킵, `ai_cost_logs`에 cache_hit=true 기록

#### 완료 기준

- [ ] 레슨별 요약이 학습자 레벨에 맞춰 생성됨
- [ ] 플래시카드가 Q&A 형태로 5~10개 생성됨
- [ ] course_id 격리가 적용됨
- [ ] 캐시 히트 시 LLM 호출 스킵
- [ ] PII Input + Output 처리 적용

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] `course_id` 기반 격리
- [ ] Semantic Cache 적용
- [ ] Layer 4: Output Validation (JSON 스키마 검증)
- [ ] `ApiResponse<T>` 래핑 응답 사용

---

### T-424: 단위 테스트 (RAG, Tutor, Grading)

> **담당**: Backend | **선행**: T-423 | **관련 FR**: NFR-MAINT-03

#### 서브 스텝

1. **RAG 파이프라인 테스트**
   - 파일: `ai/rag/SemanticChunkingTest.java`, `ai/rag/HybridSearchTest.java`, `ai/rag/RerankingTest.java`
   - SemanticChunking: 청크 크기 제한, 코드 블록 보존, 의미 경계 분리 검증
   - HybridSearch: course_id 격리, RRF 점수 계산, INACTIVE 필터링 검증
   - Reranking: Top 20 → Top 5 필터링, Fallback 동작 검증

2. **AI 튜터 테스트**
   - 파일: `ai/tutor/AiTutorServiceTest.java`, `ai/tutor/LevelingServiceTest.java`, `ai/tutor/ShortTermMemoryTest.java`
   - AiTutorService: 프롬프트 구성 순서 검증 (System → LongTerm → RAG → ShortTerm → User)
   - LevelingService: mastery 기반 레벨 결정 경계값 테스트 (0.39→L1, 0.40→L2, 0.69→L2, 0.70→L3)
   - ShortTermMemory: 10턴 초과 시 삭제, TTL 설정 검증

3. **AI 채점 테스트**
   - 파일: `ai/evaluation/AiGradingTest.java`, `ai/evaluation/ConfidenceScorerTest.java`, `ai/evaluation/OutputValidatorTest.java`
   - ConfidenceScorer: 가중치 합산 정확성, >= 0.8 자동확정, < 0.8 Manual Review 검증
   - OutputValidator: 점수 범위 초과 거부, JSON 필수 필드 누락 감지 검증
   - AiGrading: 3회 채점 determinism 계산 검증

4. **EmbeddingWorker / AiGradingWorker 테스트**
   - 파일: `worker/EmbeddingWorkerTest.java`, `worker/AiGradingWorkerTest.java`
   - 멱등성: 동일 dedup_key 재수신 시 스킵 검증
   - DLQ: 3회 실패 시 DLQ 전송 검증

5. **통합 테스트 (RAG E2E)**
   - 파일: `ai/rag/RagOrchestrationIntegrationTest.java`
   - `@SpringBootTest` — 콘텐츠 생성 → 임베딩 → 검색 → Re-ranking → 압축 → 응답 전체 흐름 검증
   - course_id 격리가 E2E에서 유지되는지 검증

#### 완료 기준

- [ ] RAG 테스트: SemanticChunking, HybridSearch, Reranking 각 2개 이상 테스트 케이스
- [ ] 튜터 테스트: LevelingService 경계값 4개 케이스, ShortTermMemory 2개 케이스
- [ ] 채점 테스트: ConfidenceScorer 가중치 3개 케이스, OutputValidator 2개 케이스
- [ ] Worker 테스트: 멱등성 + DLQ 각 1개 케이스
- [ ] 통합 테스트: RAG E2E 1개 케이스 (course_id 격리 포함)

#### 규칙 체크리스트

- [ ] `@WebMvcTest`(컨트롤러) + `@DataJpaTest`(리포지토리) + `@SpringBootTest`(통합) 구분
- [ ] Mock 사용: AI Gateway, LLM Client는 Mock 처리
- [ ] 테스트 데이터에 실제 PII 사용 금지

---

## 보안 7 Layer 검증 요약 (Phase 4 전체)

| Layer | 검증 항목 | 관련 Task |
|-------|-----------|-----------|
| Layer 1 | 입력 길이 제한 + 위험 패턴 감지 | T-410, T-411 |
| Layer 2 | PII Masking Input + Output 양방향 | T-407, T-408, T-410, T-411, T-418, T-419, T-423 |
| Layer 3 | System Prompt 격리 (노출 금지) | T-410, T-411, T-412 |
| Layer 4 | Output Validation (점수 범위 / JSON 스키마) | T-418, T-419, T-422, T-423 |
| Layer 5 | 데이터 격리 (course_id 기반 RAG) | T-403, T-405, T-414, T-418, T-423 |
| Layer 6 | Tool 제한 (DB 직접 조회 / 외부 URL 차단) | T-410 |
| Layer 7 | FinOps Kill-switch | T-407, T-408, T-410, T-418, T-419, T-423 |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-04-03 | 초안 작성: Phase 4 전체 24개 Task 세부 워크플로우 정의 |
