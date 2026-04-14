# 프로젝트 초기 설정 가이드

> Vite 5.x + React 18 + TypeScript 5.x 기준

---

## 1. 프로젝트 생성

```bash
# npm
npm create vite@latest my-app -- --template react-ts
cd my-app
npm install

# pnpm (권장 — 빠르고 디스크 절약)
pnpm create vite my-app --template react-ts
cd my-app
pnpm install
```

### Node.js 버전 고정 (.nvmrc)

```bash
echo "20" > .nvmrc
# 팀원: nvm use 로 자동 적용
```

---

## 2. 초기 패키지 한 번에 설치

```bash
# 핵심 패키지
npm install react-router-dom axios @tanstack/react-query zustand

# 개발 의존성
npm install -D tailwindcss postcss autoprefixer
npm install -D eslint prettier eslint-config-prettier
npm install -D @typescript-eslint/eslint-plugin @typescript-eslint/parser
npm install -D eslint-plugin-react eslint-plugin-react-hooks
npm install -D vitest @vitest/ui jsdom
npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event
npm install -D msw
```

---

## 3. tsconfig.json

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    /* 번들러 모드 */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",

    /* Strict 모드 */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,

    /* 절대 경로 alias */
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@hooks/*": ["src/hooks/*"],
      "@pages/*": ["src/pages/*"],
      "@api/*": ["src/api/*"],
      "@store/*": ["src/store/*"],
      "@types/*": ["src/types/*"],
      "@utils/*": ["src/utils/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

---

## 4. vite.config.ts (Path Alias 포함)

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@pages': path.resolve(__dirname, './src/pages'),
      '@api': path.resolve(__dirname, './src/api'),
      '@store': path.resolve(__dirname, './src/store'),
      '@types': path.resolve(__dirname, './src/types'),
      '@utils': path.resolve(__dirname, './src/utils'),
    },
  },
  server: {
    port: 3000,
    // Spring Boot API 프록시 (CORS 우회)
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

```bash
# path 모듈 타입 설치
npm install -D @types/node
```

---

## 5. 프로젝트 디렉토리 구조

```
src/
├── api/                   # API 호출 함수
│   ├── client.ts          # Axios 인스턴스
│   ├── auth.ts
│   └── user.ts
├── components/            # 공통 컴포넌트
│   ├── common/            # 버튼, 인풋 등 범용
│   │   ├── Button.tsx
│   │   └── Input.tsx
│   └── layout/            # 레이아웃 컴포넌트
│       ├── Header.tsx
│       ├── Sidebar.tsx
│       └── Layout.tsx
├── hooks/                 # 커스텀 훅
│   ├── useAuth.ts
│   └── useDebounce.ts
├── pages/                 # 페이지 컴포넌트
│   ├── home/
│   │   └── HomePage.tsx
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   └── SignupPage.tsx
│   └── user/
│       └── UserPage.tsx
├── router/                # 라우터 설정
│   ├── index.tsx
│   └── ProtectedRoute.tsx
├── store/                 # 상태관리 (Zustand)
│   └── authStore.ts
├── types/                 # TypeScript 타입 정의
│   ├── auth.ts
│   └── user.ts
├── utils/                 # 유틸리티 함수
│   ├── format.ts
│   └── storage.ts
├── test/                  # 테스트 설정
│   ├── setup.ts
│   └── mocks/
│       ├── server.ts
│       └── handlers.ts
├── App.tsx
└── main.tsx
```

---

## 6. main.tsx 기본 구성

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import router from '@/router';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000,  // 5분
    },
  },
});

// MSW 개발 환경 모킹 (선택)
async function enableMocking() {
  if (import.meta.env.DEV) {
    const { worker } = await import('./test/mocks/browser');
    return worker.start({ onUnhandledRequest: 'bypass' });
  }
}

enableMocking().then(() => {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </React.StrictMode>,
  );
});
```

---

## 7. package.json scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives",
    "lint:fix": "eslint . --ext ts,tsx --fix",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\"",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest run --coverage",
    "type-check": "tsc --noEmit"
  }
}
```

---

## 8. JavaScript Only 설정 (TypeScript 미사용)

TypeScript 없이 순수 JavaScript로 설정할 경우:

```bash
# JS 전용 템플릿으로 생성
npm create vite@latest my-app -- --template react
cd my-app
npm install
```

### jsconfig.json (JS에서 path alias & IDE 지원)

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@hooks/*": ["src/hooks/*"],
      "@pages/*": ["src/pages/*"],
      "@api/*": ["src/api/*"],
      "@store/*": ["src/store/*"],
      "@utils/*": ["src/utils/*"]
    },
    "checkJs": true,
    "jsx": "react-jsx"
  },
  "include": ["src"]
}
```

### vite.config.js (JS용)

```javascript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@pages': path.resolve(__dirname, './src/pages'),
      '@api': path.resolve(__dirname, './src/api'),
      '@store': path.resolve(__dirname, './src/store'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

### ESLint JS 전용 설정

```javascript
// eslint.config.js
import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';

export default [
  { ignores: ['dist', 'node_modules'] },
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parserOptions: {
        ecmaFeatures: { jsx: true },
      },
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': 'warn',
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'prefer-const': 'error',
    },
  },
];
```

### PropTypes (JS에서 타입 검사 대안)

```bash
npm install prop-types
```

```jsx
// UserCard.jsx
import PropTypes from 'prop-types';

function UserCard({ user, onDelete }) {
  return (
    <div>
      <p>{user.name}</p>
      <button onClick={() => onDelete(user.id)}>삭제</button>
    </div>
  );
}

UserCard.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    email: PropTypes.string.isRequired,
  }).isRequired,
  onDelete: PropTypes.func.isRequired,
};

export default UserCard;
```

### JS / TS 혼용 프로젝트 전환 팁

```bash
# 기존 JS 프로젝트에 TypeScript 점진적 도입
npm install -D typescript @types/react @types/react-dom
# tsconfig.json 추가 + allowJs: true 설정
# .jsx → .tsx 파일 하나씩 변환
```
