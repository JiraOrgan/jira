# React 앱 배포 가이드

> Vite 기반 React 앱을 다양한 플랫폼에 배포하는 방법 정리

---

## 1. Vercel (권장 — Next.js 최적)

### CLI 설치 및 배포

```bash
# Vercel CLI 전역 설치
npm install -g vercel

# 최초 배포 (프로젝트 루트에서 실행)
vercel

# 프로덕션 배포
vercel --prod
```

### vercel.json 설정

```json
{
  "buildCommand": "npm run build",
  "outputDirectory": "dist",
  "framework": "vite",
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ],
  "redirects": [
    { "source": "/old-page", "destination": "/new-page", "permanent": true }
  ],
  "headers": [
    {
      "source": "/assets/(.*)",
      "headers": [
        { "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }
      ]
    }
  ],
  "env": {
    "VITE_APP_ENV": "production"
  }
}
```

### GitHub 연동 자동 배포

```bash
# GitHub 레포지토리 연결 (최초 1회)
vercel link

# 이후 main 브랜치 push → 자동 프로덕션 배포
# PR 생성 → 자동 Preview URL 생성
```

**Preview / Production 환경 분리**

| 브랜치 | 환경 | URL |
|---|---|---|
| `main` | Production | `https://my-app.vercel.app` |
| PR / feature/* | Preview | `https://my-app-git-feature-xxx.vercel.app` |

- Vercel 대시보드 → Settings → Environment Variables 에서 환경별 변수 분리 설정
- `VITE_API_BASE_URL` 을 Production / Preview / Development 각각 다르게 지정

---

## 2. Netlify

### netlify.toml 설정

```toml
[build]
  command   = "npm run build"
  publish   = "dist"

[build.environment]
  NODE_VERSION = "20"

# SPA 라우팅 — 모든 경로를 index.html로 처리
[[redirects]]
  from   = "/*"
  to     = "/index.html"
  status = 200

# API 프록시 (선택 — 백엔드 URL 숨김)
[[redirects]]
  from   = "/api/*"
  to     = "https://api.example.com/:splat"
  status = 200
  force  = true

# 캐시 헤더 설정
[[headers]]
  for = "/assets/*"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"
```

### _redirects 파일 (간단 방식)

```
# public/_redirects
/*    /index.html   200
```

> `netlify.toml` 과 `_redirects` 가 동시에 존재하면 `netlify.toml` 이 우선 적용됨

### 환경변수 관리

```bash
# Netlify CLI로 환경변수 설정
npm install -g netlify-cli
netlify env:set VITE_API_BASE_URL https://api.example.com

# .env.production 파일은 Git에 포함하지 않고
# Netlify 대시보드 → Site Settings → Environment Variables 에서 직접 입력
```

---

## 3. GitHub Pages

### vite.config.ts base 설정

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // 레포지토리명과 일치시킨다 — https://username.github.io/my-app/
  base: '/my-app/',
});
```

> 커스텀 도메인 사용 시 `base: '/'` 로 설정

### GitHub Actions 워크플로우

```yaml
# .github/workflows/deploy.yml
name: Deploy to GitHub Pages

on:
  push:
    branches: [main]

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: pages
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Node.js 설정
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: npm

      - name: 의존성 설치
        run: npm ci

      - name: 빌드
        run: npm run build
        env:
          VITE_API_BASE_URL: ${{ secrets.VITE_API_BASE_URL }}

      - name: Pages 아티팩트 업로드
        uses: actions/upload-pages-artifact@v3
        with:
          path: dist

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - name: GitHub Pages 배포
        id: deployment
        uses: actions/deploy-pages@v4
```

> GitHub 레포지토리 → Settings → Pages → Source 를 **GitHub Actions** 로 변경 필요

### SPA 라우팅 404 해결 (404.html 트릭)

```bash
# public/404.html — index.html 과 동일한 내용으로 복사
cp public/index.html public/404.html
# 또는 vite.config.ts 의 rollupOptions 에서 빌드 후 복사 스크립트 추가
```

```typescript
// vite.config.ts — 빌드 후 404.html 자동 생성
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { copyFileSync } from 'fs';

export default defineConfig({
  plugins: [
    react(),
    {
      name: 'copy-404',
      closeBundle() {
        copyFileSync('dist/index.html', 'dist/404.html');
      },
    },
  ],
  base: '/my-app/',
});
```

---

## 4. Docker + Nginx (자체 서버)

### Multi-stage Dockerfile

```dockerfile
# ── 1단계: Node.js 빌드 ──────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# 의존성 레이어 캐시 최적화 (package.json 변경 시에만 재설치)
COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ── 2단계: Nginx 서빙 ────────────────────────────────────
FROM nginx:1.25-alpine

# 빌드 결과물 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx 설정 교체
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf (SPA 라우팅)

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # 정적 에셋 캐시 (Vite 해시 파일명)
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 프록시 (선택 — 백엔드가 별도 컨테이너인 경우)
    location /api/ {
        proxy_pass         http://backend:8080/;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
    }

    # SPA 라우팅 — 404 대신 index.html 반환
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 보안 헤더
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header Referrer-Policy strict-origin-when-cross-origin;
}
```

### docker-compose.yml

```yaml
services:
  frontend:
    build:
      context: .
      args:
        VITE_API_BASE_URL: https://api.example.com/api/v1
    ports:
      - "3000:80"
    restart: unless-stopped

  # 백엔드 컨테이너와 함께 사용하는 경우
  backend:
    image: my-backend:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: unless-stopped
```

```bash
# 빌드 및 실행
docker compose up --build -d

# 로그 확인
docker compose logs -f frontend

# 중지
docker compose down
```

### SSL/HTTPS — Let's Encrypt (간단 언급)

```bash
# Certbot + Nginx 조합으로 무료 SSL 발급
docker run --rm -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot certonly --standalone -d example.com

# nginx.conf 에서 443 포트 + ssl_certificate 경로 추가 후 재시작
docker compose restart frontend
```

---

## 5. 배포 전 체크리스트

### 빌드 검증

```bash
# 타입 체크 → 빌드 → 로컬 미리보기
npm run type-check && npm run build && npm run preview
```

### 환경변수 확인

```dotenv
# .env.production — 실제 운영 값으로 채워졌는지 확인
VITE_API_BASE_URL=https://api.example.com/api/v1
VITE_APP_ENV=production
VITE_ENABLE_DEVTOOLS=false
VITE_MSW_ENABLED=false
```

```bash
# 빌드된 번들에 민감한 값이 노출됐는지 확인 (dist 폴더 검색)
grep -r "localhost" dist/assets/
grep -r "secret" dist/assets/
```

### 번들 사이즈 분석

```bash
# 번들 시각화 — 무거운 패키지 파악
npx vite-bundle-visualizer

# 또는 rollup-plugin-visualizer 설치 후 vite.config.ts 에 추가
npm install -D rollup-plugin-visualizer
```

```typescript
// vite.config.ts — 빌드 시 번들 분석 리포트 생성
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    react(),
    visualizer({ open: true, filename: 'dist/bundle-report.html' }),
  ],
});
```

### public/ 필수 파일 체크

```
public/
├── favicon.ico          # 브라우저 탭 아이콘
├── favicon.svg          # 고해상도 SVG 아이콘
├── apple-touch-icon.png # iOS 홈 화면 아이콘 (180×180)
├── robots.txt           # 검색 엔진 크롤링 규칙
├── sitemap.xml          # SEO 사이트맵 (선택)
└── og-image.png         # SNS 공유 썸네일 (1200×630)
```

```html
<!-- index.html — SEO 메타태그 최소 구성 -->
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="description" content="앱 설명을 여기에 입력" />

  <!-- OG (Open Graph) -->
  <meta property="og:title" content="앱 이름" />
  <meta property="og:description" content="앱 설명" />
  <meta property="og:image" content="/og-image.png" />
  <meta property="og:type" content="website" />

  <title>앱 이름</title>
  <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
</head>
```

```
# public/robots.txt
User-agent: *
Allow: /
Disallow: /admin/

Sitemap: https://example.com/sitemap.xml
```
