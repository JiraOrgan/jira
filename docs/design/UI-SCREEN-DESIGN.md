# UI 화면 디자인 매핑 (SCR-001~014)

> **작성일**: 2026-04-09  
> **Task**: T-204  
> **와이어 사양**: [WIREFRAME-SPEC.md](../WIREFRAME-SPEC.md)  
> **디자인 시스템**: [DESIGN-SYSTEM.md](DESIGN-SYSTEM.md)

본 문서는 **Figma 시안**이 들어올 때까지의 브리지로, 각 SCR에 필요한 **레이아웃·상태·디자인 산출 체크리스트**를 정의한다.

| SCR | Figma 프레임 이름 (권장) | 필수 상태 Variants | 비고 |
|-----|-------------------------|-------------------|------|
| SCR-001 | Login | default / error / locked | NFR-007 문구 |
| SCR-002 | Dashboard Home | empty / with widgets | 역할별 가젯 자리 |
| SCR-003 | Backlog | loading / dnd | FR-010 |
| SCR-004 | Scrum Board | wip ok / column limit | FR-008 |
| SCR-005 | Kanban | wip exceeded highlight | FR-009 |
| SCR-006 | Issue Detail | edit / readonly / 403 | FR-030 |
| SCR-007 | Create Issue | per IssueType tabs | FR-001~002 |
| SCR-008 | JQL Search | empty / results / saved filter | FR-016 |
| SCR-009 | Roadmap | zoom levels | FR-012 |
| SCR-010 | Sprints | planning / active / closed | FR-011 |
| SCR-011 | Releases | unreleased / released | FR-019 |
| SCR-012 | Reports | burndown / velocity / CFD | FR-022 |
| SCR-013 | Project Settings | members / roles | FR-030 |
| SCR-014 | Audit Log | filter / export | FR-028~029 |

## Figma 링크

- (디자이너 합의 후 URL 추가)

## 검수 기준

- [ ] 모든 SCR에 모바일 브레이크포인트 또는 별도 SCR-MOB 흐름과 대응 관계 명시  
- [ ] 토큰(`DESIGN-SYSTEM`)과 색·간격 불일치 없음  
- [ ] 보드·백로그 DnD 히트 영역 시각적 피드백
