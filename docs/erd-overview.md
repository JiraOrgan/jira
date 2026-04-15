# 데이터 모델 개요 (ERD 요약)

주요 엔티티 20개, N:M 중간 테이블 4개로 구성됩니다.

```
PROJECT ──1:N── PROJECT_MEMBER ──N:1── USER_ACCOUNT
   │                                       │
   ├──1:N── SPRINT                         │
   ├──1:N── RELEASE_VERSION                │
   ├──1:N── COMPONENT                      │
   └──1:N── WIP_LIMIT                     │
                                           │
ISSUE ──N:1── PROJECT          ISSUE ──N:1── USER_ACCOUNT (assignee/reporter)
  │                               │
  ├──1:N── COMMENT               ├──N:1── SPRINT
  ├──1:N── ATTACHMENT            ├──N:1── ISSUE (parent, 자기참조)
  ├──1:N── WORKFLOW_TRANSITION   │
  ├──1:N── AUDIT_LOG             ├──N:M── LABEL (via ISSUE_LABEL)
  ├──1:N── ISSUE_LINK            ├──N:M── COMPONENT (via ISSUE_COMPONENT)
  └──1:N── ISSUE_WATCHER         └──N:M── RELEASE_VERSION (via ISSUE_FIX_VERSION)

DASHBOARD ──N:1── USER_ACCOUNT
  └──1:N── DASHBOARD_GADGET
```

상세 ERD·DDL은 [docs/design/](design/) 디렉터리를 참고하세요.
