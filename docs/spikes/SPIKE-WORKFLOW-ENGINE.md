# 기술 스파이크: 워크플로우 엔진 설계

> **작성일**: 2026-04-09  
> **Task**: T-106  
> **관련 FR**: FR-013, FR-014

---

## 1. 목표

- 표준 6단계 상태를 **유한 상태 기계(FSM)**로 모델링한다.  
- PRD §3.3 **조건부 전환**을 코드로 검증 가능하게 한다.  
- 위반 시 `WORKFLOW_VIOLATION`(409)과 명확한 메시지를 반환한다.

---

## 2. 상태·이벤트

**상태** (`IssueStatus`와 1:1)  
`BACKLOG` → `SELECTED_FOR_SPRINT` → `IN_PROGRESS` → `CODE_REVIEW` → `QA` → `DONE`

**전환 컨텍스트** (조건 판단 입력)  
- `prSubmitted`: In Progress → Code Review  
- `reviewApproved` / `changesRequested`: Code Review 분기  
- `qaPassed` / `qaFailed`: QA 분기  
- (선택) `dodChecklistComplete`: QA → Done

컨텍스트는 API 페이로드·연동 웹훅·내부 서비스가 채운다.

---

## 3. 전환 매트릭스 (권장)

| From | To | 필수 조건 |
|------|-----|-----------|
| BACKLOG | SELECTED_FOR_SPRINT | 스프린트 배정 정책 충족(선택 규칙) |
| SELECTED_FOR_SPRINT | IN_PROGRESS | (기본 허용 또는 역할 검사) |
| IN_PROGRESS | CODE_REVIEW | `prSubmitted == true` |
| CODE_REVIEW | IN_PROGRESS | `changesRequested` |
| CODE_REVIEW | QA | `reviewApproved` |
| QA | IN_PROGRESS | `qaFailed` |
| QA | DONE | `qaPassed && dodComplete` |

백로그·스프린트 밖 상태 복귀 등 추가 전환은 프로젝트 설정 테이블로 확장 가능하도록 **전환 규칙을 DB 또는 YAML**로 외부화할지 여부는 Phase 2에서 결정(초기는 코드 상수 + Enum).

---

## 4. 구현 스케치 (Spring)

```
WorkflowEngine
  TransitionResult validateAndTransition(Issue issue, IssueStatus to, TransitionContext ctx)
    1. if issue.status == to → idempotent success
    2. lookup rule (from, to)
    3. if rule.condition.test(ctx) == false → reject WORKFLOW_VIOLATION
    4. apply: issue.status = to; persist WorkflowTransition; publish domain event
```

- **규칙 표현**: Java `Predicate<TransitionContext>` 맵 또는 Spring `@Component` 목록.  
- **Drools 등 BRMS**: FR-015 자동화와 통합 시 재검토; MVP는 코드 규칙으로 충분.

---

## 5. 감사·동시성

- 모든 성공 전환은 `WorkflowTransition` + (가능 시) `AuditLog`에 기록.  
- 동시 두 전환: 낙관적 락(`@Version`) 또는 상태 조건부 UPDATE.

---

## 6. 결론

**내장 FSM + 명시적 전환 테이블 + TransitionContext**로 Phase 3에서 구현한다. 프로젝트별 커스텀 워크플로는 후속 Phase에서 규칙 외부화를 검토한다.
