---
name: react-setup
description: >
  React 프로젝트를 새로 만들거나 Vite, Tailwind, 라우팅 등 환경을 구성할 때 사용한다.
  Vite + TypeScript 기준으로 상태관리, API 통신, 스타일링, 테스트, 배포까지 전 단계를 안내하며,
  Next.js(App Router·SSR)도 지원한다. springboot-setup과 달리 프론트엔드 환경 구축에 특화되어 있다.
  키워드: React, Vite, Tailwind, React Router, Zustand, TanStack Query, Next.js,
  ESLint, Prettier, Vitest, Docker, React Hook Form, MSW, 성능 최적화, Code Splitting,
  styled-components, Axios, Yup, Husky, 배포, Vercel, Netlify
---

# React 개발 환경 구축 스킬

**Vite + React 19** 기준으로 프로젝트 초기 설정부터 운영 구성까지 안내합니다.
TypeScript / JavaScript 모두 지원합니다.

> 최종 확인일: 2026-03-19

---

## ⚡ 시작 전 버전 확인 절차

### 상황 1: 새 프로젝트
→ 디폴트 버전으로 진행. 바로 상황별 대응 가이드 참조.

### 상황 2: 기존 프로젝트가 있는 경우
기존 환경에서 추가 설정을 요청하면 먼저 버전을 확인한다.

```bash
node --version       # Node.js 버전
npm --version        # 패키지 매니저 버전
cat package.json     # React, Vite 등 현재 버전 확인
```

버전 또는 언어가 디폴트와 다르면 진행 전에 확인한다:
```
감지된 환경:
  - React: 17.x  (디폴트: 19.x)
  - Node:  18.x  (디폴트: 22 LTS)

어떤 버전/언어 기준으로 진행할까요?
  A) 현재 설치된 버전 유지
  B) 디폴트 최신 버전으로 업그레이드

JavaScript / TypeScript 중 어떤 언어로 설정할까요?
  TS) TypeScript (디폴트 — strict 모드)
  JS) JavaScript only (tsconfig 없음)
```

---

## 디폴트 버전 스택

| 구성요소 | 버전 | 비고 |
|---------|------|------|
| Node.js | 22 LTS | `.nvmrc` 고정 권장 |
| React | 19.x | |
| TypeScript | 5.x | |
| Vite | 6.x | 빌드 도구 |
| React Router | 6.x | |
| TanStack Query | 5.x | (React Query) |
| Axios | 1.x | |
| Zustand | 5.x | 상태관리 |
| Tailwind CSS | 4.x | |
| Vitest | 2.x | 테스트 |
| RTL | 16.x | |

---

## 상황별 대응 가이드

사용자 요청에서 키워드를 감지하면 해당 레퍼런스를 **Read 도구로 로드**한다.

### 📦 패키지 선택 가이드
→ `references/package-guide.md` 로드
> **트리거**: 패키지 추천, 어떤 라이브러리, 비교, 선택 가이드, UI 프레임워크
- Core(Vite/SWC) / Routing(Wouter, TanStack Router) / 상태관리(Jotai, Valtio) 선택 설정
- UI 프레임워크(Mantine, Radix UI) / HTTP(Ky) / Styling(Emotion, UnoCSS)
- Validation(Zod) / 폼(Formik) / 유틸(Day.js, date-fns) / 애니메이션(Framer Motion)
- Dev Tool(Commitlint) 전체 조합 + 프로젝트 규모별 추천 스택

### 🚀 프로젝트 초기 설정
→ `references/project-init.md` 로드
> **트리거**: 새 프로젝트, 프로젝트 생성, Vite 설정, tsconfig, 디렉토리 구조, 절대 경로
- Vite + TypeScript + React 프로젝트 생성
- `tsconfig.json` 기본 설정
- 절대 경로(Path Alias) 설정
- 프로젝트 디렉토리 구조

### 🎨 스타일링 설정
→ `references/styling.md` 로드
- Tailwind CSS 설정 (권장)
- styled-components 설정 (선택)
- CSS Modules 설정 (선택)

### 🔀 라우팅 설정 (React Router v6)
→ `references/routing.md` 로드
- `createBrowserRouter` + `RouterProvider` 패턴
- 레이아웃 라우트, 중첩 라우트
- Protected Route (인증 가드)
- 404 페이지

### 🌐 API 통신 설정
→ `references/api-client.md` 로드
- Axios 인스턴스 + 인터셉터 설정
- TanStack Query(React Query) 설정
- MSW(Mock Service Worker) 개발 환경 설정

### 🗃️ 상태관리 설정
→ `references/state-management.md` 로드
- Zustand (경량, 권장)
- Recoil (atom 기반)
- Redux Toolkit (대규모)
- 선택 가이드


### 📝 폼 & 유효성 검증
→ `references/form-validation.md` 로드
- React Hook Form 설정 & 기본 패턴
- Yup 스키마 유효성 검증
- 에러 메시지 처리 & 커스텀 훅

### 🔧 코드 품질 설정
→ `references/code-quality.md` 로드
- ESLint + Prettier 설정
- TypeScript strict 모드
- Husky + lint-staged (커밋 훅)

### 🧪 테스트 환경 설정
→ `references/testing.md` 로드
- Vitest + React Testing Library
- MSW 연동
- `vitest.config.ts` 설정

### ⚙️ 환경변수 & 빌드 설정
→ `references/env-build.md` 로드
- `.env` 파일 관리
- `vite.config.ts` 최적화
- 개발/운영 환경 분리

### 🔮 Next.js 설정 (App Router)
→ `references/nextjs.md` 로드
> **트리거**: Next.js, SSR, SSG, ISR, App Router, Server Components, 미들웨어
- App Router 기본 구조 (layout/page/loading/error)
- Server Components vs Client Components
- SSR / SSG / ISR 렌더링 전략
- API Routes, 미들웨어, 환경변수

### 🚀 배포 설정
→ `references/deployment.md` 로드
- Vercel (권장), Netlify, GitHub Pages
- Docker + Nginx SPA 배포
- 배포 전 체크리스트

### ⚡ React 19 신기능
→ `references/react19-features.md` 로드
- useActionState, useOptimistic, useFormStatus, use() 훅
- Form Actions 패턴
- React Compiler (자동 메모이제이션)
- React 18 → 19 마이그레이션

### 📈 성능 최적화
→ `references/performance.md` 로드
> **트리거**: 성능, 최적화, memo, useMemo, useCallback, 번들 크기, lazy, Code Splitting, Web Vitals
- React.memo, useMemo, useCallback 사용 기준
- Code Splitting / Lazy Loading
- 번들 분석 (rollup-plugin-visualizer)
- Web Vitals, 이미지 최적화

### 🐛 트러블슈팅
→ `references/troubleshooting.md` 로드
- Vite HMR / 빌드 에러 / 경로 alias 문제
- React 런타임 에러 (Hydration, re-renders)
- CORS / 환경변수 / 테스트 에러

---

## 공통 컨벤션

- **패키지 매니저**: `npm` (기본) 또는 `pnpm` (권장 — 빠름)
- **언어**: TypeScript strict 모드 (기본) / JavaScript 선택 가능
- **컴포넌트**: 함수형 컴포넌트 + 훅
- **파일 네이밍**: 컴포넌트 `PascalCase.tsx`, 훅 `useCamelCase.ts`
- **스타일**: Tailwind CSS (utility-first)
- **상태관리**: 서버 상태 → TanStack Query, 클라이언트 상태 → Zustand

---

## 응답 형식 가이드

- 설정 파일은 **전체 내용** 제공
- 패키지 설치 명령어는 **복사 가능한 코드 블록**으로
- 에러 해결 시 → **원인 → 해결** 순서
- TypeScript 타입은 반드시 포함
