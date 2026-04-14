# Next.js 설정 가이드 (App Router)

> Next.js 14.x + TypeScript 5.x + App Router 기준

---

## 1. 프로젝트 생성

```bash
npx create-next-app@latest my-app
# ✔ TypeScript? Yes  ✔ ESLint? Yes  ✔ Tailwind CSS? Yes
# ✔ src/ directory? No  ✔ App Router? Yes  ✔ import alias (@/*)? Yes
cd my-app && npm install
```

### 디렉토리 구조

```
my-app/
├── app/
│   ├── (auth)/            # Route Group — URL에 영향 없이 레이아웃 분리
│   │   ├── login/page.tsx
│   │   └── signup/page.tsx
│   ├── (dashboard)/       # Route Group — 대시보드 레이아웃 공유
│   │   ├── layout.tsx
│   │   ├── users/
│   │   │   ├── page.tsx
│   │   │   └── [id]/page.tsx
│   │   └── settings/page.tsx
│   ├── api/users/route.ts # Route Handlers
│   ├── layout.tsx         # 루트 레이아웃
│   ├── page.tsx
│   ├── loading.tsx        # Suspense 로딩 UI
│   ├── error.tsx          # 에러 바운더리
│   └── not-found.tsx      # 404
├── components/
├── lib/                   # 유틸리티, DB 클라이언트
├── types/
└── middleware.ts          # 미들웨어 (루트에 위치)
```

---

## 2. App Router 기본 구조

### 루트 레이아웃 & 특수 파일

```tsx
// app/layout.tsx — 루트 레이아웃 (모든 페이지 적용)
import type { Metadata } from 'next';
import { Inter } from 'next/font/google';

const inter = Inter({ subsets: ['latin'] });
export const metadata: Metadata = { title: 'My App', description: '...' };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return <html lang="ko"><body className={inter.className}>{children}</body></html>;
}
```

```tsx
// app/loading.tsx — Suspense 기반 로딩 UI (자동 적용)
export default function Loading() {
  return <div className="flex justify-center p-8">로딩 중...</div>;
}

// app/error.tsx — 에러 바운더리 ('use client' 필수)
'use client';
export default function Error({ error, reset }: { error: Error; reset: () => void }) {
  return <div><h2>오류가 발생했습니다.</h2><button onClick={reset}>다시 시도</button></div>;
}
```

### 중첩 레이아웃 & Dynamic Routes

```tsx
// app/(dashboard)/layout.tsx — Route Group 레이아웃 (URL에 (dashboard) 미포함)
export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="flex"><Sidebar /><main className="flex-1 p-6">{children}</main></div>
    </div>
  );
}
```

```tsx
// app/(dashboard)/users/[id]/page.tsx — 동적 라우트  ([...slug] = Catch-all)
interface PageProps { params: Promise<{ id: string }> }

export default async function UserPage({ params }: PageProps) {
  const { id } = await params;   // Next.js 15: params는 Promise
  const user = await getUser(Number(id));
  return <div>{user.name}</div>;
}
```

---

## 3. Server Components vs Client Components

App Router의 모든 컴포넌트는 기본적으로 Server Component입니다. 서버에서 렌더링되어 번들 크기를 줄이고 DB에 직접 접근할 수 있습니다.

```
Server Component (기본값)          │  Client Component ("use client")
───────────────────────────────────┼──────────────────────────────────
DB / 파일 시스템 직접 접근          │  useState, useEffect 등 훅 사용
민감한 API 키 (서버에만 노출)       │  이벤트 핸들러 (onClick, onChange)
대형 의존성 (클라이언트 번들 제외)  │  브라우저 전용 API (localStorage)
```

```tsx
// app/(dashboard)/users/page.tsx — async Server Component로 데이터 페칭
import { getUsers } from '@/lib/users';
import LikeButton from '@/components/ui/LikeButton'; // Client Component 포함 가능

export default async function UsersPage() {
  const users = await getUsers();  // useEffect 없이 직접 fetch
  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.name}<LikeButton /></li>
      ))}
    </ul>
  );
}
```

### Server Actions (form 처리)

```tsx
// app/(dashboard)/users/new/page.tsx
export default function NewUserPage() {
  async function createUserAction(formData: FormData) {
    'use server';                           // Server Action 선언
    await createUser({
      name:  formData.get('name')  as string,
      email: formData.get('email') as string,
    });
    redirect('/users');                     // 서버에서 직접 리다이렉트
  }

  return (
    <form action={createUserAction}>
      <input name="name" placeholder="이름" required />
      <input name="email" type="email" placeholder="이메일" required />
      <button type="submit">생성</button>
    </form>
  );
}
```

---

## 4. SSR / SSG / ISR

```typescript
// SSG — 빌드 시 정적 페이지 생성
export async function generateStaticParams() {
  const users = await getUsers();
  return users.map(user => ({ id: String(user.id) }));
}

export const revalidate = 60;         // ISR — 60초마다 백그라운드 재생성
export const dynamic = 'force-dynamic'; // SSR — 요청마다 서버에서 렌더링

// fetch 레벨 캐시 제어
const data = await fetch('/api/users', {
  cache: 'no-store',           // SSR: 항상 최신
  // next: { revalidate: 60 } // ISR
});

// 수동 캐시 무효화 (Server Action / Route Handler)
import { revalidatePath, revalidateTag } from 'next/cache';
revalidatePath('/users');    // 경로 캐시 무효화
revalidateTag('users');      // fetch 태그 기반 무효화
```

### 렌더링 방식 선택 기준

```
조건                                  │  권장 방식
──────────────────────────────────────┼─────────────────────────────
변경이 없는 콘텐츠 (약관, 문서)        │  SSG (generateStaticParams)
주기적으로 바뀌는 데이터 (뉴스, 상품)  │  ISR (revalidate = N)
사용자별 개인화 데이터                 │  SSR (dynamic = force-dynamic)
실시간 데이터 (채팅, 주식)             │  CSR (Client Component + SWR)
```

---

## 5. 미들웨어

```typescript
// middleware.ts — 프로젝트 루트에 위치
import { NextRequest, NextResponse } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const token = request.cookies.get('accessToken')?.value;
  const isAuthRoute = pathname.startsWith('/login') || pathname.startsWith('/signup');

  // 미인증 → 로그인 페이지로 (원래 경로 기억)
  if (!token && !isAuthRoute) {
    const url = new URL('/login', request.url);
    url.searchParams.set('callbackUrl', pathname);
    return NextResponse.redirect(url);
  }

  // 이미 로그인 → 홈으로
  if (token && isAuthRoute) return NextResponse.redirect(new URL('/', request.url));

  // 리라이트: /old-users → /users
  if (pathname.startsWith('/old-users'))
    return NextResponse.rewrite(new URL('/users', request.url));

  return NextResponse.next();
}

// 미들웨어 적용 경로 (정적 파일 제외)
export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'],
};
```

---

## 6. API Routes (Route Handlers)

```typescript
// app/api/users/route.ts
import { NextRequest, NextResponse } from 'next/server';

// GET /api/users?page=0&size=10
export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl;
  const page = Number(searchParams.get('page') ?? '0');
  try {
    return NextResponse.json({ success: true, data: await getUsers({ page }) });
  } catch {
    return NextResponse.json({ success: false, message: '조회 실패' }, { status: 500 });
  }
}

// POST /api/users
export async function POST(request: NextRequest) {
  const body = await request.json() as { name: string; email: string };
  return NextResponse.json({ success: true, data: await createUser(body) }, { status: 201 });
}
```

```typescript
// app/api/users/[id]/route.ts — 동적 Route Handler (GET / PUT / DELETE)
import { NextRequest, NextResponse } from 'next/server';

interface RouteContext { params: Promise<{ id: string }> }

export async function GET(_req: NextRequest, { params }: RouteContext) {
  const { id } = await params;
  const user = await getUserById(Number(id));
  if (!user) return NextResponse.json({ message: '없음' }, { status: 404 });
  return NextResponse.json({ success: true, data: user });
}

export async function PUT(req: NextRequest, { params }: RouteContext) {
  const { id } = await params;
  return NextResponse.json({ success: true, data: await updateUser(Number(id), await req.json()) });
}

export async function DELETE(_req: NextRequest, { params }: RouteContext) {
  await deleteUser(Number((await params).id));
  return new NextResponse(null, { status: 204 });
}
```

---

## 7. 환경변수 & 설정

```bash
# .env.local — 로컬 개발 (git 제외)
DATABASE_URL=postgresql://localhost:5432/mydb   # 서버 전용 (비공개)
JWT_SECRET=your-secret-key                      # 서버 전용 (비공개)
NEXT_PUBLIC_API_URL=https://api.example.com     # 클라이언트 번들에 포함
```

```
NEXT_PUBLIC_ 없음  →  서버에서만 접근 가능 (process.env.DATABASE_URL)
NEXT_PUBLIC_ 있음  →  클라이언트 번들에 포함 (process.env.NEXT_PUBLIC_API_URL)
```

```typescript
// next.config.ts — 주요 옵션
import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: 'cdn.example.com' },
    ],
  },

  async redirects() {
    return [{ source: '/old-path', destination: '/new-path', permanent: true }];
  },

  async headers() {
    return [{
      source: '/(.*)',
      headers: [
        { key: 'X-Frame-Options',       value: 'DENY' },
        { key: 'X-Content-Type-Options', value: 'nosniff' },
      ],
    }];
  },
};

export default nextConfig;
```
