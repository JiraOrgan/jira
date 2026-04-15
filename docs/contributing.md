# 기여 가이드

## 브랜치 전략

```
master           ← 운영 배포 (직접 push 금지)
  └── develop    ← 개발 통합 (기본 PR 타깃)
       ├── feature/{주제}       ← 기능·품질 묶음
       ├── feat/T-{id}-{설명}   ← 기능 개발
       ├── fix/T-{id}-{설명}    ← 버그 수정
       └── docs/{설명}          ← 문서 작업
```

기능·품질 작업은 `feature/*` 브랜치에서 진행한 뒤 `develop`으로 PR·머지합니다.

## 커밋 컨벤션

```
feat(issue): 이슈 CRUD API 구현
fix(auth):   토큰 갱신 실패 수정
docs:        PRD 문서 작성
refactor:    서비스 로직 분리
test:        회원가입 단위 테스트
chore:       build.gradle 의존성 업데이트
```

## PR 규칙

1. `develop` 브랜치로 PR 생성
2. 제목: `[T-{id}] {요약}` 형식
3. 리뷰어 1명 이상 승인 후 Squash Merge
4. CI (빌드 + 테스트) 통과 필수
5. 스프린트 단위 DoR/DoD는 [DOR-DOD.md](DOR-DOD.md)를 본다 (Sprint 맥락은 [WORKFLOW.md](WORKFLOW.md) §6)

## Cursor 에이전트 스킬

Flutter·React 환경 작업 시 참고용 스킬이 `.cursor/skills/`에 있습니다 (`flutter-setup`, `react-setup`).

자세한 Git·스프린트·CI 흐름은 [WORKFLOW.md](WORKFLOW.md)를 참고하세요.
