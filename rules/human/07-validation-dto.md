# 7. Validation & DTO RULE -- 입력 검증 및 DTO 변환 [MUST / SHOULD 혼합]

> **기준**: Bean Validation, Java Record DTO 패턴

### 7.1 DTO 구조 [MUST]

#### 7.1.1 파일 분리

- 도메인당 **{Domain}Request.java**와 **{Domain}Response.java** 2개 파일
- 내부에 record로 용도별 DTO를 정의한다
- 외부 클래스에는 어노테이션을 붙이지 않는다 (final class)

```java
// Good -- Java record 사용
public final class CourseRequest {
    public record Save(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull CourseLevel level
    ) {}

    public record Update(
        @NotBlank String title,
        String description
    ) {}
}
```

```java
// Bad -- @Data inner static class (구 패턴)
public class CourseRequest {
    @Data
    public static class SaveDTO { ... }
}
```

#### 7.1.2 Request DTO Naming

| 이름 | 용도 |
|------|------|
| Save | 등록 |
| Update | 수정 |
| Submit | 제출 (퀴즈/과제) |
| Login | 로그인 |
| SignUp | 회원가입 |

#### 7.1.3 Response DTO Naming

| 이름 | 용도 |
|------|------|
| Summary | 최소 정보 (목록용) |
| Detail | 상세 정보 (단건 조회) |
| Created | 생성 결과 |

### 7.2 DTO 변환 방식 [MUST]

**Record 생성자** 패턴을 사용한다:

```java
// Good -- Record 생성자로 Entity → DTO 변환
public final class CourseResponse {
    public record Detail(
        Long id,
        String title,
        String description,
        String instructorName,
        LocalDateTime createdAt
    ) {
        public Detail(Course course) {
            this(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor() != null ? course.getInstructor().getName() : null,
                course.getCreatedAt()
            );
        }
    }

    public record Summary(Long id, String title) {
        public Summary(Course course) {
            this(course.getId(), course.getTitle());
        }
    }
}
```

```java
// Bad -- 정적 팩토리 + @Data (구 패턴)
@Data
public static class DetailDTO {
    private Long id;
    private DetailDTO() {}
    public static DetailDTO of(Issue issue) { ... }
}
```

### 7.3 Validation [MUST]

#### 7.3.1 Request Record에 Bean Validation 적용

```java
public record Save(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull CourseLevel level,
    String thumbnail           // nullable -- 검증 없음
) {}
```

#### 7.3.2 Controller에서 @Valid 사용

```java
@PostMapping
public ResponseEntity<ApiResponse<CourseResponse.Detail>> save(
        @Valid @RequestBody CourseRequest.Save request) {
    return ResponseEntity.status(201).body(ApiResponse.created(courseService.save(request)));
}
```

### 7.4 연관 엔티티 변환 [SHOULD]

연관 엔티티의 필드를 record에 포함할 때 null 체크를 수행한다:

```java
public record Detail(Long id, String title, String instructorName) {
    public Detail(Course course) {
        this(
            course.getId(),
            course.getTitle(),
            // LAZY 연관 -- null 체크 필수
            course.getInstructor() != null ? course.getInstructor().getName() : null
        );
    }
}
```

### 7.5 금지 사항

- 외부 클래스에 @Data 등 Lombok 어노테이션 금지
- @Data 사용 금지 (Java record 사용)
- Entity를 DTO로 직접 반환 금지
- DTO에서 Entity를 직접 import하여 상속 금지
- 검증 없이 사용자 입력을 Entity에 직접 설정 금지

---

## 참고 문서

- [02-entity-jpa.md](02-entity-jpa.md) -- Entity 구조
- [05-service-layer.md](05-service-layer.md) -- Service에서 DTO 변환
- [03-api-design.md](03-api-design.md) -- API 응답 형식
