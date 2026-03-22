# REST API Design Rules

## URI Pattern
- Prefix: /api/v1/
- Resource: 복수형 명사, lowercase, kebab-case
- Sub-resource: /api/v1/{parent}/{parentId}/{children}

## Standard CRUD Endpoints
```
GET    /api/v1/{resources}          → 200 + ApiResponse.ok(list)
GET    /api/v1/{resources}/{id}     → 200 + ApiResponse.ok(detail)
POST   /api/v1/{resources}          → 201 + ApiResponse.created(data)
PUT    /api/v1/{resources}/{id}     → 200 + ApiResponse.ok(data)
DELETE /api/v1/{resources}/{id}     → 200 + ApiResponse.noContent()
```

## State Change Actions
```
POST /api/v1/sprints/{id}/start
POST /api/v1/sprints/{id}/complete
POST /api/v1/versions/{id}/release
POST /api/v1/issues/{key}/transitions
```

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
