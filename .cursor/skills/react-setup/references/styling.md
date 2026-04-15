# 스타일링 설정 가이드

---

## 1. Tailwind CSS (권장)

> **Tailwind CSS v4** (2025년 1월 출시)가 최신 버전입니다. 새 프로젝트는 v4 권장.
> 기존 v3 프로젝트는 아래 v3 섹션을 참조하세요.

### Tailwind CSS v4 설정 (권장 — 신규 프로젝트)

```bash
npm install tailwindcss @tailwindcss/vite
```

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
});
```

```css
/* src/index.css — CSS-first 설정 (tailwind.config.js 불필요) */
@import "tailwindcss";

/* 커스텀 테마 — CSS @theme 블록으로 직접 정의 */
@theme {
  --color-primary-50: #eff6ff;
  --color-primary-100: #dbeafe;
  --color-primary-500: #3b82f6;
  --color-primary-600: #2563eb;
  --color-primary-700: #1d4ed8;
  --color-primary-900: #1e3a8a;

  --font-sans: 'Pretendard', sans-serif;

  --breakpoint-xs: 480px;
}

/* 기본 스타일 */
@layer base {
  * { @apply box-border; }
  body { @apply font-sans text-gray-900 bg-white; }
}

/* 컴포넌트 스타일 */
@layer components {
  .btn-primary {
    @apply px-4 py-2 bg-primary-600 text-white rounded-lg
           hover:bg-primary-700 active:bg-primary-800
           disabled:opacity-50 disabled:cursor-not-allowed
           transition-colors duration-200;
  }
  .input-base {
    @apply w-full px-3 py-2 border border-gray-300 rounded-lg
           focus:outline-none focus:ring-2 focus:ring-primary-500
           placeholder:text-gray-400;
  }
}
```

**v3 → v4 주요 변경점:**

| v3 | v4 | 설명 |
|----|-----|------|
| `tailwind.config.js` | CSS `@theme` 블록 | 설정을 CSS로 직접 관리 |
| `@tailwind base/components/utilities` | `@import "tailwindcss"` | 단일 import로 간소화 |
| PostCSS 플러그인 | Vite 플러그인 (`@tailwindcss/vite`) | 빌드 통합 방식 변경 |
| `bg-opacity-50` | `bg-black/50` | 슬래시 표기법으로 통일 |
| `content: ['./src/**/*.tsx']` | 자동 감지 | content 설정 불필요 |

---

### Tailwind CSS v3 설정 (기존 프로젝트)

#### 설치 & 설정

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

### tailwind.config.js

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      // 커스텀 색상 (디자인 시스템 기반)
      colors: {
        primary: {
          50:  '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          900: '#1e3a8a',
        },
        gray: {
          50:  '#f9fafb',
          100: '#f3f4f6',
          500: '#6b7280',
          900: '#111827',
        },
      },
      // 커스텀 폰트
      fontFamily: {
        sans: ['Pretendard', 'sans-serif'],
      },
      // 커스텀 반응형 브레이크포인트
      screens: {
        xs: '480px',
        sm: '640px',
        md: '768px',
        lg: '1024px',
        xl: '1280px',
      },
    },
  },
  plugins: [],
};
```

### src/index.css

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 커스텀 기본 스타일 */
@layer base {
  * {
    @apply box-border;
  }
  body {
    @apply font-sans text-gray-900 bg-white;
  }
}

/* 자주 쓰는 컴포넌트 스타일 */
@layer components {
  .btn-primary {
    @apply px-4 py-2 bg-primary-600 text-white rounded-lg
           hover:bg-primary-700 active:bg-primary-800
           disabled:opacity-50 disabled:cursor-not-allowed
           transition-colors duration-200;
  }
  .input-base {
    @apply w-full px-3 py-2 border border-gray-300 rounded-lg
           focus:outline-none focus:ring-2 focus:ring-primary-500
           placeholder:text-gray-400;
  }
  .card {
    @apply bg-white rounded-xl shadow-sm border border-gray-100 p-6;
  }
}
```

### clsx + tailwind-merge (Tailwind 클래스 충돌 방지)

```bash
npm install clsx tailwind-merge
```

```typescript
// src/utils/cn.ts — 공통 유틸
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 사용 예시
<button className={cn(
  'px-4 py-2 rounded-lg',
  isActive ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700',
  disabled && 'opacity-50 cursor-not-allowed',
  className  // 외부에서 전달된 클래스 (충돌 자동 해결)
)}>
```

---

## 2. styled-components (선택)

```bash
npm install styled-components
npm install -D @types/styled-components babel-plugin-styled-components
```

### vite.config.ts에 babel 플러그인 추가

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: ['babel-plugin-styled-components'],
      },
    }),
  ],
});
```

### 테마 설정

```typescript
// src/styles/theme.ts
export const theme = {
  colors: {
    primary:   '#3b82f6',
    secondary: '#10b981',
    danger:    '#ef4444',
    text: {
      primary:   '#111827',
      secondary: '#6b7280',
    },
    bg: {
      primary:   '#ffffff',
      secondary: '#f9fafb',
    },
  },
  spacing: {
    xs: '0.25rem',
    sm: '0.5rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '2rem',
  },
  borderRadius: {
    sm: '0.25rem',
    md: '0.5rem',
    lg: '0.75rem',
    full: '9999px',
  },
} as const;

export type Theme = typeof theme;

// main.tsx
import { ThemeProvider } from 'styled-components';
<ThemeProvider theme={theme}>
  <App />
</ThemeProvider>
```

---

## 3. CSS Modules (선택)

Vite에서 기본 지원 — 별도 설치 불필요.

```tsx
// Button.module.css
.button {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  cursor: pointer;
}
.primary {
  background-color: #3b82f6;
  color: white;
}

// Button.tsx
import styles from './Button.module.css';
import { clsx } from 'clsx';

<button className={clsx(styles.button, styles.primary)}>
  클릭
</button>
```

---

## 반응형 디자인 유틸 훅

```typescript
// src/hooks/useMediaQuery.ts
import { useState, useEffect } from 'react';

export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(
    () => window.matchMedia(query).matches
  );

  useEffect(() => {
    const mq = window.matchMedia(query);
    const handler = (e: MediaQueryListEvent) => setMatches(e.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, [query]);

  return matches;
}

// 사용 예시
const isMobile = useMediaQuery('(max-width: 768px)');
```
