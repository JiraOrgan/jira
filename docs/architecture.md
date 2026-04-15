# 시스템 아키텍처

브라우저·모바일에서 Spring Boot API로 이어지는 배포 관점의 개략입니다. 로컬 개발·실행 절차는 저장소 루트 [README.md](../README.md)의 **시작하기**를 참고하세요.

```
[Browser / React SPA]     [Flutter Mobile App]
          │                       │
          │    브라우저→API: CORS (허용 Origin은 APP_CORS_ORIGINS·application.yml)
          └───────┬───────────────┘
                  ▼
        [Route 53 → CloudFront / WAF]
                  │
                  ▼
       [ALB (HTTPS, Health Check)]
                  │
      ┌───────────┼───────────┐
      ▼           ▼           ▼
 [ECS Task 1] [ECS Task 2] [ECS Task N]
 Spring Boot   Spring Boot   Auto Scaling
      │           │           │
      ├───────────┼───────────┤
      ▼           ▼           ▼
   [RDS MySQL]  [Redis]    [S3]
   Multi-AZ     Cluster    첨부파일
      │
   [SQS]
   알림/자동화 큐
```

관련: [CORS 설정](cors.md)
