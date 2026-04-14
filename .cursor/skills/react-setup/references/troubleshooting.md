# React/Vite 트러블슈팅 가이드

> 증상 → 원인 → 해결 패턴으로 정리한 실전 문제 해결 참조 문서

---

## 1. Vite 개발 서버 관련

### HMR 작동 안 함

**증상** — 파일을 저장해도 브라우저가 자동으로 갱신되지 않음

**원인** — WSL 또는 Docker 환경에서 inotify 기반 파일 시스템 이벤트가 호스트 OS까지 전달되지 않아 Vite가 변경을 감지하지 못함

**해결** — `vite.config.ts`에 폴링 방식으로 전환

```ts
// vite.config.ts
import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    watch: {
      usePolling: true,   // WSL/Docker 환경에서 필수
      interval: 1000,     // 폴링 간격(ms), 부하와 반응속도 균형 조정
    },
  },
});
```

> 폴링은 CPU 사용량이 증가하므로 개발 환경에서만 적용하고 운영 빌드에는 포함하지 않는다.

---

### 포트 충돌 — EADDRINUSE 에러

**증상** — `Error: listen EADDRINUSE: address already in use :::5173`

**원인** — 이전에 실행된 프로세스가 포트를 점유 중

**해결** — 포트를 변경하거나 기존 프로세스를 종료

```ts
// vite.config.ts — 포트 변경
export default defineConfig({
  server: {
    port: 5174,          // 사용할 포트 지정
    strictPort: true,    // 포트가 사용 중이면 에러 발생(자동 변경 방지)
  },
});
```

```bash
# 기존 프로세스 강제 종료 (Linux/macOS)
lsof -ti:5173 | xargs kill -9

# Windows PowerShell
netstat -ano | findstr :5173
taskkill /PID <PID번호> /F
```

---

## 2. 빌드 에러

### TypeScript 타입 에러로 빌드 실패

**증상** — `vite build` 실행 시 타입 에러로 빌드가 중단됨

**원인** — Vite는 기본적으로 타입 체크를 건너뛰지만 `tsc` 빌드 단계에서 실패할 수 있음

**해결** — 빌드 전 `tsc --noEmit`으로 타입 오류를 사전 확인하고, `strict` 옵션은 점진적으로 활성화

```bash
# 타입 에러만 확인 (파일 생성 없음)
npx tsc --noEmit

# package.json scripts에 등록
# "type-check": "tsc --noEmit",
# "build": "npm run type-check && vite build"
```

```json
// tsconfig.json — strict 옵션 점진적 적용 예시
{
  "compilerOptions": {
    "strict": false,               // 전체 strict 비활성화 후
    "noImplicitAny": true,         // 항목별로 단계적 적용
    "strictNullChecks": true
  }
}
```

---

### Module not found — 경로 alias 빌드 시 인식 안 됨

**증상** — 개발 서버에서는 동작하지만 빌드 시 `Cannot find module '@/components/...'` 에러

**원인** — `tsconfig.json`의 `paths`와 `vite.config.ts`의 `resolve.alias`가 동기화되지 않음

**해결** — 양쪽 설정을 일치시키거나 `vite-tsconfig-paths` 플러그인으로 자동화

```ts
// vite.config.ts — 수동 동기화
import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
    },
  },
});
```

```json
// tsconfig.json — vite alias와 일치시키기
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"]
    }
  }
}
```

```bash
# 플러그인으로 자동화 (tsconfig.json paths를 Vite가 자동 인식)
npm install -D vite-tsconfig-paths
```

```ts
// vite.config.ts — 플러그인 사용 시
import tsconfigPaths from 'vite-tsconfig-paths';

export default defineConfig({
  plugins: [tsconfigPaths()],  // tsconfig paths 자동 적용
});
```

---

### Dynamic import 실패 — 코드 스플리팅 후 청크 로드 에러

**증상** — 빌드 후 특정 페이지 이동 시 `Failed to fetch dynamically imported module` 에러

**원인** — 청크 파일 분할 방식이 기본값과 달라 해시가 변경되거나 경로가 틀어짐

**해결** — `rollupOptions.output.manualChunks`로 청크 분리 전략을 명시적으로 지정

```ts
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // 벤더 라이브러리 별도 청크로 분리
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
          query: ['@tanstack/react-query'],
        },
      },
    },
  },
});
```

---

## 3. React 런타임 에러

### Hydration mismatch (SSR/Next.js)

**증상** — `Hydration failed because the initial UI does not match what was rendered on the server`

**원인** — 서버에서 렌더링한 HTML과 클라이언트에서 React가 생성하는 DOM이 불일치. 주로 `Date.now()`, `Math.random()`, 브라우저 전용 API 사용 시 발생

**해결** — 동적 콘텐츠를 `useEffect`로 이동하거나 `suppressHydrationWarning` 적용

```tsx
// 잘못된 방식 — 서버와 클라이언트 결과가 다름
function Timestamp() {
  return <span>{new Date().toLocaleTimeString()}</span>;
}

// 올바른 방식 — 클라이언트 마운트 후 업데이트
function Timestamp() {
  const [time, setTime] = useState('');

  useEffect(() => {
    setTime(new Date().toLocaleTimeString());  // 클라이언트에서만 실행
  }, []);

  return <span>{time}</span>;
}
```

```tsx
// suppressHydrationWarning — 의도적 불일치 허용 (최후 수단)
<div suppressHydrationWarning>
  {typeof window !== 'undefined' ? window.location.href : ''}
</div>
```

---

### Too many re-renders

**증상** — `Too many re-renders. React limits the number of renders to prevent an infinite loop`

**원인** — 컴포넌트 렌더링 중 `setState`를 직접 호출하여 렌더링 → 상태 변경 → 재렌더링 무한 루프 발생

**해결** — `useEffect` 또는 이벤트 핸들러 내부로 이동

```tsx
// 잘못된 방식 — 렌더링 중 setState 직접 호출
function Counter() {
  const [count, setCount] = useState(0);
  setCount(count + 1);  // 렌더링마다 호출 → 무한 루프
  return <div>{count}</div>;
}

// 올바른 방식 1 — useEffect로 초기값 설정
function Counter() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    setCount(1);  // 마운트 시 한 번만 실행
  }, []);

  return <div>{count}</div>;
}

// 올바른 방식 2 — 이벤트 핸들러로 이동
function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <span>{count}</span>
      <button onClick={() => setCount(c => c + 1)}>증가</button>
    </div>
  );
}
```

---

### Cannot update unmounted component

**증상** — `Warning: Can't perform a React state update on an unmounted component`

**원인** — 비동기 작업(fetch, setTimeout 등)이 완료되기 전에 컴포넌트가 언마운트되어도 완료 후 `setState`를 호출

**해결** — `AbortController`로 요청 취소하거나 cleanup 함수에서 플래그 처리

```tsx
// AbortController를 이용한 fetch 취소
function UserProfile({ userId }: { userId: string }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const controller = new AbortController();  // 취소 컨트롤러 생성

    fetch(`/api/users/${userId}`, { signal: controller.signal })
      .then(res => res.json())
      .then(data => setUser(data))
      .catch(err => {
        if (err.name !== 'AbortError') console.error(err);  // 취소 에러는 무시
      });

    return () => controller.abort();  // 언마운트 시 요청 취소
  }, [userId]);

  return user ? <div>{user.name}</div> : <div>로딩 중...</div>;
}
```

---

## 4. 스타일링 문제

### Tailwind 클래스 적용 안 됨

**증상** — Tailwind 클래스를 사용해도 스타일이 전혀 반영되지 않음

**원인** — `tailwind.config.js`의 `content` 경로가 실제 소스 파일 위치와 불일치하여 Tailwind가 클래스를 퍼지(purge)함

**해결** — `content` 배열에 모든 소스 파일 경로를 정확히 등록

```js
// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',  // src 하위 모든 컴포넌트 파일 포함
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
```

```css
/* src/index.css — Tailwind 디렉티브 필수 */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

---

### CSS Module 클래스명 undefined

**증상** — `styles.container`가 `undefined`로 출력되고 스타일이 적용되지 않음

**원인** — CSS Module 파일명이 `.module.css` 규칙을 따르지 않으면 일반 CSS로 처리됨

**해결** — 파일명을 `*.module.css` 형식으로 변경

```tsx
// 잘못된 파일명: Button.css → 일반 CSS로 처리됨
import styles from './Button.css';         // styles 객체가 비어있음

// 올바른 파일명: Button.module.css
import styles from './Button.module.css';  // 클래스명이 해시값으로 변환됨

function Button() {
  return <button className={styles.container}>클릭</button>;
}
```

```css
/* Button.module.css */
.container {
  padding: 8px 16px;
  border-radius: 4px;
  background-color: #3b82f6;
}
```

---

## 5. API 통신 문제

### CORS 에러

**증상** — `Access to fetch at 'http://localhost:8080/api' from origin 'http://localhost:5173' has been blocked by CORS policy`

**원인** — 브라우저의 Same-Origin Policy에 의해 다른 포트/도메인 요청이 차단됨. 백엔드에 CORS 헤더가 설정되지 않은 경우 발생

**해결** — 개발 환경에서는 Vite 프록시를 사용하고, 운영 환경에서는 백엔드에서 CORS를 허용

```ts
// vite.config.ts — 개발 서버 프록시 설정
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 실제 백엔드 주소
        changeOrigin: true,               // Origin 헤더 변경
        rewrite: (path) => path.replace(/^\/api/, ''),  // /api 접두사 제거
      },
    },
  },
});
```

```ts
// API 호출 시 상대 경로 사용 (프록시 통해 백엔드로 전달됨)
const response = await fetch('/api/users');  // → http://localhost:8080/users
```

---

### 환경변수 undefined

**증상** — `import.meta.env.API_URL`이 `undefined`로 출력됨

**원인** — Vite는 보안상 `VITE_` 접두사가 붙은 환경변수만 클라이언트 번들에 포함시킴

**해결** — `.env` 파일에서 `VITE_` 접두사를 붙여 변수명 변경

```bash
# .env — VITE_ 접두사 필수
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=MyApp

# API_URL=http://localhost:8080  ← 이 형식은 클라이언트에서 접근 불가
```

```ts
// 환경변수 접근
const apiUrl = import.meta.env.VITE_API_URL;  // 정상 동작
const apiUrl2 = import.meta.env.API_URL;       // undefined

// TypeScript 타입 선언 (vite-env.d.ts)
interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_APP_NAME: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

---

## 6. 테스트 에러

### Vitest에서 JSX 인식 안 됨

**증상** — `SyntaxError: Unexpected token '<'` 또는 JSX 관련 변환 에러

**원인** — Vitest 설정에서 jsdom 환경과 globals 옵션이 누락됨

**해결** — `vitest.config.ts`에 환경 및 globals 설정 추가

```ts
// vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,         // describe, it, expect 전역 사용
    environment: 'jsdom',  // 브라우저 DOM 환경 시뮬레이션
    setupFiles: ['./src/test/setup.ts'],
  },
});
```

```ts
// src/test/setup.ts
import '@testing-library/jest-dom';  // toBeInTheDocument 등 매처 추가
```

```bash
# 필요 패키지 설치
npm install -D vitest jsdom @testing-library/react @testing-library/jest-dom
```

---

### RTL findBy/waitFor 타임아웃

**증상** — `TestingLibraryElementError: Unable to find an element` 또는 `Timeout - Async callback was not invoked within the 1000ms timeout`

**원인** — 비동기 상태 업데이트가 완료되기 전에 어서션이 실행되거나, 기본 타임아웃이 너무 짧음

**해결** — `act()`로 상태 업데이트를 감싸거나 `waitFor` 타임아웃을 늘림

```tsx
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// act()로 비동기 상태 업데이트 래핑
test('데이터 로드 후 표시', async () => {
  render(<UserList />);

  await act(async () => {
    // 비동기 작업이 완료될 때까지 대기
  });

  expect(screen.getByText('사용자 목록')).toBeInTheDocument();
});

// waitFor 타임아웃 증가
test('API 응답 대기', async () => {
  render(<UserList />);

  const element = await waitFor(
    () => screen.getByText('홍길동'),
    { timeout: 3000 }  // 기본 1000ms → 3000ms로 증가
  );

  expect(element).toBeInTheDocument();
});

// findBy 사용 (waitFor + getBy 조합)
test('비동기 요소 탐색', async () => {
  render(<UserList />);

  // findBy는 자동으로 요소가 나타날 때까지 대기
  const item = await screen.findByText('홍길동', {}, { timeout: 3000 });
  expect(item).toBeInTheDocument();
});
```

---

## 빠른 체크리스트

| 증상 | 먼저 확인할 것 |
|------|---------------|
| HMR 미작동 | `server.watch.usePolling: true` |
| 포트 충돌 | `lsof -ti:<port>` 또는 포트 변경 |
| 빌드 타입 에러 | `npx tsc --noEmit` |
| alias 인식 안 됨 | tsconfig paths ↔ vite alias 동기화 |
| Tailwind 미적용 | `tailwind.config.js` content 경로 |
| CORS 에러 | Vite proxy 또는 백엔드 CORS 설정 |
| 환경변수 undefined | `VITE_` 접두사 확인 |
| JSX 테스트 실패 | vitest `environment: 'jsdom'` |
