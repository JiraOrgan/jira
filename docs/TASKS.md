# LearnFlow AI - Task 목록

> **버전**: v4.0
> **작성일**: 2026-04-02
> **원본 문서**: [Project-Control-Hub/documents - 00-스케줄 v4.0](https://github.com/Project-Control-Hub/documents/blob/main/00-스케줄/00-스케줄_v4.0.md) 기반
> **연결 문서**: [PHASE.md](PHASE.md) | [WORKFLOW.md](WORKFLOW.md) | [PRD.md](PRD.md)

---

## Task 상태 정의

| 상태 | 설명 |
|------|------|
| TODO | 아직 시작하지 않음 |
| IN_PROGRESS | 진행 중 |
| REVIEW | 코드 리뷰 중 |
| DONE | 완료 |
| BLOCKED | 선행 작업 대기 |

---

## Phase 1 — 기반 구축 Tasks

> **Sprint Goal**: MVP 기반 인프라 및 핵심 도메인 구축 (Week 1~4)

### Week 1: DB 설계 + 엔티티 + JWT 인증

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-101 | DB 설계 (ERD 확정, Outbox 테이블 포함) | Backend | DONE | - | - |
| T-102 | 엔티티 구현 (users, courses, sections, lessons, enrollments, outbox_events) | Backend | DONE | - | T-101 |
| T-103 | JWT 인증 구현 (로그인/회원가입/토큰 갱신) | Backend | DONE | FR-AUTH-01~03 | T-102 |
| T-104 | CustomUserDetails + Security Filter Chain | Backend | DONE | FR-AUTH-04 | T-103 |
| T-105 | 비밀번호 BCrypt 암호화 + 로그인 실패 잠금 | Backend | DONE | NFR-SEC-02 | T-103 |
| T-106 | Refresh Token Redis 저장 | Backend | DONE | FR-AUTH-03 | T-103 |
| T-107 | Flyway 마이그레이션 V1 (초기 스키마) | Backend | DONE | NFR-MAINT-02 | T-101 |

### Week 2: 강의 CRUD + 수강 + 파일 업로드

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-108 | 강의/섹션/레슨 CRUD API | Backend | DONE | FR-COURSE-01 | T-104 |
| T-109 | 수강 신청 + 진도율 API | Backend | DONE | FR-COURSE-04 | T-108 |
| T-110 | 파일 업로드 (MinIO/S3 연동) | Backend | DONE | FR-COURSE-02 | T-108 |
| T-111 | 공통 응답 ApiResponse + GlobalExceptionHandler | Backend | DONE | - | T-102 |
| T-112 | Swagger/OpenAPI 설정 | Backend | DONE | NFR-MAINT-01 | T-108 |

### Week 3: 콘텐츠 관리

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-113 | TipTap 에디터 기반 레슨 편집 UI | Frontend | DONE | FR-COURSE-02 | T-116 |
| T-114 | 영상 업로드/스트리밍 API | Backend | DONE | FR-COURSE-02 | T-110 |
| T-115 | 비로그인 강의 목록/상세 조회 API | Backend | DONE | FR-COURSE-06 | T-108 |

### Week 4: React 레이아웃 + 강의 UI

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-116 | React 프로젝트 초기 설정 (Vite + Zustand + TanStack Query + Tailwind + shadcn/ui) | Frontend | DONE | - | - |
| T-117 | 공통 레이아웃 (GNB, 사이드바, 라우팅) | Frontend | DONE | - | T-116 |
| T-118 | 로그인/회원가입 화면 | Frontend | DONE | FR-AUTH-01~02 | T-117 |
| T-119 | 강의 목록/상세 페이지 | Frontend | DONE | FR-COURSE-06 | T-117 |
| T-120 | 토큰 관리 (Axios Interceptor) | Frontend | DONE | FR-AUTH-03 | T-118 |
| T-121 | 단위 테스트 (Auth, Course, Enrollment) | Backend | DONE | NFR-MAINT-03 | T-109 |

---

## Phase 2 — 핵심 학습 Tasks

> **Sprint Goal**: 학습 진도, 평가, 온보딩, 커뮤니티 (Week 5~8)

### Week 5: 학습 진도 추적

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-201 | learning_activities 테이블 + 진도 추적 API | Backend | TODO | FR-COURSE-05 | T-109 |
| T-202 | 레슨 완료 처리 + LessonCompleted 이벤트 정의 | Backend | TODO | FR-COURSE-05 | T-201 |

### Week 6: 퀴즈/과제 + 채점 이의 제기

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-203 | 퀴즈 엔티티 + CRUD API | Backend | TODO | FR-QUIZ-02 | T-108 |
| T-204 | 과제 제출 엔티티 + API | Backend | TODO | FR-QUIZ-08 | T-108 |
| T-205 | 채점 이의 제기(Appeal) API + GradingAppeal 이벤트 | Backend | TODO | FR-QUIZ-05 | T-203 |
| T-206 | Manual Review Queue API (강사용) | Backend | TODO | FR-QUIZ-06 | T-205 |
| T-207 | 퀴즈 결과 + AI 피드백 조회 API | Backend | TODO | FR-QUIZ-07 | T-203 |
| T-208 | 채점 이의 제기 UI | Frontend | TODO | FR-QUIZ-05 | T-119 |

### Week 7: 온보딩 진단 테스트

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-209 | diagnostic_tests 엔티티 + 진단 테스트 API (Bloom's 배분 5문항) | Backend | TODO | FR-ONBOARD-01 | T-108 |
| T-210 | 진단 결과 → 초기 mastery + confidence_weight=0.7 | Backend | TODO | FR-ONBOARD-02 | T-209 |
| T-211 | 자가 진단 API (confidence_weight=0.3) | Backend | TODO | FR-ONBOARD-03 | T-209 |

### Week 8: 커뮤니티

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-212 | 토론 게시판 + Q&A CRUD API | Backend | TODO | FR-COMMUNITY-01~02 | T-108 |
| T-213 | 커뮤니티 UI | Frontend | TODO | FR-COMMUNITY-01~02 | T-119 |
| T-214 | 단위 테스트 (Quiz, Assignment, Onboarding, Community) | Backend | TODO | NFR-MAINT-03 | T-212 |

---

## Phase 3 — 이벤트 인프라 + AI 기반 Tasks

> **Sprint Goal**: 프로덕션 수준 이벤트 인프라 및 AI 게이트웨이 (Week 9~12)

### Week 9: Outbox + Kafka + Consumer

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-301 | OutboxPublisher 구현 + outbox_events 테이블 | Backend | TODO | FR-EVENT-01 | T-102 |
| T-302 | Outbox Relay (Polling + ShedLock) | Backend | TODO | FR-EVENT-02 | T-301 |
| T-303 | Kafka Consumer 프레임워크 + dedup_key 멱등성 | Backend | TODO | FR-EVENT-03 | T-302 |
| T-304 | DLQ 처리 (Outbox DEAD_LETTER + Consumer DLQ 토픽) | Backend | TODO | FR-EVENT-04 | T-303 |
| T-305 | EmbeddingWorker, AiGradingWorker, AnalyticsWorker, NotificationWorker 스캐폴딩 | Backend | TODO | FR-EVENT-05 | T-303 |

### Week 10: AI Gateway + PII

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-306 | AiGatewayController + ModelRouter | Backend (AI) | TODO | - | T-301 |
| T-307 | PiiMaskingService (Regex + NER) | Backend (AI) | TODO | FR-PII-01 | T-306 |
| T-308 | PiiDemaskingService + Redis session-scoped 매핑 | Backend (AI) | TODO | FR-PII-02 | T-307 |
| T-309 | PiiOutputScanner (v4.0 Output PII 스캔) | Backend (AI) | TODO | FR-PII-03 | T-307 |
| T-310 | LlmClient 추상화 + ClaudeApiClient + OpenAiApiClient | Backend (AI) | TODO | - | T-306 |
| T-311 | Circuit Breaker (Resilience4j) — Claude → OpenAI Fallback | Backend (AI) | TODO | NFR-AVAIL-02 | T-310 |

### Week 11: Distributed Tracing (OTel)

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-312 | OTel + Zipkin 연동 + Sampling 설정 (env별 차등) | Backend (Infra) | TODO | FR-OBS-01 | T-306 |
| T-313 | Business Context Span Attributes (user.id, ai.cost_usd, rag.* 등) | Backend (Infra) | TODO | FR-OBS-02 | T-312 |
| T-314 | Kafka Consumer Lag Prometheus 메트릭 | Backend (Infra) | TODO | FR-OBS-04 | T-303 |

### Week 12: FinOps

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-315 | CostTracking + ai_cost_logs 테이블 | Backend (AI) | TODO | FR-FINOPS-01 | T-306 |
| T-316 | Unit Economics 산출 API | Backend (AI) | TODO | FR-FINOPS-02 | T-315 |
| T-317 | KillSwitch (Soft/Hard Limit + 동적 라우팅) | Backend (AI) | TODO | FR-FINOPS-03~04 | T-315 |
| T-318 | SemanticResponseCache (유사도 > 0.95 캐시) | Backend (AI) | TODO | FR-FINOPS-05 | T-306 |
| T-319 | AnomalyDetection (이상 패턴 감지) | Backend (AI) | TODO | FR-FINOPS-06 | T-315 |
| T-320 | 단위 테스트 (Outbox, PII, FinOps, Gateway) | Backend | TODO | NFR-MAINT-03 | T-319 |

---

## Phase 4 — RAG + AI 튜터 Tasks

> **Sprint Goal**: RAG v4.0 파이프라인 및 AI 튜터 완성 (Week 13~16)

### Week 13: Semantic Chunking + Hybrid Search

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-401 | SemanticChunking (Recursive + Semantic Boundary Detection) | Backend (AI) | TODO | FR-RAG-01 | T-305 |
| T-402 | chunk_hash (SHA-256) 중복 스킵 로직 | Backend (AI) | TODO | FR-RAG-02 | T-401 |
| T-403 | content_embeddings 테이블 + pgvector 연동 | Backend (AI) | TODO | FR-RAG-01 | T-401 |
| T-404 | EmbeddingWorker 구현 (ContentCreated/Updated 이벤트 소비) | Backend (AI) | TODO | FR-COURSE-03 | T-403 |
| T-405 | HybridSearch (pgvector Top 20 + ES BM25 Top 20 + RRF) | Backend (AI) | TODO | FR-RAG-04 | T-403 |

### Week 14: Re-ranking + Query Rewrite + Compression

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-406 | CrossEncoder Re-ranking (ms-marco-MiniLM) → Top 5 | Backend (AI) | TODO | FR-RAG-06 | T-405 |
| T-407 | QueryRewrite (사용자 질문 → 검색 최적화 쿼리) | Backend (AI) | TODO | FR-RAG-05 | T-405 |
| T-408 | ContextCompression (핵심 문장 추출, 토큰 절감) | Backend (AI) | TODO | FR-RAG-07 | T-406 |
| T-409 | ChunkVersioning + Soft Delete (ACTIVE/INACTIVE) | Backend (AI) | TODO | FR-RAG-03 | T-403 |

### Week 15: AI 튜터

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-410 | AiTutorService + 채팅 세션 관리 | Backend (AI) | TODO | FR-TUTOR-01 | T-306 |
| T-411 | SSE 스트리밍 응답 (AiTutorController) | Backend (AI) | TODO | FR-TUTOR-02 | T-410 |
| T-412 | 3단계 LevelingService (concept_mastery 기반) | Backend (AI) | TODO | FR-TUTOR-03 | T-410 |
| T-413 | ShortTermMemory (Redis 10턴 TTL 24h) + LongTermMemory (MySQL) | Backend (AI) | TODO | FR-TUTOR-04 | T-410 |
| T-414 | RAG course_id 격리 검색 통합 | Backend (AI) | TODO | FR-TUTOR-05 | T-405 |
| T-415 | 응답 피드백 API (thumbs-up/down) | Backend (AI) | TODO | FR-TUTOR-06 | T-410 |
| T-416 | 추천 질문 생성 (레슨 기반 3개) | Backend (AI) | TODO | FR-TUTOR-07 | T-410 |
| T-417 | AI 튜터 채팅 UI (SSE 스트리밍) | Frontend | TODO | FR-TUTOR-02 | T-119 |

### Week 16: AI 퀴즈 + AI 채점

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-418 | AiQuizGenerator (bloom_level 포함 자동 생성) | Backend (AI) | TODO | FR-QUIZ-01 | T-306 |
| T-419 | AiGrading (rubric_coverage + Confidence Score 산출) | Backend (AI) | TODO | FR-QUIZ-03 | T-306 |
| T-420 | ConfidenceScorer (>= 0.8 자동 확정 / < 0.8 Manual Review Queue 이관) | Backend (AI) | TODO | FR-QUIZ-04 | T-419 |
| T-421 | AiGradingWorker (QuizSubmitted/AssignmentSubmitted 이벤트 소비) | Backend (AI) | TODO | FR-QUIZ-02 | T-420 |
| T-422 | OutputValidator (점수 범위/JSON 스키마 검증) | Backend (AI) | TODO | NFR-SEC-07 | T-419 |
| T-423 | AI 요약 + 플래시카드 API | Backend (AI) | TODO | FR-SUMMARY-01~02 | T-405 |
| T-424 | 단위 테스트 (RAG, Tutor, Grading) | Backend | TODO | NFR-MAINT-03 | T-423 |

---

## Phase 5 — 분석 + 품질 관리 Tasks

> **Sprint Goal**: 학습 분석 엔진 및 AI 품질 3층 평가 체계 (Week 17~19)

### Week 17: 학습 분석

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-501 | concept_mastery 테이블 + 자동 갱신 로직 (AnalyticsWorker) | Backend | TODO | FR-ANALYTICS-02 | T-305 |
| T-502 | WeaknessDetection (취약점 분석) | Backend | TODO | FR-ANALYTICS-04 | T-501 |
| T-503 | AI 추천 API (보충 퀴즈, 관련 레슨) | Backend | TODO | FR-ANALYTICS-03 | T-502 |
| T-504 | 학습 분석 대시보드 UI (주간 학습 시간, 숙련도, 취약점 맵) | Frontend | TODO | FR-ANALYTICS-01 | T-119 |
| T-505 | 강사 수강생 분석 API | Backend | TODO | FR-ANALYTICS-06 | T-501 |
| T-506 | Cold Start 연동 (온보딩 → concept_mastery 초기값) | Backend | TODO | FR-ONBOARD-04 | T-210 |

### Week 18: 3층 평가

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-507 | RagasEvaluation (Faithfulness, Context Precision/Recall, Answer Relevancy) — 3회 중앙값 | Backend (AI) | TODO | FR-QUALITY-02 | T-405 |
| T-508 | DeepEvalService (G-Eval + Hallucination Score) | Backend (AI) | TODO | FR-QUALITY-03 | T-507 |
| T-509 | LlmJudge (Faithfulness < 0.7 자동 리포트) | Backend (AI) | TODO | FR-QUALITY-03 | T-508 |
| T-510 | Importance Sampling 배치 스케줄러 | Backend (AI) | TODO | FR-QUALITY-04 | T-507 |
| T-511 | 3층 평가 관리자 대시보드 API | Backend | TODO | FR-QUALITY-01 | T-509 |

### Week 19: A/B 테스트 + 프롬프트 관리

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-512 | AbTestService (생성/종료 + mastery_delta 측정) | Backend (AI) | TODO | FR-QUALITY-05 | T-507 |
| T-513 | PromptVersionService (버전 관리 + 즉시 롤백) | Backend (AI) | TODO | FR-QUALITY-06 | T-306 |
| T-514 | A/B 테스트 관리 UI | Frontend | TODO | FR-QUALITY-05 | T-504 |
| T-515 | 단위 테스트 (Analytics, RAGAS, DeepEval, A/B) | Backend | TODO | NFR-MAINT-03 | T-514 |

---

## Phase 6 — 고도화 및 완성 Tasks

> **Sprint Goal**: 모바일, 운영 대시보드, 보안 강화, 최종 배포 (Week 20~24)

### Week 20: Flutter 모바일 앱

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-601 | Flutter 3.x 프로젝트 초기화 (Riverpod + Dio) | Mobile | TODO | - | - |
| T-602 | 강의 수강 화면 (목록/상세/레슨) | Mobile | TODO | FR-COURSE-04 | T-601 |
| T-603 | AI 튜터 채팅 화면 (SSE 스트리밍) | Mobile | TODO | FR-TUTOR-02 | T-601 |
| T-604 | 학습 분석 핵심 화면 | Mobile | TODO | FR-ANALYTICS-01 | T-601 |

### Week 21: 알림 + 관리자 대시보드

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-605 | NotificationWorker (채점/이의제기/Manual Review 완료 알림) | Backend | TODO | FR-NOTIFY-01 | T-305 |
| T-606 | FinOps Soft/Hard Limit 알림 (CostThresholdReached 이벤트) | Backend | TODO | FR-NOTIFY-02 | T-317 |
| T-607 | Admin 대시보드 UI (사용자 관리, AI 품질, FinOps) | Frontend | TODO | FR-QUALITY-01 | T-504 |

### Week 22: Grafana AI 대시보드

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-608 | Grafana AI Quality Panel (hallucination_rate, confidence_avg) | Infra | TODO | FR-OBS-03 | T-312 |
| T-609 | Grafana RAG Panel (latency breakdown, cache_hit_rate) | Infra | TODO | FR-OBS-03 | T-312 |
| T-610 | Grafana FinOps Panel (daily_cost, unit_economics) | Infra | TODO | FR-OBS-03 | T-315 |
| T-611 | Grafana PII + Outbox/Consumer Panel | Infra | TODO | FR-OBS-03 | T-312 |

### Week 23: 보안 강화 + Chaos Testing

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-612 | 7 Layer 보안 점검 (입력 필터링 ~ FinOps Kill-switch) | QA + Infra | TODO | NFR-SEC-07 | T-317 |
| T-613 | Chaos Test: Kafka 브로커 다운 | QA | TODO | NFR-AVAIL-03 | T-304 |
| T-614 | Chaos Test: PII 대량 입력 | QA | TODO | NFR-SEC-03 | T-309 |
| T-615 | Chaos Test: 비용 폭주 시나리오 | QA | TODO | FR-FINOPS-03 | T-317 |
| T-616 | Chaos Test: LLM API 장애 (Circuit Breaker Fallback) | QA | TODO | NFR-AVAIL-02 | T-311 |
| T-617 | Chaos Test: Consumer DLQ 복구 | QA | TODO | FR-EVENT-04 | T-304 |
| T-618 | 보안 취약점 점검 보고서 (OWASP Top 10) | QA | TODO | NFR-SEC-05 | T-612 |

### Week 24: 통합 테스트 + 배포 + 문서화

| ID | Task | 담당 | 상태 | 관련 FR | 선행 |
|----|------|------|------|---------|------|
| T-619 | 통합 테스트 전체 실행 + 성능 테스트 (k6) | QA | TODO | NFR-PERF-01~04 | T-618 |
| T-620 | Docker Compose 전체 스택 배포 (13개 서비스) | Infra | TODO | - | T-619 |
| T-621 | 최종 문서 세트 (배포 가이드, 사용자 매뉴얼, 운영 매뉴얼) | 전체 | TODO | - | T-620 |
| T-622 | GitHub Actions CI/CD 파이프라인 최종 구성 | Infra | TODO | - | T-620 |

---

## Task 통계

| Phase | 주차 | 총 Task | Backend | Frontend | Mobile | Infra | QA |
|-------|------|---------|---------|----------|--------|-------|-----|
| Phase 1 | W1~4 | 21 | 15 | 6 | 0 | 0 | 0 |
| Phase 2 | W5~8 | 14 | 11 | 2 | 0 | 0 | 0 |
| Phase 3 | W9~12 | 20 | 19 | 0 | 0 | 1 | 0 |
| Phase 4 | W13~16 | 24 | 22 | 1 | 0 | 0 | 0 |
| Phase 5 | W17~19 | 15 | 12 | 2 | 0 | 0 | 0 |
| Phase 6 | W20~24 | 22 | 3 | 1 | 4 | 7 | 7 |
| **합계** | | **116** | **82** | **12** | **4** | **8** | **7** |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-03-22 | PRD 기반 Task 목록 초안 작성 (Project Control Hub, 89개) |
| v4.0 | 2026-04-02 | LearnFlow AI 전환: 24주 6Phase 기반 Task 전면 재구성 (116개) |
