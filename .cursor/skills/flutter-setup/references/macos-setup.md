# macOS Flutter 설치 가이드

## 사전 요구사항

| 항목   | 요구사항                           |
| ------ | ---------------------------------- |
| macOS  | 12 (Monterey) 이상                 |
| Xcode  | 14 이상 (App Store)                |
| 디스크 | 10 GB 이상                         |
| CPU    | Apple Silicon(M1/M2/M3) 또는 Intel |

---

## Step 1: Flutter SDK 설치

### 방법 A: Homebrew (권장)

```bash
brew install --cask flutter
```

### 방법 B: FVM 사용 (버전 관리 포함, 강력 권장)

```bash
# Homebrew로 FVM 설치
brew tap leoafarias/fvm
brew install fvm

# Flutter 설치
fvm install stable
fvm global stable
```

### 방법 C: 수동 설치

```bash
# ~/development 디렉토리에 설치
mkdir -p ~/development
cd ~/development
# 공식 사이트에서 다운로드한 tar.xz 압축 해제
```

---

## Step 2: 환경 변수 설정

### zsh (macOS 기본 쉘)

```bash
echo 'export PATH="$PATH:$HOME/development/flutter/bin"' >> ~/.zshrc
source ~/.zshrc
```

### bash 사용 시

```bash
echo 'export PATH="$PATH:$HOME/development/flutter/bin"' >> ~/.bash_profile
source ~/.bash_profile
```

### Apple Silicon (M1/M2/M3) 추가 설정

```bash
# Rosetta 2 설치 (필요 시)
sudo softwareupdate --install-rosetta --agree-to-license

# Homebrew ARM 경로 확인
echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.zshrc
```

---

## Step 3: Xcode 설정

```bash
# Xcode 설치 후 커맨드라인 도구 설치
sudo xcode-select --install

# Xcode 라이선스 동의
sudo xcodebuild -license accept

# iOS 시뮬레이터 설정
open -a Simulator
```

---

## Step 4: Android Studio 설치 (Android 개발 시)

1. https://developer.android.com/studio 에서 macOS 버전 다운로드
2. Apple Silicon은 **ARM** 버전 선택
3. 설치 후:

```bash
# Android 라이선스 동의
flutter doctor --android-licenses
```

---

## Step 5: CocoaPods 설치 (iOS 플러그인 필요 시)

```bash
# Ruby gem으로 설치
sudo gem install cocoapods

# Apple Silicon에서 에러 시
brew install cocoapods
```

---

## Step 6: 검증

```bash
flutter doctor -v
```

---

## macOS 자주 발생하는 에러

### ❌ "zsh: command not found: flutter"

```bash
# 경로 확인
which flutter
echo $PATH

# .zshrc 재적용
source ~/.zshrc
```

### ❌ "CocoaPods not installed"

```bash
brew install cocoapods
pod setup
```

### ❌ Apple Silicon에서 빌드 에러

```bash
# x86_64 아키텍처로 실행
arch -x86_64 pod install

# 또는 Podfile에 추가
# post_install do |installer|
#   installer.pods_project.targets.each do |target|
#     target.build_configurations.each do |config|
#       config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = 'arm64'
#     end
#   end
# end
```

### ❌ "iOS toolchain - Xcode not installed"

```bash
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```   target.build_configurations.each do |config|
#       config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = 'arm64'
#     end
#   end
# end
```

### ❌ "iOS toolchain - Xcode not installed"

```bash
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```
