# flutter doctor 에러 해결 가이드

> **사용법**: `flutter doctor -v` 전체 출력을 붙여넣으면 아래 항목 기준으로 분석 및 해결 방법 제시

---

## 진단 항목별 해결법

### [✗] Flutter - No Flutter installation found

**Windows:**

```powershell
# PATH 환경변수에 flutter/bin 추가 확인
$env:PATH -split ';' | Select-String 'flutter'
```

**macOS/Linux:**

```bash
echo $PATH | grep flutter
# 없으면 ~/.zshrc 또는 ~/.bashrc에 export PATH 추가
```

---

### [!] Android toolchain - Android SDK not found / licenses not accepted

```bash
# SDK 라이선스 동의
flutter doctor --android-licenses

# ANDROID_HOME 환경변수 설정 확인
# Windows
echo $env:ANDROID_HOME
# macOS/Linux
echo $ANDROID_HOME
```

macOS에서 ANDROID_HOME 설정:

```bash
echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools' >> ~/.zshrc
source ~/.zshrc
```

---

### [!] Android toolchain - Android SDK is missing command line tools

1. **Android Studio** 실행
2. **[Tools] > [SDK Manager] > [SDK Tools]** 탭
3. **Android SDK Command-line Tools (latest)** 체크
4. **Apply** 클릭

---

### [✗] Android Studio (not installed)

- https://developer.android.com/studio 에서 설치
- 설치 후 Flutter/Dart 플러그인 추가:
  - **[File] > [Settings] > [Plugins]** → "Flutter" 검색 → Install

---

### [!] Xcode installation is incomplete (macOS)

```bash
# Xcode 커맨드라인 도구 재설치
sudo xcode-select --install

# 경로 재설정
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer

# 라이선스 재동의
sudo xcodebuild -license accept
```

---

### [!] CocoaPods not installed (macOS)

```bash
# 방법 1: Homebrew
brew install cocoapods

# 방법 2: gem
sudo gem install cocoapods

# 버전 확인
pod --version
```

---

### [!] HTTP Host availability (네트워크 관련)

```bash
# 프록시 없이 테스트
flutter doctor --verbose

# 중국 사용자의 경우 미러 설정
export PUB_HOSTED_URL=https://pub.flutter-io.cn
export FLUTTER_STORAGE_BASE_URL=https://storage.flutter-io.cn
```

---

### [!] VS Code - Flutter extension not installed

1. VS Code 실행
2. Extensions (Ctrl+Shift+X / Cmd+Shift+X)
3. **"Flutter"** 검색 → **Dart Code** 제작 플러그인 설치
4. VS Code 재시작

---

### [!] Connected device - No devices available

**Android 물리 기기:**

```bash
# USB 디버깅 활성화 후
adb devices
# 기기가 보이면 flutter devices로 확인
flutter devices
```

**에뮬레이터 생성:**

```bash
# 사용 가능한 에뮬레이터 목록
flutter emulators

# 에뮬레이터 생성
flutter emulators --create --name pixel_7

# 에뮬레이터 실행
flutter emulators --launch pixel_7
```

---

### [!] Windows version - below minimum recommended version

- Windows 10 버전 1903 이상 필요
- **[설정] > [Windows 업데이트]** 에서 업데이트

---

### [✗] Visual Studio - not installed (Windows 데스크톱 개발)

1. https://visualstudio.microsoft.com/downloads/ 에서 Community 설치
2. 설치 시 **"Desktop development with C++"** 워크로드 선택

---

## 전체 재설치 없이 Flutter 채널 변경

```bash
# 현재 채널 확인
flutter channel

# stable 채널로 전환
flutter channel stable
flutter upgrade

# 캐시 초기화 후 재진단
flutter clean
flutter pub get
flutter doctor -v
```
# 캐시 초기화 후 재진단
flutter clean
flutter pub get
flutter doctor -v
```
