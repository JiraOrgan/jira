# Phase 2 — 핵심 학습 (Week 5~8) Task Workflows

> **기간**: 2026-05-05 ~ 06-01
> **마일스톤**: M2 — 학습 기능
> **Task 수**: 14개 (T-201 ~ T-214)
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [README.md](README.md)

---

## 목차

- [Week 5: 학습 진도 추적](#week-5-학습-진도-추적)
  - [T-201: learning_activities 테이블 + 진도 추적 API](#t-201-learning_activities-테이블--진도-추적-api)
  - [T-202: 레슨 완료 처리 + LessonCompleted 이벤트 정의](#t-202-레슨-완료-처리--lessoncompleted-이벤트-정의)
- [Week 6: 퀴즈/과제 + 채점 이의 제기](#week-6-퀴즈과제--채점-이의-제기)
  - [T-203: 퀴즈 엔티티 + CRUD API](#t-203-퀴즈-엔티티--crud-api)
  - [T-204: 과제 제출 엔티티 + API](#t-204-과제-제출-엔티티--api)
  - [T-205: 채점 이의 제기(Appeal) API + GradingAppeal 이벤트](#t-205-채점-이의-제기appeal-api--gradingappeal-이벤트)
  - [T-206: Manual Review Queue API (강사용)](#t-206-manual-review-queue-api-강사용)
  - [T-207: 퀴즈 결과 + AI 피드백 조회 API](#t-207-퀴즈-결과--ai-피드백-조회-api)
  - [T-208: 채점 이의 제기 UI](#t-208-채점-이의-제기-ui)
- [Week 7: 온보딩 진단 테스트](#week-7-온보딩-진단-테스트)
  - [T-209: diagnostic_tests 엔티티 + 진단 테스트 API](#t-209-diagnostic_tests-엔티티--진단-테스트-api)
  - [T-210: 진단 결과 → 초기 mastery + confidence_weight=0.7](#t-210-진단-결과--초기-mastery--confidence_weight07)
  - [T-211: 자가 진단 API (confidence_weight=0.3)](#t-211-자가-진단-api-confidence_weight03)
- [Week 8: 커뮤니티](#week-8-커뮤니티)
  - [T-212: 토론 게시판 + Q&A CRUD API](#t-212-토론-게시판--qa-crud-api)
  - [T-213: 커뮤니티 UI](#t-213-커뮤니티-ui)
  - [T-214: 단위 테스트 (Quiz, Assignment, Onboarding, Community)](#t-214-단위-테스트-quiz-assignment-onboarding-community)

---

## Week 5: 학습 진도 추적

### T-201: learning_activities 테이블 + 진도 추적 API

> **담당**: Backend | **선행**: T-109 | **관련 FR**: FR-COURSE-05

#### 서브 스텝

1. **Flyway 마이그레이션 — `learning_activities` 테이블 생성**
   - 파일: `learnflow-api/src/main/resources/db/migration/V5__create_learning_activities.sql`
   - 핵심 로직:
     - `learning_activities` 테이블: `id`, `user_id(FK)`, `lesson_id(FK)`, `enrollment_id(FK)`, `activity_type(ENUM: VIDEO_WATCH, READING, QUIZ_ATTEMPT, ASSIGNMENT_SUBMIT)`, `duration_seconds`, `progress_percent(0~100)`, `completed_at`, `created_at`, `updated_at`
     - 복합 인덱스: `(user_id, lesson_id)`, `(enrollment_id, activity_type)`
     - UNIQUE 제약: `(user_id, lesson_id, activity_type)` — 동일 유형 활동은 업데이트

2. **엔티티 구현 — `LearningActivity`**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/entity/LearningActivity.java`
   - 핵심 로직:
     - `BaseTimeEntity` 상속, `@Setter` 금지
     - `ActivityType` enum 정의 (같은 패키지 또는 내부 enum)
     - 비즈니스 메서드: `updateProgress(int progressPercent)`, `markCompleted()`
     - `@ManyToOne(fetch = LAZY)` 관계: `User`, `Lesson`, `Enrollment`

3. **Repository 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/repository/LearningActivityRepository.java`
   - 핵심 로직:
     - `findByUserIdAndLessonId(Long userId, Long lessonId)` — 특정 레슨 진도 조회
     - `findByEnrollmentId(Long enrollmentId)` — 수강 전체 학습 활동 조회
     - `countByEnrollmentIdAndCompletedAtIsNotNull(Long enrollmentId)` — 완료 레슨 수 계산

4. **DTO 정의 (record)**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/dto/LearningActivityRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/dto/LearningActivityResponse.java`
   - 핵심 로직:
     - `LearningActivityRequest`: `lessonId`, `activityType`, `durationSeconds`, `progressPercent`
     - `LearningActivityResponse`: 엔티티 필드 + `lessonTitle` 조인 정보

5. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/service/LearningActivityService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/controller/LearningActivityController.java`
   - 핵심 로직:
     - `POST /api/v1/courses/{courseId}/activities` — 학습 활동 기록 (upsert: 기존 활동이면 진도 업데이트)
     - `GET /api/v1/courses/{courseId}/activities` — 수강 내 전체 학습 활동 조회
     - `GET /api/v1/courses/{courseId}/progress` — 수강 진도율 계산 (완료 레슨 / 전체 레슨 * 100)
     - 수강 여부 검증: `Enrollment` 존재 확인 후 처리, 미수강 시 `ErrorCode.ENROLLMENT_NOT_FOUND` 예외

6. **진도율 갱신 로직**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/service/EnrollmentService.java` (기존 파일 수정)
   - 핵심 로직:
     - `updateProgress(Long enrollmentId)` 메서드 추가
     - 완료 레슨 수 / 전체 레슨 수 비율로 `enrollment.progress` 갱신
     - `LearningActivityService`에서 활동 기록 시 호출

#### 완료 기준

- [ ] `learning_activities` 테이블 Flyway 마이그레이션 정상 적용
- [ ] 학습 활동 기록 API (`POST`) 정상 동작 — upsert 방식
- [ ] 수강 진도율 조회 API (`GET /progress`) 정상 계산
- [ ] 미수강 사용자 접근 시 403 또는 적절한 에러 반환

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-202: 레슨 완료 처리 + LessonCompleted 이벤트 정의

> **담당**: Backend | **선행**: T-201 | **관련 FR**: FR-COURSE-05

#### 서브 스텝

1. **LessonCompleted 이벤트 클래스 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/LessonCompletedEvent.java`
   - 핵심 로직:
     - 필드: `userId`, `courseId`, `lessonId`, `enrollmentId`, `completedAt`
     - `BaseEvent` 또는 공통 이벤트 인터페이스 구현
     - 직렬화 가능해야 함 (Outbox에 JSON 저장)

2. **레슨 완료 판정 로직 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/service/LearningActivityService.java` (수정)
   - 핵심 로직:
     - 학습 활동 `progressPercent == 100` 도달 시 레슨 완료로 판정
     - `LearningActivity.markCompleted()` 호출 → `completedAt` 설정
     - 이미 완료된 레슨에 대한 중복 완료 처리 방지 (멱등성)

3. **Outbox 이벤트 발행 — 단일 트랜잭션**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/course/service/LearningActivityService.java` (수정)
   - 핵심 로직:
     - `@Transactional` 내에서:
       1. `LearningActivity` 완료 상태 저장
       2. `Enrollment` 진도율 갱신
       3. `OutboxPublisher.publish(lessonCompletedEvent, "lesson-completed")` 호출
     - destination_topic: `"lesson-completed"`
     - 세 작업이 하나의 트랜잭션으로 원자적 실행

4. **ErrorCode 추가**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/exception/ErrorCode.java` (수정)
   - 핵심 로직:
     - `LESSON_ALREADY_COMPLETED(409, "이미 완료된 레슨입니다")` 추가
     - `LESSON_NOT_FOUND(404, "레슨을 찾을 수 없습니다")` 추가 (없는 경우)

#### 완료 기준

- [ ] `progressPercent == 100` 도달 시 `LearningActivity.completedAt` 자동 설정
- [ ] `LessonCompletedEvent`가 `outbox_events` 테이블에 정상 INSERT
- [ ] 비즈니스 데이터 저장 + Outbox INSERT가 단일 `@Transactional`
- [ ] 이미 완료된 레슨 재완료 시도 시 멱등하게 처리 (에러 또는 무시)
- [ ] 레슨 완료 후 `enrollment.progress` 자동 갱신

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] `destination_topic` 누락 없음

---

## Week 6: 퀴즈/과제 + 채점 이의 제기

### T-203: 퀴즈 엔티티 + CRUD API

> **담당**: Backend | **선행**: T-108 | **관련 FR**: FR-QUIZ-02

#### 서브 스텝

1. **Flyway 마이그레이션 — 퀴즈 관련 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V6__create_quiz_tables.sql`
   - 핵심 로직:
     - `quizzes` 테이블: `id`, `course_id(FK)`, `lesson_id(FK, nullable)`, `title`, `description`, `time_limit_minutes`, `max_attempts`, `passing_score`, `is_published`, `created_by(FK)`, `created_at`, `updated_at`
     - `quiz_questions` 테이블: `id`, `quiz_id(FK)`, `question_type(ENUM: MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE, ESSAY)`, `content(TEXT)`, `options(JSON)`, `correct_answer(TEXT)`, `points`, `explanation(TEXT)`, `order_index`
     - `quiz_attempts` 테이블: `id`, `quiz_id(FK)`, `user_id(FK)`, `score`, `max_score`, `answers(JSON)`, `ai_feedback(TEXT, nullable)`, `started_at`, `submitted_at`, `status(ENUM: IN_PROGRESS, SUBMITTED, GRADED)`
     - 인덱스: `quiz_attempts(user_id, quiz_id)`, `quiz_questions(quiz_id)`

2. **엔티티 구현 — `Quiz`, `QuizQuestion`, `QuizAttempt`**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/entity/Quiz.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/entity/QuizQuestion.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/entity/QuizAttempt.java`
   - 핵심 로직:
     - `Quiz`: `@OneToMany(cascade = ALL)` → `QuizQuestion`, 비즈니스 메서드 `publish()`, `addQuestion()`, `removeQuestion()`
     - `QuizQuestion`: `QuestionType` enum, `@ManyToOne(fetch = LAZY)` → `Quiz`
     - `QuizAttempt`: `AttemptStatus` enum (`IN_PROGRESS`, `SUBMITTED`, `GRADED`), 비즈니스 메서드 `submit(answers)`, `applyGrade(score, feedback)`
     - `answers` 필드: `@Column(columnDefinition = "JSON")` 또는 `@Type(JsonType.class)`

3. **Repository 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/repository/QuizRepository.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/repository/QuizQuestionRepository.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/repository/QuizAttemptRepository.java`
   - 핵심 로직:
     - `QuizRepository.findByCourseIdAndIsPublishedTrue()` — 공개 퀴즈 목록
     - `QuizAttemptRepository.countByUserIdAndQuizId()` — 시도 횟수 체크 (max_attempts 제한)
     - `QuizAttemptRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc()` — 사용자 시도 이력

4. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizCreateRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizQuestionRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizAttemptRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizAttemptResponse.java`
   - 핵심 로직:
     - `QuizCreateRequest`: `title`, `description`, `timeLimitMinutes`, `maxAttempts`, `passingScore`, `List<QuizQuestionRequest> questions`
     - `QuizAttemptRequest`: `Map<Long, String> answers` (questionId → 답변)
     - 모든 DTO는 Java record

5. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/QuizService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/controller/QuizController.java`
   - 핵심 로직:
     - `POST /api/v1/quizzes` — 퀴즈 생성 (INSTRUCTOR 권한)
     - `GET /api/v1/quizzes/{quizId}` — 퀴즈 상세 조회 (문제 포함)
     - `GET /api/v1/courses/{courseId}/quizzes` — 강의별 퀴즈 목록
     - `PUT /api/v1/quizzes/{quizId}` — 퀴즈 수정 (INSTRUCTOR)
     - `DELETE /api/v1/quizzes/{quizId}` — 퀴즈 삭제 (INSTRUCTOR)
     - `POST /api/v1/quizzes/{quizId}/attempts` — 퀴즈 시도 시작
     - `PUT /api/v1/quizzes/{quizId}/attempts/{attemptId}/submit` — 답안 제출
     - 시도 횟수 검증: `max_attempts` 초과 시 `ErrorCode.QUIZ_MAX_ATTEMPTS_EXCEEDED`
     - 객관식/OX는 자동 채점 (Phase 2에서는 수동 채점 기반, AI 채점은 Phase 4)

6. **자동 채점 로직 (객관식/OX)**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/QuizGradingService.java`
   - 핵심 로직:
     - `MULTIPLE_CHOICE`, `TRUE_FALSE` 유형은 `correct_answer`와 비교하여 즉시 채점
     - `SHORT_ANSWER`, `ESSAY` 유형은 `SUBMITTED` 상태로 유지 (강사 수동 채점 대기)
     - 점수 합산: 각 문제의 `points` 기준

#### 완료 기준

- [ ] 퀴즈 CRUD API 정상 동작 (생성, 조회, 수정, 삭제)
- [ ] 퀴즈 시도 + 답안 제출 플로우 정상 동작
- [ ] 객관식/OX 자동 채점 정상 동작
- [ ] `max_attempts` 초과 시 적절한 에러 반환
- [ ] 강사(INSTRUCTOR) 권한 검증 정상 동작

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-204: 과제 제출 엔티티 + API

> **담당**: Backend | **선행**: T-108 | **관련 FR**: FR-QUIZ-08

#### 서브 스텝

1. **Flyway 마이그레이션 — 과제 관련 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V7__create_assignment_tables.sql`
   - 핵심 로직:
     - `assignments` 테이블: `id`, `course_id(FK)`, `lesson_id(FK, nullable)`, `title`, `description(TEXT)`, `rubric(JSON)`, `max_score`, `due_date`, `is_published`, `created_by(FK)`, `created_at`, `updated_at`
     - `assignment_submissions` 테이블: `id`, `assignment_id(FK)`, `user_id(FK)`, `content(TEXT)`, `file_url(VARCHAR)`, `score(nullable)`, `ai_score(nullable)`, `ai_confidence(nullable)`, `ai_feedback(TEXT, nullable)`, `instructor_feedback(TEXT, nullable)`, `status(ENUM: SUBMITTED, AI_GRADED, CONFIRMED, APPEALED, MANUAL_REVIEW)`, `submitted_at`, `graded_at`, `created_at`, `updated_at`
     - 인덱스: `assignment_submissions(user_id, assignment_id)`, `assignment_submissions(status)`
     - `ai_confidence` 컬럼은 Phase 4의 AI 채점을 위한 선행 스키마

2. **엔티티 구현 — `Assignment`, `AssignmentSubmission`**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/entity/Assignment.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/entity/AssignmentSubmission.java`
   - 핵심 로직:
     - `Assignment`: 비즈니스 메서드 `publish()`, `isOverdue()`
     - `AssignmentSubmission`: `SubmissionStatus` enum (`SUBMITTED`, `AI_GRADED`, `CONFIRMED`, `APPEALED`, `MANUAL_REVIEW`)
     - 비즈니스 메서드: `submit(content, fileUrl)`, `applyAiGrade(score, confidence, feedback)`, `confirm(instructorFeedback)`, `appeal(reason)`, `moveToManualReview()`
     - `applyAiGrade()`는 Phase 4에서 사용되지만 엔티티 메서드는 미리 정의

3. **Repository 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/repository/AssignmentRepository.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/repository/AssignmentSubmissionRepository.java`
   - 핵심 로직:
     - `AssignmentRepository.findByCourseIdAndIsPublishedTrue()` — 공개 과제 목록
     - `AssignmentSubmissionRepository.findByAssignmentIdAndUserId()` — 사용자 제출 조회
     - `AssignmentSubmissionRepository.findByStatus(SubmissionStatus status)` — 상태별 필터

4. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/dto/AssignmentCreateRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/dto/AssignmentResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/dto/SubmissionRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/dto/SubmissionResponse.java`
   - 핵심 로직:
     - `SubmissionResponse`에 `aiConfidence`, `status` 포함 (Phase 4 대비)
     - 모든 DTO는 Java record

5. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/service/AssignmentService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/assignment/controller/AssignmentController.java`
   - 핵심 로직:
     - `POST /api/v1/assignments` — 과제 생성 (INSTRUCTOR)
     - `GET /api/v1/courses/{courseId}/assignments` — 강의별 과제 목록
     - `GET /api/v1/assignments/{assignmentId}` — 과제 상세
     - `POST /api/v1/assignments/{assignmentId}/submissions` — 과제 제출 (LEARNER)
     - `GET /api/v1/assignments/{assignmentId}/submissions` — 제출 목록 (INSTRUCTOR)
     - `GET /api/v1/assignments/{assignmentId}/submissions/my` — 내 제출 조회 (LEARNER)
     - `PUT /api/v1/assignments/{assignmentId}/submissions/{submissionId}/grade` — 강사 수동 채점 (INSTRUCTOR)
     - 마감일 검증: `assignment.isOverdue()` 시 `ErrorCode.ASSIGNMENT_OVERDUE`
     - 중복 제출 방지 또는 재제출 허용 정책 결정

#### 완료 기준

- [ ] 과제 CRUD API 정상 동작
- [ ] 과제 제출 + 강사 수동 채점 플로우 정상 동작
- [ ] `assignment_submissions.status` 상태 전이 정상 동작 (`SUBMITTED` → `CONFIRMED`)
- [ ] `ai_confidence`, `ai_score` 컬럼이 스키마에 존재 (Phase 4 선행)
- [ ] 마감일 초과 제출 시 적절한 에러 반환

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-205: 채점 이의 제기(Appeal) API + GradingAppeal 이벤트

> **담당**: Backend | **선행**: T-203 | **관련 FR**: FR-QUIZ-05

#### 서브 스텝

1. **Flyway 마이그레이션 — appeal 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V8__create_grading_appeals.sql`
   - 핵심 로직:
     - `grading_appeals` 테이블: `id`, `user_id(FK)`, `appeal_type(ENUM: QUIZ, ASSIGNMENT)`, `quiz_attempt_id(FK, nullable)`, `submission_id(FK, nullable)`, `reason(TEXT)`, `status(ENUM: PENDING, ACCEPTED, REJECTED)`, `instructor_comment(TEXT, nullable)`, `resolved_by(FK, nullable)`, `resolved_at`, `created_at`, `updated_at`
     - 인덱스: `(user_id, status)`, `(appeal_type, status)`

2. **엔티티 구현 — `GradingAppeal`**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/entity/GradingAppeal.java`
   - 핵심 로직:
     - `AppealType` enum (`QUIZ`, `ASSIGNMENT`)
     - `AppealStatus` enum (`PENDING`, `ACCEPTED`, `REJECTED`)
     - 비즈니스 메서드: `accept(instructorId, comment)`, `reject(instructorId, comment)`
     - 다형적 참조: `quizAttemptId` 또는 `submissionId` 중 하나만 값을 가짐

3. **GradingAppeal 이벤트 클래스 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/global/event/events/GradingAppealEvent.java`
   - 핵심 로직:
     - 필드: `userId`, `appealId`, `appealType`, `targetId(quizAttemptId 또는 submissionId)`, `reason`, `courseId`
     - 직렬화 가능

4. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/AppealService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/controller/AppealController.java`
   - 핵심 로직:
     - `POST /api/v1/appeals` — 이의 제기 생성 (LEARNER)
       - `@Transactional` 내에서:
         1. `GradingAppeal` 엔티티 저장
         2. 대상 `QuizAttempt` 또는 `AssignmentSubmission` 상태를 `APPEALED`로 변경
         3. `OutboxPublisher.publish(gradingAppealEvent, "grading-appeal")` 호출
     - `GET /api/v1/appeals/my` — 내 이의 제기 목록 (LEARNER)
     - `PUT /api/v1/appeals/{appealId}/resolve` — 이의 제기 처리 (INSTRUCTOR)
       - `ACCEPTED` 시: 점수 재조정 가능, 상태 `CONFIRMED`로 변경
       - `REJECTED` 시: 기존 점수 유지, 상태 `CONFIRMED`로 변경

5. **상태 전이 검증**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/AppealService.java`
   - 핵심 로직:
     - 이미 `APPEALED` 상태인 항목에 중복 Appeal 방지
     - `GRADED` 또는 `AI_GRADED` 상태에서만 Appeal 가능
     - `CONFIRMED` 상태에서는 Appeal 불가

#### 완료 기준

- [ ] 이의 제기 생성 API — 퀴즈/과제 모두 지원
- [ ] `GradingAppealEvent`가 `outbox_events`에 정상 INSERT
- [ ] 대상 엔티티 상태가 `APPEALED`로 정상 전이
- [ ] 강사 처리(수락/거부) 후 상태 `CONFIRMED`로 최종 전이
- [ ] 상태 전이 규칙 위반 시 적절한 에러 반환

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] `destination_topic` 누락 없음
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-206: Manual Review Queue API (강사용)

> **담당**: Backend | **선행**: T-205 | **관련 FR**: FR-QUIZ-06

#### 서브 스텝

1. **Review Queue 조회 API**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/controller/ReviewQueueController.java`
   - 핵심 로직:
     - `GET /api/v1/instructor/review-queue` — 검토 대기 항목 목록 (INSTRUCTOR)
     - 필터: `type` (QUIZ/ASSIGNMENT/ALL), `status` (APPEALED/MANUAL_REVIEW/ALL), `courseId`
     - 페이징: `PageResponse<ReviewQueueItemResponse>` 반환
     - 정렬: 생성일 기준 오래된 순 (FIFO)

2. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/ReviewQueueItemResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/ManualGradeRequest.java`
   - 핵심 로직:
     - `ReviewQueueItemResponse`: `id`, `type(QUIZ/ASSIGNMENT)`, `studentName`, `courseName`, `title`, `submittedAt`, `status`, `aiScore(nullable)`, `aiConfidence(nullable)`, `appealReason(nullable)`
     - `ManualGradeRequest`: `score`, `feedback`

3. **Service 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/ReviewQueueService.java`
   - 핵심 로직:
     - `getReviewQueue(instructorId, filter, pageable)` — 강사가 담당하는 강의의 검토 대기 항목 조회
     - 퀴즈 시도(`SUBMITTED` 에세이) + 과제 제출(`APPEALED`, `MANUAL_REVIEW`) 통합 조회
     - 강사 권한 검증: 해당 강의의 강사인지 확인

4. **수동 채점 API**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/controller/ReviewQueueController.java`
   - 핵심 로직:
     - `PUT /api/v1/instructor/review-queue/{type}/{id}/grade` — 수동 채점 처리 (INSTRUCTOR)
     - `type`: `quiz-attempts` 또는 `submissions`
     - 채점 후 상태를 `CONFIRMED`로 변경
     - Appeal이 존재하는 경우 Appeal 상태도 함께 갱신 (`ACCEPTED` 또는 `REJECTED`)

#### 완료 기준

- [ ] 검토 대기 목록 조회 — 퀴즈/과제 통합 페이징
- [ ] 강사 권한 검증 — 담당 강의만 조회 가능
- [ ] 수동 채점 후 상태 `CONFIRMED` 전이
- [ ] 필터(type, status, courseId) 정상 동작

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-207: 퀴즈 결과 + AI 피드백 조회 API

> **담당**: Backend | **선행**: T-203 | **관련 FR**: FR-QUIZ-07

#### 서브 스텝

1. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizResultResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuestionResultResponse.java`
   - 핵심 로직:
     - `QuizResultResponse`: `attemptId`, `quizTitle`, `score`, `maxScore`, `passingScore`, `isPassed`, `aiFeedback(nullable)`, `submittedAt`, `List<QuestionResultResponse> questionResults`
     - `QuestionResultResponse`: `questionId`, `content`, `userAnswer`, `correctAnswer`, `isCorrect`, `points`, `earnedPoints`, `explanation`
     - 에세이/주관식 문제: `correctAnswer`를 노출하지 않음 (강사 설정에 따라)

2. **Service 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/QuizResultService.java`
   - 핵심 로직:
     - `getQuizResult(userId, attemptId)` — 본인 시도 결과만 조회 가능
     - `answers` JSON 파싱 → 문제별 정오 판정 매핑
     - `aiFeedback` 필드는 Phase 4에서 AI 채점 시 채워짐 (현재는 null)
     - 합격 여부: `score >= quiz.passingScore`

3. **Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/controller/QuizResultController.java`
   - 핵심 로직:
     - `GET /api/v1/quizzes/{quizId}/attempts/{attemptId}/result` — 시도 결과 상세 (LEARNER)
     - `GET /api/v1/quizzes/{quizId}/attempts/my` — 내 시도 이력 목록 (LEARNER)
     - `GET /api/v1/quizzes/{quizId}/attempts` — 전체 시도 목록 (INSTRUCTOR, 통계용)

4. **통계 집계**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/dto/QuizStatisticsResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/quiz/service/QuizResultService.java` (추가)
   - 핵심 로직:
     - `GET /api/v1/quizzes/{quizId}/statistics` — 퀴즈 통계 (INSTRUCTOR)
     - `QuizStatisticsResponse`: `totalAttempts`, `averageScore`, `passRate`, `questionAccuracyRates(Map<Long, Double>)`
     - JPQL 집계 쿼리 또는 `@Query` 활용

#### 완료 기준

- [ ] 퀴즈 결과 상세 조회 — 문제별 정오 판정 포함
- [ ] 본인 시도 결과만 조회 가능 (타인 시도 접근 차단)
- [ ] 강사 전체 시도 목록 + 통계 조회 정상 동작
- [ ] `aiFeedback` 필드가 null 허용 (Phase 4 대비)

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-208: 채점 이의 제기 UI

> **담당**: Frontend | **선행**: T-119 | **관련 FR**: FR-QUIZ-05

#### 서브 스텝

1. **API 클라이언트 구현**
   - 파일: `learnflow-web/src/lib/api/appeal.ts`
   - 핵심 로직:
     - `createAppeal(data: AppealCreateRequest): Promise<ApiResponse<AppealResponse>>` — 이의 제기 생성
     - `getMyAppeals(params?: PaginationParams): Promise<ApiResponse<PageResponse<AppealResponse>>>` — 내 이의 제기 목록
     - Axios 인스턴스 사용 (토큰 인터셉터 적용)

2. **TanStack Query 훅 구현**
   - 파일: `learnflow-web/src/hooks/useAppeals.ts`
   - 핵심 로직:
     - `useCreateAppeal()` — `useMutation`, 성공 시 관련 쿼리 invalidate
     - `useMyAppeals(params)` — `useQuery`, 페이징 지원
     - `useQuizResult(quizId, attemptId)` — `useQuery`, 결과 조회

3. **채점 결과 상세 페이지 — Appeal 버튼 포함**
   - 파일: `learnflow-web/src/pages/quiz/QuizResultPage.tsx`
   - 핵심 로직:
     - 퀴즈 시도 결과 표시: 점수, 합격 여부, 문제별 정오답
     - `GRADED` 또는 `AI_GRADED` 상태일 때 "이의 제기" 버튼 노출
     - 이미 `APPEALED` 상태이면 버튼 비활성화 + "이의 제기 진행 중" 표시

4. **이의 제기 모달/폼 컴포넌트**
   - 파일: `learnflow-web/src/components/appeal/AppealForm.tsx`
   - 핵심 로직:
     - React Hook Form + Zod 검증: `reason` 필수 (최소 10자, 최대 1000자)
     - shadcn/ui `Dialog` + `Textarea` + `Button` 사용
     - 제출 성공 시 토스트 알림 + 상태 갱신

5. **내 이의 제기 목록 페이지**
   - 파일: `learnflow-web/src/pages/appeal/MyAppealsPage.tsx`
   - 핵심 로직:
     - 이의 제기 목록: 상태(`PENDING`, `ACCEPTED`, `REJECTED`) 배지, 제출일, 사유 요약
     - 상태별 필터 탭
     - shadcn/ui `Table` + `Badge` + `Tabs` 사용

#### 완료 기준

- [ ] 퀴즈 결과 페이지에서 "이의 제기" 버튼 정상 동작
- [ ] 이의 제기 폼 Zod 검증 정상 동작 (reason 최소 10자)
- [ ] 이의 제기 제출 후 상태 즉시 반영 (낙관적 업데이트 또는 쿼리 리페치)
- [ ] 내 이의 제기 목록 페이지 상태별 필터 정상 동작

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] 폼: React Hook Form + Zod 스키마 검증
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스

---

## Week 7: 온보딩 진단 테스트

### T-209: diagnostic_tests 엔티티 + 진단 테스트 API (Bloom's 배분 5문항)

> **담당**: Backend | **선행**: T-108 | **관련 FR**: FR-ONBOARD-01

#### 서브 스텝

1. **Flyway 마이그레이션 — 진단 테스트 + concept_mastery 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V9__create_diagnostic_tables.sql`
   - 핵심 로직:
     - `diagnostic_tests` 테이블: `id`, `course_id(FK)`, `title`, `description`, `is_active`, `created_by(FK)`, `created_at`, `updated_at`
     - `diagnostic_questions` 테이블: `id`, `diagnostic_test_id(FK)`, `concept_tag(VARCHAR)`, `bloom_level(ENUM: REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE)`, `content(TEXT)`, `question_type(ENUM)`, `options(JSON)`, `correct_answer(TEXT)`, `points`, `order_index`
     - `diagnostic_results` 테이블: `id`, `user_id(FK)`, `diagnostic_test_id(FK)`, `course_id(FK)`, `diagnosed_level(VARCHAR)`, `concept_scores(JSON)`, `confidence_weight(DECIMAL, default 0.7)`, `total_score`, `max_score`, `completed_at`, `created_at`
     - `concept_mastery` 테이블: `id`, `user_id(FK)`, `course_id(FK)`, `concept_tag(VARCHAR)`, `mastery_score(DECIMAL 0~1)`, `confidence(DECIMAL 0~1)`, `source(ENUM: DIAGNOSTIC, QUIZ, MANUAL)`, `created_at`, `updated_at`
     - UNIQUE: `concept_mastery(user_id, course_id, concept_tag)`

2. **엔티티 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/entity/DiagnosticTest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/entity/DiagnosticQuestion.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/entity/DiagnosticResult.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/entity/ConceptMastery.java`
   - 핵심 로직:
     - `DiagnosticQuestion`: `BloomLevel` enum (6단계), `concept_tag`로 개념 연결
     - `DiagnosticResult`: `conceptScores` JSON 필드 — `{"java_basics": 0.8, "oop": 0.5, ...}`
     - `ConceptMastery`: `MasterySource` enum (`DIAGNOSTIC`, `QUIZ`, `MANUAL`), 비즈니스 메서드 `updateMastery(score, confidence, source)`

3. **Bloom's Taxonomy 5문항 배분 전략**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/service/DiagnosticTestService.java`
   - 핵심 로직:
     - 5문항 배분 기본 전략:
       - 1문항: REMEMBER (기초 기억)
       - 1문항: UNDERSTAND (이해)
       - 1문항: APPLY (적용)
       - 1문항: ANALYZE (분석)
       - 1문항: EVALUATE 또는 CREATE (평가/창작)
     - 강의별 `diagnostic_questions` 풀에서 Bloom 레벨별로 랜덤 추출
     - 문항 수가 부족한 레벨은 인접 레벨에서 보충

4. **Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/controller/DiagnosticController.java`
   - 핵심 로직:
     - `POST /api/v1/onboarding/diagnostic/{courseId}/start` — 진단 테스트 시작 (5문항 반환)
     - `POST /api/v1/onboarding/diagnostic/{courseId}/submit` — 진단 답안 제출
     - `GET /api/v1/onboarding/diagnostic/{courseId}/result` — 진단 결과 조회
     - 이미 진단 완료한 강의에 대한 재진단 정책 (재진단 허용 또는 기존 결과 반환)

5. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/dto/DiagnosticStartResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/dto/DiagnosticSubmitRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/dto/DiagnosticResultResponse.java`
   - 핵심 로직:
     - `DiagnosticStartResponse`: `testId`, `List<DiagnosticQuestionResponse> questions` (정답/해설 제외)
     - `DiagnosticSubmitRequest`: `testId`, `Map<Long, String> answers`
     - `DiagnosticResultResponse`: `diagnosedLevel`, `totalScore`, `maxScore`, `Map<String, Double> conceptScores`, `bloomLevelScores`

#### 완료 기준

- [ ] 진단 테스트 시작 시 Bloom's 6레벨에서 5문항 정상 배분
- [ ] 답안 제출 후 자동 채점 + `diagnostic_results` 저장
- [ ] `concept_scores` JSON에 개념별 점수 정상 기록
- [ ] `concept_mastery` 테이블 스키마 준비 완료 (T-210에서 데이터 기록)

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-210: 진단 결과 → 초기 mastery + confidence_weight=0.7

> **담당**: Backend | **선행**: T-209 | **관련 FR**: FR-ONBOARD-02

#### 서브 스텝

1. **진단 채점 후 concept_mastery 초기화 로직**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/service/DiagnosticTestService.java` (수정)
   - 핵심 로직:
     - 진단 답안 제출 처리 후, `concept_scores` 기반으로 `concept_mastery` 레코드 생성
     - 각 `concept_tag`별로:
       - `mastery_score` = 해당 개념 문제의 정답률 (0.0 ~ 1.0)
       - `confidence` = `confidence_weight` = **0.7** (진단 테스트 기반)
       - `source` = `DIAGNOSTIC`
     - 이미 mastery 레코드가 존재하면 업데이트 (재진단 시)

2. **diagnosed_level 결정 로직**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/service/DiagnosticLevelCalculator.java`
   - 핵심 로직:
     - `concept_mastery` 평균 기반 레벨 결정:
       - 평균 < 0.4 → Level 1 (초급)
       - 0.4 <= 평균 < 0.7 → Level 2 (중급)
       - 평균 >= 0.7 → Level 3 (고급)
     - 이 레벨은 AI 튜터의 설명 난이도 (Phase 4)에서 활용

3. **ConceptMastery Repository**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/repository/ConceptMasteryRepository.java`
   - 핵심 로직:
     - `findByUserIdAndCourseId(Long userId, Long courseId)` — 강의별 전체 mastery 조회
     - `findByUserIdAndCourseIdAndConceptTag(Long userId, Long courseId, String conceptTag)` — 특정 개념 mastery
     - `findAverageMasteryScoreByUserIdAndCourseId(Long userId, Long courseId)` — 평균 mastery (@Query)

4. **트랜잭션 처리**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/service/DiagnosticTestService.java` (수정)
   - 핵심 로직:
     - 단일 `@Transactional` 내에서:
       1. `DiagnosticResult` 저장 (diagnosed_level, concept_scores, confidence_weight=0.7)
       2. 각 개념별 `ConceptMastery` upsert
     - 원자적 실행 보장

#### 완료 기준

- [ ] 진단 완료 후 `concept_mastery` 레코드가 개념별로 생성
- [ ] `confidence_weight` = 0.7로 정확히 설정
- [ ] `source` = `DIAGNOSTIC`로 정확히 설정
- [ ] `diagnosed_level`이 mastery 평균 기반으로 정확히 계산 (Level 1/2/3)
- [ ] 재진단 시 기존 mastery 레코드 정상 업데이트

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리

---

### T-211: 자가 진단 API (confidence_weight=0.3)

> **담당**: Backend | **선행**: T-209 | **관련 FR**: FR-ONBOARD-03

#### 서브 스텝

1. **Flyway 마이그레이션 — 자가 진단 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V10__create_self_assessments.sql`
   - 핵심 로직:
     - `self_assessments` 테이블: `id`, `user_id(FK)`, `course_id(FK)`, `concept_tag(VARCHAR)`, `self_rated_level(ENUM: BEGINNER, INTERMEDIATE, ADVANCED)`, `confidence_weight(DECIMAL, default 0.3)`, `created_at`, `updated_at`
     - UNIQUE: `(user_id, course_id, concept_tag)` — 동일 개념 재평가 시 업데이트

2. **엔티티 구현 — `SelfAssessment`**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/entity/SelfAssessment.java`
   - 핵심 로직:
     - `SelfRatedLevel` enum (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`)
     - 레벨 → mastery_score 변환: `BEGINNER` = 0.2, `INTERMEDIATE` = 0.5, `ADVANCED` = 0.8
     - 비즈니스 메서드: `updateRating(SelfRatedLevel level)`

3. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/dto/SelfAssessmentRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/dto/SelfAssessmentResponse.java`
   - 핵심 로직:
     - `SelfAssessmentRequest`: `List<ConceptRating> ratings` where `ConceptRating(conceptTag, selfRatedLevel)`
     - 한 번에 여러 개념의 자가 진단을 제출 가능

4. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/service/SelfAssessmentService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/onboarding/controller/SelfAssessmentController.java`
   - 핵심 로직:
     - `POST /api/v1/onboarding/self-assessment/{courseId}` — 자가 진단 제출 (LEARNER)
       - 각 `conceptTag`별로 `SelfAssessment` upsert
       - `concept_mastery` 업데이트/생성: `source` = `MANUAL`, `confidence` = **0.3**
       - 기존 DIAGNOSTIC mastery가 있는 경우: 가중 평균 적용
         - `new_mastery = (diagnostic_mastery * 0.7 + self_mastery * 0.3)`
     - `GET /api/v1/onboarding/self-assessment/{courseId}` — 자가 진단 결과 조회
     - `GET /api/v1/onboarding/{courseId}/concepts` — 강의의 개념 태그 목록 (자가 진단 폼에 표시)

5. **Mastery 가중 평균 로직**
   - 파일: `learnflow-api/src/main/java/com/learnflow/analytics/service/ConceptMasteryService.java`
   - 핵심 로직:
     - `updateMasteryWithWeight(userId, courseId, conceptTag, newScore, confidenceWeight, source)` 메서드
     - 기존 mastery 존재 시: 가중 평균 `(existing * existingWeight + new * newWeight) / (existingWeight + newWeight)`
     - 기존 mastery 미존재 시: 새 레코드 생성
     - 이 로직은 Phase 4에서 QUIZ 소스 mastery 업데이트에도 재사용

#### 완료 기준

- [ ] 자가 진단 제출 API 정상 동작 (여러 개념 동시 제출)
- [ ] `confidence_weight` = 0.3으로 정확히 설정
- [ ] 기존 DIAGNOSTIC mastery와 가중 평균 정상 계산
- [ ] `concept_mastery` 레코드 정상 upsert
- [ ] 강의 개념 태그 목록 조회 정상 동작

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

## Week 8: 커뮤니티

### T-212: 토론 게시판 + Q&A CRUD API

> **담당**: Backend | **선행**: T-108 | **관련 FR**: FR-COMMUNITY-01~02

#### 서브 스텝

1. **Flyway 마이그레이션 — 커뮤니티 테이블**
   - 파일: `learnflow-api/src/main/resources/db/migration/V11__create_community_tables.sql`
   - 핵심 로직:
     - `posts` 테이블: `id`, `course_id(FK)`, `user_id(FK)`, `post_type(ENUM: DISCUSSION, QUESTION)`, `title(VARCHAR 200)`, `content(TEXT)`, `is_pinned(BOOLEAN, default false)`, `is_resolved(BOOLEAN, default false)`, `view_count(INT, default 0)`, `like_count(INT, default 0)`, `comment_count(INT, default 0)`, `created_at`, `updated_at`
     - `comments` 테이블: `id`, `post_id(FK)`, `user_id(FK)`, `parent_id(FK, nullable, self-ref)`, `content(TEXT)`, `is_accepted(BOOLEAN, default false)`, `like_count(INT, default 0)`, `created_at`, `updated_at`
     - `post_likes` 테이블: `id`, `post_id(FK)`, `user_id(FK)`, UNIQUE `(post_id, user_id)`
     - `comment_likes` 테이블: `id`, `comment_id(FK)`, `user_id(FK)`, UNIQUE `(comment_id, user_id)`
     - 인덱스: `posts(course_id, post_type)`, `posts(course_id, created_at)`, `comments(post_id)`

2. **엔티티 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/entity/Post.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/entity/Comment.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/entity/PostLike.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/entity/CommentLike.java`
   - 핵심 로직:
     - `Post`: `PostType` enum (`DISCUSSION`, `QUESTION`), 비즈니스 메서드 `pin()`, `unpin()`, `markResolved()`, `incrementViewCount()`, `updateContent(title, content)`
     - `Comment`: 대댓글 구조 (`parent_id` self-reference), `@ManyToOne(fetch = LAZY)` → `Post`, 비즈니스 메서드 `acceptAsAnswer()` (Q&A용), `updateContent(content)`
     - Like 엔티티: `@Table(uniqueConstraints = @UniqueConstraint({"post_id", "user_id"}))` — 중복 좋아요 방지

3. **Repository 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/repository/PostRepository.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/repository/CommentRepository.java`
   - 핵심 로직:
     - `PostRepository.findByCourseIdAndPostType(courseId, postType, pageable)` — 유형별 게시글 목록
     - `PostRepository.findByCourseIdOrderByIsPinnedDescCreatedAtDesc(courseId, pageable)` — 고정글 우선 정렬
     - `CommentRepository.findByPostIdOrderByCreatedAtAsc(postId)` — 댓글 목록 (시간순)
     - `CommentRepository.findByPostIdAndParentIdIsNull(postId)` — 최상위 댓글만

4. **DTO 정의**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/dto/PostCreateRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/dto/PostResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/dto/PostListResponse.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/dto/CommentCreateRequest.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/dto/CommentResponse.java`
   - 핵심 로직:
     - `PostCreateRequest`: `postType`, `title`, `content`
     - `PostResponse`: 전체 필드 + `authorName` + `List<CommentResponse> comments`
     - `PostListResponse`: 목록용 요약 (content 제외 또는 요약)
     - `CommentResponse`: 대댓글 재귀 구조 `List<CommentResponse> replies`

5. **Service + Controller 구현**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/service/PostService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/service/CommentService.java`
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/controller/CommunityController.java`
   - 핵심 로직:
     - **게시글**:
       - `POST /api/v1/courses/{courseId}/posts` — 게시글 작성 (AUTHENTICATED, 수강생/강사)
       - `GET /api/v1/courses/{courseId}/posts` — 게시글 목록 (페이징, 유형 필터)
       - `GET /api/v1/courses/{courseId}/posts/{postId}` — 게시글 상세 (조회수 증가)
       - `PUT /api/v1/courses/{courseId}/posts/{postId}` — 게시글 수정 (작성자만)
       - `DELETE /api/v1/courses/{courseId}/posts/{postId}` — 게시글 삭제 (작성자/INSTRUCTOR)
     - **댓글**:
       - `POST /api/v1/posts/{postId}/comments` — 댓글 작성 (`parentId` optional for 대댓글)
       - `PUT /api/v1/comments/{commentId}` — 댓글 수정 (작성자만)
       - `DELETE /api/v1/comments/{commentId}` — 댓글 삭제 (작성자/INSTRUCTOR)
     - **좋아요**:
       - `POST /api/v1/posts/{postId}/like` — 게시글 좋아요 토글
       - `POST /api/v1/comments/{commentId}/like` — 댓글 좋아요 토글
     - **Q&A 전용**:
       - `PUT /api/v1/posts/{postId}/resolve` — 해결 완료 표시 (작성자/INSTRUCTOR)
       - `PUT /api/v1/comments/{commentId}/accept` — 답변 채택 (질문 작성자)

6. **수강 여부 검증**
   - 파일: `learnflow-api/src/main/java/com/learnflow/domain/community/service/PostService.java`
   - 핵심 로직:
     - 게시글 작성/댓글 작성 시 해당 강의 수강 여부 확인
     - 강사(`INSTRUCTOR`)는 수강 없이도 접근 가능
     - 미수강 시 `ErrorCode.ENROLLMENT_REQUIRED`

#### 완료 기준

- [ ] 게시글 CRUD (토론/Q&A 유형 구분) 정상 동작
- [ ] 댓글 + 대댓글(1depth) 작성/수정/삭제 정상 동작
- [ ] 좋아요 토글 (중복 방지) 정상 동작
- [ ] Q&A: 답변 채택 + 해결 완료 표시 정상 동작
- [ ] 수강 여부 검증 정상 동작

#### 규칙 체크리스트

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지

---

### T-213: 커뮤니티 UI

> **담당**: Frontend | **선행**: T-119 | **관련 FR**: FR-COMMUNITY-01~02

#### 서브 스텝

1. **API 클라이언트 구현**
   - 파일: `learnflow-web/src/lib/api/community.ts`
   - 핵심 로직:
     - 게시글 CRUD: `createPost()`, `getPosts()`, `getPost()`, `updatePost()`, `deletePost()`
     - 댓글: `createComment()`, `updateComment()`, `deleteComment()`
     - 좋아요: `togglePostLike()`, `toggleCommentLike()`
     - Q&A: `resolvePost()`, `acceptComment()`

2. **TanStack Query 훅 구현**
   - 파일: `learnflow-web/src/hooks/useCommunity.ts`
   - 핵심 로직:
     - `usePosts(courseId, params)` — `useQuery`, 유형 필터 + 페이징
     - `usePost(postId)` — `useQuery`, 상세 + 댓글 포함
     - `useCreatePost()` — `useMutation`, 성공 시 목록 쿼리 invalidate
     - `useCreateComment()` — `useMutation`, 성공 시 게시글 상세 쿼리 invalidate
     - `useToggleLike()` — `useMutation`, 낙관적 업데이트

3. **게시글 목록 페이지**
   - 파일: `learnflow-web/src/pages/community/CommunityListPage.tsx`
   - 핵심 로직:
     - 탭: "토론" / "Q&A" 전환 (shadcn/ui `Tabs`)
     - 게시글 카드: 제목, 작성자, 작성일, 좋아요 수, 댓글 수, 조회수
     - Q&A 탭: 해결/미해결 배지 (`Badge`)
     - 고정글 상단 노출
     - 페이지네이션 (shadcn/ui `Pagination`)
     - 글쓰기 버튼 → 작성 페이지 이동

4. **게시글 작성/수정 페이지**
   - 파일: `learnflow-web/src/pages/community/PostFormPage.tsx`
   - 핵심 로직:
     - React Hook Form + Zod 검증: `title` (필수, 2~200자), `content` (필수, 10자 이상), `postType` (필수)
     - `content` 영역: shadcn/ui `Textarea` 또는 간단한 Markdown 입력
     - 수정 모드: 기존 데이터 프리필

5. **게시글 상세 + 댓글 페이지**
   - 파일: `learnflow-web/src/pages/community/PostDetailPage.tsx`
   - 파일: `learnflow-web/src/components/community/CommentList.tsx`
   - 파일: `learnflow-web/src/components/community/CommentForm.tsx`
   - 핵심 로직:
     - 게시글 본문 + 메타 정보 (작성자, 작성일, 조회수, 좋아요)
     - 좋아요 버튼 (하트 아이콘 토글)
     - 댓글 목록: 대댓글 들여쓰기 표현, "답글" 버튼
     - 댓글 작성 폼: React Hook Form + Zod
     - Q&A 게시글: "답변 채택" 버튼 (질문 작성자에게만 노출), "해결 완료" 표시
     - 작성자/강사에게만 수정/삭제 버튼 노출

#### 완료 기준

- [ ] 토론/Q&A 탭 전환 + 게시글 목록 페이징 정상 동작
- [ ] 게시글 작성/수정/삭제 정상 동작
- [ ] 댓글 + 대댓글 작성/수정/삭제 정상 동작
- [ ] 좋아요 토글 UI 즉시 반영 (낙관적 업데이트)
- [ ] Q&A 답변 채택 + 해결 완료 표시 정상 동작

#### 규칙 체크리스트

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] 폼: React Hook Form + Zod 스키마 검증
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스

---

### T-214: 단위 테스트 (Quiz, Assignment, Onboarding, Community)

> **담당**: Backend | **선행**: T-212 | **관련 FR**: NFR-MAINT-03

#### 서브 스텝

1. **Quiz 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/quiz/service/QuizServiceTest.java`
   - 핵심 로직:
     - 퀴즈 생성/조회/수정/삭제 테스트
     - 시도 횟수 초과 시 예외 발생 검증
     - 객관식/OX 자동 채점 정확성 검증
     - 퀴즈 미공개 상태에서 시도 차단 검증

2. **Appeal 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/quiz/service/AppealServiceTest.java`
   - 핵심 로직:
     - Appeal 생성 시 대상 상태 `APPEALED` 전이 검증
     - 중복 Appeal 방지 검증
     - 강사 Accept/Reject 후 상태 `CONFIRMED` 전이 검증
     - `OutboxPublisher.publish()` 호출 검증 (Mockito verify)

3. **Assignment 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/assignment/service/AssignmentServiceTest.java`
   - 핵심 로직:
     - 과제 생성/제출/채점 플로우 검증
     - 마감일 초과 제출 시 예외 발생 검증
     - `SubmissionStatus` 상태 전이 검증

4. **Onboarding 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/onboarding/service/DiagnosticTestServiceTest.java`
   - 파일: `learnflow-api/src/test/java/com/learnflow/onboarding/service/SelfAssessmentServiceTest.java`
   - 핵심 로직:
     - Bloom's 5문항 배분 정확성 검증 (각 레벨 최소 1문항)
     - 진단 채점 → `concept_mastery` 생성 검증 (`confidence` = 0.7)
     - 자가 진단 → mastery 가중 평균 계산 검증 (`confidence` = 0.3)
     - `diagnosed_level` 계산 정확성 검증 (Level 1/2/3 경계값)

5. **Community 서비스 단위 테스트**
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/community/service/PostServiceTest.java`
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/community/service/CommentServiceTest.java`
   - 핵심 로직:
     - 게시글 CRUD 검증
     - 수강 여부 미충족 시 예외 발생 검증
     - 좋아요 토글 (추가 → 취소 → 재추가) 검증
     - 작성자 외 수정/삭제 시도 시 예외 발생 검증
     - Q&A 답변 채택 로직 검증

6. **Controller 테스트 (선택)**
   - 파일: `learnflow-api/src/test/java/com/learnflow/domain/quiz/controller/QuizControllerTest.java`
   - 핵심 로직:
     - `@WebMvcTest` + MockMvc 기반
     - 권한(INSTRUCTOR/LEARNER) 별 접근 제어 검증
     - 요청 유효성 검증 (잘못된 입력 → 400)
     - `ApiResponse<T>` 래핑 응답 형식 검증

#### 완료 기준

- [ ] Quiz 서비스 테스트 통과 (채점, 시도 제한, 상태 전이)
- [ ] Appeal 서비스 테스트 통과 (Outbox 발행 검증 포함)
- [ ] Assignment 서비스 테스트 통과 (마감일, 상태 전이)
- [ ] Onboarding 서비스 테스트 통과 (Bloom's 배분, mastery 계산)
- [ ] Community 서비스 테스트 통과 (CRUD, 좋아요, 권한)

#### 규칙 체크리스트

- [ ] 서비스 레이어 단위 테스트 최소 포함
- [ ] Mockito로 의존성 모킹 (Repository, OutboxPublisher 등)
- [ ] 경계값 테스트 포함 (mastery 0.4/0.7 경계, max_attempts 경계)
- [ ] 예외 케이스 테스트 포함 (권한 없음, 리소스 미존재, 상태 전이 불가)

---

## Phase 2 완료 기준 (M2 마일스톤)

| 항목 | 검증 방법 |
|------|-----------|
| 학습 활동 기록 + 진도율 계산 | API 테스트: 레슨 완료 → 진도율 갱신 |
| 레슨 완료 이벤트 Outbox 발행 | DB 확인: `outbox_events` 테이블에 `LessonCompleted` 레코드 |
| 퀴즈 시도 + 자동 채점 | API 테스트: 객관식 답안 제출 → 즉시 채점 결과 |
| 과제 제출 + 강사 수동 채점 | API 테스트: 제출 → 채점 → CONFIRMED 상태 전이 |
| 채점 이의 제기 플로우 | API 테스트: Appeal 생성 → APPEALED → 강사 처리 → CONFIRMED |
| 진단 테스트 + mastery 초기화 | API 테스트: 5문항 풀기 → concept_mastery 생성 (confidence=0.7) |
| 자가 진단 + 가중 평균 | API 테스트: 자가 진단 → mastery 가중 업데이트 (confidence=0.3) |
| 커뮤니티 CRUD | API 테스트: 게시글/댓글/좋아요/답변 채택 |
| 단위 테스트 전체 통과 | `./gradlew test` 성공 |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v4.0 | 2026-04-03 | 초안 작성: 14개 Task (T-201 ~ T-214) 세부 워크플로우 정의 |
