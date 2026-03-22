# Validation & DTO Rules

## DTO File Structure
```
{Domain}Request.java   -- 입력 DTOs
{Domain}Response.java  -- 출력 DTOs
```
- 외부 클래스: 어노테이션 없음
- 내부 static class: @Data

## Request DTO Naming
| Name | Purpose |
|------|---------|
| SaveDTO | 등록 |
| UpdateDTO | 수정 |
| TransitionDTO | 상태 변경 |
| JoinDTO | 회원가입 |

## Response DTO Naming
| Name | Purpose |
|------|---------|
| MinDTO | 목록용 최소 정보 |
| DetailDTO | 단건 조회 상세 |
| MemberDTO | 하위 리소스 |

## DTO Conversion Pattern (Static Factory)
```java
@Data
public static class DetailDTO {
    private Long id;
    private String name;

    private DetailDTO() {}

    public static DetailDTO of(Entity entity) {
        DetailDTO dto = new DetailDTO();
        dto.id = entity.getId();
        dto.name = entity.getName();
        return dto;
    }
}
```

## Validation
- Request DTO: @NotNull, @NotBlank, @Email, @Size 등 Bean Validation
- Controller: @Valid @RequestBody
- LAZY 연관 엔티티 변환 시 null 체크 필수

## Forbidden
- 외부 클래스에 Lombok 어노테이션 금지
- Entity 직접 반환 금지
- Entity 상속/포함 금지
- 검증 없이 사용자 입력을 Entity에 설정 금지
