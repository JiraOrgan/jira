# 테스트 및 품질

| 구분 | 명령 | 설명 |
|------|------|------|
| 백엔드 단위·통합 | `./gradlew test` 또는 `npm run test:api` | JUnit, `@SpringBootTest`, MockMvc 등 |
| 웹 | `npm run test:web` | Vitest + Testing Library (`apps/web/src/**/*.test.ts(x)`) |
| 모바일 | `cd apps/mobile && flutter test` | 위젯 테스트 등 |
| 부하(k6) | `npm run test:load:k6` | `scripts/load/k6-api-smoke.js` 스모크 시나리오 |

검증 시나리오·알려진 이슈 요약:

- [FINAL-TEST-SCENARIOS.md](FINAL-TEST-SCENARIOS.md)
- [TEST-ISSUES-REPORT.md](TEST-ISSUES-REPORT.md)

k6 사용법·옵션은 루트 [README.md — 시작하기](../README.md#-시작하기)의 부하 테스트 절을 참고하세요.
