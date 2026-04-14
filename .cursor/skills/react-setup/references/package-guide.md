# React 패키지 선택 가이드

사용자가 패키지를 선택하면 해당 설정 코드를 제공한다.
각 카테고리에서 하나만 선택하거나, 조합해서 사용 가능.

---

## 1️⃣ Core / Runtime

| 패키지 | 설명 | 설치 |
|--------|------|------|
| **React** | UI 컴포넌트 라이브러리 | 기본 포함 |
| **React DOM** | DOM 렌더링 | 기본 포함 |
| **Vite** | 빌드 및 개발 서버 (권장) | `npm create vite@latest` |
| **TypeScript** | 타입 시스템 | `npm i -D typescript` |
| **SWC** | 빠른 JS 컴파일러 (Babel 대체) | `@vitejs/plugin-react-swc` |

### Vite + SWC 설정

```bash
npm create vite@latest my-app -- --template react-swc-ts
```

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';  // Babel 대신 SWC

export default defineConfig({
  plugins: [react()],
});
```

> SWC은 Babel보다 빌드 속도가 ~20배 빠름. 새 프로젝트에 권장.

---

## 2️⃣ Routing

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **React Router v6** | 표준, 풍부한 생태계 | 대부분의 프로젝트 (권장) |
| **Wouter** | 3KB 경량, React Router 호환 API | 번들 크기 민감할 때 |
| **TanStack Router** | 완전한 타입 안전, 파일 기반 라우팅 | TypeScript 프로젝트 |

### Wouter 설정

```bash
npm install wouter
```

```tsx
import { Route, Switch, Link, useLocation, useRoute } from 'wouter';

function App() {
  return (
    <Switch>
      <Route path="/" component={HomePage} />
      <Route path="/users/:id">
        {(params) => <UserPage id={params.id} />}
      </Route>
      <Route>404 Not Found</Route>
    </Switch>
  );
}

// 프로그래밍 방식 이동
const [, setLocation] = useLocation();
setLocation('/users/1');
```

### TanStack Router 설정

```bash
npm install @tanstack/react-router
npm install -D @tanstack/router-devtools @tanstack/router-plugin
```

```typescript
// vite.config.ts — 파일 기반 라우팅 플러그인
import { TanStackRouterVite } from '@tanstack/router-plugin/vite';

export default defineConfig({
  plugins: [TanStackRouterVite(), react()],
});

// src/routes/__root.tsx
import { createRootRoute, Outlet } from '@tanstack/react-router';
export const Route = createRootRoute({
  component: () => <Outlet />,
});

// src/routes/index.tsx
import { createFileRoute } from '@tanstack/react-router';
export const Route = createFileRoute('/')({
  component: HomePage,
});

// main.tsx
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { routeTree } from './routeTree.gen';  // 자동 생성
const router = createRouter({ routeTree });
<RouterProvider router={router} />
```

---

## 3️⃣ State Management

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Zustand** | 경량(~1KB), 단순 API | 소~중형 (권장) |
| **Redux Toolkit** | 표준, DevTools 강력 | 대규모, 팀 표준화 |
| **Jotai** | atom 단위, React 철학 | 세밀한 리렌더 최적화 |
| **Recoil** | Facebook 개발, Selector 강력 | atom 기반 복잡한 파생 상태 |
| **Valtio** | Proxy 기반, 뮤터블 스타일 | Vue 스타일 선호 |

### Jotai 설정

```bash
npm install jotai
```

```typescript
// atoms/authAtom.ts
import { atom } from 'jotai';
import { atomWithStorage } from 'jotai/utils';

// 기본 atom
export const countAtom = atom(0);

// 파생 atom (selector와 유사)
export const doubleCountAtom = atom((get) => get(countAtom) * 2);

// localStorage 동기화
export const tokenAtom = atomWithStorage<string | null>('token', null);

// 쓰기 가능 파생 atom
export const authAtom = atom(
  (get) => ({ token: get(tokenAtom), isAuthenticated: !!get(tokenAtom) }),
  (_get, set, token: string | null) => set(tokenAtom, token)
);

// 컴포넌트
import { useAtom, useAtomValue, useSetAtom } from 'jotai';
const [count, setCount] = useAtom(countAtom);
const double = useAtomValue(doubleCountAtom);   // 읽기만
const setToken = useSetAtom(tokenAtom);          // 쓰기만
```

### Valtio 설정

```bash
npm install valtio
```

```typescript
// store/authStore.ts
import { proxy, useSnapshot } from 'valtio';

export const authStore = proxy({
  user: null as User | null,
  token: null as string | null,
  isAuthenticated: false,

  // 액션 (메서드로 직접 정의)
  login(token: string, user: User) {
    this.token = token;
    this.user = user;
    this.isAuthenticated = true;
  },
  logout() {
    this.token = null;
    this.user = null;
    this.isAuthenticated = false;
  },
});

// 컴포넌트 — useSnapshot으로 반응형 구독
function Header() {
  const snap = useSnapshot(authStore);
  return (
    <div>
      <span>{snap.user?.name}</span>
      <button onClick={() => authStore.logout()}>로그아웃</button>
    </div>
  );
}
```

---

## 4️⃣ Server State / Data Fetch

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **TanStack Query** | 강력한 캐싱·동기화 | REST API (권장) |
| **SWR** | 단순, Vercel 제작 | 간단한 프로젝트 |
| **RTK Query** | Redux와 통합 | Redux 사용 중일 때 |
| **Apollo Client** | GraphQL 클라이언트 | GraphQL API |
| **Relay** | Facebook GraphQL | 대규모 GraphQL |

### SWR 설정

```bash
npm install swr
```

```typescript
import useSWR, { SWRConfig } from 'swr';

// 전역 fetcher 설정
const fetcher = (url: string) =>
  fetch(url).then(res => {
    if (!res.ok) throw new Error('API Error');
    return res.json();
  });

// main.tsx
<SWRConfig value={{ fetcher, revalidateOnFocus: false }}>
  <App />
</SWRConfig>

// 컴포넌트
function UserList() {
  const { data, error, isLoading, mutate } = useSWR<ApiResponse<User[]>>('/api/v1/users');

  if (isLoading) return <Spinner />;
  if (error) return <ErrorView />;

  return (
    <ul>
      {data?.data.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}

// mutation 후 재검증
await createUser(newUser);
mutate();  // 캐시 무효화 → 재요청
```

### Apollo Client 설정

```bash
npm install @apollo/client graphql
```

```typescript
// src/lib/apolloClient.ts
import { ApolloClient, InMemoryCache, createHttpLink, from } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { useAuthStore } from '@/store/authStore';

const httpLink = createHttpLink({ uri: '/graphql' });

const authLink = setContext((_, { headers }) => ({
  headers: {
    ...headers,
    authorization: `Bearer ${useAuthStore.getState().accessToken}`,
  },
}));

export const apolloClient = new ApolloClient({
  link: from([authLink, httpLink]),
  cache: new InMemoryCache(),
});

// main.tsx
import { ApolloProvider } from '@apollo/client';
<ApolloProvider client={apolloClient}><App /></ApolloProvider>

// 컴포넌트
import { useQuery, useMutation, gql } from '@apollo/client';

const GET_USERS = gql`
  query GetUsers {
    users { id name email }
  }
`;

const { data, loading, error } = useQuery(GET_USERS);
```

---

## 5️⃣ HTTP / API

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Axios** | 인터셉터, 광범위한 지원 | 대부분의 프로젝트 (권장) |
| **Ky** | fetch 래퍼, 모던 API | 브라우저 최신 환경 |
| **Superagent** | Node.js/브라우저 지원 | 레거시 프로젝트 |

### Ky 설정

```bash
npm install ky
```

```typescript
// src/api/client.ts
import ky, { type Options } from 'ky';
import { useAuthStore } from '@/store/authStore';

export const apiClient = ky.create({
  prefixUrl: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
  hooks: {
    beforeRequest: [
      (request) => {
        const token = useAuthStore.getState().accessToken;
        if (token) request.headers.set('Authorization', `Bearer ${token}`);
      },
    ],
    afterResponse: [
      async (_request, _options, response) => {
        if (response.status === 401) {
          // 토큰 재발급 로직
          useAuthStore.getState().logout();
          window.location.href = '/login';
        }
        return response;
      },
    ],
  },
});

// 사용
const data = await apiClient.get('users').json<ApiResponse<User[]>>();
const user = await apiClient.post('users', { json: newUser }).json<ApiResponse<User>>();
```

---

## 6️⃣ UI Framework (Design Library)

### 선택 가이드

| 순위 | 라이브러리 | 특징 | 추천 상황 |
|------|-----------|------|---------|
| 1 | **shadcn/ui** | Radix 기반 복사 붙여넣기, Tailwind | 완전 커스텀 디자인 (최근 트렌드) |
| 2 | **Material UI** | Google Material Design, 완성도 높음 | 기업 관리 시스템 |
| 3 | **Ant Design** | 풍부한 컴포넌트, 테이블/폼 특화 | 데이터 집약 대시보드 |
| 4 | **Chakra UI** | 접근성 우수, 커스텀 쉬움 | 일반 웹앱 |
| 5 | **Mantine** | 완성도 높음, 훅 풍부 | 모던 웹앱 |
| 6 | **Radix UI** | Headless, 완전 커스텀 | Tailwind + 자체 디자인 |
| 7 | **DaisyUI** | Tailwind 플러그인, 빠른 프로토타입 | 빠른 개발, 소규모 |
| 8 | **Flowbite React** | Tailwind 기반 완성 컴포넌트 | 빠른 개발, Tailwind 환경 |

---

### 1위. shadcn/ui (현재 트렌드 1위)

> 설치형 라이브러리가 아닌 **코드 복사 방식** — 완전한 소유권과 커스터마이징

```bash
# 프로젝트에 shadcn/ui 초기화 (Vite + Tailwind 필요)
npx shadcn@latest init
```

```
✔ Which style would you like to use? › Default
✔ Which color would you like to use as base color? › Slate
✔ Would you like to use CSS variables for colors? › yes
```

```bash
# 컴포넌트 개별 추가 (src/components/ui/ 에 코드 복사됨)
npx shadcn@latest add button
npx shadcn@latest add input card dialog form
npx shadcn@latest add table dropdown-menu toast
```

```tsx
// 사용 — 직접 소유한 컴포넌트
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import {
  Dialog, DialogContent, DialogHeader,
  DialogTitle, DialogTrigger,
} from '@/components/ui/dialog';

function LoginCard() {
  return (
    <Card className="w-[400px]">
      <CardHeader>
        <CardTitle>로그인</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <Input type="email" placeholder="이메일" />
        <Input type="password" placeholder="비밀번호" />
        <Button className="w-full">로그인</Button>
      </CardContent>
    </Card>
  );
}

// shadcn Form + React Hook Form 공식 통합
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Form, FormControl, FormField,
  FormItem, FormLabel, FormMessage,
} from '@/components/ui/form';

function LoginForm() {
  const form = useForm({ resolver: zodResolver(loginSchema) });

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>이메일</FormLabel>
              <FormControl>
                <Input placeholder="example@email.com" {...field} />
              </FormControl>
              <FormMessage />  {/* 에러 메시지 자동 표시 */}
            </FormItem>
          )}
        />
        <Button type="submit">로그인</Button>
      </form>
    </Form>
  );
}
```

**tailwind.config.js** (shadcn/ui 자동 설정됨)
```javascript
export default {
  darkMode: ['class'],
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: 'hsl(var(--primary))',
          foreground: 'hsl(var(--primary-foreground))',
        },
        // ... CSS 변수 기반 테마
      },
    },
  },
};
```

---

### 2위. Material UI (MUI)

```bash
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material   # 아이콘 (선택)
npm install @mui/x-data-grid      # 데이터 그리드 (선택)
```

```tsx
// main.tsx — 테마 설정
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

const theme = createTheme({
  palette: {
    primary:   { main: '#3b82f6' },
    secondary: { main: '#10b981' },
  },
  typography: {
    fontFamily: 'Pretendard, sans-serif',
  },
  components: {
    // 글로벌 컴포넌트 오버라이드
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: '8px', textTransform: 'none' },
      },
    },
  },
});

<ThemeProvider theme={theme}>
  <CssBaseline />   {/* CSS 노멀라이즈 */}
  <App />
</ThemeProvider>

// 컴포넌트
import { Button, TextField, Box, Stack, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';

const columns: GridColDef[] = [
  { field: 'id', headerName: 'ID', width: 90 },
  { field: 'name', headerName: '이름', flex: 1 },
  { field: 'email', headerName: '이메일', flex: 1 },
];

<DataGrid rows={users} columns={columns} pageSizeOptions={[10, 25]} />
```

---

### 3위. Ant Design

```bash
npm install antd
npm install @ant-design/icons   # 아이콘
```

```tsx
// main.tsx
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';

<ConfigProvider
  locale={koKR}
  theme={{
    token: {
      colorPrimary: '#3b82f6',
      borderRadius: 8,
      fontFamily: 'Pretendard, sans-serif',
    },
  }}
>
  <App />
</ConfigProvider>

// 컴포넌트 — Table, Form, 날짜 등 풍부한 컴포넌트
import { Table, Form, Input, Button, DatePicker, Select } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const columns: ColumnsType<User> = [
  { title: '이름', dataIndex: 'name', key: 'name', sorter: true },
  { title: '이메일', dataIndex: 'email', key: 'email' },
  {
    title: '액션',
    key: 'action',
    render: (_, record) => (
      <Button type="link" onClick={() => handleEdit(record)}>수정</Button>
    ),
  },
];

<Table
  columns={columns}
  dataSource={users}
  rowKey="id"
  pagination={{ pageSize: 10, showTotal: (total) => `총 ${total}건` }}
/>
```

---

### 4위. Chakra UI

```bash
npm install @chakra-ui/react @emotion/react @emotion/styled framer-motion
```

```tsx
// main.tsx
import { ChakraProvider, extendTheme } from '@chakra-ui/react';

const theme = extendTheme({
  colors: {
    brand: {
      500: '#3b82f6',
      600: '#2563eb',
    },
  },
  fonts: {
    body: 'Pretendard, sans-serif',
  },
  components: {
    Button: {
      defaultProps: { colorScheme: 'brand' },
    },
  },
});

<ChakraProvider theme={theme}><App /></ChakraProvider>

// 컴포넌트
import {
  Box, Button, Input, FormControl, FormLabel, FormErrorMessage,
  Stack, HStack, VStack, Heading, Text, useDisclosure,
  Modal, ModalOverlay, ModalContent, ModalHeader, ModalBody,
  useToast,
} from '@chakra-ui/react';

function Example() {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const toast = useToast();

  return (
    <VStack spacing={4} align="stretch">
      <Button onClick={onOpen}>모달 열기</Button>
      <Modal isOpen={isOpen} onClose={onClose}>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>제목</ModalHeader>
          <ModalBody>내용</ModalBody>
        </ModalContent>
      </Modal>
      <Button
        onClick={() => toast({
          title: '저장됨',
          status: 'success',
          duration: 3000,
        })}
      >
        토스트
      </Button>
    </VStack>
  );
}
```

---

### 5위. Mantine

```bash
npm install @mantine/core @mantine/hooks @mantine/notifications @mantine/form
npm install -D postcss postcss-preset-mantine postcss-simple-vars
```

```typescript
// main.tsx
import { MantineProvider, createTheme } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

const theme = createTheme({
  primaryColor: 'blue',
  fontFamily: 'Pretendard, sans-serif',
  components: {
    Button: { defaultProps: { radius: 'md' } },
  },
});

<MantineProvider theme={theme}>
  <Notifications position="top-right" />
  <App />
</MantineProvider>

// 컴포넌트
import { Button, TextInput, Stack, Group, Badge } from '@mantine/core';
import { useDisclosure, useLocalStorage, useDebouncedValue } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';

function Example() {
  const [opened, { toggle }] = useDisclosure();
  const [value, setValue] = useLocalStorage({ key: 'setting', defaultValue: '' });
  const [debounced] = useDebouncedValue(value, 300);

  return (
    <Stack>
      <TextInput
        label="검색"
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
      <Group>
        <Button onClick={() => notifications.show({ title: '성공', message: '저장됨!' })}>
          저장
        </Button>
        <Badge color="blue">New</Badge>
      </Group>
    </Stack>
  );
}
```

---

### 6위. Radix UI

```bash
# 필요한 컴포넌트만 개별 설치
npm install @radix-ui/react-dialog @radix-ui/react-dropdown-menu
npm install @radix-ui/react-select @radix-ui/react-tooltip @radix-ui/react-popover
npm install @radix-ui/react-checkbox @radix-ui/react-switch @radix-ui/react-tabs
```

```tsx
import * as Dialog from '@radix-ui/react-dialog';
import * as DropdownMenu from '@radix-ui/react-dropdown-menu';

// Tailwind로 직접 스타일링
function Modal({ trigger, title, children }: ModalProps) {
  return (
    <Dialog.Root>
      <Dialog.Trigger asChild>{trigger}</Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 animate-fade-in" />
        <Dialog.Content className="
          fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2
          bg-white rounded-xl p-6 w-full max-w-md shadow-xl
          animate-slide-up
        ">
          <Dialog.Title className="text-lg font-semibold mb-4">{title}</Dialog.Title>
          {children}
          <Dialog.Close className="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            ✕
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}

// 드롭다운 메뉴
function UserMenu() {
  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger className="btn-primary">메뉴</DropdownMenu.Trigger>
      <DropdownMenu.Content className="bg-white rounded-lg shadow-lg p-1 min-w-[160px]">
        <DropdownMenu.Item className="px-3 py-2 rounded hover:bg-gray-100 cursor-pointer">
          프로필
        </DropdownMenu.Item>
        <DropdownMenu.Separator className="my-1 border-t border-gray-100" />
        <DropdownMenu.Item className="px-3 py-2 rounded hover:bg-red-50 text-red-600 cursor-pointer">
          로그아웃
        </DropdownMenu.Item>
      </DropdownMenu.Content>
    </DropdownMenu.Root>
  );
}
```

---

### 7위. DaisyUI

```bash
npm install -D daisyui
```

```javascript
// tailwind.config.js
export default {
  content: ['./src/**/*.{js,ts,jsx,tsx}'],
  theme: { extend: {} },
  plugins: [require('daisyui')],
  daisyui: {
    themes: ['light', 'dark', 'cupcake', 'corporate'],  // 기본 테마
    // 커스텀 테마 추가
    themes: [
      {
        mytheme: {
          'primary': '#3b82f6',
          'secondary': '#10b981',
          'accent': '#f59e0b',
          'neutral': '#374151',
          'base-100': '#ffffff',
        },
      },
      'dark',
    ],
  },
};
```

```tsx
// 사용 — HTML class 기반, JSX 완전 호환
function Example() {
  return (
    <div className="p-4 space-y-4">
      {/* 버튼 */}
      <button className="btn btn-primary">기본 버튼</button>
      <button className="btn btn-outline btn-secondary">아웃라인</button>
      <button className="btn btn-sm btn-ghost">작은 버튼</button>

      {/* 카드 */}
      <div className="card w-96 bg-base-100 shadow-xl">
        <div className="card-body">
          <h2 className="card-title">카드 제목</h2>
          <p>카드 내용</p>
          <div className="card-actions justify-end">
            <button className="btn btn-primary">확인</button>
          </div>
        </div>
      </div>

      {/* 인풋 */}
      <input type="text" placeholder="입력" className="input input-bordered w-full" />

      {/* 배지 */}
      <span className="badge badge-primary">NEW</span>
      <span className="badge badge-secondary badge-outline">BETA</span>

      {/* 토글 */}
      <input type="checkbox" className="toggle toggle-primary" />

      {/* 테마 전환 */}
      <label className="swap swap-rotate">
        <input type="checkbox" data-theme-controller="dark" />
        <span className="swap-on">🌙</span>
        <span className="swap-off">☀️</span>
      </label>
    </div>
  );
}
```

---

### 8위. Flowbite React

```bash
npm install flowbite-react
```

```javascript
// tailwind.config.js
import flowbite from 'flowbite-react/tailwind';

export default {
  content: [
    './src/**/*.{js,ts,jsx,tsx}',
    flowbite.content(),        // Flowbite 콘텐츠 경로 추가
  ],
  plugins: [flowbite.plugin()],
};
```

```tsx
import {
  Button, Card, TextInput, Label,
  Modal, Navbar, Sidebar, Table,
  Dropdown, Badge, Spinner, Toast,
  FileInput, Select, Checkbox,
} from 'flowbite-react';

function Example() {
  const [openModal, setOpenModal] = useState(false);

  return (
    <div className="p-4 space-y-4">
      {/* 카드 + 폼 */}
      <Card className="max-w-sm">
        <h5 className="text-xl font-bold">로그인</h5>
        <form className="space-y-4">
          <div>
            <Label htmlFor="email">이메일</Label>
            <TextInput id="email" type="email" placeholder="이메일 입력" required />
          </div>
          <div>
            <Label htmlFor="password">비밀번호</Label>
            <TextInput id="password" type="password" required />
          </div>
          <Button type="submit" className="w-full">로그인</Button>
        </form>
      </Card>

      {/* 모달 */}
      <Button onClick={() => setOpenModal(true)}>모달 열기</Button>
      <Modal show={openModal} onClose={() => setOpenModal(false)}>
        <Modal.Header>모달 제목</Modal.Header>
        <Modal.Body>모달 내용입니다.</Modal.Body>
        <Modal.Footer>
          <Button onClick={() => setOpenModal(false)}>확인</Button>
          <Button color="gray" onClick={() => setOpenModal(false)}>취소</Button>
        </Modal.Footer>
      </Modal>

      {/* 테이블 */}
      <Table>
        <Table.Head>
          <Table.HeadCell>이름</Table.HeadCell>
          <Table.HeadCell>이메일</Table.HeadCell>
          <Table.HeadCell>상태</Table.HeadCell>
        </Table.Head>
        <Table.Body>
          {users.map(user => (
            <Table.Row key={user.id}>
              <Table.Cell>{user.name}</Table.Cell>
              <Table.Cell>{user.email}</Table.Cell>
              <Table.Cell>
                <Badge color="success">활성</Badge>
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table>
    </div>
  );
}
```

---

### 라이브러리 선택 결정 트리

```
커스텀 디자인이 중요한가?
  YES → Tailwind 사용 중인가?
          YES → shadcn/ui (1위 추천)
                또는 Radix UI (완전 헤드리스)
                또는 DaisyUI (빠른 프로토타입)
                또는 Flowbite React (완성 컴포넌트)
          NO  → Radix UI + 자체 스타일

  NO (디자인 시스템 제공 라이브러리 사용) →
    데이터 집약 대시보드/테이블?
      YES → Ant Design (3위)
             또는 MUI + DataGrid (2위)

    접근성이 최우선?
      YES → Chakra UI (4위)

    훅/유틸이 많이 필요?
      YES → Mantine (5위)

    빠르게 만들어야 함?
      YES → DaisyUI (7위) 또는 Flowbite React (8위)
```

---

## 7️⃣ Styling

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Tailwind CSS** | utility-first, 빠름 | 커스텀 디자인 (권장) |
| **styled-components** | CSS-in-JS 원조 | CSS-in-JS 선호 시 |
| **Emotion** | 빠른 CSS-in-JS, MUI 기반 | MUI 사용 시 |
| **Sass** | CSS 확장, 변수/믹스인 | 기존 Sass 프로젝트 |
| **UnoCSS** | 초고속 utility CSS | 번들 크기 극소화 |

### Emotion 설정

```bash
npm install @emotion/react @emotion/styled
```

```tsx
/** @jsxImportSource @emotion/react */
import { css, keyframes } from '@emotion/react';
import styled from '@emotion/styled';

// CSS 태그드 템플릿
const buttonStyle = css`
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  background: #3b82f6;
  color: white;
  &:hover { background: #2563eb; }
`;

// Styled component
const Card = styled.div<{ isActive: boolean }>`
  background: ${({ isActive }) => isActive ? '#eff6ff' : 'white'};
  border: 1px solid ${({ isActive }) => isActive ? '#3b82f6' : '#e5e7eb'};
  padding: 1rem;
  border-radius: 0.75rem;
`;
```

### UnoCSS 설정

```bash
npm install -D unocss
```

```typescript
// vite.config.ts
import UnoCSS from 'unocss/vite';
export default defineConfig({
  plugins: [UnoCSS(), react()],
});

// main.tsx
import 'virtual:uno.css';

// 사용 — Tailwind와 거의 동일한 문법
<div className="flex items-center gap-4 p-4 bg-blue-500 text-white rounded-lg">
```

---

## 8️⃣ Form

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **React Hook Form** | 성능 최적화, 비제어 | 대부분의 폼 (권장) |
| **Formik** | 제어 컴포넌트, 오래된 표준 | 레거시 프로젝트 |
| **Final Form** | 구독 기반 최적화 | 복잡한 폼 |

### Formik + Yup 설정

```bash
npm install formik yup
```

```tsx
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';

const loginSchema = Yup.object({
  email:    Yup.string().email('이메일 형식 오류').required('필수'),
  password: Yup.string().min(8, '8자 이상').required('필수'),
});

function LoginForm() {
  return (
    <Formik
      initialValues={{ email: '', password: '' }}
      validationSchema={loginSchema}
      onSubmit={async (values, { setSubmitting }) => {
        await loginApi(values);
        setSubmitting(false);
      }}
    >
      {({ isSubmitting }) => (
        <Form>
          <Field name="email" type="email" placeholder="이메일" />
          <ErrorMessage name="email" component="p" className="text-red-500" />

          <Field name="password" type="password" placeholder="비밀번호" />
          <ErrorMessage name="password" component="p" className="text-red-500" />

          <button type="submit" disabled={isSubmitting}>로그인</button>
        </Form>
      )}
    </Formik>
  );
}
```

---

## 9️⃣ Validation

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Yup** | schema validation, RHF 연동 쉬움 | React 프론트엔드 (권장) |
| **Zod** | TypeScript-first, 런타임 타입 | TypeScript 프로젝트 |
| **Joi** | Node.js 원조, 강력 | 백엔드 공유 스키마 |

### Zod 설정

```bash
npm install zod @hookform/resolvers
```

```typescript
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';

// 스키마 정의
const signupSchema = z.object({
  email:    z.string().email('이메일 형식이 아닙니다'),
  password: z.string()
    .min(8, '8자 이상')
    .regex(/[A-Za-z]/, '영문 포함')
    .regex(/[0-9]/, '숫자 포함'),
  age:      z.number().min(14, '14세 이상').max(120),
}).refine(
  (data) => data.password.includes(data.email.split('@')[0]) === false,
  { message: '비밀번호에 이메일 아이디를 포함할 수 없습니다', path: ['password'] }
);

// 타입 자동 추론
type SignupInput = z.infer<typeof signupSchema>;

// React Hook Form 연동
const { register, handleSubmit, formState: { errors } } = useForm<SignupInput>({
  resolver: zodResolver(signupSchema),
});

// 런타임 검증 (API 응답 타입 안전)
const UserSchema = z.object({
  id:    z.number(),
  email: z.string().email(),
  name:  z.string(),
});

const user = UserSchema.parse(apiResponse);  // 실패 시 ZodError
const result = UserSchema.safeParse(data);   // 실패 시 { success: false, error }
```

---

## 🔟 Utility

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Lodash** | JS 유틸 전집 | 다양한 유틸 필요 시 |
| **clsx** | className 조건부 결합 | Tailwind 조합 (권장) |
| **classnames** | clsx 유사, 구버전 호환 | 레거시 프로젝트 |
| **Day.js** | 경량 날짜 처리 (2KB) | 날짜 포맷 (권장) |
| **date-fns** | 함수형 날짜 유틸 | 트리쉐이킹 필요 시 |

### Day.js 설정

```bash
npm install dayjs
```

```typescript
import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import relativeTime from 'dayjs/plugin/relativeTime';
import customParseFormat from 'dayjs/plugin/customParseFormat';

dayjs.locale('ko');
dayjs.extend(relativeTime);
dayjs.extend(customParseFormat);

// 사용
dayjs().format('YYYY년 MM월 DD일');          // 2025년 03월 15일
dayjs('2025-01-01').from(dayjs());           // 2달 전
dayjs().subtract(7, 'day').format('MM/DD'); // 03/08
dayjs.duration(3600, 'seconds').format('HH:mm:ss'); // 01:00:00
```

### date-fns 설정

```typescript
import { format, formatDistanceToNow, addDays, isAfter } from 'date-fns';
import { ko } from 'date-fns/locale';

format(new Date(), 'yyyy년 MM월 dd일', { locale: ko });
formatDistanceToNow(new Date('2025-01-01'), { locale: ko, addSuffix: true }); // 2달 전
addDays(new Date(), 7);
isAfter(new Date(), new Date('2025-01-01'));  // true
```

---

## 1️⃣1️⃣ Animation

| 패키지 | 특징 | 추천 상황 |
|--------|------|---------|
| **Framer Motion** | React 통합, 선언적 | 대부분의 UI 애니메이션 (권장) |
| **React Spring** | 물리 기반, 자연스러움 | 물리 인터랙션 |
| **GSAP** | 강력, 타임라인 | 복잡한 시퀀스 애니메이션 |

### Framer Motion 설정

```bash
npm install framer-motion
```

```tsx
import { motion, AnimatePresence } from 'framer-motion';

// 기본 애니메이션
<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  exit={{ opacity: 0, y: -20 }}
  transition={{ duration: 0.3, ease: 'easeOut' }}
>
  콘텐츠
</motion.div>

// 페이지 전환
function PageWrapper({ children }: { children: React.ReactNode }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.2 }}
    >
      {children}
    </motion.div>
  );
}

// 조건부 렌더링 애니메이션
<AnimatePresence>
  {isVisible && (
    <motion.div
      key="modal"
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.9 }}
    >
      모달 콘텐츠
    </motion.div>
  )}
</AnimatePresence>

// 리스트 아이템 순차 등장
const container = {
  hidden: { opacity: 0 },
  show:   { opacity: 1, transition: { staggerChildren: 0.1 } },
};
const item = {
  hidden: { opacity: 0, y: 20 },
  show:   { opacity: 1, y: 0 },
};

<motion.ul variants={container} initial="hidden" animate="show">
  {items.map(i => (
    <motion.li key={i.id} variants={item}>{i.name}</motion.li>
  ))}
</motion.ul>
```

---

## 1️⃣2️⃣ Dev Tool

| 패키지 | 특징 | 설치 |
|--------|------|------|
| **ESLint** | 코드 규칙 검사 | `npm i -D eslint` |
| **Prettier** | 코드 포맷 | `npm i -D prettier` |
| **Husky** | git hook 실행 | `npm i -D husky` |
| **lint-staged** | 스테이징 파일만 검사 | `npm i -D lint-staged` |
| **Commitlint** | 커밋 메시지 규칙 | `npm i -D @commitlint/cli @commitlint/config-conventional` |

### 전체 Dev Tool 한 번에 설정

```bash
npm install -D eslint prettier husky lint-staged
npm install -D @commitlint/cli @commitlint/config-conventional
npm install -D eslint-config-prettier @typescript-eslint/eslint-plugin @typescript-eslint/parser
npm install -D eslint-plugin-react eslint-plugin-react-hooks

# Husky 초기화
npx husky init
```

### commitlint.config.js

```javascript
export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [2, 'always', [
      'feat',     // 새 기능
      'fix',      // 버그 수정
      'docs',     // 문서
      'style',    // 포맷 (기능 변경 없음)
      'refactor', // 리팩토링
      'test',     // 테스트
      'chore',    // 빌드/설정
      'perf',     // 성능 개선
      'revert',   // 되돌리기
      'ui',       // UI/스타일 변경
    ]],
    'subject-max-length': [2, 'always', 72],
    'subject-case': [0],  // 한국어 허용
  },
};
```

### .husky/pre-commit

```bash
npx lint-staged
```

### .husky/commit-msg

```bash
npx --no -- commitlint --edit "$1"
```

### package.json lint-staged 설정

```json
{
  "lint-staged": {
    "src/**/*.{ts,tsx,js,jsx}": [
      "eslint --fix",
      "prettier --write"
    ],
    "src/**/*.{css,json,md}": [
      "prettier --write"
    ]
  }
}
```

---

## 패키지 조합 추천

### 📦 가벼운 프로젝트

```
Vite + React + JavaScript
Wouter (경량 라우터)
Zustand (상태관리)
SWR + Axios (데이터)
Tailwind CSS
React Hook Form + Yup
Day.js
ESLint + Prettier
```

### 📦 일반 프로젝트 (권장)

```
Vite + React + TypeScript + SWC
React Router v6
Zustand
TanStack Query + Axios
Tailwind CSS + shadcn/ui + clsx/twMerge
React Hook Form + Zod
Framer Motion
Day.js
ESLint + Prettier + Husky + Commitlint
```

### 📦 대규모 / 엔터프라이즈

```
Vite + React + TypeScript
TanStack Router (타입 안전)
Redux Toolkit + RTK Query
Mantine (UI 프레임워크)
Tailwind CSS
React Hook Form + Zod
Framer Motion
date-fns
ESLint + Prettier + Husky + Commitlint
```
