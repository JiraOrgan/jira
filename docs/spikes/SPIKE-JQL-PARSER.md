# 기술 스파이크: JQL 파서 설계

> **작성일**: 2026-04-09  
> **Task**: T-105  
> **관련 FR**: FR-016

---

## 1. 목표

- PRD 수준의 **PCH Query Language**(내부명 JQL)를 안전하게 파싱·검증한다.  
- SQL 인젝션·무한 비용 쿼리를 방지한다.  
- NFR-002(P95 < 500ms)를 달성할 수 있는 실행 경로를 남긴다.

---

## 2. 문법 범위 (MVP → 확장)

**MVP (Phase 4)**  
- 필드: `project`, `status`, `type`, `assignee`, `priority`, `sprint`, `text`(summary/description 검색)  
- 연산: `=`, `!=`, `IN`, `~`(contains), `IS EMPTY`  
- 논리: `AND`, `OR`, 괄호  
- 정렬: `ORDER BY` 단일·복합 필드, `ASC`/`DESC`  
- 페이징: `startAt`, `maxResults`는 API 파라미터로 분리 권장(문법 외)

**확장 (후속)**  
- 함수: `updated()`, `created()`, `currentUser()`  
- 서브쿼리·히스토리 필드

---

## 3. 구현 옵션 비교

| 방식 | 장점 | 단점 | 권장도 |
|------|------|------|--------|
| **ANTLR4** 문법 정의 | 확장·테스트 용이, 에러 메시지 통제 | 초기 학습·빌드 단계 추가 | ★★★ |
| **전용 재귀 하향 파서** | 의존성 최소 | 유지보수 비용, 보안 버그 위험 | ★★ |
| **단순 토큰 분할 + Specification** | 구현 빠름 | 복합 괄호·우선순위 버그, 확장성 낮음 | ★ (프로토타입만) |
| **PostgreSQL full-text + 제한 DSL** | 성능 좋음 | 필드별 매핑 복잡 | ★★ (text 검색 보조) |

---

## 4. 권장 아키텍처

1. **파싱**: ANTLR4로 AST 생성 → 방문자 패턴으로 **내부 질의 모델**(Immutable DTO).  
2. **실행**:  
   - JPA **QueryDSL** 또는 Specification으로 AST → `Predicate`.  
   - 필드·연산자 화이트리스트만 허용; 테이블 조인 깊이 제한.  
3. **보호**:  
   - `maxResults` 상한(예: 100).  
   - `text` 검색은 `LIKE` 대신 **pg_trgm** 또는 전용 검색 인덱스 검토(PR드 DB가 PostgreSQL 기준).  
   - 현재 저장소는 MySQL 개발 중이면, Phase 2 ERD 정본 DB 전환 시 인덱스 전략 재검토.

---

## 5. 산출물·다음 단계

- [ ] `src/main/antlr` 또는 별도 모듈에 `.g4` 초안 추가 (Phase 2~3).  
- [ ] 통합 테스트: 잘못된 문법·악의적 입력 차단.  
- [ ] `04-API정의서`의 `/search` 스키마와 오류 코드 정합.

---

## 6. 결론

**ANTLR4 + QueryDSL(또는 JPA Criteria) + 화이트리스트** 조합을 기본안으로 채택한다. 프로토타입 단계에서만 단순 파서를 허용하고, Phase 4 착수 전 문법凍結를 권장한다.
