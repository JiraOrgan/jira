# Service Layer Rules

## Class Template
```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class {Domain}Service {

    private final {Domain}Repository {domain}Repository;

    // 조회 -- readOnly 상속
    public {Domain}Response.Detail findById(Long id) {
        {Domain} entity = {domain}Repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return new {Domain}Response.Detail(entity);
    }

    // 쓰기 -- @Transactional 개별 선언
    @Transactional
    public {Domain}Response.Detail save({Domain}Request.Save request) {
        {Domain} entity = {Domain}.builder()...build();
        {domain}Repository.save(entity);
        return new {Domain}Response.Detail(entity);
    }
}
```

## Annotation Order
1. @Transactional(readOnly = true)
2. @RequiredArgsConstructor
3. @Service

## Transaction Rules
- 클래스: @Transactional(readOnly = true) 필수
- 쓰기 메서드만: @Transactional 개별 선언

## Outbox Pattern (이벤트 발행)
```java
@Transactional
public void submitAssignment(Long assignmentId, SubmitRequest request) {
    // 1. 비즈니스 데이터 저장
    Submission submission = submissionRepository.save(...);
    // 2. 같은 트랜잭션에서 Outbox 이벤트 발행
    outboxPublisher.publish(new AssignmentSubmitted(submission.getId()), "assignment-grading");
    // KafkaTemplate 직접 호출 금지!
}
```

## Exception Pattern
```java
.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
```
- RuntimeException 직접 throw 금지
- null 반환 후 null 체크 금지

## Return Type
- Service는 DTO(record) 반환 (Entity 반환 금지)
- 변환: `new {Domain}Response.Detail(entity)` (record 생성자)

## Method Naming
- findAll, findById, findByKey -- 조회
- save -- 생성
- update -- 수정
- delete -- 삭제
- submit, enroll, grade, appeal -- 도메인 동사
