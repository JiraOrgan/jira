# 환경변수 & 빌드 설정 가이드

---

## 1. 환경변수 파일 구성

```
.env                  # 공통 기본값 (Git 포함 가능)
.env.development      # 개발 환경 (Git 포함 가능)
.env.production       # 운영 환경 (Git 제외 권장)
.env.local            # 로컬 전용 (Git 제외 필수)
.env.example          # 템플릿 (Git 포함)
```

**우선순위**: `.env.local` > `.env.[mode]` > `.env`

---

## 2. 환경변수 파일 예시

### .env.example

```dotenv
# API
VITE_API_BASE_URL=http://localhost:8080/api/v1

# 앱 정보
VITE_APP_NAME=My App
VITE_APP_VERSION=1.0.0

# 소셜 로그인 (선택)
VITE_KAKAO_CLIENT_ID=
VITE_GOOGLE_CLIENT_ID=

# 분석 (선택)
VITE_GA_TRACKING_ID=

# 기능 플래그 (선택)
VITE_FEATURE_PAYMENT=true
VITE_FEATURE_CHAT=false
```

### .env.development

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_ENV=development
VITE_ENABLE_DEVTOOLS=true
VITE_MSW_ENABLED=true
```

### .env.production

```dotenv
VITE_API_BASE_URL=https://api.example.com/api/v1
VITE_APP_ENV=production
VITE_ENABLE_DEVTOOLS=false
VITE_MSW_ENABLED=false
```

---

## 3. 환경변수 타입 안전하게 사용

```typescript
// src/config/env.ts — 타입 검증 포함
function requireEnv(key: string): string {
  const value = import.meta.env[key];
  if (!value) throw new Error(`환경변수 ${key}가 설정되지 않았습니다`);
  return value;
}

export const env = {
  apiBaseUrl:    import.meta.env.VITE_API_BASE_URL as string,
  appName:       import.meta.env.VITE_APP_NAME as string,
  appEnv:        import.meta.env.VITE_APP_ENV as 'development' | 'production',
  isDevelopment: import.meta.env.DEV,
  isProduction:  import.meta.env.PROD,
  isMswEnabled:  import.meta.env.VITE_MSW_ENABLED === 'true',

  // 필수값 (없으면 에러)
  kakaoClientId: requireEnv('VITE_KAKAO_CLIENT_ID'),
} as const;

// vite-env.d.ts — 타입 정의 추가
/// <reference types="vite/client" />
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_APP_NAME: string;
  readonly VITE_APP_ENV: 'development' | 'production';
  readonly VITE_MSW_ENABLED: string;
}
```

---

## 4. vite.config.ts 빌드 최적화

```typescript
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],

    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },

    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL ?? 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },

    build: {
      outDir: 'dist',
      sourcemap: mode !== 'production',  // 운영 환경에서 소스맵 비활성화
      minify: 'esbuild',
      target: 'es2020',

      // 청크 분리 (코드 스플리팅)
      rollupOptions: {
        output: {
          manualChunks: {
            // 벤더 라이브러리 별도 청크
            'react-vendor': ['react', 'react-dom'],
            'router':       ['react-router-dom'],
            'query':        ['@tanstack/react-query'],
            'ui':           ['clsx', 'tailwind-merge'],
          },
        },
      },

      // 청크 크기 경고 기준 (500KB)
      chunkSizeWarningLimit: 500,
    },

    // 테스트 설정 (vitest.config.ts 분리 권장)
    test: {
      globals:     true,
      environment: 'jsdom',
      setupFiles:  ['./src/test/setup.ts'],
    },
  };
});
```

---

## 5. .gitignore 필수 항목

```gitignore
# 환경변수 (민감 정보)
.env.local
.env.*.local
.env.production

# 빌드 결과물
dist/
build/

# 테스트 커버리지
coverage/

# 의존성
node_modules/

# 에디터
.vscode/
!.vscode/settings.json
!.vscode/extensions.json
.idea/

# OS
.DS_Store
Thumbs.db
```

---

## 6. 빌드 & 배포 명령어

```bash
# 개발 서버 실행
npm run dev

# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview

# 타입 체크 (빌드 없이)
npm run type-check

# 린트 검사
npm run lint

# 포맷팅
npm run format
```
