# Phase 5 — 분석 + 품질 관리 (Week 17~19) Task Workflows

> **기간**: 2026-07-28 ~ 08-17
> **마일스톤**: M5 — 품질 관리
> **Task 수**: 15개 (T-501 ~ T-515)
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [README.md](README.md)

---

## Week 17: 학습 분석

---

### T-501: concept_mastery 테이블 + 자동 갱신 로직 (AnalyticsWorker)

> **담당**: Backend | **선행**: T-305 | **관련 FR**: FR-ANALYTICS-02

#### 서브 스텝

1. **Flyway 마이그레이션 — concept_mastery 테이블 생성**
   - 파일: `learnflow-api/src/main/resources/db/migration/V17__concept_mastery.sql`
   - 컬럼: `id`, `user_id`, `course_id`, `concept_name`, `mastery_score`(DECIMAL 0~1), `confidence`(DECIMAL 0~1), `source`(ENUM: DIAGNOSTIC, QUIZ, MANUAL), `attempt_count`, `last_updated_at`, `created_at`
   - 복합 UNIQUE 제약: `(user_id, course_id, concept_name)`
   - 인덱스: `idx_concept_mastery_user_course` on `(user_id, course_id)`

2. **ConceptMastery 엔티티 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/ConceptMastery.java`
   - `@Setter` 금지 — 비즈니스 메서드 `updateMastery(newScore, source)` 제공
   - `updateMastery()`: EMA(지수이동평균) 방식으로 기존 mastery_score와 새 점수 가중 평균 계산
   - `confidence` 필드: attempt_count 기반 자동 산출 (attempts >= 5 → confidence 1.0, 선형 비례)
   - Repository: `ConceptMasteryRepository` — `findByUserIdAndCourseId()`, `findByUserIdAndCourseIdAndConceptName()`

3. **AnalyticsWorker — QuizCompleted/AssignmentGraded 이벤트 소비**
   - 파일: `learnflow-api/src/main/java/com/learnflow/worker/AnalyticsWorker.java`
   - Kafka Consumer: `quiz.completed`, `assignment.graded` 토픽 구독
   - 이벤트에서 concept_name, score 추출 → `ConceptMastery.updateMastery()` 호출
   - 멱등성: `dedup_key` 체크 (이미 처리된 이벤트 스킵)
   - DLQ: 3회 실패 시 `analytics.dlq` 토픽으로 이동

4. **LearningAnalyticsService — 숙련도 조회 API**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/LearningAnalyticsService.java`
   - `getMasteryByCourse(userId, courseId)`: 해당 강의의 전체 concept_mastery 목록 반환
   - `getOverallMastery(userId)`: 수강 중인 모든 강의의 평균 mastery 반환
   - Controller: `GET /api/v1/analytics/mastery?courseId={id}` → `ApiResponse<List<ConceptMasteryResponse>>`

#### 완료 기준

- [ ] Flyway 마이그레이션 적용 후 concept_mastery 테이블 정상 생성
- [ ] QuizCompleted 이벤트 소비 시 mastery_score가 EMA로 갱신됨
- [ ] 동일 dedup_key 이벤트 중복 소비 시 데이터 변경 없음 (멱등성)
- [ ] `/api/v1/analytics/mastery` API가 `ApiResponse<T>` 형태로 정상 응답
- [ ] DLQ 이동 로직 동작 확인

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인

---

### T-502: WeaknessDetection (취약점 분석)

> **담당**: Backend | **선행**: T-501 | **관련 FR**: FR-ANALYTICS-04

#### 서브 스텝

1. **WeaknessDetection 서비스 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/WeaknessDetection.java`
   - `detectWeaknesses(userId, courseId)`: concept_mastery 테이블에서 `mastery_score < 0.4`인 개념 추출
   - 결과를 `WeaknessReport` DTO로 반환: `List<WeakConcept>` (conceptName, masteryScore, confidence, suggestedAction)
   - 취약점 우선순위 정렬: mastery_score 오름차순 (가장 낮은 것부터)

2. **취약점 맵 생성 로직**
   - concept 간 관계(prerequisites)를 고려한 연쇄 취약점 탐지
   - 선수 개념이 취약하면 후속 개념도 취약 가능성 표시 (`cascading_weakness` 플래그)
   - 파일: `WeaknessReport.java` (record) — `weakConcepts`, `cascadingRisks`, `overallWeaknessRatio`

3. **취약점 분석 API**
   - Controller: `GET /api/v1/analytics/weakness?courseId={id}` → `ApiResponse<WeaknessReport>`
   - 인증: LEARNER 권한 필요, 본인 데이터만 조회 가능
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/LearningAnalyticsController.java`

4. **취약점 이력 저장 (선택적 배치)**
   - 주기적 배치(주 1회)로 취약점 스냅샷 저장 → 추세 분석 가능
   - 테이블: `weakness_snapshots` (Flyway V17_2)

#### 완료 기준

- [ ] mastery_score < 0.4인 개념이 정확히 취약점으로 추출됨
- [ ] 취약점 우선순위 정렬(오름차순) 동작 확인
- [ ] 연쇄 취약점(cascading) 탐지 로직 동작 확인
- [ ] API 응답이 `ApiResponse<WeaknessReport>` 형태

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)

---

### T-503: AI 추천 API (보충 퀴즈, 관련 레슨)

> **담당**: Backend | **선행**: T-502 | **관련 FR**: FR-ANALYTICS-03

#### 서브 스텝

1. **AI 추천 서비스 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/RecommendationService.java`
   - `getRecommendations(userId, courseId)`: WeaknessDetection 결과 기반 추천 생성
   - 추천 타입: `SUPPLEMENTARY_QUIZ` (취약 개념 보충 퀴즈), `RELATED_LESSON` (관련 레슨 재학습), `REVIEW_MATERIAL` (복습 자료)
   - 취약 개념 → 해당 concept을 태그로 가진 레슨/퀴즈 매핑

2. **AI Gateway 경유 개인화 추천 (선택적)**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/RecommendationAiService.java`
   - 취약점 + 학습 패턴 데이터를 AI Gateway로 전송하여 개인화 추천 메시지 생성
   - PII Masking 필수: 사용자 이름 등 개인정보 마스킹 후 LLM 전송
   - FinOps: Tier 1 (Haiku) 사용, ai_cost_logs 기록

3. **추천 API 엔드포인트**
   - Controller: `GET /api/v1/analytics/recommendations?courseId={id}` → `ApiResponse<RecommendationResponse>`
   - `RecommendationResponse` (record): `List<Recommendation>` (type, targetId, targetTitle, reason, priority)

4. **추천 결과 캐싱**
   - Redis 캐시: `cache:recommendation:{userId}:{courseId}` (TTL 1h)
   - concept_mastery 갱신 시 관련 캐시 무효화

#### 완료 기준

- [ ] 취약 개념 기반 보충 퀴즈/관련 레슨 추천이 정상 생성됨
- [ ] AI 개인화 추천 시 AI Gateway 경유 및 PII Masking 적용됨
- [ ] 추천 결과 Redis 캐싱 동작 및 무효화 동작 확인
- [ ] API 응답이 `ApiResponse<RecommendationResponse>` 형태

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리

---

### T-504: 학습 분석 대시보드 UI (주간 학습 시간, 숙련도, 취약점 맵)

> **담당**: Frontend | **선행**: T-119 | **관련 FR**: FR-ANALYTICS-01

#### 서브 스텝

1. **대시보드 페이지 구조**
   - 파일: `learnflow-web/src/pages/analytics/AnalyticsDashboard.tsx`
   - 3-섹션 레이아웃: 주간 학습 시간(차트), 개념별 숙련도(레이더 차트), 취약점 맵(히트맵)
   - shadcn/ui Card 컴포넌트 기반 구성

2. **TanStack Query 커스텀 훅 구현**
   - 파일: `learnflow-web/src/hooks/useAnalytics.ts`
   - `useMastery(courseId)`: `GET /api/v1/analytics/mastery` 호출
   - `useWeakness(courseId)`: `GET /api/v1/analytics/weakness` 호출
   - `useRecommendations(courseId)`: `GET /api/v1/analytics/recommendations` 호출
   - `useLearningTime(courseId, period)`: `GET /api/v1/analytics/learning-time` 호출

3. **차트 컴포넌트 구현**
   - 파일: `learnflow-web/src/components/analytics/MasteryRadarChart.tsx`
   - 파일: `learnflow-web/src/components/analytics/WeaknessHeatmap.tsx`
   - 파일: `learnflow-web/src/components/analytics/LearningTimeChart.tsx`
   - 차트 라이브러리: Recharts 활용, Tailwind 스타일링

4. **추천 카드 섹션**
   - 파일: `learnflow-web/src/components/analytics/RecommendationCards.tsx`
   - 추천 타입별 아이콘/색상 분류, 클릭 시 해당 레슨/퀴즈로 라우팅

5. **강의 선택 필터 + 기간 필터**
   - Zustand store: `learnflow-web/src/stores/analyticsStore.ts`
   - 선택 강의 ID, 분석 기간 등 클라이언트 상태 관리

#### 완료 기준

- [ ] 주간 학습 시간 차트가 정상 렌더링됨
- [ ] 개념별 숙련도 레이더 차트에 concept_mastery 데이터 반영
- [ ] 취약점 맵이 mastery_score < 0.4인 개념을 시각적으로 강조
- [ ] 추천 카드 클릭 시 해당 콘텐츠 페이지로 정상 라우팅
- [ ] 강의 필터 변경 시 데이터 자동 갱신

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스

---

### T-505: 강사 수강생 분석 API

> **담당**: Backend | **선행**: T-501 | **관련 FR**: FR-ANALYTICS-06

#### 서브 스텝

1. **강사용 수강생 분석 서비스 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/InstructorAnalyticsService.java`
   - `getCourseAnalytics(courseId, instructorId)`: 강의 전체 수강생 mastery 평균, 분포, 취약 개념 TOP 5
   - `getStudentDetail(courseId, studentId, instructorId)`: 특정 수강생의 개념별 mastery 상세
   - 강사 본인 강의만 조회 가능 — 소유권 검증 필수

2. **수강생 목록 + 위험 학생 감지**
   - `getAtRiskStudents(courseId)`: mastery_score 평균이 0.3 미만인 수강생 목록
   - 결과 DTO: `AtRiskStudentResponse` (record) — userId, userName, avgMastery, weakConceptCount, lastActiveAt

3. **강사 분석 API 엔드포인트**
   - Controller: `learnflow-api/src/main/java/com/learnflow/analytics/InstructorAnalyticsController.java`
   - `GET /api/v1/instructor/analytics/courses/{courseId}` → `ApiResponse<CourseAnalyticsResponse>`
   - `GET /api/v1/instructor/analytics/courses/{courseId}/students` → `ApiResponse<PageResponse<StudentMasteryResponse>>`
   - `GET /api/v1/instructor/analytics/courses/{courseId}/at-risk` → `ApiResponse<List<AtRiskStudentResponse>>`
   - 인증: INSTRUCTOR 권한

4. **집계 쿼리 최적화**
   - Repository에 `@Query`로 집계 쿼리 작성 (전체 스캔 방지)
   - 수강생 수가 많은 경우 PageResponse로 페이징 처리

#### 완료 기준

- [ ] 강사가 본인 강의 수강생의 mastery 분포를 조회할 수 있음
- [ ] 위험 학생(avg mastery < 0.3) 목록이 정확히 반환됨
- [ ] 타 강사 강의 데이터 접근 시 403 에러 반환
- [ ] 페이징이 정상 동작함

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-506: Cold Start 연동 (온보딩 → concept_mastery 초기값)

> **담당**: Backend | **선행**: T-210 | **관련 FR**: FR-ONBOARD-04

#### 서브 스텝

1. **온보딩 결과 → concept_mastery 초기값 생성 로직**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/ColdStartService.java`
   - `initializeMastery(userId, courseId, diagnosticResult)`: 진단 결과에서 concept별 점수 추출
   - `confidence_weight = 0.7` (진단 테스트 기반이므로 높은 신뢰도)
   - `source = DIAGNOSTIC`로 설정

2. **DiagnosticCompleted 이벤트 연동**
   - 진단 테스트 완료 시 `DiagnosticCompleted` 이벤트 발행 (OutboxPublisher 경유)
   - AnalyticsWorker에서 `diagnostic.completed` 토픽 소비 → `ColdStartService.initializeMastery()` 호출
   - 이벤트 페이로드: userId, courseId, conceptScores (Map<String, Double>)

3. **기존 mastery 존재 시 처리**
   - 이미 concept_mastery 레코드가 존재하면 덮어쓰지 않음 (DIAGNOSTIC은 최초 1회만 적용)
   - 자가 진단(SelfAssessment) 결과: `confidence_weight = 0.3`, `source = MANUAL`

4. **ColdStartService 통합 테스트**
   - 진단 완료 → concept_mastery 생성 → mastery_score/confidence 확인
   - 중복 진단 시 기존 데이터 유지 확인

#### 완료 기준

- [ ] 진단 테스트 완료 시 concept_mastery 초기값이 자동 생성됨
- [ ] confidence_weight=0.7, source=DIAGNOSTIC으로 정확히 설정됨
- [ ] 기존 mastery 존재 시 덮어쓰지 않음
- [ ] OutboxPublisher를 통한 이벤트 발행 확인

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경

---

## Week 18: 3층 평가

---

### T-507: RagasEvaluation (Faithfulness, Context Precision/Recall, Answer Relevancy) — 3회 중앙값

> **담당**: Backend (AI) | **선행**: T-405 | **관련 FR**: FR-QUALITY-02

#### 서브 스텝

1. **Flyway 마이그레이션 — ragas_evaluations 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V18__ragas_evaluations.sql`
   - 컬럼: `id`, `session_id`, `message_id`, `run_number`(1~3), `faithfulness`(DECIMAL), `context_precision`(DECIMAL), `context_recall`(DECIMAL), `answer_relevancy`(DECIMAL), `overall_score`(DECIMAL), `evaluated_at`
   - 복합 인덱스: `(session_id, message_id, run_number)` UNIQUE

2. **RagasEvaluation 엔티티 + 서비스 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/RagasEvaluation.java` (엔티티)
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/RagasEvaluationService.java`
   - `evaluate(sessionId, messageId, question, answer, contexts)`: 3회 실행 → 각 메트릭의 중앙값 산출
   - 각 메트릭 산출 시 AI Gateway 경유 (Tier 2 Sonnet 사용)
   - 3회 결과 저장 후 `getMedianScores(sessionId, messageId)` → 중앙값 반환

3. **메트릭 산출 로직**
   - Faithfulness: 답변의 각 문장이 제공된 context에 근거하는지 비율 (claim extraction → verification)
   - Context Precision: 검색된 context 중 실제 관련 있는 비율
   - Context Recall: 정답에 필요한 정보가 context에 포함된 비율
   - Answer Relevancy: 답변이 질문에 얼마나 관련 있는지
   - 프롬프트: `ai/prompt/templates/ragas_faithfulness.txt`, `ragas_context_precision.txt` 등

4. **3회 중앙값 계산 유틸리티**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/MedianCalculator.java`
   - 3개 값 정렬 → 중앙값 반환 (단순 정렬, 안정성 위해 3회 실행)

5. **Layer 1 Rule-based 사전 검증**
   - 답변 포맷 검증 (빈 응답, 과도한 길이, JSON 스키마 위반 등)
   - Rule-based 검증 실패 시 RAGAS 평가 스킵 → 즉시 실패 처리

#### 완료 기준

- [ ] 3회 실행 결과가 ragas_evaluations 테이블에 run_number 1~3으로 저장됨
- [ ] 중앙값 계산이 정확함 (예: [0.6, 0.8, 0.7] → 0.7)
- [ ] 4가지 메트릭 (Faithfulness, Context Precision, Context Recall, Answer Relevancy) 모두 산출됨
- [ ] AI Gateway 경유하여 LLM 호출 및 FinOps 비용 기록됨

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)

---

### T-508: DeepEvalService (G-Eval + Hallucination Score)

> **담당**: Backend (AI) | **선행**: T-507 | **관련 FR**: FR-QUALITY-03

#### 서브 스텝

1. **DeepEvalService 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/DeepEvalService.java`
   - `evaluateWithGEval(question, answer, contexts)`: G-Eval 방식으로 1~5점 스코어 산출
   - G-Eval: LLM에게 평가 criteria를 제공하고, 각 criterion별로 1~5 점수 + chain-of-thought 생성
   - 프롬프트: `ai/prompt/templates/deep_eval_geval.txt`

2. **Hallucination Score 계산**
   - `calculateHallucinationScore(answer, contexts)`: 답변에서 context에 없는 주장(claim) 비율 산출
   - Hallucination Score = 1 - (grounded_claims / total_claims)
   - 프롬프트: `ai/prompt/templates/deep_eval_hallucination.txt`
   - Hallucination Score > 0.3이면 경고 플래그 설정

3. **DeepEvalResult 저장**
   - 테이블 확장: `ragas_evaluations`에 `geval_score`(DECIMAL), `hallucination_score`(DECIMAL) 컬럼 추가
   - Flyway: `V18_2__add_deepeval_columns.sql`
   - 엔티티 필드 추가: `gevalScore`, `hallucinationScore`

4. **RAGAS + DeepEval 통합 평가 오케스트레이터**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/QualityEvaluationOrchestrator.java`
   - `evaluateFull(sessionId, messageId, question, answer, contexts)`: Layer 1(Rule) → Layer 2(RAGAS + DeepEval) 순차 실행
   - 각 레이어 결과를 `QualityEvaluationResult` record로 통합 반환

#### 완료 기준

- [ ] G-Eval 점수 (1~5)가 정상 산출됨
- [ ] Hallucination Score가 0~1 범위로 계산됨
- [ ] hallucination_score > 0.3 경고 플래그 동작 확인
- [ ] QualityEvaluationOrchestrator가 Layer 1 → Layer 2 순차 실행

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)
- [ ] Layer 4: Output Validation (점수 범위 검증: G-Eval 1~5, Hallucination 0~1)

---

### T-509: LlmJudge (Faithfulness < 0.7 자동 리포트)

> **담당**: Backend (AI) | **선행**: T-508 | **관련 FR**: FR-QUALITY-03

#### 서브 스텝

1. **LlmJudge 서비스 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/LlmJudge.java`
   - `judge(evaluationResult)`: Layer 2 결과를 받아 Faithfulness < 0.7인 경우 상세 분석 리포트 생성
   - 리포트 내용: 어떤 문장이 context에 근거하지 않는지, 개선 제안, 심각도(LOW/MEDIUM/HIGH)
   - AI Gateway 경유, Tier 3 (Sonnet) 사용
   - 프롬프트: `ai/prompt/templates/llm_judge_report.txt`

2. **자동 리포트 저장**
   - 테이블: `quality_reports` (Flyway V18_3)
   - 컬럼: `id`, `evaluation_id`, `severity`, `ungrounded_claims`(JSON), `improvement_suggestions`(TEXT), `created_at`
   - 엔티티: `QualityReport.java`

3. **관리자 알림 트리거**
   - Faithfulness < 0.5 (심각): `QualityAlertEvent` 이벤트 발행 → OutboxPublisher 경유
   - NotificationWorker가 소비 → 관리자에게 알림 (이메일/인앱)

4. **Layer 3 실행 조건 제어**
   - LlmJudge는 모든 응답에 대해 실행하지 않음 (비용 절감)
   - 실행 조건: Faithfulness < 0.7 OR hallucination_score > 0.3 OR 사용자 thumbs-down 피드백

#### 완료 기준

- [ ] Faithfulness < 0.7인 응답에 대해 자동으로 상세 리포트 생성됨
- [ ] quality_reports 테이블에 리포트 정상 저장
- [ ] Faithfulness < 0.5 시 관리자 알림 이벤트 발행됨
- [ ] Faithfulness >= 0.7인 응답에 대해서는 LlmJudge 미실행 (비용 절감)

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] `destination_topic` 누락 없음

---

### T-510: Importance Sampling 배치 스케줄러

> **담당**: Backend (AI) | **선행**: T-507 | **관련 FR**: FR-QUALITY-04

#### 서브 스텝

1. **ImportanceSampler 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/ImportanceSampler.java`
   - `selectSamples(batchSize)`: 평가 대상 응답 샘플링 (전수 평가 아님, 비용 절감)
   - 샘플링 가중치 기준:
     - 낮은 Confidence 응답 (AI 채점 confidence < 0.8): 가중치 3x
     - 사용자 부정 피드백 (thumbs-down): 가중치 5x
     - 신규 프롬프트 버전 적용 응답: 가중치 2x
     - 일반 응답: 가중치 1x
   - 가중 랜덤 샘플링으로 batchSize만큼 선택

2. **배치 스케줄러 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/QualityBatchScheduler.java`
   - `@Scheduled(cron = "0 0 3 * * *")`: 매일 새벽 3시 실행
   - ShedLock으로 멀티 인스턴스 환경 중복 실행 방지
   - 프로세스: 샘플링 → QualityEvaluationOrchestrator 실행 → 결과 저장

3. **배치 실행 이력 관리**
   - 테이블: `quality_batch_runs` (Flyway V18_4)
   - 컬럼: `id`, `run_date`, `total_sampled`, `avg_faithfulness`, `avg_hallucination`, `alerts_generated`, `status`, `duration_ms`
   - 배치 완료 후 요약 통계 저장

4. **비용 제어**
   - 일일 품질 평가 예산 한도 설정 (FinOps Guard 연동)
   - 예산 초과 시 배치 중단 + 관리자 알림
   - 배치당 기본 batchSize: 50건 (설정 가능)

#### 완료 기준

- [ ] 가중 랜덤 샘플링이 우선순위에 따라 정확히 동작함 (thumbs-down > low confidence > 일반)
- [ ] 배치가 매일 새벽 3시에 자동 실행됨
- [ ] ShedLock으로 중복 실행 방지 동작 확인
- [ ] 배치 실행 이력이 quality_batch_runs에 저장됨
- [ ] FinOps 예산 초과 시 배치 중단

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리

---

### T-511: 3층 평가 관리자 대시보드 API

> **담당**: Backend | **선행**: T-509 | **관련 FR**: FR-QUALITY-01

#### 서브 스텝

1. **관리자 품질 대시보드 API 구현**
   - Controller: `learnflow-api/src/main/java/com/learnflow/ai/quality/QualityDashboardController.java`
   - `GET /api/v1/admin/ai/quality/overview` → `ApiResponse<QualityOverviewResponse>`
     - 최근 7일/30일 평균: Faithfulness, Context Precision, Context Recall, Answer Relevancy, G-Eval, Hallucination
     - 추세 그래프 데이터 (일별 평균)
   - `GET /api/v1/admin/ai/quality/reports` → `ApiResponse<PageResponse<QualityReportResponse>>`
     - quality_reports 목록 (severity 필터, 날짜 범위 필터)
   - `GET /api/v1/admin/ai/quality/batch-runs` → `ApiResponse<List<BatchRunResponse>>`
     - 최근 배치 실행 이력

2. **품질 서비스 레이어**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/QualityDashboardService.java`
   - 집계 쿼리 최적화: 일별/주별 평균 산출 시 DB 집계 함수 활용
   - 캐싱: 대시보드 개요는 Redis 캐시 (TTL 5min) — `cache:quality:overview`

3. **개별 응답 품질 상세 조회**
   - `GET /api/v1/admin/ai/quality/evaluations/{evaluationId}` → `ApiResponse<EvaluationDetailResponse>`
   - 3회 run 결과 + 중앙값 + DeepEval 결과 + LlmJudge 리포트(있는 경우) 통합 반환

4. **인증/인가**
   - ADMIN 권한만 접근 가능
   - 모든 DTO는 Java record 사용

#### 완료 기준

- [ ] 관리자가 품질 메트릭 전체 개요를 조회할 수 있음
- [ ] 일별 추세 데이터가 정확히 산출됨
- [ ] quality_reports 목록이 severity/날짜 필터로 조회 가능
- [ ] ADMIN 권한 없는 사용자 접근 시 403 반환

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

## Week 19: A/B 테스트 + 프롬프트 관리

---

### T-512: AbTestService (생성/종료 + mastery_delta 측정)

> **담당**: Backend (AI) | **선행**: T-507 | **관련 FR**: FR-QUALITY-05

#### 서브 스텝

1. **Flyway 마이그레이션 — A/B 테스트 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V19__ab_tests.sql`
   - `ab_tests` 테이블: `id`, `name`, `description`, `prompt_name`, `control_version`(INT), `treatment_version`(INT), `traffic_split`(DECIMAL, 기본 0.5), `status`(ENUM: DRAFT, RUNNING, COMPLETED, CANCELLED), `started_at`, `ended_at`, `created_by`
   - `ab_test_assignments` 테이블: `id`, `test_id`, `user_id`, `group`(ENUM: CONTROL, TREATMENT), `assigned_at`
   - `ab_test_results` 테이블: `id`, `test_id`, `group`, `participant_count`, `avg_mastery_before`, `avg_mastery_after`, `mastery_delta`, `avg_quality_score`, `calculated_at`

2. **AbTestService 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/AbTestService.java`
   - `createTest(request)`: A/B 테스트 생성 (DRAFT 상태)
   - `startTest(testId)`: RUNNING 전환, 기존 사용자 traffic_split 기준으로 CONTROL/TREATMENT 배정
   - `getAssignment(testId, userId)`: 사용자가 어떤 그룹인지 반환 → ModelRouter에서 프롬프트 버전 분기
   - `endTest(testId)`: COMPLETED 전환, mastery_delta 계산

3. **mastery_delta 측정 로직**
   - 테스트 시작 시점 사용자별 concept_mastery 평균 스냅샷 저장
   - 테스트 종료 시점 concept_mastery 평균과 비교 → delta 산출
   - CONTROL vs TREATMENT 그룹별 delta 비교 → 통계적 유의성 검정 (간이 t-test)

4. **A/B 테스트 API 엔드포인트**
   - Controller: `learnflow-api/src/main/java/com/learnflow/ai/quality/AbTestController.java`
   - `POST /api/v1/admin/ai/quality/ab-tests` → 생성
   - `PUT /api/v1/admin/ai/quality/ab-tests/{id}/start` → 시작
   - `PUT /api/v1/admin/ai/quality/ab-tests/{id}/end` → 종료 + 결과 산출
   - `GET /api/v1/admin/ai/quality/ab-tests/{id}/results` → 결과 조회
   - 인증: ADMIN 권한

5. **AI Gateway 연동 — 프롬프트 분기**
   - ModelRouter에서 A/B 테스트 활성 여부 확인
   - TREATMENT 그룹 사용자 → treatment_version 프롬프트 사용
   - CONTROL 그룹 사용자 → control_version 프롬프트 사용

#### 완료 기준

- [ ] A/B 테스트 생성 → 시작 → 종료 라이프사이클 정상 동작
- [ ] 사용자 그룹 배정이 traffic_split 비율에 따라 정확히 분배됨
- [ ] mastery_delta가 시작/종료 시점 비교로 정확히 계산됨
- [ ] ModelRouter에서 그룹별 프롬프트 버전 분기 동작 확인
- [ ] ADMIN 권한 검증 동작

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리

---

### T-513: PromptVersionService (버전 관리 + 즉시 롤백)

> **담당**: Backend (AI) | **선행**: T-306 | **관련 FR**: FR-QUALITY-06

#### 서브 스텝

1. **Flyway 마이그레이션 — prompt_versions 테이블 확장**
   - 파일: `learnflow-api/src/main/resources/db/migration/V19_2__prompt_versions.sql`
   - `prompt_versions` 테이블: `id`, `name`(VARCHAR), `version`(INT), `template`(TEXT), `description`(TEXT), `is_active`(BOOLEAN), `created_by`, `created_at`, `activated_at`
   - 복합 UNIQUE: `(name, version)`
   - 동일 name에 대해 `is_active = true`인 레코드는 최대 1개 (비즈니스 로직으로 보장)

2. **PromptVersionService 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/PromptVersionService.java`
   - `createVersion(name, template, description)`: 새 버전 생성 (is_active=false, 기존 max version + 1)
   - `activateVersion(name, version)`: 해당 버전 활성화, 기존 활성 버전 비활성화 (단일 트랜잭션)
   - `rollback(name)`: 현재 활성 버전 비활성화 → 직전 버전 활성화 (즉시 롤백)
   - `getActiveTemplate(name)`: 활성 프롬프트 템플릿 조회 (TemplateEngine 연동)

3. **TemplateEngine 연동**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/prompt/TemplateEngine.java`
   - `resolve(name)`: 먼저 `prompt_versions` 테이블에서 is_active=true 조회, 없으면 파일 시스템 fallback (`ai/prompt/templates/`)
   - Redis 캐시: `cache:prompt:{name}` (TTL 10min) — 활성화/롤백 시 캐시 즉시 무효화

4. **프롬프트 버전 관리 API**
   - Controller: `learnflow-api/src/main/java/com/learnflow/ai/quality/PromptVersionController.java`
   - `GET /api/v1/admin/ai/quality/prompts` → 전체 프롬프트 목록 (name 그룹)
   - `GET /api/v1/admin/ai/quality/prompts/{name}/versions` → 특정 프롬프트의 버전 이력
   - `POST /api/v1/admin/ai/quality/prompts/{name}/versions` → 새 버전 생성
   - `PUT /api/v1/admin/ai/quality/prompts/{name}/versions/{version}/activate` → 활성화
   - `POST /api/v1/admin/ai/quality/prompts/{name}/rollback` → 즉시 롤백
   - 인증: ADMIN 권한

5. **감사 로그**
   - 프롬프트 활성화/롤백은 `@Audited`로 before/after 기록 (어떤 버전에서 어떤 버전으로 변경했는지)

#### 완료 기준

- [ ] 프롬프트 새 버전 생성 → 활성화 → 롤백 라이프사이클 정상 동작
- [ ] 활성화 시 기존 버전 자동 비활성화 (동시 활성 방지)
- [ ] 롤백 시 직전 버전으로 즉시 전환됨
- [ ] TemplateEngine이 DB 우선 → 파일 시스템 fallback으로 동작
- [ ] 활성화/롤백 시 Redis 캐시 즉시 무효화

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] `@Audited` 감사 로그 적용
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리

---

### T-514: A/B 테스트 관리 UI

> **담당**: Frontend | **선행**: T-504 | **관련 FR**: FR-QUALITY-05

#### 서브 스텝

1. **A/B 테스트 목록 페이지**
   - 파일: `learnflow-web/src/pages/admin/AbTestList.tsx`
   - 상태별 필터 (DRAFT, RUNNING, COMPLETED, CANCELLED)
   - shadcn/ui Table 컴포넌트로 목록 표시: name, status, traffic_split, started_at, mastery_delta
   - ADMIN 라우트 보호

2. **A/B 테스트 생성 폼**
   - 파일: `learnflow-web/src/pages/admin/AbTestCreate.tsx`
   - React Hook Form + Zod 스키마 검증
   - 필드: name, description, prompt_name(드롭다운), control_version, treatment_version, traffic_split(슬라이더)
   - 프롬프트 버전 목록은 API에서 동적으로 로드

3. **A/B 테스트 상세 + 결과 페이지**
   - 파일: `learnflow-web/src/pages/admin/AbTestDetail.tsx`
   - 테스트 정보, 시작/종료 버튼, 결과 차트
   - 결과 시각화: CONTROL vs TREATMENT 그룹의 mastery_delta 비교 (Bar Chart)
   - 품질 메트릭 비교: 그룹별 평균 Faithfulness, G-Eval 등

4. **TanStack Query 훅**
   - 파일: `learnflow-web/src/hooks/useAbTest.ts`
   - `useAbTests(status?)`: A/B 테스트 목록 조회
   - `useAbTestDetail(id)`: 상세 조회
   - `useAbTestResults(id)`: 결과 조회
   - `useCreateAbTest()`, `useStartAbTest()`, `useEndAbTest()`: mutation 훅

5. **프롬프트 버전 관리 UI (간이)**
   - 파일: `learnflow-web/src/pages/admin/PromptVersions.tsx`
   - 프롬프트별 버전 목록, 활성 버전 표시, 롤백 버튼
   - 새 버전 생성 모달 (template 텍스트 에디터)

#### 완료 기준

- [ ] A/B 테스트 생성 → 시작 → 종료 UI 플로우 동작
- [ ] 결과 차트에서 CONTROL vs TREATMENT mastery_delta 비교 가능
- [ ] Zod 스키마 검증으로 잘못된 입력 차단
- [ ] 프롬프트 버전 목록/활성화/롤백 UI 동작

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] 폼: React Hook Form + Zod 스키마 검증
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스

---

### T-515: 단위 테스트 (Analytics, RAGAS, DeepEval, A/B)

> **담당**: Backend | **선행**: T-514 | **관련 FR**: NFR-MAINT-03

#### 서브 스텝

1. **Analytics 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/analytics/ConceptMasteryServiceTest.java`
   - 테스트 케이스:
     - EMA 갱신 로직 정확성 (기존 0.5, 새 0.8 → 가중 평균 검증)
     - confidence 자동 산출 (attempt_count 기반)
     - Cold Start 초기값 생성 (confidence_weight=0.7)
   - 파일: `learnflow-api/src/test/java/com/learnflow/analytics/WeaknessDetectionTest.java`
   - 테스트 케이스:
     - mastery < 0.4인 개념 추출
     - 연쇄 취약점 탐지
     - 빈 데이터 시 빈 리포트 반환

2. **RAGAS 평가 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/RagasEvaluationServiceTest.java`
   - 테스트 케이스:
     - 3회 실행 → 중앙값 계산 정확성 ([0.6, 0.8, 0.7] → 0.7)
     - AI Gateway mock으로 메트릭 산출 플로우 검증
     - run_number 1~3 저장 확인
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/MedianCalculatorTest.java`
   - 엣지 케이스: 동일 값 3개, 정렬 순서 검증

3. **DeepEval 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/DeepEvalServiceTest.java`
   - 테스트 케이스:
     - G-Eval 점수 1~5 범위 검증
     - Hallucination Score 0~1 범위 검증
     - hallucination > 0.3 경고 플래그 동작
   - QualityEvaluationOrchestrator 통합 테스트: Layer 1 → Layer 2 순차 실행 검증

4. **A/B 테스트 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/AbTestServiceTest.java`
   - 테스트 케이스:
     - 테스트 생성 → DRAFT 상태 확인
     - 시작 → RUNNING 전환 + 사용자 그룹 배정 (traffic_split 비율 검증)
     - 종료 → mastery_delta 계산 정확성
     - 이미 종료된 테스트 재시작 시도 → 예외 발생
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/PromptVersionServiceTest.java`
   - 테스트 케이스:
     - 새 버전 생성 → version 자동 증가
     - 활성화 → 기존 활성 버전 비활성화
     - 롤백 → 직전 버전 활성화
     - 캐시 무효화 확인

5. **강사 분석 + ImportanceSampler 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/analytics/InstructorAnalyticsServiceTest.java`
   - 타 강사 데이터 접근 차단 검증
   - at-risk 학생 필터링 정확성
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/ImportanceSamplerTest.java`
   - 가중 샘플링: thumbs-down 응답이 일반 응답보다 높은 확률로 선택되는지 통계적 검증

6. **컨트롤러 테스트 (@WebMvcTest)**
   - 파일: `learnflow-api/src/test/java/com/learnflow/analytics/LearningAnalyticsControllerTest.java`
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/quality/QualityDashboardControllerTest.java`
   - 인증/인가 검증, 응답 포맷(`ApiResponse`) 검증, 에러 코드 검증

#### 완료 기준

- [ ] Analytics 서비스 테스트 전체 PASS (EMA, WeaknessDetection, ColdStart)
- [ ] RAGAS 테스트 전체 PASS (3회 중앙값, 메트릭 산출)
- [ ] DeepEval 테스트 전체 PASS (G-Eval, Hallucination)
- [ ] A/B 테스트 전체 PASS (라이프사이클, mastery_delta, 프롬프트 버전 관리)
- [ ] 컨트롤러 테스트 전체 PASS (인증/인가, 응답 포맷)

#### 규칙 체크리스트

- [ ] `@WebMvcTest` (컨트롤러) + `@DataJpaTest` (리포지토리) + `@SpringBootTest` (통합) 구분
- [ ] AI 관련 테스트는 Gateway mock 사용
- [ ] 테스트에서 KafkaTemplate 직접 사용 금지 — OutboxPublisher mock

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-04-03 | 초안 작성: Phase 5 (T-501 ~ T-515) 15개 Task 세부 워크플로우 정의 |
