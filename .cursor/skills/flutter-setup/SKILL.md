---
name: flutter-setup
description: >
  Flutter 개발 환경의 설치·설정·문제 해결을 안내하는 스킬. 사용자가 "Flutter 설치해줘",
  "flutter doctor 에러 해결", "에뮬레이터 설정", "Riverpod 세팅", "freezed 설정",
  "Flutter 패키지 추가", "Flutter Web/Desktop 빌드" 등 Flutter 개발 환경 구축이나
  설정 문제를 언급하면 반드시 이 스킬을 사용하세요. SDK 설치(Windows/Linux/macOS),
  IDE 설정, FVM 버전 관리, 상태관리(Riverpod/Bloc/Provider), 필수 패키지(Dio/GoRouter/Hive),
  로깅, Web/Desktop 플랫폼 설정까지 전 범위를 커버합니다.
  단, Flutter 앱의 비즈니스 로직 구현, UI 위젯 코딩, 특정 앱 기능 개발은 이 스킬의 범위가 아닙니다.
---

# Flutter 환경 설정 스킬

Flutter 개발 환경의 **설치 → IDE → 에뮬레이터 → 상태관리 → 패키지 → 로깅 → Web/Desktop**까지 전 단계를 안내하는 스킬입니다.

## 작동 방식

1. 사용자 상황 파악 (신규 설치 vs 기존 환경 문제 vs 특정 주제)
2. OS 확인 (Windows / Linux / macOS)
3. 해당 참조 파일 로드 후 단계별 안내
4. flutter doctor 출력 분석 시 → 전체 붙여넣기 요청 후 항목별 해결

---

## ⚡ 시작 전 버전 확인 절차

### 상황 1: 새 프로젝트 (설치된 환경 없음)

→ 디폴트 버전으로 진행. 바로 `상황별 대응 가이드` 참조.

### 상황 2: 기존 Flutter 프로젝트가 있는 경우

기존 환경에서 추가 설정을 요청하면 먼저 버전을 확인한다.

```bash
flutter --version    # Flutter SDK · Dart 버전
dart --version       # Dart 단독 확인
cat pubspec.yaml     # 의존성 버전 확인
```

버전이 디폴트와 다르면 진행 전에 확인한다:

```
감지된 환경:
  - Flutter: 3.22.x  (디폴트: 3.27.x)
  - Dart:    3.4.x   (디폴트: 3.6.x)

어떤 버전 기준으로 진행할까요?
  A) 현재 설치된 버전 유지
  B) 디폴트 최신 버전으로 업그레이드
```

---

## 디폴트 버전 스택

> 최종 확인일: 2026-04-11

| 구성요소    | 버전    | 비고                  |
| ----------- | ------- | --------------------- |
| Flutter SDK | 3.41.x  | Stable 채널           |
| Dart        | 3.11.x  | Flutter 내장          |
| Riverpod    | 3.x     | 상태관리              |
| Dio         | 5.9.x   | HTTP 클라이언트       |
| GoRouter    | 17.x    | 라우팅                |
| freezed     | 3.x     | 코드 생성 (불변 모델) |
| Hive        | 2.2.x   | 로컬 저장소           |
| ScreenUtil  | 5.9.x   | 반응형 UI             |

---

## 상황별 대응 가이드

사용자 요청에서 키워드를 감지하면 해당 레퍼런스를 **Read 도구로 로드**한다.

### 신규 설치 요청

→ OS 확인 후 참조 파일을 Read 도구로 로드

> **트리거**: Flutter 설치, SDK 설치, 개발 환경 구축, 시작하기

| OS             | 참조 파일                     |
| -------------- | ----------------------------- |
| Windows        | `references/windows-setup.md` |
| Linux (Ubuntu) | `references/linux-setup.md`   |
| macOS          | `references/macos-setup.md`   |

### flutter doctor 에러 분석

→ `references/flutter-doctor-fixes.md` 로드

> **트리거**: flutter doctor, [✗], [!], Android toolchain, cmdline-tools

- `flutter doctor -v` 전체 출력 붙여넣기 요청
- [✗] / [!] 항목을 우선순위 순서대로 해결

### FVM (Flutter Version Manager)

→ `references/fvm-guide.md` 로드

- 프로젝트별 Flutter 버전 고정
- CI/CD (GitHub Actions) 연동 포함

### IDE 설정

→ `references/ide-setup.md` 로드

- Android Studio 플러그인, 단축키, launch.json
- VS Code 설정, analysis_options.yaml

### 에뮬레이터 설정

→ `references/emulator-setup.md` 로드

- Android AVD Manager (Windows / Linux)
- 물리 기기 USB 디버깅 연결

### 상태관리 설정

→ `references/state-management.md` 로드

> **트리거**: Riverpod, Bloc, Provider, 상태관리, StateNotifier, ChangeNotifier

- Riverpod / Bloc / Provider 초기 설정 및 구조
- 프로젝트 규모별 선택 가이드

### freezed (불변 모델 / Union 타입)

→ `references/freezed-guide.md` 로드

- 불변 데이터 클래스 생성 (copyWith, ==, toString 자동)
- sealed union 타입 + 패턴 매칭
- json_serializable 연동
- Riverpod / Bloc 상태 정의 패턴

### 필수 패키지 설정

→ `references/flutter-packages.md` 로드

> **트리거**: 패키지 추가, Dio, Retrofit, GoRouter, Hive, SharedPreferences, ScreenUtil, json_serializable

- **네트워크**: Dio + Retrofit + Pretty Dio Logger
- **JSON**: json_serializable
- **로컬 저장소**: SharedPreferences / Hive
- **라우팅**: GoRouter + Riverpod 연동
- **UI**: ScreenUtil / CachedNetworkImage / Flutter SVG
- **유틸**: intl / uuid / collection

### 로깅 & 디버깅

→ `references/flutter-logging.md` 로드

- logger 패키지 설정
- flutter_bloc 이벤트 로그
- 릴리즈 빌드 로그 제거 전략

### Web & Desktop 플랫폼

→ `references/web-desktop-setup.md` 로드

- **Flutter Web**: Chrome 실행, 렌더러 선택, 배포
- **Windows Desktop**: Visual Studio 설정, MSIX 패키징
- **Linux Desktop**: GTK 의존성, AppImage/Snap 배포
- **macOS Desktop**: Xcode 연동

---

## 공통 환경 변수 체크리스트

```bash
# Flutter SDK 경로 확인
flutter --version

# 환경 변수 확인
echo $PATH                # Linux / macOS
$env:PATH                 # Windows PowerShell

# 전체 진단
flutter doctor -v
```

---

## 응답 형식 가이드

- 단계는 **번호 목록**으로 명확하게 제시
- 명령어는 반드시 **코드 블록** 사용
- 에러 해결 시 → **원인 → 해결책** 순서
- Windows는 PowerShell / CMD 명령어 구분 표기
- UI 경로는 **[메뉴 > 하위메뉴]** 형식으로 표시
- Linux는 Ubuntu 22.04 LTS / 24.04 LTS 기준
