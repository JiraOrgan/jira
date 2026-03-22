# Service Layer Rules

## Class Template
```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class {Domain}Service {

    private final {Domain}Repository {domain}Repository;

    // 조회 -- readOnly 상속
    public {Domain}Response.DetailDTO findById(Long id) {
        {Domain} entity = {domain}Repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return {Domain}Response.DetailDTO.of(entity);
    }

    // 쓰기 -- @Transactional 개별 선언
    @Transactional
    public {Domain}Response.DetailDTO save({Domain}Request.SaveDTO reqDTO) {
        {Domain} entity = {Domain}.builder()...build();
        {domain}Repository.save(entity);
        return {Domain}Response.DetailDTO.of(entity);
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

## Exception Pattern
```java
.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
```
- RuntimeException 직접 throw 금지
- null 반환 후 null 체크 금지

## Return Type
- Service는 DTO 반환 (Entity 반환 금지)
- 변환: {Domain}Response.DetailDTO.of(entity)

## Method Naming
- findAll, findById, findByKey -- 조회
- save -- 생성
- update -- 수정
- delete -- 삭제
- start, complete, release, transition -- 상태 변경
