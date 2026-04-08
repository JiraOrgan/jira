# LearnFlow AI - 개발 환경 구성 가이드

> 로컬 개발 환경 설정 및 실행 방법

---

## 사전 요구사항

| 도구 | 최소 버전 | 확인 명령어 |
|------|----------|------------|
| Java (JDK) | 21+ | `java -version` |
| Docker & Docker Compose | 최신 | `docker compose version` |
| Gradle | 9.x (Wrapper 포함) | `./gradlew --version` |
| pnpm | 8.x+ | `pnpm --version` |
| Flutter | 3.x | `flutter --version` |
| Git | 2.x+ | `git --version` |

---

## 1. 레포지토리 클론

```bash
git clone https://github.com/Project-Control-Hub/phs.git
cd phs
```

---

## 2. 환경변수 설정

```bash
cp .env.example .env
```

`.env` 파일을 편집하여 필요한 값을 설정한다:

```dotenv
# 필수 설정
DB_PASSWORD=root
JWT_SECRET=dev-secret-key-must-be-at-least-32-characters-long
CLAUDE_API_KEY=your-claude-api-key      # AI 기능 사용 시 필수
OPENAI_API_KEY=your-openai-api-key      # Fallback용 (선택)
```

> **주의**: `.env` 파일은 `.gitignore`에 포함되어 있어 Git에 추적되지 않는다.

---

## 3. 인프라 서비스 실행 (Docker Compose)

```bash
docker compose up -d
```

### 서비스 목록

| 서비스 | 포트 | 용도 |
|--------|------|------|
| MySQL | 3306 | 메인 데이터베이스 |
| Redis | 6379 | 캐시, 세션, Rate Limiting, PII 매핑 |
| Kafka + Zookeeper | 9092 | 이벤트 메시징 |
| Debezium | 8083 | Outbox CDC (Phase 2) |
| pgvector (PostgreSQL) | 5433 | RAG 벡터 데이터베이스 |
| Elasticsearch | 9200 | BM25 검색 (Hybrid Search) |
| MinIO | 9000 / 9001 (Console) | 파일 스토리지 (S3 호환) |
| Zipkin | 9411 | 분산 추적 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3001 | 모니터링 대시보드 |

### 서비스 상태 확인

```bash
docker compose ps
```

### 완전 정리 (볼륨 포함)

```bash
docker compose down -v
```

---

## 4. Backend 실행

```bash
cd learnflow-api

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 확인

- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Flyway 마이그레이션

- 마이그레이션 파일: `src/main/resources/db/migration/V{번호}__{설명}.sql`
- `bootRun` 시 자동 실행
- **기존 마이그레이션 파일 수정 금지** — 새 버전 파일만 추가

---

## 5. Web (React) 실행

```bash
cd learnflow-web

# 의존성 설치
pnpm install

# 개발 서버
pnpm dev
```

- 개발 서버: http://localhost:3000

### 프로덕션 빌드

```bash
pnpm build
```

### 테스트

```bash
pnpm test
```

---

## 6. Mobile (Flutter) 실행

```bash
cd learnflow-mobile

# 의존성 설치
flutter pub get

# 실행
flutter run
```

### 테스트

```bash
flutter test
```

---

## 7. 테스트

### Backend 테스트

```bash
cd learnflow-api

# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "com.learnflow.domain.user.UserServiceTest"
```

### 테스트 종류

| 어노테이션 | 용도 | 범위 |
|-----------|------|------|
| `@WebMvcTest` | 컨트롤러 테스트 | 슬라이스 |
| `@DataJpaTest` | 리포지토리 테스트 | 슬라이스 |
| `@SpringBootTest` | 통합 테스트 | 전체 컨텍스트 |

---

## 8. 주요 외부 서비스 접속

| 서비스 | URL | 용도 |
|--------|-----|------|
| Swagger UI | http://localhost:8080/swagger-ui.html | API 문서 |
| Grafana | http://localhost:3001 | 모니터링 (admin/admin) |
| MinIO Console | http://localhost:9001 | 파일 스토리지 관리 |
| Zipkin | http://localhost:9411 | 분산 추적 |
| Prometheus | http://localhost:9090 | 메트릭 |
| Elasticsearch | http://localhost:9200 | 검색 엔진 |

---

## 트러블슈팅

### Docker Compose 포트 충돌

이미 사용 중인 포트가 있으면 `.env`에서 포트를 변경하거나 충돌 프로세스를 종료한다:

```bash
# 포트 사용 확인 (예: 3306)
lsof -i :3306   # macOS/Linux
netstat -ano | findstr :3306   # Windows
```

### Flyway 마이그레이션 실패

```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 실패한 마이그레이션 복구 (주의: 개발 환경에서만)
./gradlew flywayRepair
```

### Kafka 연결 실패

Kafka가 완전히 시작되기 전에 애플리케이션이 연결을 시도할 수 있다. Docker Compose에서 Kafka가 정상 작동하는지 확인:

```bash
docker compose logs kafka
```
