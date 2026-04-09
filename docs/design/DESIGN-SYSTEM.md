# UI 디자인 시스템 v0.1

> **작성일**: 2026-04-09  
> **Task**: T-203  
> **선행**: [WIREFRAME-SPEC.md](../WIREFRAME-SPEC.md)

Figma 컴포넌트 라이브러리 생성 전, **토큰·컴포넌트 인벤토리**를 저장소에 고정한다. 시각 시안은 디자이너 산출물로 갱신한다.

## 1. 디자인 토큰 (초안)

| 토큰 | 용도 | 값 (예시) |
|------|------|-----------|
| `--color-bg` | 앱 배경 | `#F8FAFC` |
| `--color-surface` | 카드·패널 | `#FFFFFF` |
| `--color-primary` | 주 액션 | `#2563EB` |
| `--color-danger` | 삭제·경고 | `#DC2626` |
| `--color-text` | 본문 | `#0F172A` |
| `--color-muted` | 보조 텍스트 | `#64748B` |
| `--radius-sm` | 입력·칩 | `6px` |
| `--radius-md` | 카드 | `10px` |
| `--space-unit` | 4px 그리드 | `4` |
| `--font-sans` | 본문 | 시스템 UI / Inter |

다크 모드는 Phase 2.1에서 토큰 세트 분리.

## 2. 컴포넌트 인벤토리 (웹 React)

| 컴포넌트 | 사용 화면 (SCR) | 비고 |
|----------|-----------------|------|
| AppShell (GNB+Sidebar) | 002~014 | [WIREFRAME-SPEC](../WIREFRAME-SPEC.md) |
| DataTable / VirtualList | 003,008,014 | 정렬·필터 슬롯 |
| KanbanColumn + IssueCard | 004,005 | DnD 라이브러리와 스타일 통일 |
| IssueForm (동적 필드) | 007 | 타입별 필드 세트 |
| IssueDetailPanel | 006 | 탭: 설명·댓글·첨부·히스토리 |
| QueryInput (JQL) | 008 | 자동완성 드롭다운 |
| ChartHost | 002,012 | Recharts 래퍼 |
| Modal / Drawer | 공통 | 확인·폼 |
| Toast / Banner | 공통 | 409·에러 |

## 3. 접근성

- 키보드 DnD 대체(보드).  
- 대비 AA 이상, 포커스 링 visible.

## 4. 모바일 (Flutter)

- Material 3 기준, 브랜드 컬러만 위 토큰과 동기화.  
- 터치 타겟 최소 44dp.
