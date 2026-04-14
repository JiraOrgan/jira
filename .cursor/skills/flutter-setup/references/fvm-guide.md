# FVM (Flutter Version Manager) 가이드

> 여러 Flutter 버전을 프로젝트별로 관리. 팀 개발 / CI-CD 환경에 강력 권장.

---

## 설치

### macOS / Linux

```bash
# Homebrew
brew tap leoafarias/fvm
brew install fvm

# 또는 pub global
dart pub global activate fvm
```

### Windows

```powershell
# Chocolatey
choco install fvm

# 또는 pub global
dart pub global activate fvm
# PATH에 pub global 경로 추가
# %LOCALAPPDATA%\Pub\Cache\bin
```

---

## 기본 사용법

```bash
# 사용 가능한 Flutter 버전 목록
fvm releases

# 특정 버전 설치
fvm install 3.24.0
fvm install stable
fvm install beta

# 전역 기본 버전 설정
fvm global stable

# 프로젝트별 버전 설정 (프로젝트 루트에서 실행)
fvm use 3.24.0

# 현재 사용 버전 확인
fvm current

# 설치된 버전 목록
fvm list
```

---

## 프로젝트 설정

`fvm use` 실행 시 `.fvm/fvm_config.json` 자동 생성:

```json
{
  "flutterSdkVersion": "3.24.0"
}
```

### .gitignore에 추가

```
.fvm/flutter_sdk
```

### .fvm/fvm_config.json은 Git에 포함 (버전 공유)

---

## VS Code 연동

`.vscode/settings.json`:

```json
{
  "dart.flutterSdkPath": ".fvm/flutter_sdk",
  "search.exclude": {
    "**/.fvm": true
  },
  "files.watcherExclude": {
    "**/.fvm": true
  }
}
```

---

## Android Studio 연동

1. **[File] > [Project Structure] > [SDK Location]**
2. **Flutter SDK path**: `[프로젝트경로]/.fvm/flutter_sdk`

---

## fvm flutter 명령어 사용

```bash
# fvm 통해 flutter 명령 실행 (프로젝트 버전 자동 적용)
fvm flutter run
fvm flutter build apk
fvm flutter pub get

# 별칭 설정으로 편리하게 (macOS/Linux ~/.zshrc)
alias flutter="fvm flutter"
alias dart="fvm dart"
```

---

## CI/CD (GitHub Actions) 연동

```yaml
# .github/workflows/flutter.yml
- name: Setup FVM
  uses: kuhnroyal/flutter-fvm-config-action@v1

- name: Setup Flutter
  uses: subosito/flutter-action@v2
  with:
    flutter-version-file: .fvm/fvm_config.json
    channel: stable
```

---

## 자주 발생하는 FVM 에러

### ❌ "No FVM Flutter version configured for this project"

```bash
# 프로젝트 루트에서 실행
fvm use stable
```

### ❌ fvm 명령어를 찾을 수 없음

```bash
# pub global bin 경로 추가
export PATH="$PATH:$HOME/.pub-cache/bin"
```

### ❌ 버전 전환 후 IDE가 인식 못함

- VS Code: `Ctrl+Shift+P` → **"Dart: Change SDK"** → FVM 경로 선택
- Android Studio: SDK 경로를 `.fvm/flutter_sdk`로 재설정 후 재시작
export PATH="$PATH:$HOME/.pub-cache/bin"
```

### ❌ 버전 전환 후 IDE가 인식 못함

- VS Code: `Ctrl+Shift+P` → **"Dart: Change SDK"** → FVM 경로 선택
- Android Studio: SDK 경로를 `.fvm/flutter_sdk`로 재설정 후 재시작
