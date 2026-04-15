# 코드 품질 설정 가이드

---

## 1. ESLint + Prettier 설정

```bash
npm install -D eslint prettier eslint-config-prettier
npm install -D @typescript-eslint/eslint-plugin @typescript-eslint/parser
npm install -D eslint-plugin-react eslint-plugin-react-hooks
npm install -D eslint-plugin-import eslint-plugin-jsx-a11y
```

### eslint.config.js (Flat Config — ESLint 9.x)

```javascript
import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'coverage'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],
      // TypeScript
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': ['warn', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      }],
      '@typescript-eslint/prefer-nullish-coalescing': 'warn',
      '@typescript-eslint/prefer-optional-chain': 'warn',
      // 일반
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'prefer-const': 'error',
    },
  },
);
```

### .prettierrc

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "bracketSpacing": true,
  "arrowParens": "always",
  "endOfLine": "lf",
  "importOrder": [
    "^react",
    "^react-dom",
    "<THIRD_PARTY_MODULES>",
    "^@/(.*)$",
    "^[./]"
  ],
  "importOrderSeparation": true,
  "importOrderSortSpecifiers": true
}
```

### .prettierignore

```
dist
node_modules
coverage
*.min.js
```

### VS Code 설정 (.vscode/settings.json)

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": "explicit",
    "source.organizeImports": "explicit"
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescriptreact]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

---

## 2. TypeScript Strict 모드 추가 설정

```json
// tsconfig.json — strict 관련 추가 옵션
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noUncheckedIndexedAccess": true,  // 배열 인덱스 접근 시 undefined 포함
    "exactOptionalPropertyTypes": true  // optional 프로퍼티 undefined 명시적 처리
  }
}
```

### 자주 쓰는 타입 패턴

```typescript
// src/types/common.ts

// API 공통 응답
export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

// 페이지네이션
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 컴포넌트 공통 props
export interface BaseProps {
  className?: string;
  children?: React.ReactNode;
}

// 유틸리티 타입
export type Optional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;
export type Nullable<T> = T | null;
export type AsyncState<T> = {
  data: T | null;
  isLoading: boolean;
  error: Error | null;
};
```

---

## 3. Husky + lint-staged (커밋 훅)

```bash
npm install -D husky lint-staged
npx husky init
```

### .husky/pre-commit

```bash
#!/usr/bin/env sh
. "$(dirname -- "$0")/_/husky.sh"
npx lint-staged
```

### package.json에 lint-staged 설정 추가

```json
{
  "lint-staged": {
    "src/**/*.{ts,tsx}": [
      "eslint --fix",
      "prettier --write"
    ],
    "src/**/*.{css,json,md}": [
      "prettier --write"
    ]
  }
}
```

### .husky/commit-msg (커밋 메시지 검사 — 선택)

```bash
#!/usr/bin/env sh
. "$(dirname -- "$0")/_/husky.sh"
npx --no -- commitlint --edit "$1"
```

```bash
# commitlint 설치
npm install -D @commitlint/cli @commitlint/config-conventional
echo "export default { extends: ['@commitlint/config-conventional'] };" > commitlint.config.js
```

---

## 4. 경로 정렬 (import order)

```bash
npm install -D @trivago/prettier-plugin-sort-imports
```

```json
// .prettierrc 에 추가
{
  "plugins": ["@trivago/prettier-plugin-sort-imports"],
  "importOrder": [
    "^react$",
    "^react-dom",
    "^react-router",
    "<THIRD_PARTY_MODULES>",
    "^@/(.*)$",
    "^[./]"
  ],
  "importOrderSeparation": true
}
```
