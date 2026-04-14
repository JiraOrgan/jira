# 라우팅 설정 가이드 (React Router v6)

---

## 설치

```bash
npm install react-router-dom
```

---

## 1. createBrowserRouter 설정 (권장 — v6.4+)

```tsx
// src/router/index.tsx
import { createBrowserRouter } from 'react-router-dom';
import Layout from '@/components/layout/Layout';
import ProtectedRoute from './ProtectedRoute';

// 페이지 레이지 로딩 (코드 스플리팅)
const HomePage     = lazy(() => import('@/pages/home/HomePage'));
const LoginPage    = lazy(() => import('@/pages/auth/LoginPage'));
const SignupPage   = lazy(() => import('@/pages/auth/SignupPage'));
const UserPage     = lazy(() => import('@/pages/user/UserPage'));
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'));

const router = createBrowserRouter([
  // ── 공개 라우트 ──────────────────────────
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/signup',
    element: <SignupPage />,
  },

  // ── 인증 필요 라우트 (공통 레이아웃 포함) ─
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <Layout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,                    // path='/' 의 기본 자식
        element: <HomePage />,
      },
      {
        path: 'users/:id',
        element: <UserPage />,
      },
      {
        path: 'users',
        element: <UserListPage />,
      },
    ],
  },

  // ── 404 ─────────────────────────────────
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);

export default router;
```

---

## 2. 레이아웃 컴포넌트

```tsx
// src/components/layout/Layout.tsx
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="flex">
        <Sidebar />
        <main className="flex-1 p-6">
          {/* 자식 라우트가 여기에 렌더링됨 */}
          <Suspense fallback={<PageLoader />}>
            <Outlet />
          </Suspense>
        </main>
      </div>
    </div>
  );
}
```

---

## 3. Protected Route (인증 가드)

```tsx
// src/router/ProtectedRoute.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: 'USER' | 'ADMIN';
}

export default function ProtectedRoute({
  children,
  requiredRole,
}: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  // 미인증 → 로그인 페이지로 (현재 경로 기억)
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 권한 부족 → 접근 거부 페이지
  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}
```

---

## 4. 네비게이션 & 유틸 훅

```tsx
// 훅 사용 패턴
import {
  useNavigate,
  useParams,
  useSearchParams,
  useLocation,
  Link,
  NavLink,
} from 'react-router-dom';

// 프로그래밍 방식 이동
const navigate = useNavigate();
navigate('/users');
navigate('/users/1');
navigate(-1);                              // 뒤로
navigate('/login', { replace: true });    // 히스토리 교체

// 로그인 후 원래 페이지로 돌아가기
const location = useLocation();
const from = (location.state as any)?.from?.pathname ?? '/';
navigate(from, { replace: true });

// URL 파라미터
const { id } = useParams<{ id: string }>();

// 쿼리스트링
const [searchParams, setSearchParams] = useSearchParams();
const page = Number(searchParams.get('page') ?? '0');
const keyword = searchParams.get('keyword') ?? '';

// 쿼리스트링 업데이트
setSearchParams({ page: String(page + 1), keyword });
```

---

## 5. 커스텀 라우터 훅 모음

```typescript
// src/hooks/useQueryParams.ts — 쿼리스트링 타입 안전 파싱
import { useSearchParams } from 'react-router-dom';

interface PaginationParams {
  page: number;
  size: number;
  keyword: string;
}

export function useQueryParams(): [
  PaginationParams,
  (params: Partial<PaginationParams>) => void
] {
  const [searchParams, setSearchParams] = useSearchParams();

  const params: PaginationParams = {
    page:    Number(searchParams.get('page') ?? '0'),
    size:    Number(searchParams.get('size') ?? '10'),
    keyword: searchParams.get('keyword') ?? '',
  };

  const updateParams = (newParams: Partial<PaginationParams>) => {
    const merged = { ...params, ...newParams };
    setSearchParams({
      page:    String(merged.page),
      size:    String(merged.size),
      keyword: merged.keyword,
    });
  };

  return [params, updateParams];
}
```

---

## 트러블슈팅

### ❌ 새로고침 시 404 (Vite 개발 서버)
```typescript
// vite.config.ts
export default defineConfig({
  server: {
    historyApiFallback: true,   // SPA 라우팅 지원
  },
});
```

### ❌ 배포 후 새로고침 시 404 (Nginx)
```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```
