# 5. Service Layer RULE -- 서비스 레이어 및 트랜잭션 [MUST / SHOULD 혼합]

> **기준**: Spring @Service, @Transactional

### 5.1 트랜잭션 관리 [MUST]

#### 5.1.1 기본 패턴

- 클래스 레벨: **@Transactional(readOnly = true)** 선언
- 쓰기 메서드: **@Transactional** 개별 선언
- readOnly=true는 dirty-checking 비활성화로 성능에 유리하다

```java
// Good
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueService {

    public IssueResponse.DetailDTO findByKey(String issueKey) { ... }  // readOnly 상속

    @Transactional  // 쓰기 메서드 - 개별 선언
    public IssueResponse.DetailDTO save(IssueRequest.SaveDTO reqDTO) { ... }
}

// Bad -- 클래스 레벨 @Transactional 없음
@Service
public class IssueService {
    public IssueResponse.DetailDTO findByKey(String issueKey) { ... }  // 트랜잭션 없음
}
```

#### 5.1.2 어노테이션 순서

```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class {Domain}Service {
```

### 5.2 예외 처리 [MUST]

- Repository에서 Optional을 반환받아 **orElseThrow**로 처리
- BusinessException + ErrorCode를 사용한다

```java
// Good
Issue issue = issueRepository.findByIssueKey(issueKey)
        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

// Bad -- null 체크
Issue issue = issueRepository.findByIssueKey(issueKey).orElse(null);
if (issue == null) throw new RuntimeException("not found");
```

### 5.3 DTO 반환 원칙 [MUST]

- Service는 **DTO를 생성하여 반환**한다
- Entity를 Controller로 직접 전달하지 않는다

```java
// Good
public IssueResponse.DetailDTO findByKey(String issueKey) {
    Issue issue = issueRepository.findByIssueKeyWithDetails(issueKey)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    return IssueResponse.DetailDTO.of(issue);
}

// Bad -- Entity 직접 반환
public Issue findByKey(String issueKey) {
    return issueRepository.findByIssueKey(issueKey).orElseThrow();
}
```

### 5.4 메서드 네이밍 [SHOULD]

| 기능 | 메서드명 | 비고 |
|------|---------|------|
| 목록 조회 | findAll, findByProject | 접두사: find |
| 단건 조회 | findById, findByKey | |
| 생성 | save | |
| 수정 | update | |
| 삭제 | delete | |
| 상태 변경 | start, complete, release, transition | 도메인 동사 사용 |

### 5.5 금지 사항

- Service에서 HttpServletRequest/Response 직접 사용 금지
- Entity를 Controller에 직접 반환 금지
- RuntimeException 직접 throw 금지 (BusinessException 사용)
- 트랜잭션 없이 쓰기 작업 금지

---

## 참고 문서

- [06-exception.md](06-exception.md) -- ErrorCode 체계
- [07-validation-dto.md](07-validation-dto.md) -- DTO 변환 규칙
