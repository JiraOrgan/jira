# DoR / DoD 체크리스트 (M1.1)

> **버전**: v1.1  
> **작성일**: 2026-04-09  
> **최종수정일**: 2026-04-14 (WORKFLOW 교차 링크)  
> **Task**: T-107  
> **마일스톤**: M1.1 (2026-04-21 기준 [PHASE.md](PHASE.md))

---

## Definition of Ready (DoR) — 스프린트 백로그 항목

스토리가 스프린트에 **착수 가능**하려면 다음을 만족한다.

| # | 항목 | 설명 |
|---|------|------|
| 1 | FR·스토리 ID | `REQUIREMENTS-v2` 또는 `STORY-MAP`에 추적 가능 |
| 2 | 수용 기준 | Given/When/Then 또는 체크리스트 2개 이상 |
| 3 | 의존성 | 선행 Task·API·디자인 식별, BLOCKED 없음 |
| 4 | 추정 | Story Point 또는 시간 추정 기록 |
| 5 | 디자인 | UI 스토리는 `WIREFRAME-SPEC` 또는 Figma 링크 |
| 6 | 기술 메모 | 스파이크 필요 시 SPIKE 문서 링크 |

---

## Definition of Done (DoD) — 스프린트 완료

증분이 **완료**로 간주되려면 다음을 만족한다.

| # | 항목 | 설명 |
|---|------|------|
| 1 | 코드 병합 | `develop`에 머지, CI 통과 |
| 2 | 테스트 | 신규 로직에 단위 테스트; API는 계약 테스트 또는 수동 시나리오 문서 |
| 3 | 문서 | 공개 API·환경변수 변경 시 README 또는 API 문서 갱신 |
| 4 | 보안 | 시크릿 미커밋; 권한 변경 시 RBAC 표 검토 |
| 5 | 리뷰 | 1인 이상 코드 리뷰(팀 규모에 따라 조정) |
| 6 | 데모 | 스프린트 리뷰에서 시연 가능 상태 |

브랜치 네이밍·PR 템플릿·머지(Squash 등)는 [WORKFLOW.md](WORKFLOW.md) §1·§4를 본다.

---

## Phase 1 산출물 완료 기준 (기획 단계 DoD)

| 산출물 | 문서 | 상태 확인 |
|--------|------|-----------|
| 요구사항 정의서 v2.0 | [REQUIREMENTS-v2.md](REQUIREMENTS-v2.md) | FR 표 완비 |
| NFR 검증 | [NFR-VERIFICATION.md](NFR-VERIFICATION.md) | 갭·액션 명시 |
| 스토리 맵 | [STORY-MAP.md](STORY-MAP.md) | Epic·스토리 매핑 |
| 와이어 사양 | [WIREFRAME-SPEC.md](WIREFRAME-SPEC.md) | 14화면 + 모바일 |
| 백로그 초안 | [SPRINT-BACKLOG-DRAFT.md](SPRINT-BACKLOG-DRAFT.md) | Phase별 묶음 |
| 기술 스파이크 | [spikes/](spikes/) | JQL·워크플로 |

---

## 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| v1.0 | 2026-04-09 | T-107: DoR/DoD·Phase1 산출물 연계 |
| v1.1 | 2026-04-14 | DoD 하단 [WORKFLOW.md](WORKFLOW.md) §1·§4 교차 링크 |
