# REST API Design Rules

## URI Pattern
- Prefix: /api/v1/
- Resource: 복수형 명사, lowercase, kebab-case
- Sub-resource: /api/v1/{parent}/{parentId}/{children}

## API 경로 규칙
```
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

## Standard CRUD Endpoints
```
GET    /api/v1/{resources}          → 200 + ApiResponse.ok(list)
GET    /api/v1/{resources}/{id}     → 200 + ApiResponse.ok(detail)
POST   /api/v1/{resources}          → 201 + ApiResponse.created(data)
PUT    /api/v1/{resources}/{id}     → 200 + ApiResponse.ok(data)
DELETE /api/v1/{resources}/{id}     → 200 + ApiResponse.noContent()
```

## SSE Streaming (AI 튜터)
```
GET /api/v1/ai/chat/{sessionId}/stream → text/event-stream (SSE)
```
- Content-Type: text/event-stream
- X-AI-Model 헤더로 사용 모델 표시

## Response Wrapper
모든 응답은 ApiResponse<T>로 래핑:
```java
return ResponseEntity.ok(ApiResponse.ok(data));
return ResponseEntity.status(201).body(ApiResponse.created(data));
return ResponseEntity.ok(ApiResponse.noContent());
```

## Pagination
- @PageableDefault(size = 20) Pageable pageable
- Page<T> (총 건수 필요) 또는 Slice<T> (무한 스크롤)

## Forbidden
- 동사 기반 URI 금지 (/getUsers)
- Entity 직접 반환 금지
- ApiResponse 없이 응답 금지
