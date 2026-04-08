# Validation & DTO Rules

## DTO Style: Java Record
```
{Domain}Request.java   -- 입력 DTOs (Java record)
{Domain}Response.java  -- 출력 DTOs (Java record)
```
- 외부 클래스: 어노테이션 없음, final class
- 내부 record로 용도별 DTO 정의

## Request DTO Naming
| Name | Purpose |
|------|---------|
| Save | 등록 |
| Update | 수정 |
| Submit | 제출 (퀴즈/과제) |
| Login | 로그인 |
| SignUp | 회원가입 |

## Response DTO Naming
| Name | Purpose |
|------|---------|
| Summary | 목록용 최소 정보 |
| Detail | 단건 조회 상세 |
| Created | 생성 응답 |

## DTO Conversion Pattern (Record Constructor)
```java
public final class CourseResponse {
    public record Detail(
        Long id,
        String title,
        String description,
        String instructorName
    ) {
        public Detail(Course course) {
            this(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor() != null ? course.getInstructor().getName() : null
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

## Validation
- Request record: @NotNull, @NotBlank, @Email, @Size 등 Bean Validation
- Controller: @Valid @RequestBody
- LAZY 연관 엔티티 변환 시 null 체크 필수

## Forbidden
- 외부 클래스에 Lombok 어노테이션 금지
- Entity 직접 반환 금지
- Entity 상속/포함 금지
- 검증 없이 사용자 입력을 Entity에 설정 금지
- @Data 사용 금지 (Java record 사용)
