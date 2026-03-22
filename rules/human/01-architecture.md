# 1. Architecture RULE -- 레이어 아키텍처 및 패키지 구조 [MUST]

> 이 프로젝트는 도메인별 flat 패키지 구조를 사용한다.
> **기준**: Spring Boot 4.0.3 / Java 21

### 1.1 패키지 구조 [MUST]

#### 1.1.1 도메인별 패키지 원칙

도메인 단위로 패키지를 구성한다. 기술 레이어(controller/, service/, repository/)가 아닌 비즈니스 도메인(user/, issue/, project/)으로 분류한다.

```text
com.jira.mng/
├── global/               # 공통 인프라
│   ├── config/           # 설정 (Security, Redis, Swagger, Gson)
│   ├── exception/        # 예외 (BusinessException, ErrorCode, GlobalExceptionHandler)
│   ├── response/         # 공통 응답 (ApiResponse)
│   ├── enums/            # 공유 Enum
│   ├── filter/           # 서블릿 필터 (MdcLoggingFilter)
│   └── aop/              # AOP (LoggingAspect, ExecutionTime)
├── user/                 # 사용자 도메인
│   ├── UserAccount.java
│   ├── UserAccountRepository.java
│   ├── UserAccountRequest.java
│   ├── UserAccountResponse.java
│   ├── UserAccountService.java
│   └── UserAccountApiController.java
├── project/              # 프로젝트 도메인
├── issue/                # 이슈 도메인
├── sprint/               # 스프린트 도메인
└── ...
```

```java
// Good -- 도메인별 패키지
com.jira.mng.issue.Issue
com.jira.mng.issue.IssueService
com.jira.mng.issue.IssueApiController

// Bad -- 기술 레이어별 패키지
com.jira.mng.entity.Issue
com.jira.mng.service.IssueService
com.jira.mng.controller.IssueApiController
```

#### 1.1.2 금지

- 기술 레이어 기준 패키지 분류 금지 (entity/, service/, controller/ 등)
- 도메인 간 순환 참조 금지
- global 패키지에 비즈니스 로직 배치 금지

### 1.2 레이어 책임 분리 [MUST]

| 레이어 | 클래스 패턴 | 책임 | 금지 사항 |
|--------|-----------|------|----------|
| Controller | {Domain}ApiController | 요청 수신, 입력 검증, 응답 직렬화 | 비즈니스 로직 금지 |
| Service | {Domain}Service | 트랜잭션, 도메인 로직, DTO 변환 | HTTP 객체 직접 사용 금지 |
| Repository | {Domain}Repository | DB 접근, 쿼리 | 비즈니스 로직 금지 |
| Entity | {Domain} | 데이터 모델, 도메인 상태 | Controller/Service에 직접 노출 금지 |
| DTO | {Domain}Request / {Domain}Response | 입출력 데이터 전달 | Entity를 상속하거나 포함 금지 |

### 1.3 의존 방향 [MUST]

```text
Controller → Service → Repository → Entity
    ↓            ↓
  Request      Response
   DTO          DTO
```

- 상위 레이어만 하위 레이어를 참조한다
- Repository가 Service를 참조하거나 Controller가 Repository를 직접 참조하면 안 된다
- 예외: 조회 전용 API(AuditLog 등)에서 Controller가 Repository를 직접 사용 가능 [SHOULD]

---

## 참고 문서

- [02-entity-jpa.md](02-entity-jpa.md) -- Entity 설계 규칙
- [05-service-layer.md](05-service-layer.md) -- Service 레이어 상세
