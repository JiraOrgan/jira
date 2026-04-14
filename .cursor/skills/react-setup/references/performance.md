# React 성능 최적화 가이드

React 애플리케이션의 규모가 커질수록 성능 최적화는 사용자 경험에 직접적인 영향을 미친다.
이 문서는 실무에서 자주 사용하는 최적화 기법을 체계적으로 정리한다.

---

## 1. 메모이제이션 전략

메모이제이션은 이전 계산 결과를 캐싱하여 불필요한 재계산이나 리렌더링을 방지하는 기법이다.
React는 세 가지 메모이제이션 도구를 제공한다.

### React.memo — 컴포넌트 리렌더링 방지

`React.memo`는 컴포넌트를 감싸서 props가 변경되지 않으면 리렌더링을 건너뛴다.

**언제 사용하는가**

- 렌더링 비용이 높은 컴포넌트 (복잡한 DOM 트리, 많은 자식 요소)
- props가 자주 바뀌지 않는 컴포넌트
- 부모가 자주 리렌더되지만 자식에게 전달하는 props는 안정적인 경우

**언제 사용하지 말아야 하는가**

- props가 거의 매번 바뀌는 컴포넌트 (비교 비용만 추가됨)
- 렌더링 비용이 매우 낮은 단순 컴포넌트
- children을 전달받는 컴포넌트 (children은 매번 새 참조)

```tsx
const UserCard = React.memo(function UserCard({ user, onSelect }: Props) {
  return (
    <div onClick={() => onSelect(user.id)}>
      <h3>{user.name}</h3>
      <p>{user.email}</p>
    </div>
  );
});
```

**커스텀 비교 함수**

기본 얕은 비교 대신 직접 비교 로직을 제공할 수 있다.

```tsx
const UserCard = React.memo(
  function UserCard({ user, onSelect }: Props) {
    return (
      <div onClick={() => onSelect(user.id)}>
        <h3>{user.name}</h3>
        <p>{user.email}</p>
      </div>
    );
  },
  (prevProps, nextProps) => {
    // true를 반환하면 리렌더링을 건너뜀
    return prevProps.user.id === nextProps.user.id
        && prevProps.user.name === nextProps.user.name;
  }
);
```

> 커스텀 비교 함수는 버그 가능성을 높이므로, 꼭 필요한 경우에만 사용한다.

---

### useMemo — 비용이 높은 계산 캐싱

`useMemo`는 의존성이 변경될 때만 계산을 다시 수행한다.

**사용 기준**

- 계산 비용이 높은 경우에만 사용 (정렬, 필터링 + 대량 데이터, 복잡한 변환)
- 단순 필터링이나 간단한 연산에는 불필요
- 의존성 배열이 정확해야 효과가 있음

```tsx
const sortedUsers = useMemo(
  () => users.toSorted((a, b) => a.name.localeCompare(b.name)),
  [users]
);
```

**의존성 배열 관리 주의사항**

```tsx
// BAD — 매 렌더마다 options 객체가 새로 생성됨
const result = useMemo(() => process(data, options), [data, options]);

// GOOD — 원시값을 의존성으로 사용
const result = useMemo(
  () => process(data, { limit, offset }),
  [data, limit, offset]
);
```

---

### useCallback — 함수 참조 안정화

`useCallback`은 함수의 참조를 안정적으로 유지한다.
**React.memo로 감싼 자식 컴포넌트에 함수를 전달할 때만 의미가 있다.**

```tsx
const handleDelete = useCallback((id: number) => {
  setUsers(prev => prev.filter(u => u.id !== id));
}, []);
```

`useCallback` 없이 함수를 전달하면 매 렌더마다 새 함수 참조가 생성되어
`React.memo`의 비교가 실패한다.

```tsx
// BAD — handleClick이 매번 새로 생성되어 MemoizedButton의 memo가 무효화됨
function Parent() {
  const handleClick = () => doSomething();
  return <MemoizedButton onClick={handleClick} />;
}

// GOOD — 참조가 안정적으로 유지됨
function Parent() {
  const handleClick = useCallback(() => doSomething(), []);
  return <MemoizedButton onClick={handleClick} />;
}
```

---

### React Compiler와의 관계

- React 19의 React Compiler가 자동으로 메모이제이션을 처리하는 방향으로 발전 중
- 수동 `useMemo`, `useCallback`, `React.memo`는 점진적으로 불필요해질 예정
- 현재는 Compiler가 아직 실험적 단계이므로 수동 관리가 권장됨
- Compiler 도입 시 기존 수동 메모이제이션 코드는 그대로 두어도 충돌하지 않음

---

## 2. Code Splitting & Lazy Loading

초기 번들 크기를 줄여 첫 로딩 속도를 개선하는 핵심 전략이다.

### React.lazy + Suspense

페이지 단위로 코드를 분할하여 방문할 때만 로드한다.

```tsx
const AdminPage = lazy(() => import('@/pages/admin/AdminPage'));

// 라우터에서 사용
{
  path: '/admin',
  element: (
    <Suspense fallback={<PageSkeleton />}>
      <AdminPage />
    </Suspense>
  ),
}
```

- `fallback`에는 스켈레톤 UI나 스피너를 제공
- 페이지 전환 시 깜빡임을 줄이려면 `startTransition`과 함께 사용

---

### 조건부 로딩 (대형 라이브러리)

차트, 에디터, 지도 같은 무거운 라이브러리는 실제로 필요한 시점에만 로드한다.

```tsx
// 차트 라이브러리를 필요할 때만 로드
const ChartComponent = lazy(() => import('@/components/Chart'));

function Dashboard({ showChart }) {
  return (
    <div>
      <Stats />
      {showChart && (
        <Suspense fallback={<ChartSkeleton />}>
          <ChartComponent />
        </Suspense>
      )}
    </div>
  );
}
```

이렇게 하면 `showChart`가 `false`인 동안 차트 관련 코드가 전혀 로드되지 않는다.

---

### Vite manualChunks로 벤더 청크 분리

자주 변경되지 않는 라이브러리를 별도 청크로 분리하면 캐싱 효율이 높아진다.

```typescript
// vite.config.ts
build: {
  rollupOptions: {
    output: {
      manualChunks: {
        'react-vendor': ['react', 'react-dom'],
        'router': ['react-router-dom'],
        'query': ['@tanstack/react-query'],
      },
    },
  },
},
```

- `react-vendor` 청크는 React 버전을 올리지 않는 한 브라우저 캐시에 유지됨
- 앱 코드만 변경될 때 벤더 청크를 다시 다운로드하지 않아도 됨

---

## 3. 번들 분석

최적화의 첫 단계는 현재 번들의 구성을 파악하는 것이다.

### rollup-plugin-visualizer

빌드 결과를 트리맵으로 시각화하여 큰 모듈을 한눈에 확인할 수 있다.

```bash
npm install -D rollup-plugin-visualizer
```

```typescript
// vite.config.ts
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    react(),
    visualizer({
      open: true,
      filename: 'bundle-stats.html',
      gzipSize: true,
    }),
  ],
});
```

**분석 시 확인할 포인트**

- 예상보다 큰 라이브러리가 포함되어 있지 않은지
- 동일 라이브러리가 여러 버전으로 중복 포함되지 않았는지
- 트리쉐이킹이 제대로 동작하지 않는 모듈이 있는지
- 사용하지 않는 라이브러리가 번들에 포함되어 있지 않은지

### source-map-explorer (대안)

소스맵 기반으로 번들 구성을 분석한다.

```bash
npx source-map-explorer dist/assets/*.js
```

- `rollup-plugin-visualizer`보다 가벼운 대안
- 소스맵이 활성화되어 있어야 사용 가능

---

## 4. 리렌더링 최적화

React에서 가장 흔한 성능 문제는 불필요한 리렌더링이다.
메모이제이션 이전에 컴포넌트 구조 자체를 개선하는 것이 더 효과적이다.

### 상태 끌어올리기 vs 상태 내리기

자주 변하는 상태는 가능한 한 하위 컴포넌트에 배치하여
불필요한 리렌더 전파를 방지한다.

```tsx
// BAD — 전체 페이지가 리렌더됨
function Page() {
  const [search, setSearch] = useState('');
  return (
    <div>
      <input value={search} onChange={e => setSearch(e.target.value)} />
      <HeavyList /> {/* search와 무관하지만 리렌더됨 */}
    </div>
  );
}

// GOOD — 검색 입력을 별도 컴포넌트로 분리
function SearchInput() {
  const [search, setSearch] = useState('');
  return <input value={search} onChange={e => setSearch(e.target.value)} />;
}

function Page() {
  return (
    <div>
      <SearchInput />
      <HeavyList /> {/* 리렌더 안 됨 */}
    </div>
  );
}
```

이 패턴은 메모이제이션 없이도 리렌더링을 효과적으로 방지한다.

---

### Zustand selector로 구독 최소화

전역 상태 관리 라이브러리를 사용할 때, 스토어 전체를 구독하면
관련 없는 상태 변경에도 리렌더링이 발생한다.

```tsx
// BAD — 스토어 전체 구독 (어떤 필드가 바뀌어도 리렌더)
const store = useAuthStore();

// GOOD — 필요한 필드만 구독
const isAuthenticated = useAuthStore(s => s.isAuthenticated);
const userName = useAuthStore(s => s.userName);
```

여러 필드가 필요한 경우 `useShallow`를 활용할 수 있다.

```tsx
import { useShallow } from 'zustand/react/shallow';

const { isAuthenticated, userName } = useAuthStore(
  useShallow(s => ({ isAuthenticated: s.isAuthenticated, userName: s.userName }))
);
```

---

### children 패턴으로 리렌더 차단

`children`으로 전달된 요소는 부모의 리렌더와 무관하게 유지된다.
이는 React의 reconciliation 메커니즘 덕분이다.

```tsx
function MotionWrapper({ children }) {
  const [scroll, setScroll] = useState(0);

  useEffect(() => {
    const handler = () => setScroll(window.scrollY);
    window.addEventListener('scroll', handler);
    return () => window.removeEventListener('scroll', handler);
  }, []);

  return (
    <div style={{ transform: `translateY(${scroll * 0.5}px)` }}>
      {children}
    </div>
  );
}

// 사용 — HeavyContent는 scroll 변경에 리렌더되지 않음
<MotionWrapper>
  <HeavyContent />
</MotionWrapper>
```

`children`은 부모 컴포넌트가 아닌 그 위의 호출자가 생성하므로,
`MotionWrapper`의 상태 변경이 `children`의 리렌더를 유발하지 않는다.

---

## 5. 이미지 최적화

이미지는 웹 페이지에서 가장 큰 리소스를 차지하는 경우가 많다.
적절한 최적화로 LCP와 전체 로딩 시간을 크게 개선할 수 있다.

### loading="lazy" 네이티브 지연 로딩

뷰포트에 들어올 때만 이미지를 로드한다.

```tsx
<img
  src={user.avatar}
  alt={user.name}
  loading="lazy"
  decoding="async"
/>
```

- `loading="lazy"` — 뷰포트 근처에 도달할 때 로드 시작
- `decoding="async"` — 이미지 디코딩을 메인 스레드와 분리
- **히어로 이미지 등 최초 뷰포트에 보이는 이미지에는 사용하지 않는다** (LCP 지연 원인)

---

### 반응형 이미지 (srcSet)

디바이스 크기에 맞는 이미지를 제공하여 불필요한 대역폭 사용을 줄인다.

```tsx
<img
  src="/images/hero-800.webp"
  srcSet="
    /images/hero-400.webp 400w,
    /images/hero-800.webp 800w,
    /images/hero-1200.webp 1200w
  "
  sizes="(max-width: 768px) 400px, (max-width: 1024px) 800px, 1200px"
  alt="히어로 이미지"
  loading="lazy"
/>
```

- `srcSet` — 브라우저가 디바이스 해상도에 맞는 이미지를 선택
- `sizes` — 레이아웃에서 이미지가 차지하는 크기를 브라우저에 힌트로 제공

---

### WebP/AVIF 포맷 사용

차세대 이미지 포맷은 JPEG/PNG 대비 30~50% 작은 파일 크기를 제공한다.

- **WebP** — 대부분의 모던 브라우저에서 지원
- **AVIF** — WebP보다 더 높은 압축률, 지원 범위가 점차 확대 중
- Vite에서 빌드 시 자동 변환: `vite-plugin-image-optimizer` 플러그인 활용

```bash
npm install -D vite-plugin-image-optimizer
```

---

## 6. Web Vitals 측정

성능 최적화는 측정에서 시작한다.
Google의 Core Web Vitals를 기준으로 실제 사용자 경험을 수치화한다.

```bash
npm install web-vitals
```

```typescript
// src/utils/webVitals.ts
import { onCLS, onINP, onLCP, onFCP, onTTFB } from 'web-vitals';

function reportVitals(metric) {
  console.log(metric.name, metric.value);
  // 분석 서비스로 전송 (Google Analytics, Datadog 등)
}

onCLS(reportVitals);   // Cumulative Layout Shift — 레이아웃 변동
onINP(reportVitals);   // Interaction to Next Paint — 상호작용 응답성 (FID 대체)
onLCP(reportVitals);   // Largest Contentful Paint — 최대 콘텐츠 렌더링
onFCP(reportVitals);   // First Contentful Paint — 첫 콘텐츠 렌더링
onTTFB(reportVitals);  // Time to First Byte — 서버 응답 시간
```

### 목표 수치

| 지표 | Good | Needs Improvement | Poor |
|------|------|-------------------|------|
| LCP | <= 2.5s | <= 4.0s | > 4.0s |
| INP | <= 200ms | <= 500ms | > 500ms |
| CLS | <= 0.1 | <= 0.25 | > 0.25 |

- **LCP** — 히어로 이미지 최적화, 서버 응답 시간 단축, 렌더 블로킹 리소스 제거
- **INP** — 긴 작업 분할, 메인 스레드 블로킹 최소화
- **CLS** — 이미지/광고에 고정 크기 지정, 웹폰트 `font-display: swap` 적용

---

## 7. React DevTools Profiler 활용

코드 수준의 성능 병목을 식별하는 데 React DevTools Profiler가 필수적이다.

### 기본 사용법

1. React DevTools 브라우저 확장 설치
2. DevTools에서 **Profiler** 탭 선택
3. 녹화 버튼 클릭 후 문제가 되는 인터랙션 수행
4. 녹화 중지 후 결과 분석

### Flamegraph로 렌더링 병목 식별

- 각 컴포넌트의 렌더링 시간이 색상으로 표시됨
- 노란색/빨간색 컴포넌트가 병목 후보
- 회색 컴포넌트는 해당 커밋에서 렌더링되지 않은 것

### "왜 리렌더되었나?" 기능 활성화

DevTools 설정에서 **"Record why each component rendered while profiling"** 옵션을 활성화하면
각 컴포넌트가 리렌더된 이유를 확인할 수 있다.

- **Props changed** — 부모로부터 새로운 props를 받음
- **State changed** — 컴포넌트 자체의 상태가 변경됨
- **Context changed** — 구독 중인 Context 값이 변경됨
- **Parent rendered** — 부모가 리렌더되어 함께 리렌더됨

### Highlight Updates 설정

DevTools 설정에서 **"Highlight updates when components render"** 를 활성화하면
리렌더되는 컴포넌트가 실시간으로 하이라이트된다.

- 의도하지 않은 리렌더링을 시각적으로 빠르게 발견 가능
- 스크롤, 타이핑 등 연속적인 인터랙션에서 특히 유용

---

## 정리: 최적화 우선순위

성능 최적화는 측정 없이 감으로 하면 안 된다. 다음 순서를 권장한다.

1. **측정** — DevTools Profiler, Web Vitals로 병목 지점 파악
2. **구조 개선** — 상태 내리기, children 패턴 등 컴포넌트 구조 변경
3. **Code Splitting** — 페이지 단위 분할, 대형 라이브러리 조건부 로딩
4. **메모이제이션** — 구조 개선으로 해결되지 않는 경우에만 적용
5. **에셋 최적화** — 이미지 포맷 변환, 지연 로딩
6. **번들 분석** — 주기적으로 번들 크기 모니터링
