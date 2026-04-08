# 2. Entity & JPA RULE -- 엔티티 설계 및 ORM 규칙 [MUST / SHOULD 혼합]

> **기준**: JPA/Hibernate, MySQL, QueryDSL 5.1

### 2.1 Entity 클래스 구조 [MUST]

#### 2.1.1 어노테이션 순서

```java
// Good -- 표준 순서
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "courses")
public class Course extends BaseTimeEntity {
```

```java
// Bad -- @Data 사용, _tb suffix
@Entity
@Data
@Table(name = "course_tb")
@NoArgsConstructor
public class Course {
```

#### 2.1.2 BaseTimeEntity 상속

모든 엔티티는 `BaseTimeEntity`를 상속하여 생성/수정 시각을 자동 관리한다:

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

- `@EnableJpaAuditing`을 Config에 선언해야 한다
- `@CreationTimestamp` / `@UpdateTimestamp` (Hibernate 전용) 대신 Spring Data JPA 표준 사용

#### 2.1.3 PK 설계

- PK 타입은 **Long**을 사용한다
- 생성 전략은 **GenerationType.IDENTITY**를 사용한다 (MySQL AUTO_INCREMENT)

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

#### 2.1.4 테이블 네이밍

- snake_case, **suffix 없음** (과거 `_tb` suffix 사용하지 않음)
- Entity 클래스명의 camelCase를 snake_case로 변환

| Entity 클래스 | 테이블명 |
|--------------|---------|
| User | users |
| Course | courses |
| Enrollment | enrollments |
| AiChatSession | ai_chat_sessions |
| ContentEmbedding | content_embeddings |
| OutboxEvent | outbox_events |

#### 2.1.5 @Setter 금지 — 비즈니스 메서드 사용

엔티티에 `@Setter`를 사용하지 않는다. 상태 변경은 의미 있는 비즈니스 메서드를 통해 수행한다:

```java
// Good -- 비즈니스 메서드
public void applyAiGrade(int score, double confidence) {
    this.aiScore = score;
    this.aiConfidence = confidence;
    this.status = confidence >= 0.8
        ? SubmissionStatus.CONFIRMED
        : SubmissionStatus.MANUAL_REVIEW;
}

public void updateProgress(int percent) {
    this.progress = percent;
    if (percent >= 100) {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}

// Bad -- @Setter 사용
@Setter
private int aiScore;
```

#### 2.1.6 Builder 패턴

- @Builder는 **생성자**에 선언한다 (클래스 레벨 금지)
- 컬렉션 필드(List, Set)는 Builder 생성자에서 **제외**한다

```java
// Good
@Builder
public Course(Long id, String title, String description, User instructor) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.instructor = instructor;
}

// Bad -- 클래스 레벨 @Builder
@Builder
@Entity
public class Course { ... }
```

### 2.2 연관관계 [MUST]

#### 2.2.1 Fetch 전략

- 모든 연관관계의 기본 Fetch는 **LAZY**를 사용한다
- EAGER는 **절대 사용하지 않는다**
- 필요 시 JPQL JOIN FETCH 또는 @EntityGraph로 해결한다

```java
// Good
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "course_id", nullable = false)
private Course course;

// Good -- 필요 시 JOIN FETCH
@Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.id = :userId")
List<Enrollment> findByUserIdWithCourse(@Param("userId") Long userId);
```

#### 2.2.2 N+1 방지

- 목록 조회 시 연관 엔티티가 필요하면 반드시 **JOIN FETCH** 사용
- open-in-view는 **false**로 유지
- batch_fetch_size: 100 설정으로 N+1 완화

#### 2.2.3 N:M 관계

- @ManyToMany 사용 **금지**
- 중간 엔티티를 생성하여 1:N + N:1로 풀어낸다
- 중간 엔티티에는 Long id PK를 부여한다

### 2.3 금지 사항

- @Setter 사용 금지 (비즈니스 메서드 사용)
- @Data 사용 금지 (@Getter만 사용)
- FetchType.EAGER 사용 금지
- @ManyToMany 사용 금지
- Entity를 Controller 응답으로 직접 반환 금지
- 클래스 레벨 @Builder 사용 금지
- @CreationTimestamp / @UpdateTimestamp 사용 금지 (BaseTimeEntity 상속)

---

## 참고 문서

- [01-architecture.md](01-architecture.md) -- 패키지 구조
- [05-service-layer.md](05-service-layer.md) -- 트랜잭션 규칙
- [07-validation-dto.md](07-validation-dto.md) -- DTO 변환 규칙
