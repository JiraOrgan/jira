# React 19 신기능 가이드

---

## 1. useActionState (폼 액션 상태 관리)

> 이전 이름: `useFormState` (React 18 canary) → React 19에서 `useActionState`로 변경

폼 제출의 **pending 상태**, **에러 처리**, **응답 데이터**를 하나의 훅으로 통합 관리한다.
서버 컴포넌트와 클라이언트 컴포넌트 양쪽에서 사용할 수 있다.

```tsx
const [state, formAction, isPending] = useActionState(actionFn, initialState);
```

| 반환값 | 설명 |
|--------|------|
| `state` | 액션 함수가 반환한 최신 상태 |
| `formAction` | `<form action>`에 전달할 래핑된 액션 |
| `isPending` | 액션 실행 중 여부 (boolean) |

### 로그인 폼 예시

```tsx
import { useActionState } from 'react';

async function loginAction(prevState, formData) {
  const email = formData.get('email');
  const password = formData.get('password');
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
      headers: { 'Content-Type': 'application/json' },
    });
    if (!res.ok) return { error: '이메일 또는 비밀번호가 올바르지 않습니다.' };
    const data = await res.json();
    return { success: true, message: `${data.user.name}님, 환영합니다!` };
  } catch {
    return { error: '서버 오류가 발생했습니다.' };
  }
}

function LoginForm() {
  const [state, formAction, isPending] = useActionState(loginAction, null);
  return (
    <form action={formAction}>
      <input name="email" type="email" required placeholder="이메일" />
      <input name="password" type="password" required placeholder="비밀번호" />
      <button disabled={isPending}>{isPending ? '로그인 중...' : '로그인'}</button>
      {state?.error && <p style={{ color: 'red' }}>{state.error}</p>}
      {state?.success && <p style={{ color: 'green' }}>{state.message}</p>}
    </form>
  );
}
```

### 회원가입 폼 예시

```tsx
async function signupAction(prevState, formData) {
  const password = formData.get('password');
  if (password.length < 8) return { error: '비밀번호는 8자 이상이어야 합니다.' };
  const res = await fetch('/api/auth/signup', {
    method: 'POST',
    body: JSON.stringify(Object.fromEntries(formData)),
    headers: { 'Content-Type': 'application/json' },
  });
  if (res.status === 409) return { error: '이미 등록된 이메일입니다.' };
  return { success: true, message: '회원가입이 완료되었습니다!' };
}
```

---

## 2. useOptimistic (낙관적 업데이트)

서버 응답을 기다리지 않고 **UI를 즉시 업데이트**하여 체감 속도를 높인다.
서버 요청이 실패하면 원래 상태로 **자동 롤백**된다.

### 좋아요 버튼 예시

```tsx
import { useOptimistic } from 'react';

function LikeButton({ liked, likeCount, onToggleLike }) {
  const [optimistic, setOptimistic] = useOptimistic(
    { liked, likeCount },
    (current, _) => ({
      liked: !current.liked,
      likeCount: current.liked ? current.likeCount - 1 : current.likeCount + 1,
    })
  );
  async function handleClick() {
    setOptimistic(null);     // 즉시 UI 반영
    await onToggleLike();    // 서버 요청 (실패 시 자동 롤백)
  }
  return (
    <button onClick={handleClick}>
      {optimistic.liked ? '❤️' : '🤍'} {optimistic.likeCount}
    </button>
  );
}
```

### 댓글 추가 예시

```tsx
function CommentSection({ comments, addComment }) {
  const [optimisticComments, addOptimisticComment] = useOptimistic(
    comments,
    (state, newComment) => [...state, { ...newComment, pending: true }]
  );
  async function handleSubmit(formData) {
    const text = formData.get('comment');
    addOptimisticComment({ id: Date.now(), text, author: '나' });
    await addComment(text);
  }
  return (
    <ul>
      {optimisticComments.map((c) => (
        <li key={c.id} style={{ opacity: c.pending ? 0.6 : 1 }}>
          <strong>{c.author}</strong>: {c.text}
        </li>
      ))}
    </ul>
  );
}
```

---

## 3. useFormStatus (폼 상태 접근)

부모 `<form>`의 제출 상태를 **자식 컴포넌트에서 접근**할 수 있다.
반드시 `<form>` 내부의 자식 컴포넌트에서 호출해야 한다.

| 속성 | 설명 |
|------|------|
| `pending` | 폼 제출 진행 중 여부 |
| `data` | 제출된 FormData 객체 |
| `method` | HTTP 메서드 (get / post) |
| `action` | form의 action에 전달된 함수 참조 |

### 재사용 가능한 Submit 버튼

```tsx
import { useFormStatus } from 'react-dom';

function SubmitButton({ children, loadingText = '처리 중...' }) {
  const { pending } = useFormStatus();
  return (
    <button type="submit" disabled={pending}>
      {pending ? loadingText : children}
    </button>
  );
}
```

---

## 4. use() 훅 (Promise / Context 읽기)

`use()`는 **Promise**나 **Context**를 읽는 새로운 API다.
기존 훅 규칙의 예외로, `if`문이나 반복문 안에서도 호출할 수 있다.

### 데이터 페칭 (Promise + Suspense)

```tsx
import { use, Suspense } from 'react';

function UserProfile({ userPromise }) {
  const user = use(userPromise); // Suspense가 로딩 상태 처리
  return <h1>{user.name}</h1>;
}

<Suspense fallback={<p>로딩 중...</p>}>
  <UserProfile userPromise={fetchUser(id)} />
</Suspense>
```

### Context 조건부 읽기

```tsx
import { use, createContext } from 'react';
const ThemeContext = createContext('light');

function ThemedButton({ showIcon }) {
  if (showIcon) {
    const theme = use(ThemeContext); // 조건부 호출 가능
    return <button className={`btn-${theme}`}>아이콘 버튼</button>;
  }
  return <button>일반 버튼</button>;
}
```

---

## 5. React Compiler (자동 메모이제이션)

React Compiler는 **컴파일 타임에 자동으로 메모이제이션을 적용**한다.
`useMemo`, `useCallback`, `React.memo`를 수동으로 작성할 필요가 없어진다.

```tsx
// Before (React 18) — 수동 메모이제이션
const List = memo(function List({ items, onSelect }) {
  const sorted = useMemo(() => items.sort((a, b) => a.name.localeCompare(b.name)), [items]);
  const handleClick = useCallback((id) => onSelect(id), [onSelect]);
  return sorted.map((item) => <li key={item.id} onClick={() => handleClick(item.id)}>{item.name}</li>);
});

// After (React 19 + Compiler) — 자동 최적화
function List({ items, onSelect }) {
  const sorted = items.sort((a, b) => a.name.localeCompare(b.name));
  return sorted.map((item) => <li key={item.id} onClick={() => onSelect(item.id)}>{item.name}</li>);
}
```

### 설정 방법

```bash
npm install -D babel-plugin-react-compiler
```

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [
    react({ babel: { plugins: ['babel-plugin-react-compiler'] } }),
  ],
});

// next.config.ts
export default { experimental: { reactCompiler: true } };
```

> **주의:** 아직 실험적 기능이며 React 19+ 필요. 기존 `useMemo`/`useCallback`과 충돌하지 않아 점진적 적용 가능.

---

## 6. 기타 React 19 변경사항

### ref를 props로 직접 전달 (forwardRef 불필요)

```tsx
// Before (React 18) — forwardRef 필수
const Input = forwardRef((props, ref) => <input ref={ref} {...props} />);

// After (React 19) — ref를 일반 props로 전달
function Input({ ref, ...props }) {
  return <input ref={ref} {...props} />;
}
```

### Context를 직접 Provider로 사용

```tsx
// Before — Context.Provider
<ThemeContext.Provider value="dark"><App /></ThemeContext.Provider>

// After — Context 직접 사용
<ThemeContext value="dark"><App /></ThemeContext>
```

### 문서 메타데이터를 컴포넌트에서 직접 렌더링

```tsx
// Before (React 18) — react-helmet 등 외부 라이브러리 필요
<Helmet><title>{product.name}</title></Helmet>

// After (React 19) — 내장 지원
function ProductPage({ product }) {
  return (
    <>
      <title>{product.name} - 쇼핑몰</title>
      <meta name="description" content={product.description} />
      <h1>{product.name}</h1>
    </>
  );
}
```

### 스타일시트 우선순위 제어

```tsx
<link rel="stylesheet" href="/base.css" precedence="default" />
<link rel="stylesheet" href="/theme.css" precedence="high" />
```

### ref callback의 cleanup 함수

```tsx
// Before (React 18) — cleanup이 어려움
const ref = useCallback((node) => {
  if (node) { const obs = new ResizeObserver(() => {}); obs.observe(node); }
}, []);

// After (React 19) — cleanup 함수 반환
<div ref={(node) => {
  const obs = new ResizeObserver(() => {});
  obs.observe(node);
  return () => obs.disconnect();
}} />
```

### useDeferredValue에 initialValue 추가

```tsx
const deferredQuery = useDeferredValue(query, ''); // 초기값 지정 가능
```

---

## 7. React 18 → 19 마이그레이션

### 업그레이드 설치

```bash
npm install react@19 react-dom@19
npm install -D @types/react@19 @types/react-dom@19
```

### 주요 Breaking Changes

| 변경 사항 | React 18 | React 19 |
|-----------|----------|----------|
| ref 전달 | `forwardRef()` 필수 | props로 직접 전달 |
| Context Provider | `<Ctx.Provider>` | `<Ctx>` |
| `useFormState` | `react-dom` 소속 | `useActionState`로 변경, `react` 소속 |
| 문서 메타데이터 | 외부 라이브러리 | 내장 `<title>`, `<meta>` |
| ref callback cleanup | 미지원 | cleanup 함수 반환 가능 |

### 단계별 마이그레이션

```tsx
// 1단계: forwardRef 제거
// 변경 전
const MyInput = forwardRef<HTMLInputElement, Props>((props, ref) => <input ref={ref} {...props} />);
// 변경 후
function MyInput({ ref, ...props }: Props & { ref?: React.Ref<HTMLInputElement> }) {
  return <input ref={ref} {...props} />;
}

// 2단계: Context.Provider 전환
// 변경 전: <AuthContext.Provider value={authState}>
// 변경 후: <AuthContext value={authState}>

// 3단계: useFormState → useActionState
// 변경 전: import { useFormState } from 'react-dom';
// 변경 후: import { useActionState } from 'react';
```

### 호환성 체크리스트

- [ ] `forwardRef` → ref props 방식으로 전환
- [ ] `Context.Provider` → `Context`로 전환
- [ ] `useFormState` → `useActionState`로 전환
- [ ] `ReactDOM.render` → `createRoot` 전환 확인 (React 18에서 완료 필요)
- [ ] 테스트 라이브러리 호환성 확인 (`@testing-library/react` v15+)
- [ ] TypeScript 타입 정의 업데이트 (`@types/react@19`)
- [ ] 서드파티 라이브러리의 React 19 지원 여부 확인
