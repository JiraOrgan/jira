# 2. Entity & JPA RULE -- 엔티티 설계 및 ORM 규칙 [MUST / SHOULD 혼합]

> **기준**: JPA/Hibernate, MySQL, QueryDSL 5.1

### 2.1 Entity 클래스 구조 [MUST]

#### 2.1.1 어노테이션 순서

```java
// Good -- 표준 순서
@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_tb")
public class Issue {
```

```java
// Bad -- 비표준 순서
@Entity
@Data
@Table(name = "issue_tb")
@NoArgsConstructor
public class Issue {
```

#### 2.1.2 PK 설계

- PK 타입은 **Long**을 사용한다
- 생성 전략은 **GenerationType.IDENTITY**를 사용한다 (MySQL AUTO_INCREMENT)

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

#### 2.1.3 테이블 네이밍

- snake_case + **_tb** 접미사를 사용한다
- Entity 클래스명의 camelCase를 snake_case로 변환 후 _tb를 붙인다

| Entity 클래스 | 테이블명 |
|--------------|---------|
| UserAccount | user_account_tb |
| Issue | issue_tb |
| ProjectMember | project_member_tb |
| DashboardGadget | dashboard_gadget_tb |

#### 2.1.4 Builder 패턴

- @Builder는 **생성자**에 선언한다 (클래스 레벨 금지)
- 컬렉션 필드(List, Set)는 Builder 생성자에서 **제외**한다

```java
// Good
@Builder
public Issue(Long id, String issueKey, String summary, Project project) {
    this.id = id;
    this.issueKey = issueKey;
    this.summary = summary;
    this.project = project;
}

// Bad -- 클래스 레벨 @Builder
@Builder
@Entity
public class Issue { ... }
```

### 2.2 연관관계 [MUST]

#### 2.2.1 Fetch 전략

- 모든 연관관계의 기본 Fetch는 **LAZY**를 사용한다
- EAGER는 **절대 사용하지 않는다**
- 필요 시 JPQL JOIN FETCH 또는 @EntityGraph로 해결한다

```java
// Good
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "project_id", nullable = false)
private Project project;

// Bad
@ManyToOne(fetch = FetchType.EAGER)
private Project project;

// Good -- 필요 시 JOIN FETCH
@Query("SELECT i FROM Issue i JOIN FETCH i.project WHERE i.issueKey = :key")
Optional<Issue> findByIssueKeyWithProject(@Param("key") String key);
```

#### 2.2.2 N+1 방지

- 목록 조회 시 연관 엔티티가 필요하면 반드시 **JOIN FETCH** 사용
- open-in-view는 **false**로 유지 (이미 설정됨)
- batch_fetch_size: 100 설정으로 N+1 완화 (이미 설정됨)

#### 2.2.3 N:M 관계

- @ManyToMany 사용 **금지**
- 중간 엔티티를 생성하여 1:N + N:1로 풀어낸다
- 중간 엔티티에는 Long id PK를 부여한다

```java
// Good -- 중간 엔티티 사용
@Entity @Table(name = "issue_label_tb")
public class IssueLabel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Issue issue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Label label;
}

// Bad -- @ManyToMany 직접 사용
@ManyToMany
private List<Label> labels;
```

### 2.3 감사 필드 [SHOULD]

- 생성일: @CreationTimestamp private LocalDateTime createdAt
- 수정일: @UpdateTimestamp private LocalDateTime updatedAt
- 특수 시각 필드(transitionedAt, changedAt)는 @CreationTimestamp로 자동 생성

### 2.4 금지 사항

- FetchType.EAGER 사용 금지
- @ManyToMany 사용 금지
- Entity를 Controller 응답으로 직접 반환 금지
- 클래스 레벨 @Builder 사용 금지
- @Column에 MySQL 예약어 사용 시 백틱 이스케이프 필수

---

## 참고 문서

- [01-architecture.md](01-architecture.md) -- 패키지 구조
- [05-service-layer.md](05-service-layer.md) -- 트랜잭션 규칙
- [07-validation-dto.md](07-validation-dto.md) -- DTO 변환 규칙
