# Flutter Web & Desktop 설정 가이드

---

## Flutter Web

### 사전 요구사항

| 항목        | 요구사항                    |
| ----------- | --------------------------- |
| Flutter SDK | 3.0 이상 (stable)           |
| 브라우저    | Chrome 권장 (DevTools 지원) |

### 활성화 및 프로젝트 생성

```bash
# Web 지원 활성화 확인
flutter config --enable-web

# 새 프로젝트 생성 (Web 포함)
flutter create my_web_app
cd my_web_app

# 기존 프로젝트에 Web 지원 추가
flutter create --platforms web .
```

### 실행 및 빌드

```bash
# Chrome에서 개발 모드 실행
flutter run -d chrome

# 특정 포트 지정
flutter run -d chrome --web-port=8080

# 릴리즈 빌드
flutter build web

# 빌드 결과물: build/web/
```

### Web 렌더러 선택

| 렌더러          | 특징                                      | 적합한 용도                       |
| --------------- | ----------------------------------------- | --------------------------------- |
| **CanvasKit**   | 높은 렌더링 품질, 큰 다운로드 크기 (~2MB) | 그래픽 중심 앱, 데스크톱 브라우저 |
| **HTML**        | 작은 다운로드 크기, 텍스트 렌더링 우수    | 텍스트 중심 앱, SEO 중요 시       |
| **auto** (기본) | 모바일→HTML, 데스크톱→CanvasKit 자동 선택 | 일반적 용도                       |

```bash
# 렌더러 지정 실행
flutter run -d chrome --web-renderer canvaskit
flutter run -d chrome --web-renderer html

# 빌드 시 렌더러 지정
flutter build web --web-renderer canvaskit
```

### Web 전용 설정

```dart
// Web 환경 감지
import 'package:flutter/foundation.dart' show kIsWeb;

if (kIsWeb) {
  // Web 전용 로직
}
```

#### index.html 커스터마이징

`web/index.html`에서 로딩 화면, SEO 메타태그, favicon 등을 설정:

```html
<!-- web/index.html -->
<head>
  <meta name="description" content="My Flutter Web App" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/png" href="favicon.png" />
  <title>My App</title>
</head>
```

### Web 배포

```bash
# Firebase Hosting
firebase init hosting
flutter build web
firebase deploy

# GitHub Pages
flutter build web --base-href "/repo-name/"
# build/web/ 디렉토리를 gh-pages 브랜치에 배포

# Nginx / Apache
# build/web/ 디렉토리를 웹서버 루트에 복사
```

### Web 자주 발생하는 문제

#### CORS 에러

```
→ 백엔드 서버에서 CORS 헤더 설정 필요
→ 개발 시 프록시 사용: flutter run -d chrome --web-browser-flag "--disable-web-security" (임시)
```

#### 플러그인 Web 미지원

```
→ pub.dev에서 플랫폼 지원 확인 (Web 아이콘 체크)
→ 대안: conditional import로 플랫폼별 구현 분리
```

#### 이미지/폰트 로딩 실패

```
→ assets 경로가 pubspec.yaml에 등록되었는지 확인
→ Web에서는 CORS 정책으로 외부 이미지 로딩 제한될 수 있음
```

---

## Flutter Desktop

### Windows Desktop

#### 사전 요구사항

| 항목               | 요구사항                                         |
| ------------------ | ------------------------------------------------ |
| OS                 | Windows 10 이상 (64-bit)                         |
| Visual Studio 2022 | **"Desktop development with C++"** 워크로드 필수 |
| Flutter SDK        | 3.0 이상 (stable)                                |

#### 설정

```powershell
# Windows Desktop 활성화
flutter config --enable-windows-desktop

# 새 프로젝트 생성
flutter create my_desktop_app

# 기존 프로젝트에 Windows 지원 추가
flutter create --platforms windows .

# 실행
flutter run -d windows

# 릴리즈 빌드
flutter build windows
# 결과물: build\windows\x64\runner\Release\
```

#### Visual Studio 워크로드 확인

```powershell
# Visual Studio Installer에서:
# 1. "Desktop development with C++" 워크로드 체크
# 2. 개별 구성요소에서 "Windows 10/11 SDK" 확인
```

#### Windows 설치 파일 생성 (MSIX)

```yaml
# pubspec.yaml
msix_config:
  display_name: My App
  publisher_display_name: My Company
  identity_name: com.mycompany.myapp
  msix_version: 1.0.0.0
  logo_path: assets/icon.png
```

```powershell
# msix 패키지 생성
dart pub add --dev msix
dart run msix:create
```

---

### Linux Desktop

#### 사전 요구사항

```bash
# Ubuntu 22.04 / 24.04 기준
sudo apt update
sudo apt install -y clang cmake ninja-build pkg-config \
  libgtk-3-dev liblzma-dev libstdc++-12-dev
```

#### 설정

```bash
# Linux Desktop 활성화
flutter config --enable-linux-desktop

# 기존 프로젝트에 Linux 지원 추가
flutter create --platforms linux .

# 실행
flutter run -d linux

# 릴리즈 빌드
flutter build linux
# 결과물: build/linux/x64/release/bundle/
```

#### Linux 배포 (AppImage / Snap)

```bash
# AppImage 생성 (appimage_tool 패키지 사용)
dart pub add --dev appimage_tool
dart run appimage_tool:create

# Snap 패키지 (snapcraft.yaml 필요)
snapcraft
```

---

### macOS Desktop

#### 사전 요구사항

| 항목      | 요구사항           |
| --------- | ------------------ |
| macOS     | 12 (Monterey) 이상 |
| Xcode     | 14 이상            |
| CocoaPods | 설치 필요          |

#### 설정

```bash
# macOS Desktop 활성화
flutter config --enable-macos-desktop

# 기존 프로젝트에 macOS 지원 추가
flutter create --platforms macos .

# 실행
flutter run -d macos

# 릴리즈 빌드
flutter build macos
```

---

## Desktop 공통 팁

### 윈도우 크기 설정

```dart
// main.dart (desktop 전용)
import 'dart:io' show Platform;
import 'package:flutter/material.dart';
import 'package:window_manager/window_manager.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (Platform.isWindows || Platform.isLinux || Platform.isMacOS) {
    await windowManager.ensureInitialized();
    await windowManager.setSize(const Size(1280, 720));
    await windowManager.setMinimumSize(const Size(800, 600));
    await windowManager.center();
    await windowManager.setTitle('My App');
  }

  runApp(const MyApp());
}
```

```yaml
# pubspec.yaml
dependencies:
  window_manager: ^0.4.3
```

### 플랫폼별 조건부 코드

```dart
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

bool get isDesktop =>
    !kIsWeb && (Platform.isWindows || Platform.isLinux || Platform.isMacOS);

bool get isMobile =>
    !kIsWeb && (Platform.isAndroid || Platform.isIOS);
```

### Desktop 자주 발생하는 문제

#### Windows: "Visual Studio not installed"

```
→ Visual Studio 2022 Community 설치
→ "Desktop development with C++" 워크로드 선택
→ flutter doctor로 확인
```

#### Linux: "cmake not found" / "ninja not found"

```bash
sudo apt install -y cmake ninja-build pkg-config libgtk-3-dev
```

#### 플러그인 Desktop 미지원

```
→ pub.dev에서 플랫폼 지원 확인
→ 미지원 시: platform channel로 네이티브 코드 직접 연동
→ 또는 conditional import로 플랫폼별 구현 분리
```# 플러그인 Desktop 미지원

```
→ pub.dev에서 플랫폼 지원 확인
→ 미지원 시: platform channel로 네이티브 코드 직접 연동
→ 또는 conditional import로 플랫폼별 구현 분리
```
