# 3. REST API Design RULE -- API 설계 및 URI 규칙 [MUST / SHOULD 혼합]

> **기준**: RESTful API, JSON, ApiResponse 래퍼

### 3.1 URI 설계 [MUST]

#### 3.1.1 기본 규칙

- 접두사: **/api/v1/** 사용
- 리소스명: **복수형 명사** (courses, quizzes, assignments)
- 소문자 + 하이픈 (kebab-case)

```text
/api/v1/courses                          # 강의 컬렉션
/api/v1/courses/{id}                     # 강의 단일 리소스
/api/v1/courses/{courseId}/sections       # 강의 하위 섹션
/api/v1/courses/{courseId}/sections/{sectionId}/lessons  # 섹션 하위 레슨
```

```text
// Bad
/api/v1/getCourses           # 동사 사용
/api/v1/course               # 단수형
/api/v1/course_list          # underscore
```

#### 3.1.2 API 경로 규칙

```text
인증:        /api/v1/auth/*                    → PUBLIC
사용자:      /api/v1/users/*                   → AUTHENTICATED
강의:        /api/v1/courses/*, sections/*, lessons/* → PUBLIC(조회) / INSTRUCTOR(쓰기)
AI 튜터:     /api/v1/ai/chat/*                 → LEARNER (SSE 스트리밍)
퀴즈/과제:   /api/v1/quizzes/*, assignments/*  → LEARNER(제출) / INSTRUCTOR(출제)
학습 분석:   /api/v1/analytics/*               → LEARNER / INSTRUCTOR
온보딩:      /api/v1/onboarding/*              → LEARNER
AI 품질:     /api/v1/admin/ai/quality/*        → ADMIN
FinOps:      /api/v1/admin/finops/*            → ADMIN
강사 검토:   /api/v1/instructor/review-queue/* → INSTRUCTOR
```

#### 3.1.3 SSE 스트리밍 (AI 튜터)

AI 튜터 채팅은 SSE(Server-Sent Events)로 스트리밍 응답을 제공한다:

```text
GET /api/v1/ai/chat/{sessionId}/stream → text/event-stream
```

- Content-Type: text/event-stream
- X-AI-Model 응답 헤더로 사용 모델 표시 (예: claude-3-sonnet)
- X-AI-Cost 응답 헤더로 요청 비용 표시

### 3.2 HTTP 메서드 및 상태 코드 [MUST]

| 메서드 | 용도 | 성공 코드 | 응답 |
|--------|------|----------|------|
| GET | 조회 | 200 | ApiResponse.ok(data) |
| POST | 생성 | 201 | ApiResponse.created(data) |
| PUT | 전체 수정 | 200 | ApiResponse.ok(data) |
| PATCH | 부분 수정 | 200 | ApiResponse.ok(data) |
| DELETE | 삭제 | 200 | ApiResponse.noContent() |

### 3.3 응답 래퍼 [MUST]

모든 API 응답은 **ApiResponse**로 감싼다:

```java
// Good
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CourseResponse.Detail>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.ok(courseService.findById(id)));
}

@PostMapping
public ResponseEntity<ApiResponse<CourseResponse.Detail>> save(
        @Valid @RequestBody CourseRequest.Save request) {
    return ResponseEntity.status(201).body(ApiResponse.created(courseService.save(request)));
}

@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    courseService.delete(id);
    return ResponseEntity.ok(ApiResponse.noContent());
}
```

### 3.4 페이징 [SHOULD]

- 목록 조회는 **Page** 또는 **Slice** 사용
- 기본 페이지 크기: 20
- 정렬 허용 필드를 제한한다 (SQL Injection 방지)

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<CourseResponse.Summary>>> findAll(
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.ok(courseService.findAll(pageable)));
}
```

### 3.5 금지 사항

- 동사 기반 URI 금지 (/getCourses, /createQuiz)
- Entity를 API 응답으로 직접 반환 금지
- ApiResponse 래퍼 없이 응답 금지
- GET 요청에 @RequestBody 사용 금지

---

## 참고 문서

- [06-exception.md](06-exception.md) -- 에러 응답 형식
- [07-validation-dto.md](07-validation-dto.md) -- 요청 검증
