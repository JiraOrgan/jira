# 인프라 설계서 (AWS)

> **작성일**: 2026-04-09  
> **Task**: T-205  
> **기준**: [PRD.md §8.4](../PRD.md) · [NFR-VERIFICATION.md](../NFR-VERIFICATION.md)

## 1. 아키텍처 개요

```
Internet
  → Route 53
  → CloudFront (정적 React/Flutter Web 빌드, WAF 옵션)
  → ALB (HTTPS TLS 1.3)
  → ECS Fargate Service (Spring Boot, min/max Auto Scaling)
  → RDS MySQL 8 또는 PostgreSQL (PRD는 PG, 현재 앱은 MySQL — 운영 전 확정)
  → ElastiCache Redis (세션/Refresh/보드 캐시)
  → S3 (첨부, 버전드)
  → SQS + DLQ (알림·자동화)
  → CloudWatch + Grafana (메트릭·로그)
  → Secrets Manager (DB, JWT, API 키)
```

## 2. 환경 분리

| 환경 | 목적 | 비고 |
|------|------|------|
| dev | 개발자 로컬 / 공유 Dev | Docker Compose 가능 |
| staging | 통합·부하 테스트 | RDS 스냅샷 복원 주기 정의 |
| prod | 고객 | Multi-AZ, 백업 보존기간 명시 |

## 3. 주요 리소스 스펙 (초안)

| 리소스 | 초안 |
|--------|------|
| ECS | Fargate 0.5 vCPU / 1GB 시작, CPU 기반 스케일 |
| RDS | db.t4g.medium 이상, Multi-AZ, 7~35일 백업 |
| Redis | cache.t4g.small, 클러스터 모드는 트래픽 따라 |
| S3 | 버킷 버저닝 + 수명 주기(IA/Glacier) |

## 4. 보안·규정

- NFR-006: ALB에서 TLS 1.3, HSTS.  
- NFR-005~007: 앱 레벨 bcrypt·잠금 (Phase 3).  
- VPC 퍼블릭 서브넷(ALB만) / 프라이빗(ECS, RDS, Redis).  
- IAM 역할 최소 권한, 태스크 역할로 S3·SQS 접근.

## 5. 운영 체크리스트

- [ ] RDS 파라미터 그룹·성능 인사이트  
- [ ] ALB 타겟 헬스 경로 `/actuator/health`  
- [ ] 로그 드라이브: FireLens 또는 CloudWatch Logs  
- [ ] 재해 복구: RTO/RPO 문서화
