# Entity & JPA Rules

## Entity Class Template
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "{snake_case}")
public class {Domain} extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // fields...

    @Builder
    public {Domain}(Long id, ...) {  // 컬렉션 필드 제외
        this.id = id;
    }

    // 비즈니스 메서드로 상태 변경
    public void updateTitle(String title) {
        this.title = title;
    }
}
```

## BaseTimeEntity
```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## Annotation Order
1. @NoArgsConstructor(access = AccessLevel.PROTECTED)
2. @Getter
3. @Entity
4. @Table(name = "xxx")

## Table Naming
- snake_case (suffix 없음): UserAccount → user_accounts, Course → courses

## PK
- Type: Long
- Strategy: GenerationType.IDENTITY

## Setter Policy
- **@Setter 사용 금지** — 비즈니스 메서드로 상태 변경
- 예: `submission.applyAiGrade(score, confidence)`, `enrollment.updateProgress(percent)`

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
- BaseTimeEntity 상속으로 자동 관리 (@CreatedDate, @LastModifiedDate)
- JPA Auditing 활성화 필수 (@EnableJpaAuditing)
