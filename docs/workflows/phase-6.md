# Phase 6 — 고도화 및 완성 (Week 20~24) Task Workflows

> **기간**: 2026-08-18 ~ 09-27
> **마일스톤**: M6 — 릴리즈
> **Task 수**: 22개 (T-601 ~ T-622)
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [README.md](README.md)

---

## Week 20: Flutter 모바일 앱

---

### T-601: Flutter 3.x 프로젝트 초기화 (Riverpod + Dio)

> **담당**: Mobile | **선행**: - | **관련 FR**: -

#### 서브 스텝

1. **Flutter 프로젝트 생성 및 디렉토리 구조 설정**
   - `flutter create learnflow_mobile` → `learnflow-mobile/` 으로 이동
   - 디렉토리 구조:
     ```
     lib/
     ├── core/          # constants, theme, routes, dio_client, interceptors
     ├── features/      # 도메인별 feature 폴더 (course, tutor, analytics, auth)
     ├── shared/        # 공용 위젯, 모델, utils
     └── main.dart
     ```

2. **핵심 의존성 추가 (pubspec.yaml)**
   - `flutter_riverpod` / `riverpod_annotation` / `riverpod_generator` — 상태 관리
   - `dio` / `dio_smart_retry` — HTTP 클라이언트
   - `go_router` — 라우팅
   - `flutter_secure_storage` — JWT 토큰 안전 저장
   - `json_annotation` / `json_serializable` / `build_runner` — JSON 직렬화
   - `freezed_annotation` / `freezed` — 불변 모델 생성

3. **Dio 클라이언트 + JWT 인터셉터 구현**
   - 파일: `lib/core/network/dio_client.dart`
   - `AuthInterceptor`: `flutter_secure_storage`에서 accessToken 읽어 `Authorization: Bearer` 헤더 주입
   - 401 응답 시 refreshToken으로 자동 갱신 → 실패 시 로그인 화면 리다이렉트
   - baseUrl: 환경별 설정 (`lib/core/constants/api_constants.dart`)

4. **Riverpod Provider 기반 구조 설정**
   - 파일: `lib/core/providers/dio_provider.dart` — Dio 인스턴스 Provider
   - 파일: `lib/core/providers/auth_provider.dart` — 인증 상태 관리 (AuthState: authenticated/unauthenticated/loading)
   - `ProviderScope`로 `main.dart`의 `runApp` 래핑

5. **로그인/회원가입 화면 및 인증 플로우**
   - 파일: `lib/features/auth/presentation/login_screen.dart`
   - 파일: `lib/features/auth/presentation/register_screen.dart`
   - 파일: `lib/features/auth/data/auth_repository.dart` — `/api/v1/auth/login`, `/api/v1/auth/register` 호출
   - GoRouter redirect guard: 미인증 시 로그인 화면 강제 이동

6. **공통 위젯 및 테마 설정**
   - 파일: `lib/core/theme/app_theme.dart` — Material 3 기반 라이트/다크 테마
   - 파일: `lib/shared/widgets/` — `LoadingIndicator`, `ErrorView`, `EmptyState` 등

#### 완료 기준

- [ ] `flutter run` 으로 Android/iOS 에뮬레이터에서 앱 정상 실행
- [ ] 로그인 → JWT 저장 → 인증 상태 유지 → 토큰 만료 시 자동 갱신 동작 확인
- [ ] Riverpod Provider 트리 정상 작동 (DevTools에서 확인)
- [ ] `flutter test` 기본 위젯 테스트 통과

#### 규칙 체크리스트

- [ ] Riverpod으로 상태 관리 (setState 직접 사용 금지)
- [ ] Dio 인스턴스 Provider를 통해 주입 (하드코딩 금지)
- [ ] JWT 토큰은 `flutter_secure_storage`에 저장 (SharedPreferences 금지)
- [ ] 환경별 API baseUrl 분리 (dev / staging / prod)

---

### T-602: 강의 수강 화면 (목록/상세/레슨)

> **담당**: Mobile | **선행**: T-601 | **관련 FR**: FR-COURSE-04

#### 서브 스텝

1. **강의 데이터 모델 + Repository 구현**
   - 파일: `lib/features/course/data/models/course_model.dart` — `@freezed` 불변 모델 (`Course`, `Section`, `Lesson`)
   - 파일: `lib/features/course/data/course_repository.dart`
   - API: `GET /api/v1/courses` (목록), `GET /api/v1/courses/{id}` (상세), `GET /api/v1/courses/{id}/sections` (섹션+레슨)
   - 페이지네이션 지원: `PageResponse<T>` 형태 응답 파싱

2. **강의 목록 화면**
   - 파일: `lib/features/course/presentation/course_list_screen.dart`
   - `AsyncNotifierProvider`로 강의 목록 상태 관리 (`loading` / `data` / `error`)
   - 무한 스크롤: `ScrollController` + `ref.read(courseListProvider.notifier).loadMore()`
   - 검색 + 카테고리 필터링 지원

3. **강의 상세 화면**
   - 파일: `lib/features/course/presentation/course_detail_screen.dart`
   - 강의 정보 (제목, 설명, 강사, 진도율) + 섹션/레슨 아코디언 목록
   - 수강 신청 버튼: `POST /api/v1/courses/{id}/enrollments`
   - 진도율 프로그레스 바 표시

4. **레슨 학습 화면**
   - 파일: `lib/features/course/presentation/lesson_screen.dart`
   - 텍스트 콘텐츠: Markdown 렌더링 (`flutter_markdown`)
   - 영상 콘텐츠: `video_player` 패키지, MinIO URL 스트리밍
   - 레슨 완료 API: `POST /api/v1/lessons/{id}/complete` → 진도율 자동 갱신

5. **오프라인 캐싱 (선택)**
   - Dio `CacheInterceptor` 또는 `hive` 로컬 DB로 마지막 조회 강의 목록 캐싱
   - 네트워크 미연결 시 캐시 데이터 표시, 연결 복구 시 자동 갱신

#### 완료 기준

- [ ] 강의 목록 → 상세 → 레슨 내비게이션 정상 동작
- [ ] 수강 신청 및 진도율 업데이트 API 연동 확인
- [ ] 레슨 완료 시 진도율 실시간 반영
- [ ] 에뮬레이터에서 텍스트/영상 콘텐츠 렌더링 확인

#### 규칙 체크리스트

- [ ] Riverpod `AsyncNotifier` 기반 상태 관리
- [ ] Dio를 Provider를 통해 주입받아 사용
- [ ] `@freezed` 불변 모델 사용 (mutable 모델 금지)
- [ ] 에러 상태 처리 (로딩/에러/빈 상태 위젯)

---

### T-603: AI 튜터 채팅 화면 (SSE 스트리밍)

> **담당**: Mobile | **선행**: T-601 | **관련 FR**: FR-TUTOR-02

#### 서브 스텝

1. **채팅 데이터 모델 + Repository**
   - 파일: `lib/features/tutor/data/models/chat_message_model.dart` — `@freezed ChatMessage(role, content, timestamp, isStreaming)`
   - 파일: `lib/features/tutor/data/models/chat_session_model.dart` — `@freezed ChatSession(sessionId, courseId, messages)`
   - 파일: `lib/features/tutor/data/tutor_repository.dart`
   - API: `POST /api/v1/ai/chat/sessions` (세션 생성), `GET /api/v1/ai/chat/sessions/{id}/messages` (이력 조회)

2. **SSE 스트리밍 클라이언트 구현**
   - 파일: `lib/features/tutor/data/sse_client.dart`
   - Dio `responseType: ResponseType.stream` 또는 `http` 패키지의 `Client.send()` 사용
   - `POST /api/v1/ai/chat/sessions/{id}/messages` → SSE 응답 (`text/event-stream`)
   - `StreamController<String>`으로 토큰 단위 파싱: `data:` 라인 추출 → `[DONE]` 감지
   - 연결 끊김 시 자동 재시도 (최대 3회, exponential backoff)

3. **채팅 화면 UI 구현**
   - 파일: `lib/features/tutor/presentation/chat_screen.dart`
   - 말풍선 UI: 사용자(오른쪽, 파란색) / AI(왼쪽, 회색)
   - SSE 스트리밍 중 AI 메시지 실시간 타이핑 효과 (토큰 도착마다 `setState` → Riverpod state 업데이트)
   - `TextEditingController` + 전송 버튼, 키보드 열림 시 자동 스크롤

4. **채팅 상태 관리 (Riverpod)**
   - 파일: `lib/features/tutor/presentation/providers/chat_provider.dart`
   - `StateNotifier<ChatState>`: messages 리스트, isStreaming 플래그, error
   - 전송 시: 사용자 메시지 추가 → isStreaming=true → SSE 시작 → 토큰 축적 → 완료 시 isStreaming=false
   - 세션 전환 시 상태 초기화

5. **강의 컨텍스트 연동**
   - 채팅 시작 시 현재 수강 중인 `courseId` 전달 (course_id 기반 RAG 격리)
   - 강의 선택 드롭다운 또는 레슨 화면에서 "AI에게 질문" 버튼으로 진입

#### 완료 기준

- [ ] AI 튜터에게 질문 전송 → SSE 스트리밍 응답이 실시간으로 화면에 표시
- [ ] 대화 이력 유지 및 스크롤 동작 정상
- [ ] 네트워크 끊김 시 에러 메시지 표시 및 재시도 가능
- [ ] `courseId` 기반으로 세션 생성 확인 (다른 강의 데이터 미노출)

#### 규칙 체크리스트

- [ ] Riverpod `StateNotifier` 기반 채팅 상태 관리
- [ ] SSE 스트리밍 연결/해제 라이프사이클 관리 (화면 이탈 시 dispose)
- [ ] course_id 격리 원칙 준수 (채팅 세션 생성 시 courseId 필수)
- [ ] System Prompt 사용자 노출 금지 (응답에 system 메시지 포함 시 필터링)

---

### T-604: 학습 분석 핵심 화면

> **담당**: Mobile | **선행**: T-601 | **관련 FR**: FR-ANALYTICS-01

#### 서브 스텝

1. **분석 데이터 모델 + Repository**
   - 파일: `lib/features/analytics/data/models/learning_analytics_model.dart`
   - `@freezed` 모델: `LearningProgress`, `ConceptMastery`, `WeaknessReport`, `StudyTimeStats`
   - 파일: `lib/features/analytics/data/analytics_repository.dart`
   - API: `GET /api/v1/analytics/progress` (진도), `GET /api/v1/analytics/mastery` (숙련도), `GET /api/v1/analytics/weakness` (취약점)

2. **학습 대시보드 화면**
   - 파일: `lib/features/analytics/presentation/analytics_dashboard_screen.dart`
   - 상단: 전체 진도율 원형 차트 (`fl_chart` 패키지)
   - 중단: 주간 학습 시간 막대 그래프
   - 하단: 개념별 숙련도 레이더 차트

3. **개념 숙련도 상세 화면**
   - 파일: `lib/features/analytics/presentation/mastery_detail_screen.dart`
   - 개념별 mastery_score 목록 (색상 코딩: < 0.4 빨강, 0.4~0.7 노랑, >= 0.7 초록)
   - 각 개념 탭 시 관련 퀴즈 이력 + 오답 패턴 표시

4. **취약점 리포트 화면**
   - 파일: `lib/features/analytics/presentation/weakness_screen.dart`
   - 취약 개념 Top 5 + AI 추천 학습 경로 표시
   - "AI 튜터에게 질문하기" 버튼 → T-603 채팅 화면으로 해당 개념 컨텍스트 전달

#### 완료 기준

- [ ] 학습 대시보드에서 진도율, 학습 시간, 숙련도 차트 정상 렌더링
- [ ] 개념별 숙련도 상세 + 취약점 리포트 데이터 정상 표시
- [ ] 차트 데이터가 API 응답과 일치하는지 검증
- [ ] 에뮬레이터에서 차트 렌더링 성능 확인 (프레임 드롭 없음)

#### 규칙 체크리스트

- [ ] Riverpod `AsyncNotifier` 기반 분석 데이터 로딩
- [ ] `fl_chart` 사용 시 데이터 포맷 변환 로직 분리 (Presentation ↔ Data 분리)
- [ ] 빈 데이터 상태 처리 (아직 학습 기록 없음 안내)
- [ ] 네트워크 에러 시 재시도 UI 제공

---

## Week 21: 알림 + 관리자 대시보드

---

### T-605: NotificationWorker (채점/이의제기/Manual Review 완료 알림)

> **담당**: Backend | **선행**: T-305 | **관련 FR**: FR-NOTIFY-01

#### 서브 스텝

1. **알림 엔티티 + Repository**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/notification/Notification.java`
   - 컬럼: `id`, `userId`, `type`(GRADING_COMPLETE, APPEAL_RESULT, MANUAL_REVIEW_DONE, COST_ALERT), `title`, `body`, `isRead`, `referenceId`, `referenceType`, `createdAt`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/notification/NotificationRepository.java`
   - Flyway: `V{번호}__create_notifications_table.sql`

2. **알림 이벤트 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/GradingCompleted.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/AppealResolved.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/ManualReviewCompleted.java`
   - 각 이벤트: `userId`, `referenceId`, `result` 포함

3. **NotificationWorker (Kafka Consumer)**
   - 파일: `learnflow-api/src/main/java/com/learnflow/worker/NotificationWorker.java`
   - `@KafkaListener(topics = {"notification.grading", "notification.appeal", "notification.manual-review"})`
   - 멱등성: `dedup_key` 체크 — `notification_dedup` 테이블 or Redis SET (`SETNX`)
   - 이벤트 수신 → `Notification` 엔티티 INSERT → SSE/WebSocket 실시간 푸시 (선택)
   - 3회 실패 시 DLQ(`notification.dlq`) 전송

4. **알림 REST API**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/notification/NotificationController.java`
   - `GET /api/v1/notifications` — 사용자 알림 목록 (페이지네이션, 미읽음 우선 정렬)
   - `PATCH /api/v1/notifications/{id}/read` — 읽음 처리
   - `PATCH /api/v1/notifications/read-all` — 전체 읽음
   - `GET /api/v1/notifications/unread-count` — 미읽음 수 (Badge 용)
   - 응답: `ApiResponse<PageResponse<NotificationResponse>>` record DTO

5. **기존 서비스에서 Outbox 이벤트 발행 연결**
   - `AiGradingWorker`: 채점 완료 시 `OutboxPublisher.publish(GradingCompleted, "notification.grading")`
   - `AppealService`: Appeal 처리 완료 시 `OutboxPublisher.publish(AppealResolved, "notification.appeal")`
   - `ManualReviewQueue`: 강사 리뷰 완료 시 `OutboxPublisher.publish(ManualReviewCompleted, "notification.manual-review")`
   - 모두 비즈니스 데이터 변경과 동일 `@Transactional` 내 실행

#### 완료 기준

- [ ] 채점/이의제기/Manual Review 완료 시 알림 자동 생성 확인
- [ ] Consumer 멱등성 테스트: 동일 이벤트 중복 수신 시 알림 1건만 생성
- [ ] DLQ 3회 실패 시 `notification.dlq` 토픽 전송 확인
- [ ] 알림 목록 API 페이지네이션 + 미읽음 카운트 정상 동작
- [ ] `@SpringBootTest` 통합 테스트 작성

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음
- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — `markAsRead()` 등 비즈니스 메서드 사용
- [ ] `ApiResponse<T>` 래핑 응답
- [ ] Flyway 마이그레이션 파일 추가 (기존 파일 수정 금지)

---

### T-606: FinOps Soft/Hard Limit 알림 (CostThresholdReached 이벤트)

> **담당**: Backend | **선행**: T-317 | **관련 FR**: FR-NOTIFY-02

#### 서브 스텝

1. **CostThresholdReached 이벤트 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/CostThresholdReached.java`
   - 필드: `thresholdType`(SOFT_LIMIT, HARD_LIMIT), `currentCost`, `limit`, `percentage`, `timestamp`

2. **FinOpsGuard에서 임계값 감지 + 이벤트 발행**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/finops/FinOpsGuard.java` (기존 수정)
   - `checkBudget()` 호출 시 `cost_thresholds` 조회 → soft_limit/hard_limit 비교
   - Soft limit 도달: `OutboxPublisher.publish(CostThresholdReached(SOFT_LIMIT, ...), "notification.cost-alert")` + 모델 다운그레이드
   - Hard limit 도달: `OutboxPublisher.publish(CostThresholdReached(HARD_LIMIT, ...), "notification.cost-alert")` + Kill-switch (`is_killed = true`)
   - 동일 임계값에 대해 1일 1회만 알림 발행 (Redis key: `finops:alert:{type}:{date}`, TTL 24h)

3. **NotificationWorker 확장: cost-alert 토픽 구독**
   - `NotificationWorker`에 `notification.cost-alert` 토픽 리스너 추가
   - Soft limit: ADMIN 역할 전체에게 경고 알림 생성
   - Hard limit: ADMIN + 시스템 이메일 (Phase 6 범위 내 알림 엔티티 저장만, 이메일은 후속)

4. **Admin 알림 조회 API 확인**
   - 기존 `GET /api/v1/notifications` 에서 ADMIN 사용자도 자기 알림 조회 가능
   - `type = COST_ALERT` 필터 파라미터 지원

#### 완료 기준

- [ ] Soft limit(70%) 도달 시 ADMIN 알림 생성 + 모델 다운그레이드 동작 확인
- [ ] Hard limit(100%) 도달 시 ADMIN 알림 생성 + Kill-switch 활성화 확인
- [ ] 동일 임계값 1일 중복 알림 방지 확인
- [ ] `FinOpsGuard` 단위 테스트 + `NotificationWorker` 통합 테스트

#### 규칙 체크리스트

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] `destination_topic` = `"notification.cost-alert"` 명시
- [ ] FinOps Kill-switch 로직 검증 (Layer 7 보안)

---

### T-607: Admin 대시보드 UI (사용자 관리, AI 품질, FinOps)

> **담당**: Frontend | **선행**: T-504 | **관련 FR**: FR-QUALITY-01

#### 서브 스텝

1. **Admin 라우트 및 레이아웃 구성**
   - 파일: `learnflow-web/src/pages/admin/AdminLayout.tsx`
   - 사이드바: 사용자 관리, AI 품질, FinOps, 알림 메뉴
   - `ADMIN` 역할 가드: `useAuth()` 훅에서 역할 확인, 비인가 시 403 페이지
   - 라우트: `/admin/users`, `/admin/ai-quality`, `/admin/finops`

2. **사용자 관리 페이지**
   - 파일: `learnflow-web/src/pages/admin/UserManagementPage.tsx`
   - TanStack Query: `useQuery(['admin', 'users'], ...)` — `GET /api/v1/admin/users`
   - 테이블: shadcn/ui `DataTable` 컴포넌트 (검색, 정렬, 페이지네이션)
   - 역할 변경, 계정 잠금/해제 기능 (`PATCH /api/v1/admin/users/{id}/role`, `.../lock`)

3. **AI 품질 대시보드 페이지**
   - 파일: `learnflow-web/src/pages/admin/AiQualityPage.tsx`
   - TanStack Query: `GET /api/v1/admin/ai/quality/summary` — hallucination_rate, confidence_avg, feedback 통계
   - 차트: Recharts 라이브러리 — 일별 hallucination rate 추이, confidence 분포 히스토그램
   - RAGAS 평가 결과 테이블: faithfulness, context_precision, answer_relevancy (최근 3회 중앙값)
   - 프롬프트 버전 관리 목록: `GET /api/v1/admin/ai/quality/prompt-versions`

4. **FinOps 대시보드 페이지**
   - 파일: `learnflow-web/src/pages/admin/FinOpsPage.tsx`
   - TanStack Query: `GET /api/v1/admin/finops/summary`
   - 일별 비용 추이 라인 차트, 서비스별 비용 파이 차트
   - Unit Economics 테이블: cost_per_tutor_session, cost_per_quiz_generation, cost_per_grading
   - Kill-switch 상태 표시 + 수동 토글 (`PATCH /api/v1/admin/finops/kill-switch`)
   - Soft/Hard limit 설정 폼 (React Hook Form + Zod validation)

5. **실시간 알림 뱃지 (Header)**
   - 파일: `learnflow-web/src/components/layout/NotificationBell.tsx`
   - TanStack Query: `GET /api/v1/notifications/unread-count` (30초 polling `refetchInterval`)
   - 클릭 시 알림 드롭다운 목록, 읽음 처리 (`PATCH`)

#### 완료 기준

- [ ] Admin 대시보드 3개 페이지 (사용자/AI 품질/FinOps) 정상 렌더링
- [ ] API 연동 데이터 표시 + 차트 시각화 동작 확인
- [ ] Kill-switch 토글 및 Limit 설정 폼 동작 확인
- [ ] ADMIN 역할 외 접근 시 403 처리
- [ ] 반응형 레이아웃 (모바일/태블릿 대응)

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] 폼: React Hook Form + Zod 스키마 검증
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스
- [ ] `ApiResponse<T>` 구조 파싱 (success/data/error)

---

## Week 22: Grafana AI 대시보드

---

### T-608: Grafana AI Quality Panel (hallucination_rate, confidence_avg)

> **담당**: Infra | **선행**: T-312 | **관련 FR**: FR-OBS-03

#### 서브 스텝

1. **Prometheus 메트릭 노출 확인 (Backend)**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/quality/AiQualityMetrics.java`
   - Micrometer Counter/Gauge 등록:
     - `ai_hallucination_total` (Counter) — hallucination 감지 횟수
     - `ai_hallucination_rate` (Gauge, 최근 1시간 비율)
     - `ai_grading_confidence_avg` (Gauge) — 최근 채점 confidence 이동 평균
     - `ai_feedback_positive_total`, `ai_feedback_negative_total` (Counter)
   - `/actuator/prometheus` 엔드포인트에서 메트릭 스크래핑 확인

2. **Prometheus 스크래핑 설정**
   - 파일: `infra/prometheus/prometheus.yml`
   - `scrape_configs`에 `learnflow-api:8080` 타겟 확인
   - 라벨: `job=learnflow-api`, `instance=api:8080`

3. **Grafana 대시보드 JSON 생성**
   - 파일: `infra/grafana/dashboards/ai-quality.json`
   - Panel 1: **Hallucination Rate** — Time series, PromQL: `ai_hallucination_rate`, 임계값 라인 (5% 경고, 10% 위험)
   - Panel 2: **Confidence 분포** — Histogram, PromQL: `histogram_quantile(0.5, ai_grading_confidence_bucket)` + p95
   - Panel 3: **일별 피드백 비율** — Stat panel, positive / (positive + negative) * 100
   - Panel 4: **RAGAS 평가 추이** — Table panel, faithfulness/context_precision/answer_relevancy

4. **Grafana 프로비저닝 설정**
   - 파일: `infra/grafana/provisioning/dashboards/dashboards.yml` — `ai-quality.json` 자동 로드 설정
   - 파일: `infra/grafana/provisioning/datasources/datasources.yml` — Prometheus datasource 확인

#### 완료 기준

- [ ] Grafana에서 AI Quality 대시보드 자동 프로비저닝 확인
- [ ] hallucination_rate, confidence_avg 실시간 메트릭 표시
- [ ] 임계값 라인(5%, 10%) 알림 표시 동작
- [ ] `docker compose up` 후 `http://localhost:3001` 접속 시 대시보드 즉시 확인 가능

#### 규칙 체크리스트

- [ ] Prometheus 메트릭 Micrometer 기반 노출
- [ ] Grafana 대시보드 JSON 프로비저닝 (수동 UI 생성 금지)
- [ ] PromQL 쿼리 정확성 검증
- [ ] 대시보드 변수 (timeRange, interval) 활용

---

### T-609: Grafana RAG Panel (latency breakdown, cache_hit_rate)

> **담당**: Infra | **선행**: T-312 | **관련 FR**: FR-OBS-03

#### 서브 스텝

1. **RAG 파이프라인 메트릭 노출**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/rag/RagMetrics.java`
   - Micrometer Timer 등록:
     - `rag_query_rewrite_duration_seconds` — Query Rewrite 단계 소요 시간
     - `rag_hybrid_search_duration_seconds` — Hybrid Search 단계
     - `rag_reranking_duration_seconds` — Re-ranking 단계
     - `rag_context_compression_duration_seconds` — Context Compression 단계
     - `rag_total_duration_seconds` — 전체 RAG 파이프라인 소요 시간
   - Counter:
     - `rag_cache_hits_total`, `rag_cache_misses_total` — 캐시 히트/미스
     - `rag_requests_total` — 총 요청 수

2. **Grafana 대시보드 JSON 생성**
   - 파일: `infra/grafana/dashboards/rag-performance.json`
   - Panel 1: **RAG Latency Breakdown** — Stacked bar chart, 각 단계별 P50/P95/P99 소요 시간
   - Panel 2: **Cache Hit Rate** — Gauge panel, `rag_cache_hits_total / rag_requests_total * 100`
   - Panel 3: **RAG 처리량** — Time series, `rate(rag_requests_total[5m])` (req/sec)
   - Panel 4: **P95 총 소요 시간 추이** — Time series with threshold (4초 목표 라인)

3. **Grafana 프로비저닝에 추가**
   - `infra/grafana/provisioning/dashboards/dashboards.yml`에 `rag-performance.json` 추가

#### 완료 기준

- [ ] RAG 파이프라인 각 단계별 소요 시간 Grafana에서 확인 가능
- [ ] 캐시 히트율 게이지 실시간 표시
- [ ] P95 < 4초 목표 라인 표시 및 초과 시 색상 경고
- [ ] 대시보드 JSON 프로비저닝 자동 로드 확인

#### 규칙 체크리스트

- [ ] Prometheus Timer (Micrometer `@Timed` 또는 수동 `Timer.Sample`)
- [ ] Grafana JSON 프로비저닝 (수동 UI 생성 금지)
- [ ] PromQL `histogram_quantile` 사용 시 `le` 라벨 확인
- [ ] 성능 목표 (P95 < 4s) 임계값 라인 반영

---

### T-610: Grafana FinOps Panel (daily_cost, unit_economics)

> **담당**: Infra | **선행**: T-315 | **관련 FR**: FR-OBS-03

#### 서브 스텝

1. **FinOps 메트릭 노출**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/finops/FinOpsMetrics.java`
   - Gauge:
     - `finops_daily_cost_usd` — 당일 누적 비용 (Redis `finops:daily_cost` 반영)
     - `finops_budget_remaining_percent` — 잔여 예산 비율
     - `finops_kill_switch_active` — Kill-switch 활성 상태 (0/1)
   - Counter:
     - `finops_requests_by_model_total{model="haiku|sonnet|opus"}` — 모델별 요청 수
     - `finops_tokens_by_model_total{model="..."}` — 모델별 토큰 사용량
   - Gauge (Unit Economics):
     - `finops_cost_per_tutor_session_usd` — 세션당 비용
     - `finops_cost_per_quiz_generation_usd` — 퀴즈 생성 건당 비용
     - `finops_cost_per_grading_usd` — 채점 건당 비용

2. **Grafana 대시보드 JSON 생성**
   - 파일: `infra/grafana/dashboards/finops.json`
   - Panel 1: **일별 비용 추이** — Time series, `finops_daily_cost_usd`, Soft/Hard limit 라인
   - Panel 2: **모델별 요청 분포** — Pie chart, `finops_requests_by_model_total`
   - Panel 3: **Unit Economics** — Stat panels, 각 지표 현재값 + 목표 대비 색상 (초록: 목표 이하, 빨강: 초과)
   - Panel 4: **Kill-switch 상태** — Single stat, `finops_kill_switch_active` (0=정상 초록, 1=활성 빨강)
   - Panel 5: **예산 잔여율** — Gauge, `finops_budget_remaining_percent` (30% 이하 빨강)

3. **Grafana Alert Rule 설정**
   - `finops_daily_cost_usd > soft_limit` → Slack/Email 알림 (Grafana alerting)
   - `finops_kill_switch_active == 1` → 긴급 알림

#### 완료 기준

- [ ] 일별 비용, 모델별 분포, Unit Economics Grafana에서 확인 가능
- [ ] Kill-switch 상태 실시간 반영
- [ ] Unit Economics 목표 (session < $0.15, quiz < $0.05, grading < $0.03) 대비 색상 표시
- [ ] Grafana Alert Rule 트리거 테스트 통과

#### 규칙 체크리스트

- [ ] Prometheus 메트릭 Micrometer 기반 노출
- [ ] Grafana JSON 프로비저닝
- [ ] FinOps 목표값 하드코딩 금지 — Grafana 변수 또는 Prometheus recording rule 사용
- [ ] Alert Rule은 `infra/grafana/provisioning/alerting/` 에서 프로비저닝

---

### T-611: Grafana PII + Outbox/Consumer Panel

> **담당**: Infra | **선행**: T-312 | **관련 FR**: FR-OBS-03

#### 서브 스텝

1. **PII 메트릭 노출**
   - 파일: `learnflow-api/src/main/java/com/learnflow/ai/gateway/PiiMetrics.java`
   - Counter:
     - `pii_masking_total{direction="input|output"}` — Input/Output PII 마스킹 건수
     - `pii_detected_total{type="name|phone|email|ssn|..."}` — 타입별 PII 감지 수
   - Timer: `pii_processing_duration_seconds{direction="input|output"}` — PII 처리 소요 시간
   - Counter: `pii_output_new_detected_total` — Output에서 LLM이 새로 생성한 PII 감지 건수

2. **Outbox/Consumer 메트릭 노출**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/outbox/OutboxMetrics.java`
   - Gauge: `outbox_pending_count` — 미발행 outbox 이벤트 수
   - Counter: `outbox_published_total`, `outbox_failed_total` — 발행 성공/실패
   - Counter: `consumer_processed_total{worker="embedding|grading|analytics|notification"}` — 워커별 처리 건수
   - Counter: `consumer_dlq_total{worker="..."}` — 워커별 DLQ 건수
   - Gauge: `consumer_lag{topic="..."}` — Kafka consumer lag

3. **Grafana 대시보드 JSON 생성**
   - 파일: `infra/grafana/dashboards/pii-outbox.json`
   - Row 1 (PII):
     - Panel: **PII 감지 추이** — Time series, Input/Output 분리
     - Panel: **PII 타입별 분포** — Bar chart, `pii_detected_total` by type
     - Panel: **Output 신규 PII 감지** — Stat (0이 목표, 1 이상 시 빨강)
   - Row 2 (Outbox/Consumer):
     - Panel: **Outbox 미발행 대기** — Gauge, `outbox_pending_count` (100 이상 경고)
     - Panel: **워커별 처리량** — Time series, `rate(consumer_processed_total[5m])` by worker
     - Panel: **DLQ 누적** — Bar chart, `consumer_dlq_total` by worker (0이 목표)
     - Panel: **Consumer Lag** — Time series by topic

4. **Grafana 프로비저닝에 추가**
   - `infra/grafana/provisioning/dashboards/dashboards.yml`에 `pii-outbox.json` 추가

#### 완료 기준

- [ ] PII 감지 건수, 타입별 분포, Output 신규 PII Grafana에서 확인
- [ ] Outbox 미발행 대기, 워커별 처리량, DLQ 건수 Grafana에서 확인
- [ ] Consumer lag 실시간 모니터링 가능
- [ ] 모든 패널 데이터 정상 표시 (빈 메트릭 시 "No data" 표시)

#### 규칙 체크리스트

- [ ] Prometheus 메트릭 Micrometer 기반 노출
- [ ] Grafana JSON 프로비저닝
- [ ] PII Output 신규 감지 수 = 0 유지 목표 (알림 설정)
- [ ] Outbox pending 100 이상 시 알림 설정

---

## Week 23: 보안 강화 + Chaos Testing

---

### T-612: 7 Layer 보안 점검 (입력 필터링 ~ FinOps Kill-switch)

> **담당**: QA + Infra | **선행**: T-317 | **관련 FR**: NFR-SEC-07

#### 서브 스텝

1. **Layer 1: 입력 필터링 점검**
   - 테스트: 최대 길이 초과 입력 (10,000자 이상), SQL Injection 패턴, XSS 스크립트 삽입
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/gateway/InputFilterTest.java`
   - 검증: 위험 패턴 차단 + 적절한 에러 응답 반환

2. **Layer 2: PII Masking 점검**
   - 테스트: 한국 주민등록번호, 전화번호, 이메일, 이름 포함 입력 → 마스킹 확인
   - Output에서 LLM이 생성한 새로운 PII → 감지 + 마스킹 확인
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/gateway/PiiMaskingIntegrationTest.java`

3. **Layer 3~4: System Prompt 격리 + Output Validation**
   - Prompt Injection 공격: "Ignore previous instructions...", "What is your system prompt?" 등
   - 채점 결과 Output: 점수 범위 (0~100), JSON 스키마 준수 검증
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/gateway/SecurityLayerTest.java`

4. **Layer 5~6: 데이터 격리 + Tool 제한**
   - RAG 검색 시 다른 course_id 데이터 노출 시도 → 차단 확인
   - LLM 응답에 DB 쿼리, 외부 URL, 파일 시스템 접근 시도 → 차단 확인

5. **Layer 7: FinOps Kill-switch 점검**
   - Hard limit 초과 시나리오 → Kill-switch 활성화 → 이후 LLM 호출 거부 확인
   - Kill-switch 해제 → 정상 서비스 복구 확인

6. **보안 점검 결과 보고서 작성**
   - 7개 Layer별 테스트 결과 + 발견된 취약점 + 조치 내역 문서화

#### 완료 기준

- [ ] 7개 Layer 모든 테스트 통과
- [ ] Prompt Injection 공격 차단 확인
- [ ] PII가 LLM으로 전송되지 않음 확인 (로그 검증)
- [ ] course_id 격리 위반 불가 확인
- [ ] 보안 점검 보고서 작성 완료

#### 규칙 체크리스트

- [ ] Layer 1: 입력 필터링 (길이 제한 + 위험 패턴 감지)
- [ ] Layer 2: PII Masking (Input + Output 양방향)
- [ ] Layer 3: System Prompt 격리 (노출 금지)
- [ ] Layer 4: Output Validation (점수 범위 / JSON 스키마)
- [ ] Layer 5: 데이터 격리 (`course_id` 기반)
- [ ] Layer 6: Tool 제한 (DB 직접 조회 / 외부 URL / 파일 차단)
- [ ] Layer 7: FinOps Kill-switch

---

### T-613: Chaos Test: Kafka 브로커 다운

> **담당**: QA | **선행**: T-304 | **관련 FR**: NFR-AVAIL-03

#### 서브 스텝

1. **테스트 환경 구성**
   - Docker Compose로 전체 스택 실행
   - 테스트 스크립트: `infra/chaos/kafka-broker-down.sh`
   - 시나리오: 정상 운영 중 `docker stop kafka` → 복구 후 `docker start kafka`

2. **Outbox Relay 복원력 테스트**
   - Kafka 다운 중 비즈니스 이벤트 발생 → `outbox_events` 테이블에 정상 INSERT 확인
   - Kafka 복구 후 Outbox Relay가 미발행 이벤트 자동 재발행 확인
   - 검증: `outbox_events.status` = `PENDING` → Kafka 복구 후 `PUBLISHED` 전환

3. **Consumer 복원력 테스트**
   - Kafka 복구 후 Consumer가 자동 재연결 + 미처리 메시지 소비 확인
   - 이벤트 유실 0건 검증: 발행 건수 == 소비 건수

4. **결과 보고서**
   - Kafka 다운 시간, 복구 시간, 이벤트 유실 여부, Outbox 대기 최대 건수 기록

#### 완료 기준

- [ ] Kafka 다운 중 Outbox에 이벤트 안전 저장 확인
- [ ] Kafka 복구 후 미발행 이벤트 전량 발행 확인
- [ ] 이벤트 유실 0건
- [ ] Consumer 자동 재연결 및 처리 완료 확인

#### 규칙 체크리스트

- [ ] Outbox 패턴 복원력 검증 (KafkaTemplate 직접 호출이 아닌 Outbox 경유)
- [ ] Consumer 멱등성 보장 (중복 소비 시 dedup_key 체크)
- [ ] DLQ 처리 로직 정상 동작

---

### T-614: Chaos Test: PII 대량 입력

> **담당**: QA | **선행**: T-309 | **관련 FR**: NFR-SEC-03

#### 서브 스텝

1. **PII 대량 입력 테스트 데이터 생성**
   - 파일: `infra/chaos/pii-mass-input-data.json`
   - 100개 이상의 PII 포함 메시지 (이름, 전화번호, 주민등록번호, 이메일, 주소 혼합)
   - 한국어 특화 PII: 주민등록번호 (6-7자리), 휴대폰 (010-XXXX-XXXX), 한국식 이름

2. **부하 테스트 실행**
   - 도구: k6 또는 shell script (`infra/chaos/pii-mass-input.sh`)
   - 동시 50 요청으로 AI 튜터 채팅 API (`POST /api/v1/ai/chat/sessions/{id}/messages`) 호출
   - 모든 요청에 PII 포함 데이터 전송

3. **검증 항목**
   - PII Masking 서비스 정상 작동 (100% 마스킹 성공)
   - LLM 요청 로그에서 원본 PII 미존재 확인 (감사 로그 검증)
   - 성능: PII 처리로 인한 P95 지연 시간 측정 (목표: 200ms 이내 추가 지연)
   - Redis PII 매핑 세션 스코프 정리 확인

4. **결과 보고서**
   - PII 감지율, 마스킹 성공률, 추가 지연 시간, 메모리 사용량 기록

#### 완료 기준

- [ ] 100% PII 마스킹 성공 (원본 PII가 LLM으로 전송되지 않음)
- [ ] PII 처리 추가 지연 P95 < 200ms
- [ ] 동시 50 요청 부하에서 서비스 안정성 유지 (에러율 < 1%)
- [ ] Redis PII 매핑 세션 종료 후 자동 삭제 확인

#### 규칙 체크리스트

- [ ] PII Masking Input + Output 양방향 검증
- [ ] 감사 로그에 PII 마스킹 기록 존재
- [ ] Redis `pii:mapping:{sessionId}` TTL 확인

---

### T-615: Chaos Test: 비용 폭주 시나리오

> **담당**: QA | **선행**: T-317 | **관련 FR**: FR-FINOPS-03

#### 서브 스텝

1. **테스트 시나리오 설정**
   - `cost_thresholds` 테이블에 낮은 임계값 설정 (soft_limit: $1.00, hard_limit: $2.00)
   - 테스트 스크립트: `infra/chaos/cost-spike.sh`

2. **비용 폭주 시뮬레이션**
   - Opus 모델 강제 라우팅으로 고비용 요청 연속 발생
   - 또는 `ai_cost_logs` 직접 INSERT로 비용 누적 시뮬레이션
   - 단계별 검증: 잔여 > 70% → 정상, 50~70% → Opus 비활성, 30~50% → Haiku 우선, < 30% → Haiku 전용

3. **Kill-switch 동작 검증**
   - Hard limit 도달 → `is_killed = true` 자동 전환
   - Kill-switch 활성 후 모든 LLM 호출 즉시 거부 확인 (적절한 에러 응답)
   - `CostThresholdReached` 이벤트 → NotificationWorker → ADMIN 알림 생성 확인

4. **복구 검증**
   - ADMIN이 Kill-switch 수동 해제 (`PATCH /api/v1/admin/finops/kill-switch`)
   - 해제 후 정상 서비스 복구 확인
   - 예산 리셋 또는 증액 후 라우팅 정상화 확인

#### 완료 기준

- [ ] 예산 단계별 모델 다운그레이드 동작 확인 (4단계)
- [ ] Hard limit 도달 시 Kill-switch 자동 활성화 + LLM 호출 거부
- [ ] ADMIN 알림 (Soft/Hard) 정상 생성
- [ ] Kill-switch 해제 후 정상 복구 확인

#### 규칙 체크리스트

- [ ] FinOps Kill-switch (Layer 7 보안)
- [ ] `OutboxPublisher.publish()` 경유 알림 이벤트 발행
- [ ] `ai_cost_logs` 정확한 비용 기록

---

### T-616: Chaos Test: LLM API 장애 (Circuit Breaker Fallback)

> **담당**: QA | **선행**: T-311 | **관련 FR**: NFR-AVAIL-02

#### 서브 스텝

1. **Claude API 장애 시뮬레이션**
   - 방법 1: WireMock으로 Claude API 엔드포인트 모킹 → 500/503 응답 반환
   - 방법 2: `ClaudeApiClient`에 테스트용 장애 플래그 주입
   - 파일: `learnflow-api/src/test/java/com/learnflow/ai/client/CircuitBreakerFallbackTest.java`

2. **Circuit Breaker 동작 검증 (Resilience4j)**
   - 연속 실패 임계값 도달 → Circuit OPEN 상태 전환 확인
   - OPEN 상태에서 Claude 호출 차단 + OpenAI Fallback 자동 전환 확인
   - Half-Open → 성공 시 CLOSED 복구 확인

3. **Fallback 응답 품질 검증**
   - OpenAI Fallback 응답이 정상적으로 사용자에게 전달되는지 확인
   - PII Masking, FinOps 비용 기록이 Fallback 경로에서도 동작하는지 확인
   - Fallback 시 `ai_cost_logs`에 `model=gpt-4o` 기록 확인

4. **이중 장애 시나리오**
   - Claude + OpenAI 모두 장애 시 → 적절한 에러 메시지 반환 (503 Service Unavailable)
   - 사용자에게 "일시적 서비스 장애" 안내 메시지 확인

#### 완료 기준

- [ ] Circuit Breaker OPEN 시 Claude → OpenAI 자동 Fallback 동작
- [ ] Fallback 경로에서도 PII Masking + FinOps 비용 기록 정상 동작
- [ ] Claude 복구 시 Circuit CLOSED 자동 복원
- [ ] 이중 장애 시 적절한 에러 응답 (503) 반환
- [ ] Resilience4j 이벤트 로그 + Prometheus 메트릭 기록 확인

#### 규칙 체크리스트

- [ ] AI Gateway 경유 확인 (Fallback 경로 포함)
- [ ] PII Masking 파이프라인 Fallback에서도 동작
- [ ] FinOps 비용 기록 Fallback 모델 포함
- [ ] Circuit Breaker 상태 Prometheus 메트릭 노출

---

### T-617: Chaos Test: Consumer DLQ 복구

> **담당**: QA | **선행**: T-304 | **관련 FR**: FR-EVENT-04

#### 서브 스텝

1. **DLQ 발생 시나리오 구성**
   - 의도적 Consumer 실패 유도: 잘못된 이벤트 페이로드, DB 커넥션 장애 시뮬레이션
   - 대상 워커: EmbeddingWorker, AiGradingWorker, NotificationWorker
   - 파일: `infra/chaos/consumer-dlq-test.sh`

2. **DLQ 메시지 확인**
   - Consumer 3회 재시도 실패 → DLQ 토픽(`*.dlq`) 전송 확인
   - DLQ 메시지 내용 검증: 원본 이벤트 + 실패 사유 + 재시도 횟수 포함

3. **DLQ 복구 프로세스 테스트**
   - DLQ 메시지 수동 재처리: Admin API 또는 CLI 도구로 DLQ 메시지 재발행
   - 장애 원인 제거 후 재처리 성공 확인
   - `outbox_events.status` = `DEAD_LETTER` → 재처리 후 `PUBLISHED` 전환

4. **모니터링 연동 확인**
   - DLQ 발생 시 Grafana `consumer_dlq_total` 메트릭 증가 확인 (T-611 패널)
   - DLQ 발생 시 알림 트리거 확인

#### 완료 기준

- [ ] 3회 실패 후 DLQ 토픽 전송 확인
- [ ] DLQ 메시지에 원본 이벤트 + 실패 정보 포함
- [ ] DLQ 재처리 후 정상 소비 완료 확인
- [ ] Grafana DLQ 메트릭 + 알림 동작 확인

#### 규칙 체크리스트

- [ ] Consumer 멱등성 보장 (재처리 시 중복 방지)
- [ ] DLQ 메시지 구조: 원본 이벤트 + `dedup_key` + 실패 사유
- [ ] `outbox_events` 상태 전이 검증 (PENDING → PUBLISHED → DEAD_LETTER)

---

### T-618: 보안 취약점 점검 보고서 (OWASP Top 10)

> **담당**: QA | **선행**: T-612 | **관련 FR**: NFR-SEC-05

#### 서브 스텝

1. **OWASP Top 10 (2021) 항목별 점검**
   - A01: Broken Access Control — API 엔드포인트별 인가 검증 (LEARNER/INSTRUCTOR/ADMIN 역할 분리)
   - A02: Cryptographic Failures — JWT 서명 알고리즘, 비밀번호 BCrypt, HTTPS 적용
   - A03: Injection — SQL Injection (JPA 파라미터 바인딩), XSS (React 기본 이스케이프), Command Injection
   - A07: Authentication — 로그인 실패 잠금, 세션 관리, Refresh Token 탈취 대응

2. **자동화 보안 스캔 도구 실행**
   - OWASP ZAP 또는 `trivy`로 Docker 이미지 취약점 스캔
   - `./gradlew dependencyCheckAnalyze` — OWASP Dependency Check (라이브러리 CVE)
   - `pnpm audit` — npm 패키지 취약점

3. **AI 특화 보안 점검**
   - T-612 결과 통합: 7 Layer 보안 점검 결과 포함
   - Prompt Injection 저항력 검증 결과
   - PII 유출 방지 검증 결과

4. **보고서 작성**
   - 파일: `docs/security/owasp-top10-report.md`
   - 항목별: 점검 내용, 결과 (Pass/Fail/Partial), 발견 취약점, 조치 방안, 조치 완료 여부
   - 심각도 분류: Critical / High / Medium / Low

#### 완료 기준

- [ ] OWASP Top 10 전체 항목 점검 완료
- [ ] 자동화 스캔 도구 실행 결과 포함
- [ ] Critical/High 취약점 0건 (또는 전량 조치 완료)
- [ ] AI 특화 보안 점검 (7 Layer) 결과 통합
- [ ] 보고서 작성 및 팀 공유 완료

#### 규칙 체크리스트

- [ ] OWASP Top 10 (2021) 기준 적용
- [ ] AI 7 Layer 보안 점검 결과 포함
- [ ] 발견 취약점에 대한 조치 방안 필수 기재
- [ ] 자동화 도구 스캔 결과 증빙 첨부

---

## Week 24: 통합 테스트 + 배포 + 문서화

---

### T-619: 통합 테스트 전체 실행 + 성능 테스트 (k6)

> **담당**: QA | **선행**: T-618 | **관련 FR**: NFR-PERF-01~04

#### 서브 스텝

1. **통합 테스트 전체 실행**
   - `./gradlew test` — Backend 전체 단위 + 통합 테스트
   - `pnpm test` — Frontend Vitest 전체 실행
   - `flutter test` — Mobile 전체 실행
   - 실패 테스트 0건 목표, 실패 시 수정 후 재실행

2. **k6 성능 테스트 시나리오 작성**
   - 파일: `infra/k6/scenarios/`
     - `auth-flow.js` — 로그인 → 토큰 발급 시나리오
     - `course-browsing.js` — 강의 목록 → 상세 → 레슨 조회
     - `ai-tutor-chat.js` — AI 튜터 채팅 (SSE 스트리밍)
     - `quiz-submission.js` — 퀴즈 제출 → AI 채점
     - `full-journey.js` — 전체 학습 여정 시나리오 (E2E)

3. **성능 목표 검증**
   - VUs (Virtual Users): 100명 동시 접속
   - P95 응답 시간: API < 500ms, AI 튜터 (first token) < 4s
   - 에러율: < 1%
   - 이벤트 유실: 0건

4. **성능 테스트 실행 및 결과 분석**
   - `k6 run --out json=results.json infra/k6/scenarios/full-journey.js`
   - Grafana k6 대시보드에서 결과 시각화 (선택)
   - 병목 지점 식별 → 최적화 조치 → 재테스트

#### 완료 기준

- [ ] Backend/Frontend/Mobile 전체 테스트 통과 (실패 0건)
- [ ] k6 성능 테스트 5개 시나리오 실행 완료
- [ ] P95 < 4s (AI 튜터), P95 < 500ms (일반 API) 달성
- [ ] 이벤트 유실 0건 확인
- [ ] 성능 테스트 결과 보고서 작성

#### 규칙 체크리스트

- [ ] `@WebMvcTest` (컨트롤러) + `@DataJpaTest` (리포지토리) + `@SpringBootTest` (통합) 테스트 유형 분리
- [ ] k6 시나리오에 thresholds 설정 (자동 Pass/Fail 판정)
- [ ] 성능 테스트는 Docker Compose 전체 스택 환경에서 실행

---

### T-620: Docker Compose 전체 스택 배포 (13개 서비스)

> **담당**: Infra | **선행**: T-619 | **관련 FR**: -

#### 서브 스텝

1. **Docker Compose 최종 구성 검증**
   - 파일: `docker-compose.yml` (루트)
   - 13개 서비스 전체 정의 확인:
     - `api` (8080), `web` (3000), `mysql` (3306), `redis` (6379)
     - `kafka` (9092) + `zookeeper` (2181), `debezium` (8083)
     - `pgvector` (5433), `elasticsearch` (9200)
     - `minio` (9000/9001), `zipkin` (9411)
     - `prometheus` (9090), `grafana` (3001)
   - 서비스 간 `depends_on` + `healthcheck` 설정 (시작 순서 보장)

2. **Dockerfile 최적화**
   - 파일: `learnflow-api/Dockerfile` — Multi-stage build (builder → runtime), JRE 21 slim 기반
   - 파일: `learnflow-web/Dockerfile` — Multi-stage (pnpm build → nginx static serve)
   - `.dockerignore` 최적화: `node_modules`, `.git`, `build/`, `target/` 제외

3. **환경변수 및 시크릿 관리**
   - 파일: `.env.example` — 필수 환경변수 템플릿 (실제 값 미포함)
   - `CLAUDE_API_KEY`, `DB_ROOT_PASSWORD`, `JWT_SECRET` 등 → `.env` (Git 미추적)
   - Docker Compose `env_file: .env` 설정

4. **전체 스택 기동 + 헬스체크**
   - `docker compose up -d` → 13개 서비스 전체 정상 기동 확인
   - 헬스체크 스크립트: `infra/scripts/healthcheck-all.sh`
     - API: `curl http://localhost:8080/actuator/health`
     - Web: `curl http://localhost:3000`
     - MySQL, Redis, Kafka, ES, pgvector 연결 확인

5. **볼륨 및 데이터 영속성**
   - MySQL, pgvector, MinIO, ES 데이터 볼륨 마운트 설정
   - `docker compose down` 후 `docker compose up` 시 데이터 유지 확인
   - `docker compose down -v` 로 완전 초기화 옵션 확인

#### 완료 기준

- [ ] `docker compose up -d` 로 13개 서비스 전체 정상 기동
- [ ] 모든 서비스 헬스체크 통과
- [ ] API → MySQL, Redis, Kafka, pgvector, ES, MinIO 연결 정상
- [ ] Grafana 대시보드 자동 프로비저닝 + 메트릭 수집 정상
- [ ] `.env.example` 제공 + `.env` Git 미추적 확인

#### 규칙 체크리스트

- [ ] Docker 이미지 Multi-stage build (불필요한 빌드 도구 미포함)
- [ ] 환경변수로 시크릿 관리 (하드코딩 금지)
- [ ] `healthcheck` 설정으로 서비스 시작 순서 보장
- [ ] 데이터 볼륨 영속성 확인

---

### T-621: 최종 문서 세트 (배포 가이드, 사용자 매뉴얼, 운영 매뉴얼)

> **담당**: 전체 | **선행**: T-620 | **관련 FR**: -

#### 서브 스텝

1. **배포 가이드 작성**
   - 파일: `docs/deployment-guide.md`
   - 사전 요구사항 (Docker, Docker Compose, 최소 시스템 사양)
   - `.env` 설정 가이드 (환경변수 목록 + 설명)
   - 단계별 배포 절차: `docker compose up -d` → 헬스체크 → 초기 데이터 마이그레이션
   - 트러블슈팅 FAQ (포트 충돌, 메모리 부족, 서비스 미기동 등)

2. **사용자 매뉴얼 작성**
   - 파일: `docs/user-manual.md`
   - 학습자 가이드: 회원가입 → 강의 수강 → AI 튜터 사용 → 퀴즈/과제 → 학습 분석 확인
   - 강사 가이드: 강의 생성 → 콘텐츠 업로드 → 퀴즈/과제 출제 → Manual Review Queue 처리
   - 관리자 가이드: Admin 대시보드 → 사용자 관리 → AI 품질 모니터링 → FinOps 관리
   - 스크린샷/다이어그램 포함

3. **운영 매뉴얼 작성**
   - 파일: `docs/operations-manual.md`
   - Grafana 대시보드 활용 가이드 (AI Quality, RAG, FinOps, PII/Outbox 패널)
   - 장애 대응 절차: Kafka 다운, LLM API 장애, Kill-switch 발동, DLQ 처리
   - 백업/복구 절차: MySQL 백업, MinIO 데이터 백업
   - 스케일링 가이드: Consumer 워커 수평 확장, Redis Cluster 전환 시 고려사항

4. **API 문서 최종 검증**
   - Swagger/OpenAPI 스펙 최종 확인 (`/swagger-ui.html`)
   - 엔드포인트 목록과 실제 구현 일치 확인

#### 완료 기준

- [ ] 배포 가이드: 문서만으로 신규 개발자가 전체 스택 기동 가능
- [ ] 사용자 매뉴얼: 학습자/강사/관리자 역할별 가이드 완비
- [ ] 운영 매뉴얼: 장애 대응 + 모니터링 + 백업 절차 포함
- [ ] API 문서 (Swagger) 최신 상태 확인

#### 규칙 체크리스트

- [ ] 문서 내 시크릿/API 키 미포함 확인
- [ ] 환경변수 설명에 기본값 및 필수 여부 명시
- [ ] 장애 대응 절차에 Chaos Test 결과 (T-613~T-617) 반영

---

### T-622: GitHub Actions CI/CD 파이프라인 최종 구성

> **담당**: Infra | **선행**: T-620 | **관련 FR**: -

#### 서브 스텝

1. **CI 파이프라인 (Pull Request)**
   - 파일: `.github/workflows/ci.yml`
   - 트리거: `pull_request` → `main`, `dev` 브랜치
   - Jobs:
     - `lint`: Checkstyle (Java) + ESLint (TypeScript) + Dart Analyzer (Flutter)
     - `test`: `./gradlew test` + `pnpm test` + `flutter test`
     - `build`: `./gradlew build` + `pnpm build` + `flutter build apk`
     - `security`: OWASP Dependency Check + `pnpm audit` + `trivy` 이미지 스캔
     - `commit-lint`: Conventional Commits 형식 검증

2. **CD 파이프라인 (Main 병합)**
   - 파일: `.github/workflows/cd.yml`
   - 트리거: `push` → `main` 브랜치
   - Jobs:
     - `build-and-push`: Docker 이미지 빌드 → Container Registry 푸시
     - 태깅 전략: `latest`, `commit-sha` (예: `abc1234`), `version` (예: `v1.0.0`)
     - `deploy`: Docker Compose 기반 배포 (또는 `docker stack deploy`)

3. **시크릿 관리**
   - GitHub Secrets: `CLAUDE_API_KEY`, `DB_ROOT_PASSWORD`, `DOCKER_REGISTRY_TOKEN`, `JWT_SECRET`
   - `.github/workflows/` 에서 `${{ secrets.* }}` 참조
   - 시크릿이 로그에 노출되지 않도록 `::add-mask::` 확인

4. **캐싱 최적화**
   - Gradle: `actions/cache` — `~/.gradle/caches`, `~/.gradle/wrapper`
   - pnpm: `actions/cache` — `node_modules/.pnpm-store`
   - Docker: `docker/build-push-action`의 `cache-from`/`cache-to` 설정

5. **Status Badge + Branch Protection**
   - `README.md`에 CI 상태 뱃지 추가
   - Branch Protection Rule: `main` 브랜치 → CI 통과 필수, 1명 이상 리뷰 필수

#### 완료 기준

- [ ] PR 시 lint + test + build + security + commit-lint 자동 실행
- [ ] main 병합 시 Docker 이미지 자동 빌드 + 3-tier 태깅 + Registry 푸시
- [ ] GitHub Secrets 설정 완료 + 로그 노출 없음
- [ ] 캐싱으로 CI 실행 시간 50% 이상 단축 (2회차 기준)
- [ ] Branch Protection Rule 활성화

#### 규칙 체크리스트

- [ ] Conventional Commits 형식 검증 자동화
- [ ] Docker 이미지 3-tier 태깅 (latest, commit-sha, version)
- [ ] 시크릿 로그 노출 방지
- [ ] PR 제목: `[LF-{이슈번호}] {타입}: {설명}` 형식 검증 (선택)

---

## Phase 6 완료 기준 요약

| 영역 | 핵심 지표 | 목표 |
|------|----------|------|
| Flutter 모바일 | 강의/채팅/분석 3개 핵심 화면 | 에뮬레이터 정상 동작 |
| 알림 | 채점/Appeal/Cost 알림 | 이벤트 발생 → 알림 자동 생성 |
| Grafana | AI Quality/RAG/FinOps/PII 4개 대시보드 | 실시간 메트릭 시각화 |
| 보안 | 7 Layer + OWASP Top 10 | Critical/High 0건 |
| Chaos Test | 5개 시나리오 | 이벤트 유실 0, 자동 복구 |
| 성능 | k6 부하 테스트 | P95 < 4s, 에러율 < 1% |
| 배포 | Docker Compose 13개 서비스 | 전체 스택 정상 기동 |
| CI/CD | GitHub Actions | lint + test + build + deploy 자동화 |
| 문서 | 배포/사용자/운영 매뉴얼 | 3종 문서 완비 |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v1.0 | 2026-04-03 | 초안 작성: T-601 ~ T-622 세부 워크플로우 정의 |
