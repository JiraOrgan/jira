# Architecture Rules

## Package Structure
- 도메인별 flat 패키지: `com.pch.mng.{domain}/`
- 공통 인프라: `com.pch.mng.global/` (config, exception, response, enums, filter, aop)
- 기술 레이어별 패키지 분류 금지 (entity/, service/, controller/)

## File Naming per Domain
```
{Domain}.java              -- Entity
{Domain}Repository.java    -- Repository (Spring Data JPA interface)
{Domain}Request.java       -- Request DTOs (inner static classes)
{Domain}Response.java      -- Response DTOs (inner static classes)
{Domain}Service.java       -- Service
{Domain}ApiController.java -- REST Controller
```

## Layer Dependencies
```
Controller → Service → Repository → Entity
```
- 상위 레이어만 하위 참조
- Controller → Repository 직접 참조 금지 (조회 전용 API 예외 가능)
- 순환 참조 금지

## Layer Responsibilities
| Layer | Does | Does NOT |
|-------|------|----------|
| Controller | 요청 수신, @Valid 검증, ApiResponse 래핑 | 비즈니스 로직 |
| Service | 트랜잭션, 도메인 로직, DTO 변환 | HTTP 객체 사용 |
| Repository | DB 접근, JPQL/QueryDSL | 비즈니스 로직 |
| Entity | 데이터 모델 | 직접 API 노출 |
