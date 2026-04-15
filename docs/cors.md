# CORS (브라우저 통신)

- Spring Security에 **`/api/**` CORS**가 등록되어 있으며, 허용 Origin은 설정으로만 지정합니다(와일드카드 Origin 미사용).
- 기본값은 `application.yml`의 `app.security.cors.allowed-origins`이며, **`APP_CORS_ORIGINS` 환경 변수**로 덮어쓸 수 있습니다.
- 로컬 웹(`http://localhost:5173`, `http://127.0.0.1:5173`)이 기본 포함됩니다. Vite 개발 서버는 `apps/web/vite.config.ts`에서 `/api`를 백엔드로 **프록시**하므로, 같은 Origin으로만 호출할 때는 CORS가 개입하지 않을 수 있습니다.

환경 변수 예시는 루트 [README.md](../README.md)의 **환경변수 설정** 절을 참고하세요.
