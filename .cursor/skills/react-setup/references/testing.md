# 테스트 환경 설정 가이드

---

## 설치

```bash
npm install -D vitest @vitest/ui jsdom
npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event
npm install -D msw
```

---

## 1. vitest.config.ts

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals:     true,
    environment: 'jsdom',
    setupFiles:  ['./src/test/setup.ts'],
    css:         true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/', 'src/test/',
        '**/*.d.ts', '**/*.config.*',
        'src/main.tsx',
      ],
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@api': path.resolve(__dirname, './src/api'),
      '@store': path.resolve(__dirname, './src/store'),
    },
  },
});
```

---

## 2. src/test/setup.ts

```typescript
import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll } from 'vitest';
import { server } from './mocks/server';

// MSW 서버 설정
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => {
  server.resetHandlers();  // 핸들러 초기화
  cleanup();               // 렌더링된 컴포넌트 정리
});
afterAll(() => server.close());
```

---

## 3. 테스트 유틸 (렌더링 래퍼)

```tsx
// src/test/utils.tsx
import { ReactNode } from 'react';
import { render, type RenderOptions } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

interface RenderWithProvidersOptions extends RenderOptions {
  initialRoute?: string;
}

// 전체 프로바이더 포함 렌더러
export function renderWithProviders(
  ui: ReactNode,
  { initialRoute = '/', ...options }: RenderWithProvidersOptions = {}
) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0 },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialRoute]}>
        {ui}
      </MemoryRouter>
    </QueryClientProvider>,
    options
  );
}

// 인증된 상태로 렌더링
export function renderAuthenticated(
  ui: ReactNode,
  options?: RenderWithProvidersOptions
) {
  // Zustand authStore에 토큰 주입
  const { useAuthStore } = require('@/store/authStore');
  useAuthStore.setState({
    isAuthenticated: true,
    accessToken: 'mock-token',
    user: { id: 1, name: '테스터', email: 'test@test.com', role: 'USER' },
  });
  return renderWithProviders(ui, options);
}
```

---

## 4. 컴포넌트 테스트 예시

```tsx
// src/components/common/Button.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '@/test/utils';
import Button from './Button';

describe('Button', () => {
  it('텍스트를 렌더링한다', () => {
    renderWithProviders(<Button>클릭</Button>);
    expect(screen.getByRole('button', { name: '클릭' })).toBeInTheDocument();
  });

  it('클릭 시 onClick 핸들러를 호출한다', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    renderWithProviders(<Button onClick={onClick}>클릭</Button>);
    await user.click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('disabled 상태에서 클릭이 되지 않는다', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    renderWithProviders(<Button disabled onClick={onClick}>클릭</Button>);
    await user.click(screen.getByRole('button'));
    expect(onClick).not.toHaveBeenCalled();
  });

  it('isLoading 상태에서 로딩 스피너를 표시한다', () => {
    renderWithProviders(<Button isLoading>클릭</Button>);
    expect(screen.getByTestId('spinner')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
```

---

## 5. API 연동 컴포넌트 테스트

```tsx
// src/pages/user/UserList.test.tsx
import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';
import { renderWithProviders } from '@/test/utils';
import UserList from './UserList';

describe('UserList', () => {
  it('사용자 목록을 불러와 렌더링한다', async () => {
    renderWithProviders(<UserList />);
    expect(screen.getByTestId('loading')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('홍길동')).toBeInTheDocument();
    });
  });

  it('API 오류 시 에러 메시지를 표시한다', async () => {
    server.use(
      http.get('/api/v1/users', () =>
        HttpResponse.json({ message: 'Server Error' }, { status: 500 })
      )
    );

    renderWithProviders(<UserList />);

    await waitFor(() => {
      expect(screen.getByText(/오류가 발생했습니다/)).toBeInTheDocument();
    });
  });

  it('목록이 비어있으면 빈 상태를 표시한다', async () => {
    server.use(
      http.get('/api/v1/users', () =>
        HttpResponse.json({ success: true, data: { content: [], totalElements: 0 } })
      )
    );

    renderWithProviders(<UserList />);

    await waitFor(() => {
      expect(screen.getByText(/등록된 사용자가 없습니다/)).toBeInTheDocument();
    });
  });
});
```

---

## 실행 명령어

```bash
# 개발 중 watch 모드
npm test

# UI 모드 (브라우저에서 결과 확인)
npm run test:ui

# 커버리지 측정
npm run test:coverage
# → coverage/index.html 로 확인
```
