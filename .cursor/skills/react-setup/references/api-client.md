# API 통신 설정 가이드

---

## 1. Axios 인스턴스 & 인터셉터

```typescript
// src/api/client.ts
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/store/authStore';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
});

// ── 요청 인터셉터: 토큰 자동 주입 ──────────────
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── 응답 인터셉터: 토큰 재발급 & 에러 처리 ────
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach(({ resolve, reject }) =>
    error ? reject(error) : resolve(token!)
  );
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 재발급 중인 다른 요청들을 큐에 대기
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        const { data } = await axios.post('/api/v1/auth/reissue', null, {
          headers: { 'Refresh-Token': refreshToken },
        });
        const newToken = data.data.accessToken;
        useAuthStore.getState().setTokens(newToken, data.data.refreshToken);
        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

---

## 2. API 함수 모듈

```typescript
// src/api/user.ts
import { apiClient } from './client';
import type { User, UserRequest, ApiResponse, PageResponse } from '@/types';

export const userApi = {
  getUsers: (params?: { page?: number; size?: number; keyword?: string }) =>
    apiClient.get<ApiResponse<PageResponse<User>>>('/users', { params }),

  getUser: (id: number) =>
    apiClient.get<ApiResponse<User>>(`/users/${id}`),

  createUser: (data: UserRequest.Create) =>
    apiClient.post<ApiResponse<User>>('/users', data),

  updateUser: (id: number, data: UserRequest.Update) =>
    apiClient.patch<ApiResponse<User>>(`/users/${id}`, data),

  deleteUser: (id: number) =>
    apiClient.delete<ApiResponse<void>>(`/users/${id}`),
};
```

```typescript
// src/types/api.ts — 공통 타입
export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

---

## 3. TanStack Query (React Query) 설정

### 쿼리 훅 패턴

```typescript
// src/hooks/useUsers.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/api/user';
import type { UserRequest } from '@/types';

// 쿼리 키 상수 관리
export const userKeys = {
  all:    () => ['users'] as const,
  list:   (params?: object) => [...userKeys.all(), 'list', params] as const,
  detail: (id: number) => [...userKeys.all(), 'detail', id] as const,
};

// 목록 조회
export function useUsers(params?: { page?: number; keyword?: string }) {
  return useQuery({
    queryKey: userKeys.list(params),
    queryFn:  () => userApi.getUsers(params).then(res => res.data.data),
    staleTime: 5 * 60 * 1000,  // 5분 캐시
  });
}

// 단건 조회
export function useUser(id: number) {
  return useQuery({
    queryKey: userKeys.detail(id),
    queryFn:  () => userApi.getUser(id).then(res => res.data.data),
    enabled:  !!id,             // id가 있을 때만 실행
  });
}

// 생성 뮤테이션
export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UserRequest.Create) =>
      userApi.createUser(data).then(res => res.data.data),
    onSuccess: () => {
      // 목록 캐시 무효화 → 자동 리페치
      queryClient.invalidateQueries({ queryKey: userKeys.all() });
    },
  });
}

// 삭제 뮤테이션 (Optimistic Update)
export function useDeleteUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => userApi.deleteUser(id),
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: userKeys.list() });
      const previous = queryClient.getQueryData(userKeys.list());
      // 즉시 UI 업데이트 (낙관적 업데이트)
      queryClient.setQueryData(userKeys.list(), (old: any) => ({
        ...old,
        content: old?.content.filter((u: any) => u.id !== id),
      }));
      return { previous };
    },
    onError: (_err, _id, context) => {
      // 실패 시 롤백
      queryClient.setQueryData(userKeys.list(), context?.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.all() });
    },
  });
}
```

### 컴포넌트에서 사용

```tsx
function UserList() {
  const { data, isLoading, error } = useUsers({ page: 0 });
  const deleteUser = useDeleteUser();

  if (isLoading) return <Skeleton />;
  if (error)     return <ErrorView />;

  return (
    <ul>
      {data?.content.map(user => (
        <li key={user.id}>
          {user.name}
          <button onClick={() => deleteUser.mutate(user.id)}
                  disabled={deleteUser.isPending}>
            삭제
          </button>
        </li>
      ))}
    </ul>
  );
}
```

---

## 4. MSW (Mock Service Worker) 개발 환경 설정

```bash
npm install -D msw
npx msw init public/ --save
```

```typescript
// src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/v1/users', ({ request }) => {
    const url = new URL(request.url);
    const page = Number(url.searchParams.get('page') ?? '0');
    return HttpResponse.json({
      success: true,
      data: {
        content: [
          { id: 1, name: '홍길동', email: 'hong@test.com' },
          { id: 2, name: '김철수', email: 'kim@test.com' },
        ],
        totalElements: 2,
        totalPages: 1,
        number: page,
      },
    });
  }),

  http.post('/api/v1/auth/login', async ({ request }) => {
    const body = await request.json() as { email: string; password: string };
    if (body.password !== 'correct') {
      return HttpResponse.json(
        { success: false, message: '비밀번호가 올바르지 않습니다' },
        { status: 401 }
      );
    }
    return HttpResponse.json({
      success: true,
      data: {
        accessToken:  'mock-access-token',
        refreshToken: 'mock-refresh-token',
      },
    });
  }),
];
```

```typescript
// src/test/mocks/browser.ts (개발 환경용)
import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';
export const worker = setupWorker(...handlers);

// src/test/mocks/server.ts (테스트 환경용)
import { setupServer } from 'msw/node';
import { handlers } from './handlers';
export const server = setupServer(...handlers);
```
