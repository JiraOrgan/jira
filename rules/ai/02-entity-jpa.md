# Entity & JPA Rules

## Entity Class Template
```java
@NoArgsConstructor
@Data
@Entity
@Table(name = "{snake_case}_tb")
public class {Domain} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // fields...

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public {Domain}(Long id, ...) {  // 컬렉션 필드 제외
        this.id = id;
    }
}
```

## Annotation Order
1. @NoArgsConstructor
2. @Data
3. @Entity
4. @Table(name = "xxx_tb")

## Table Naming
- snake_case + _tb suffix: UserAccount → user_account_tb

## PK
- Type: Long
- Strategy: GenerationType.IDENTITY

## Builder
- @Builder는 생성자에 선언 (클래스 레벨 금지)
- 컬렉션(List, Set) 필드는 Builder에서 제외

## Fetch Strategy
- 기본: FetchType.LAZY (모든 연관관계)
- EAGER 사용 금지
- 필요 시 JPQL JOIN FETCH 사용

## N:M Relations
- @ManyToMany 금지
- 중간 엔티티로 분리 (Long id PK 부여)

## Timestamps
- 생성일: @CreationTimestamp LocalDateTime createdAt
- 수정일: @UpdateTimestamp LocalDateTime updatedAt
