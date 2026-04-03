# LearnFlow AI - Task Workflow 가이드

> **버전**: v4.0
> **작성일**: 2026-04-03
> **연결 문서**: [TASKS.md](../TASKS.md) | [PHASE.md](../PHASE.md) | [WORKFLOW.md](../WORKFLOW.md) | [PRD.md](../PRD.md)

---

## 개요

이 문서는 [TASKS.md](../TASKS.md)에 정의된 116개 Task 각각의 **세부 구현 워크플로우**를 Phase별로 정리한다.
각 Task는 서브 스텝(파일 경로, 클래스명, 핵심 로직), 완료 기준(체크박스), 규칙 체크리스트를 포함한다.

---

## Phase별 문서

| Phase | 문서 | 기간 | Task 수 | 범위 |
|-------|------|------|---------|------|
| Phase 1 | [phase-1.md](phase-1.md) | Week 1~4 | 21개 (T-101 ~ T-121) | 기반 구축: DB, 엔티티, JWT, 강의 CRUD, React UI |
| Phase 2 | [phase-2.md](phase-2.md) | Week 5~8 | 14개 (T-201 ~ T-214) | 핵심 학습: 진도 추적, 퀴즈/과제, 온보딩, 커뮤니티 |
| Phase 3 | [phase-3.md](phase-3.md) | Week 9~12 | 20개 (T-301 ~ T-320) | 이벤트 인프라 + AI 기반: Outbox, AI Gateway, PII, OTel, FinOps |
| Phase 4 | [phase-4.md](phase-4.md) | Week 13~16 | 24개 (T-401 ~ T-424) | RAG + AI 튜터: Chunking, Hybrid Search, SSE, AI 채점 |
| Phase 5 | [phase-5.md](phase-5.md) | Week 17~19 | 15개 (T-501 ~ T-515) | 분석 + 품질 관리: 학습 분석, RAGAS, DeepEval, A/B 테스트 |
| Phase 6 | [phase-6.md](phase-6.md) | Week 20~24 | 22개 (T-601 ~ T-622) | 고도화 및 완성: Flutter, 알림, Grafana, Chaos Test, 배포 |

---

## 문서 사용법

### Task 워크플로우 구조

각 Task는 다음 구조를 따른다:

```markdown
### T-{ID}: {Task 제목}

> **담당**: {담당} | **선행**: {선행 Task} | **관련 FR**: {FR 코드}

#### 서브 스텝

1. **스텝 제목**
   - 파일: `{패키지 경로}/{파일명}.java`
   - 핵심 로직 설명

#### 완료 기준

- [ ] 체크박스 형태의 검증 항목

#### 규칙 체크리스트

- [ ] 도메인별 아키텍처 규칙 준수 항목
```

### 진행 관리

1. Task 착수 시 → TASKS.md에서 상태를 `IN_PROGRESS`로 변경
2. 서브 스텝 완료 시 → 해당 워크플로우의 체크박스 체크
3. 모든 완료 기준 충족 시 → TASKS.md에서 상태를 `DONE`으로 변경

---

## 도메인별 규칙 체크리스트 매핑

CLAUDE.md의 아키텍처 원칙에 따라, 각 Task에는 해당 도메인의 규칙 체크리스트가 자동 부착된다.

### 공통 (모든 Backend Task)

- [ ] DTO는 Java record 사용
- [ ] 엔티티에 `@Setter` 금지 — 비즈니스 메서드로 상태 변경
- [ ] `ApiResponse<T>` 래핑 응답 사용
- [ ] `GlobalExceptionHandler` + `ErrorCode` enum 예외 처리
- [ ] `@Audited` 감사 로그 필요 여부 확인

### AI 관련 (ai/ 패키지 내 Task)

- [ ] AI Gateway 경유 확인 (직접 LLM 호출 금지)
- [ ] PII Masking 파이프라인 통과 확인 (Input + Output)
- [ ] FinOps 비용 기록 (`ai_cost_logs` INSERT, `cache_hit` 여부)
- [ ] Trace ID 전파 확인 (OTel)
- [ ] 에러 시 Fallback 동작 확인 (Circuit Breaker)
- [ ] Semantic Cache 적용 가능 여부 검토
- [ ] 프롬프트 하드코딩 금지 — `ai/prompt/templates/` 관리

### 이벤트/Kafka 관련

- [ ] `OutboxPublisher.publish()` 사용 (KafkaTemplate 직접 호출 금지)
- [ ] 비즈니스 데이터 + `outbox_events` INSERT가 단일 `@Transactional`
- [ ] Consumer 멱등성 보장 (`dedup_key` 체크)
- [ ] DLQ 처리 로직 확인
- [ ] `destination_topic` 누락 없음

### RAG 관련

- [ ] `course_id` 기반 격리 (다른 강의 데이터 노출 금지)
- [ ] `chunk_hash` (SHA-256) 동일 내용 재임베딩 스킵
- [ ] 임베딩은 비동기 (Outbox 경유)

### AI 채점 관련

- [ ] Confidence Score 계산 (rubric_match 0.3 + determinism 0.25 + consistency 0.25 + rubric_coverage 0.2)
- [ ] Confidence >= 0.8 → 자동 확정 (CONFIRMED)
- [ ] Confidence < 0.8 → Manual Review Queue 이관
- [ ] 학습자 이의 제기(Appeal) 플로우 확인

### DB 변경 관련

- [ ] Flyway 마이그레이션 파일 추가 (`V{번호}__{설명}.sql`)
- [ ] 기존 마이그레이션 파일 수정 금지
- [ ] 수동 DDL 금지

### Frontend 관련

- [ ] 서버 상태 = TanStack Query, 클라이언트 상태 = Zustand
- [ ] 폼: React Hook Form + Zod 스키마 검증
- [ ] UI: shadcn/ui + Tailwind 유틸리티 클래스
- [ ] AI 튜터 채팅: SSE 스트리밍

### 보안 7 Layer 검증 (AI 코드)

- [ ] Layer 1: 입력 필터링 (길이 제한 + 위험 패턴 감지)
- [ ] Layer 2: PII Masking (Input + Output 양방향)
- [ ] Layer 3: System Prompt 격리 (노출 금지)
- [ ] Layer 4: Output Validation (점수 범위 / JSON 스키마)
- [ ] Layer 5: 데이터 격리 (`course_id` 기반)
- [ ] Layer 6: Tool 제한 (DB 직접 조회 / 외부 URL / 파일 차단)
- [ ] Layer 7: FinOps Kill-switch

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| v4.0 | 2026-04-03 | 초안 작성: 116개 Task 세부 워크플로우 정의 |
