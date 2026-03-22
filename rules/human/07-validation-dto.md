# 7. Validation & DTO RULE -- 입력 검증 및 DTO 변환 [MUST / SHOULD 혼합]

> **기준**: Bean Validation, 정적 팩토리 메서드 패턴

### 7.1 DTO 구조 [MUST]

#### 7.1.1 파일 분리

- 도메인당 **{Domain}Request.java**와 **{Domain}Response.java** 2개 파일
- 내부에 static class로 용도별 DTO를 정의한다
- 외부 클래스에는 어노테이션을 붙이지 않는다

```java
// Good
public class IssueRequest {
    @Data
    public static class SaveDTO { ... }

    @Data
    public static class UpdateDTO { ... }

    @Data
    public static class TransitionDTO { ... }
}
```

#### 7.1.2 Request DTO 네이밍

| 이름 | 용도 |
|------|------|
| SaveDTO | 등록 |
| UpdateDTO | 수정 |
| TransitionDTO | 상태 변경 |
| LoginDTO | 로그인 |
| JoinDTO | 회원가입 |

#### 7.1.3 Response DTO 네이밍

| 이름 | 용도 |
|------|------|
| MinDTO | 최소 정보 (목록용) |
| DetailDTO | 상세 정보 (단건 조회) |
| MemberDTO | 하위 리소스 응답 |

### 7.2 DTO 변환 방식 [MUST]

**정적 팩토리 메서드 `of(Entity)`** 패턴을 사용한다:

```java
// Good -- 정적 팩토리 메서드
@Data
public static class DetailDTO {
    private Long id;
    private String issueKey;
    private String summary;

    private DetailDTO() {}  // private 기본 생성자

    public static DetailDTO of(Issue issue) {
        DetailDTO dto = new DetailDTO();
        dto.id = issue.getId();
        dto.issueKey = issue.getIssueKey();
        dto.summary = issue.getSummary();
        return dto;
    }
}

// Bad -- 생성자에서 직접 변환
public DetailDTO(Issue issue) {
    this.id = issue.getId();
}

// Bad -- Service에서 수동 매핑
DetailDTO dto = new DetailDTO();
dto.setId(issue.getId());
```

### 7.3 Validation [MUST]

#### 7.3.1 Request DTO에 Bean Validation 적용

```java
@Data
public static class SaveDTO {
    @NotNull private Long projectId;
    @NotBlank private String summary;
    @NotNull private IssueType issueType;
    @NotNull private Priority priority;
    private String description;          // nullable -- 검증 없음
    private Integer storyPoints;
}
```

#### 7.3.2 Controller에서 @Valid 사용

```java
@PostMapping
public ResponseEntity<ApiResponse<IssueResponse.DetailDTO>> save(
        @Valid @RequestBody IssueRequest.SaveDTO reqDTO) {
    return ResponseEntity.status(201).body(ApiResponse.created(issueService.save(reqDTO)));
}
```

### 7.4 연관 엔티티 변환 [SHOULD]

연관 엔티티의 필드를 DTO에 포함할 때 null 체크를 수행한다:

```java
public static DetailDTO of(Issue issue) {
    DetailDTO dto = new DetailDTO();
    dto.id = issue.getId();
    // LAZY 연관 -- null 체크 필수
    if (issue.getAssignee() != null) {
        dto.assigneeId = issue.getAssignee().getId();
        dto.assigneeName = issue.getAssignee().getName();
    }
    return dto;
}
```

### 7.5 금지 사항

- 외부 클래스에 @Data 등 Lombok 어노테이션 금지
- Entity를 DTO로 직접 반환 금지
- DTO에서 Entity를 직접 import하여 상속 금지
- 검증 없이 사용자 입력을 Entity에 직접 설정 금지

---

## 참고 문서

- [02-entity-jpa.md](02-entity-jpa.md) -- Entity 구조
- [05-service-layer.md](05-service-layer.md) -- Service에서 DTO 변환
- [03-api-design.md](03-api-design.md) -- API 응답 형식
