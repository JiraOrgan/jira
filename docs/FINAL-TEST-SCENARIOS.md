# 최종 테스트 시나리오 문서 (Final Test Scenarios)

> **버전**: v1.1  
> **작성일**: 2026-04-15  
> **목적**: 릴리즈 직전 **UAT·통합·회귀**에서 실행할 시나리오를 한 문서로 묶고, 요구사항·규칙·API 정의와의 **추적성**을 확보한다.  
> **참조 원본**  
> - **공개 문서 레포** [Project-Control-Hub/documents](https://github.com/Project-Control-Hub/documents) (전체 Public)  
>   - [README.md — 문서 인덱스](https://github.com/Project-Control-Hub/documents/blob/main/README.md) (최종 갱신 2026-04-09): `00`~`15` 번호 체계, `etc/` 보조 문서. **참고**: README 상단에 따르면 일부 `project-doc-manager` 스킬 표준과 폴더 번호 대응(예: 12·13)이 다를 수 있으므로 **본 레포 README 표**를 정본으로 한다.  
>   - [10-테스트전략서 v4.0](https://github.com/Project-Control-Hub/documents/blob/main/10-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%A0%84%EB%9E%B5%EC%84%9C/10-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%A0%84%EB%9E%B5%EC%84%9C_v4.0.md): TC-001~, 부하·보안·CI 게이트·API 계약·Flutter(§17) 등 **56+ TC** 및 테스트 피라미드.  
>   - [13-테스트보고서 v1.0](https://github.com/Project-Control-Hub/documents/blob/main/13-%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%B3%B4%EA%B3%A0%EC%84%9C/13-%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%B3%B4%EA%B3%A0%EC%84%9C_v1.0.md): 전략서 준거, FR별 테스트 여부, **릴리스 판정(§8)** 골격.  
>   - API·요구사항 정본: 동 레포 [04-API정의서 v4.0](https://github.com/Project-Control-Hub/documents/blob/main/04-API%EC%A0%95%EC%9D%98%EC%84%9C/04-API%EC%A0%95%EC%9D%98%EC%84%9C_v4.0.md), [07-요구사항정의서 v2.3](https://github.com/Project-Control-Hub/documents/blob/main/07-%EC%9A%94%EA%B5%AC%EC%82%AC%ED%95%AD%EC%A0%95%EC%9D%98%EC%84%9C/07-%EC%9A%94%EA%B5%AC%EC%82%AC%ED%95%AD%EC%A0%95%EC%9D%98%EC%84%9C_v2.3.md).  
> - 저장소 `docs/`: [E2E-LIFECYCLE-SCENARIOS.md](E2E-LIFECYCLE-SCENARIOS.md), [PRD.md](PRD.md), [REQUIREMENTS-v2.md](REQUIREMENTS-v2.md), [NFR-VERIFICATION.md](NFR-VERIFICATION.md), [design/API-SPEC-v4-implementation.md](design/API-SPEC-v4-implementation.md)  
> - 저장소 `rules/`: [human/03-api-design.md](../rules/human/03-api-design.md), [human/04-security.md](../rules/human/04-security.md), [human/06-exception.md](../rules/human/06-exception.md), [human/07-validation-dto.md](../rules/human/07-validation-dto.md)

---

## 1. 범위·합격 기준

| 구분 | 내용 |
|------|------|
| **대상** | 웹(`apps/web`) · REST API(Spring Boot) · 모바일(`apps/mobile`) — 구현된 기능에 한함 |
| **최종 합격** | 아래 **Part A~E**에서 **필수(P0)** 항목 전원 통과, **P1**은 계획된 스코프 내 통과 또는 기록된 예외 승인 |
| **공통 성공** | HTTP·UI 기대 코드 준수, `ApiResponse` 래퍼(`success`, `data`, `error`) 일관성, 새로고침 후 데이터 정합, 민감정보 비노출 |
| **릴리스 서식** | 수치·판정 기록은 공식 [13-테스트보고서](https://github.com/Project-Control-Hub/documents/blob/main/13-%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%B3%B4%EA%B3%A0%EC%84%9C/13-%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%B3%B4%EA%B3%A0%EC%84%9C_v1.0.md) §3~§8을 채운 뒤, 본 문서 §9 실행 기록과 상호 링크한다. |

**P0 / P1 구분**

- **P0**: 인증·권한, 프로젝트·이슈 CRUD 핵심, 워크플로 전환, 스프린트 시작/완료, 보호 라우트, 401/403 처리  
- **P1**: JQL·로드맵·리포트·릴리즈·Audit·자동화·VCS 링크·알림(환경 의존) 등

---

## 2. 환경·역할·데이터

| 항목 | 권장 |
|------|------|
| **API Base** | 스테이징 또는 로컬 — 웹과 동일 베이스 URL(프록시/`API_BASE_URL`) |
| **OpenAPI** | 기동 후 `/v3/api-docs`, Swagger UI `/swagger-ui.html`로 계약 교차 검증 |
| **계정** | ADMIN 1명(프로젝트 창립), MEMBER(또는 DEVELOPER/QA 등) 1명 이상 — [rules 보안 RBAC](../rules/human/04-security.md) 매트릭스 검증용 |
| **식별자** | 프로젝트 키·이슈 키·스프린트 ID를 시나리오별로 기록해 재현에 사용 |
| **도구·게이트(정본)** | [10-테스트전략서 v4.0](https://github.com/Project-Control-Hub/documents/blob/main/10-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%A0%84%EB%9E%B5%EC%84%9C/10-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%A0%84%EB%9E%B5%EC%84%9C_v4.0.md) §4 환경 표, §12 CI/CD 게이트, §9 k6 SLA — 구현 저장소 URL·JDK 버전 등은 스테이징 실제값으로 대체 |

---

## 2.1 공식 TC와 본 문서 Part 매핑 (documents §3)

아래는 **테스트 전략서 v4.0**에 명시된 TC ID와 본 문서 Part의 대응이다. 상세 절차·사전 조건·기대 결과는 **전략서 원문**을 따른다.

| 공식 TC | 기능 | 본 문서 |
|---------|------|---------|
| TC-001 ~ TC-003 | 인증·로그인 잠금 | Part A **A-01**, Part C **C-03**, Part E NFR-007 |
| TC-010 ~ TC-011 | 이슈 생성(Story, Bug 필수 필드) | Part A **A-04** |
| TC-020 ~ TC-023 | 워크플로·DoD | Part A **A-06** |
| TC-030 ~ TC-031 | JQL | Part A **A-08** |
| TC-040 ~ TC-042 | 권한·기밀 이슈 | Part C **C-02**, Part A **A-03** |
| TC-050 | WIP 제한 | Part A **A-06** |
| TC-060 | 보드 DnD·Audit | Part A **A-06**, Part A **A-10** |
| TC-070 | Audit Log 필드 추적 | Part A **A-10**, Part E NFR-008 |
| TC-080 ~ TC-085 | 스프린트 생명주기·Viewer | Part A **A-05**, **A-11**, Part C **C-02** |

**전략서 나머지 절**과 본 문서: §6 자동화·§7 피라미드 → CI 설계; §8 DoD → PR/배포 체크리스트; §9 부하 → **Part E** 및 k6 수치; §10 보안(RBAC 35조합, OWASP) → **Part C** 확장·ZAP; §13 접근성·§14 Chaos·§15 VRT → 릴리즈 전 선택 게이트; §16 API 계약(Pact/OpenAPI) → **Part B**; §17 Flutter → **Part A** 모바일 및 **Part E** 모바일 NFR.

---

## 3. Part A — 엔드투엔드·UAT (제품 흐름)

상세 단계·재검증 이력은 [E2E-LIFECYCLE-SCENARIOS.md](E2E-LIFECYCLE-SCENARIOS.md) v1.2를 정본으로 한다. **최종 실행 시** 아래 체크리스트로 통과 여부만 기록한다.

| ID | 시나리오 요약 | P0 | 비고 |
|----|----------------|:--:|------|
| A-01 | S-01 로그인·세션·보호 라우트 | ✓ | 무효 자격증명·401 후 refresh 실패 흐름 포함 |
| A-02 | S-02 프로젝트 생성·키 중복·프로젝트 홈 | ✓ | `POST /api/v1/projects` |
| A-03 | S-03 멤버·역할(2계정) |  | ADMIN 전용 메뉴 MEMBER에서 차단 |
| A-04 | S-04 이슈 생성·백로그·순서·미배정 | ✓ | FR-001, FR-010 |
| A-05 | S-05 스프린트 생성·시작·배정 | ✓ | FR-011; 활성 스프린트 제한 규칙 |
| A-06 | S-06 스크럼/칸반·허용/금지 전이 | ✓ | FR-008~009, FR-014 (`WORKFLOW_VIOLATION` 등) |
| A-07 | S-07 댓글·멘션·첨부 |  | 알림은 환경 의존 |
| A-08 | S-08 JQL·아카이브 기본·로드맵 |  | FR-016, FR-012 |
| A-09 | S-09 리포트·Fix Version·릴리즈 노트 |  | 빈 데이터 graceful |
| A-10 | S-10 설정·이슈 아카이브·전이 차단·Audit |  | ADMIN Audit |
| A-11 | S-11 스프린트 완료·미완료 처리·로그아웃 | ✓ | |
| A-R | 회귀 R-01~R-04 | ✓ | 타 프로젝트 이슈 접근 거부 등 |

**모바일**: 동일 계정 기준 로그인 유지, 이슈 목록·상세·보드 터치 전환(구현 범위 내).

---

## 4. Part B — API·계약 (rules §3 + API-SPEC)

| ID | 시나리오 | 기대 |
|----|----------|------|
| B-01 | 모든 성공 응답이 `ApiResponse` 형태 | `success`, `data` 또는 `error` 필드 일관 |
| B-02 | URI 규칙 | 접두사 `/api/v1/`, 리소스 복수형·kebab-case, 상태 변경은 POST + 액션 경로 |
| B-03 | 생성/삭제 HTTP 코드 | POST 생성 201, DELETE 성공 시 정의된 200/`noContent` 정책과 일치 |
| B-04 | 인증 | 보호 `GET/POST`에 Bearer 없으면 401 |
| B-05 | 페이징 목록 | `Page`/`Slice` 응답 스키마와 UI/클라이언트 소비 일치 |
| B-06 | OpenAPI 대조 | 구현 컨트롤러와 `/v3/api-docs` 경로·스키마 일치(샘플 5엔드포인트 이상) |

**우선 검증 엔드포인트 샘플** ( [API-SPEC-v4-implementation.md](design/API-SPEC-v4-implementation.md) ):  
`/api/auth/login`, `POST /api/v1/projects`, `GET /api/v1/projects/by-key/{key}`, `POST /api/v1/issues`, `POST /api/v1/issues/{key}/transitions`, `POST /api/v1/sprints/{id}/start|complete`, 자동화·VCS 링크(스코프에 있을 때).

---

## 5. Part C — 보안·RBAC (rules §4)

| ID | 시나리오 | 기대 |
|----|----------|------|
| C-01 | JWT | Access 만료 시 클라이언트가 refresh 또는 로그인 유도; Secret·DB 비밀번호는 환경변수 주입(코드·로그에 평문 없음) |
| C-02 | RBAC 행렬 | ADMIN/DEVELOPER/QA/REPORTER/VIEWER별 이슈·스프린트·설정 권한이 [04-security](../rules/human/04-security.md) 표와 합치 |
| C-03 | 비밀번호 | BCrypt 저장(평문 미저장), 로그인 실패 잠금 정책이 구현된 경우 [NFR-007](NFR-VERIFICATION.md) 시나리오로 검증 |
| C-04 | CORS | 운영/stage에서 허용 오리진 명시; credentials 사용 시 와일드카드 금지 |

---

## 6. Part D — 예외·검증 (rules §6·§7)

| ID | 시나리오 | 기대 HTTP | 기대 코드 예 |
|----|----------|------------|---------------|
| D-01 | 잘못된 입력(JSON 검증 실패) | 400 | `INVALID_INPUT_VALUE` 등 |
| D-02 | 존재하지 않는 리소스 | 404 | `ENTITY_NOT_FOUND` |
| D-03 | 중복(이메일·프로젝트 키 등) | 409 | `DUPLICATE_RESOURCE` / 도메인 코드 |
| D-04 | 권한 없음 | 403 | `FORBIDDEN` |
| D-05 | 워크플로 위반 | 409 등 정책 | `WORKFLOW_VIOLATION` 계열 |
| D-06 | 전역 핸들러 | 응답이 `ApiResponse.fail` 형태, 운영에서 stacktrace 미노출 |

---

## 7. Part E — NFR 스팟 체크 ([NFR-VERIFICATION.md](NFR-VERIFICATION.md))

| NFR | 최종 테스트에서 할 일 |
|-----|------------------------|
| NFR-001~002,004 | 스테이징 부하 도구(k6/Gatling) 시나리오가 있으면 P95 기록; 없으면 “미측정”으로 표시 |
| NFR-005 | BCrypt cost ≥ 12 설정 여부 코드·설정 확인 |
| NFR-007 | 로그인 5회/30분 잠금 구현 시 시나리오 실행 |
| NFR-008 | Audit API/DB row 샘플로 필드 수준 기록 여부 확인 |
| NFR-010~011 | 모바일 콜드 스타트·오프라인은 해당 마일스톤에서 별도 항목으로만 포함 |

---

## 8. 요구사항 추적 (요약 RTM)

| FR 군 | Part A ID |
|-------|-----------|
| FR-001~007,013~014 | A-04, A-06 |
| FR-008~012,016 | A-05, A-06, A-08 |
| FR-017~022,025 | A-09 및 산정·워치 UI/API |
| FR-023~024,026~031 | A-07, A-10 |
| FR-015,033 | 자동화·VCS API (A-08~10 및 Part B 샘플) |
| FR-MOBILE-001~002 | 모바일 동행 검증 |

---

## 9. 실행 기록 (복사용)

| 날짜 | 환경 | 실행자 | Part A | Part B | Part C | Part D | Part E | 결함 ID | 판정 |
|------|------|--------|--------|--------|--------|--------|--------|---------|------|
| | | | /11 | /6 | /4 | /6 | /선택 | | 통과/조건부/보류 |

---

## 10. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| v1.0 | 2026-04-15 | `documents` GitHub·`docs/`·`rules/` 반영 최종 시나리오 통합본 초안 |
| v1.1 | 2026-04-15 | 공개 [Project-Control-Hub/documents](https://github.com/Project-Control-Hub/documents) README·10-테스트전략서 v4.0·13-테스트보고서 v1.0 원문 재확인; TC↔Part 매핑(§2.1), 릴리스 보고서 연계, 폴더 번호 안내 반영 |
