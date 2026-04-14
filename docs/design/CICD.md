# CI/CD 파이프라인 설계

> **작성일**: 2026-04-09  
> **Task**: T-206  
> **선행**: [INFRA-AWS.md](INFRA-AWS.md)

## 1. 목표

- `develop` 머지 시 **빌드·테스트·이미지 푸시** 자동화.  
- `main`(또는 `release/*`)에서 **스테이징/프로덕션** 배포 게이트.  
- 마이그레이션(DDL/Flyway)은 **배포 단계에서 분리 실행** 또는 init 컨테이너.

## 2. GitHub Actions (초안)

구현 워크플로: 저장소 루트 [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) — `develop`/`main` **push·PR** 시 백엔드 `./gradlew test`(Redis)·`apps/web` **lint+빌드**(`npm run lint:web`·`build:web`)·`apps/mobile` **Flutter analyze**(stable 채널).

### 워크플로 `ci-backend.yml` (develop, PR)

| 단계 | 내용 |
|------|------|
| checkout | actions/checkout |
| JDK 21 | actions/setup-java (Temurin) |
| cache | Gradle |
| build | `./gradlew test bootJar` |
| artifact | JAR 또는 Docker build |

### 워크플로 `deploy-ecs.yml` (태그 또는 수동)

| 단계 | 내용 |
|------|------|
| build | Docker Buildx, ECR push |
| migrate | (옵션) Flyway job / one-shot task |
| deploy | ECS 서비스 새 태스크 정의 revision |
| verify | 스모크 `/actuator/health` |

## 3. 브랜치 전략 연계

- [WORKFLOW.md](../WORKFLOW.md) — `feature/*` → PR → `develop` → 릴리즈 브랜치 → `main`.  
- 프로덕션 배포는 **승인자 1인 이상** (GitHub Environments `production`).

## 4. 비밀·설정

- `AWS_ROLE_ARN`, `ECR_REPOSITORY`, `ECS_CLUSTER`, `ECS_SERVICE`.  
- 런타임 시크릿은 AWS Secrets Manager에서 ECS 태스크 주입.

## 5. 향후 과제

- [x] 프론트(Vite) 프로덕션 빌드·Flutter 정적 분석(`analyze`) — `ci.yml`의 `web`·`mobile-analyze` 잡.  
- [ ] Flutter `build apk`/iOS·웹 배포 아티팩트.  
- [ ] SonarQube 또는 CodeQL 정적 분석.  
- [ ] 스테이징 자동 배포 on `develop`.
