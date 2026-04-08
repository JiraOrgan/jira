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
public class CourseService {

    public CourseResponse.Detail findById(Long id) { ... }  // readOnly 상속

    @Transactional  // 쓰기 메서드 - 개별 선언
    public CourseResponse.Detail save(CourseRequest.Save request) { ... }
}
```

#### 5.1.2 어노테이션 순서

```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class {Domain}Service {
```

#### 5.1.3 Transactional Outbox 패턴 [MUST]

이벤트 발행이 필요한 쓰기 작업에서는 비즈니스 데이터와 Outbox 이벤트를 **같은 트랜잭션**에서 처리한다:

```java
// Good -- Outbox 패턴
@Transactional
public void submitAssignment(Long assignmentId, SubmitRequest request) {
    Submission submission = submissionRepository.save(
        Submission.builder()
            .assignment(assignment)
            .content(request.content())
            .build()
    );
    // 같은 @Transactional 안에서 Outbox 발행
    outboxPublisher.publish(
        new AssignmentSubmitted(submission.getId()),
        "assignment-grading"
    );
}

// Bad -- KafkaTemplate 직접 호출
@Transactional
public void submitAssignment(...) {
    submissionRepository.save(submission);
    kafkaTemplate.send("assignment-grading", event);  // 금지!
}
```

### 5.2 예외 처리 [MUST]

- Repository에서 Optional을 반환받아 **orElseThrow**로 처리
- BusinessException + ErrorCode를 사용한다

```java
// Good
Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

// Bad -- null 체크
Course course = courseRepository.findById(courseId).orElse(null);
if (course == null) throw new RuntimeException("not found");
```

### 5.3 DTO 반환 원칙 [MUST]

- Service는 **DTO(Java record)를 생성하여 반환**한다
- Entity를 Controller로 직접 전달하지 않는다

```java
// Good -- record 생성자 사용
public CourseResponse.Detail findById(Long id) {
    Course course = courseRepository.findByIdWithInstructor(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
    return new CourseResponse.Detail(course);
}

// Bad -- Entity 직접 반환
public Course findById(Long id) {
    return courseRepository.findById(id).orElseThrow();
}
```

### 5.4 메서드 네이밍 [SHOULD]

| 기능 | 메서드명 | 비고 |
|------|---------|------|
| 목록 조회 | findAll, findByCourse | 접두사: find |
| 단건 조회 | findById | |
| 생성 | save | |
| 수정 | update | |
| 삭제 | delete | |
| 도메인 동사 | submit, enroll, grade, appeal, diagnose | LearnFlow 도메인 동사 |

### 5.5 금지 사항

- Service에서 HttpServletRequest/Response 직접 사용 금지
- Entity를 Controller에 직접 반환 금지
- RuntimeException 직접 throw 금지 (BusinessException 사용)
- 트랜잭션 없이 쓰기 작업 금지
- KafkaTemplate 직접 호출 금지 (OutboxPublisher.publish() 사용)

---

## 참고 문서

- [06-exception.md](06-exception.md) -- ErrorCode 체계
- [07-validation-dto.md](07-validation-dto.md) -- DTO 변환 규칙
