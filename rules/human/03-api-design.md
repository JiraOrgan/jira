# 3. REST API Design RULE -- API 설계 및 URI 규칙 [MUST / SHOULD 혼합]

> **기준**: RESTful API, JSON, ApiResponse 래퍼

### 3.1 URI 설계 [MUST]

#### 3.1.1 기본 규칙

- 접두사: **/api/v1/** 사용
- 리소스명: **복수형 명사** (issues, projects, sprints)
- 소문자 + 하이픈 (kebab-case)

```text
/api/v1/projects                        # 컬렉션
/api/v1/projects/{id}                   # 단일 리소스
/api/v1/projects/{projectId}/members    # 하위 리소스
/api/v1/issues/{issueKey}/transitions   # 상태 변경 액션 (POST)
/api/v1/issues/{issueKey}/comments      # 이슈 하위 댓글
```

```text
// Bad
/api/v1/getProjects          # 동사 사용
/api/v1/project              # 단수형
/api/v1/project_list         # underscore
```

#### 3.1.2 상태 변경 액션

CRUD가 아닌 상태 변경은 **POST + 동사 경로**를 사용한다:

```text
POST /api/v1/sprints/{id}/start       # 스프린트 시작
POST /api/v1/sprints/{id}/complete    # 스프린트 완료
POST /api/v1/versions/{id}/release    # 버전 릴리즈
POST /api/v1/issues/{key}/transitions # 이슈 상태 전환
```

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
public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.ok(projectService.findById(id)));
}

@PostMapping
public ResponseEntity<ApiResponse<ProjectResponse.DetailDTO>> save(...) {
    return ResponseEntity.status(201).body(ApiResponse.created(projectService.save(reqDTO)));
}

@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    projectService.delete(id);
    return ResponseEntity.ok(ApiResponse.noContent());
}

// Bad -- 래퍼 없이 직접 반환
@GetMapping("/{id}")
public ProjectResponse.DetailDTO findById(@PathVariable Long id) {
    return projectService.findById(id);
}
```

### 3.4 페이징 [SHOULD]

- 목록 조회는 **Page** 또는 **Slice** 사용
- 기본 페이지 크기: 20
- 정렬 허용 필드를 제한한다 (SQL Injection 방지)

```java
@GetMapping("/project/{projectId}")
public ResponseEntity<ApiResponse<Page<IssueResponse.MinDTO>>> findByProject(
        @PathVariable Long projectId,
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.ok(issueService.findByProject(projectId, pageable)));
}
```

### 3.5 금지 사항

- 동사 기반 URI 금지 (/getUsers, /createIssue)
- Entity를 API 응답으로 직접 반환 금지
- ApiResponse 래퍼 없이 응답 금지
- GET 요청에 @RequestBody 사용 금지
- 200 이외의 성공 코드 미사용 금지 (생성은 201, 삭제는 200+noContent)

---

## 참고 문서

- [06-exception.md](06-exception.md) -- 에러 응답 형식
- [07-validation-dto.md](07-validation-dto.md) -- 요청 검증
