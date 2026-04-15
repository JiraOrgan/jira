# 상태관리 설정 가이드

---

## 선택 가이드

| 라이브러리 | 적합한 상황 | 학습 난이도 |
|-----------|-----------|-----------|
| **Zustand** | 소~중형, 빠른 개발 | ⭐ 쉬움 (권장) |
| **Jotai** | atom 단위, 경량, React 철학 | ⭐ 쉬움 (atom 기반 권장) |
| ~~Recoil~~ | atom 기반 (유지보수 중단) | ⭐⭐ 보통 |
| **Redux Toolkit** | 대규모, 팀 표준화 | ⭐⭐⭐ 높음 |

> **서버 상태**(API 데이터)는 TanStack Query로 관리.
> **클라이언트 상태**(로그인 정보, UI 상태 등)만 아래 라이브러리로 관리.

---

## 1. Zustand (권장)

```bash
npm install zustand
```

### 인증 스토어

```typescript
// src/store/authStore.ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface User {
  id: number;
  email: string;
  name: string;
  role: 'USER' | 'ADMIN';
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;

  // Actions
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, isAuthenticated: true }),

      setUser: (user) => set({ user }),

      logout: () =>
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'auth-storage',              // localStorage 키
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({          // 저장할 필드만 선택
        accessToken:  state.accessToken,
        refreshToken: state.refreshToken,
        user:         state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
```

### UI 스토어

```typescript
// src/store/uiStore.ts
import { create } from 'zustand';

interface UIState {
  isSidebarOpen: boolean;
  theme: 'light' | 'dark';
  toggleSidebar: () => void;
  setTheme: (theme: 'light' | 'dark') => void;
}

export const useUIStore = create<UIState>((set) => ({
  isSidebarOpen: true,
  theme: 'light',
  toggleSidebar: () =>
    set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),
  setTheme: (theme) => set({ theme }),
}));
```

### 사용 패턴

```tsx
// 컴포넌트에서 사용
import { useAuthStore } from '@/store/authStore';

function Header() {
  const { user, logout } = useAuthStore();
  // 특정 필드만 구독 (리렌더 최소화)
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);

  return (
    <header>
      <span>{user?.name}</span>
      <button onClick={logout}>로그아웃</button>
    </header>
  );
}

// Zustand 훅 외부(인터셉터 등)에서 스토어 직접 접근
const token = useAuthStore.getState().accessToken;
useAuthStore.getState().logout();
```

---

## 2. Recoil

> ⚠️ **Recoil은 2023년 이후 업데이트가 중단**되었습니다. 신규 프로젝트에서는 **Jotai**를 권장합니다.
> Jotai는 Recoil과 유사한 atom 기반 API를 제공하며, 번들 크기가 더 작고 활발히 유지보수됩니다.
> Jotai 설정 예시는 `references/package-guide.md`의 Jotai 섹션을 참조하세요.

```bash
npm install recoil
```

```tsx
// main.tsx
import { RecoilRoot } from 'recoil';
<RecoilRoot><App /></RecoilRoot>

// src/store/authAtom.ts
import { atom, selector } from 'recoil';

export const authAtom = atom<{
  user: User | null;
  accessToken: string | null;
}>({
  key: 'authState',
  default: { user: null, accessToken: null },
});

// selector: 파생 상태
export const isAuthenticatedSelector = selector({
  key: 'isAuthenticated',
  get: ({ get }) => !!get(authAtom).accessToken,
});

// 사용
import { useRecoilState, useRecoilValue } from 'recoil';
const [auth, setAuth] = useRecoilState(authAtom);
const isAuthenticated = useRecoilValue(isAuthenticatedSelector);
```

---

## 3. Redux Toolkit

```bash
npm install @reduxjs/toolkit react-redux
```

```typescript
// src/store/slices/authSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null as User | null,
    accessToken: null as string | null,
    isAuthenticated: false,
  },
  reducers: {
    setCredentials: (state, action: PayloadAction<{
      user: User;
      accessToken: string;
    }>) => {
      state.user = action.payload.user;
      state.accessToken = action.payload.accessToken;
      state.isAuthenticated = true;
    },
    logout: (state) => {
      state.user = null;
      state.accessToken = null;
      state.isAuthenticated = false;
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;

// src/store/index.ts
import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';

export const store = configureStore({
  reducer: { auth: authReducer },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// main.tsx
import { Provider } from 'react-redux';
<Provider store={store}><App /></Provider>
```
