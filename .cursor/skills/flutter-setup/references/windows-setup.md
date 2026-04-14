# Windows Flutter 설치 가이드

## 사전 요구사항

| 항목   | 최소 요구사항            |
| ------ | ------------------------ |
| OS     | Windows 10 (64-bit) 이상 |
| 디스크 | 10 GB 이상 여유 공간     |
| RAM    | 8 GB 이상 권장           |
| Git    | 설치 필요                |

---

## Step 1: Flutter SDK 설치

### 방법 A: 공식 설치 관리자 (권장)

1. https://docs.flutter.dev/get-started/install/windows 접속
2. **Flutter SDK** 최신 stable 버전 다운로드
3. `C:\flutter` 경로에 압축 해제 (경로에 공백/특수문자 금지)

### 방법 B: winget 사용

```powershell
winget install Flutter.Flutter
```

---

## Step 2: 환경 변수 설정

```powershell
# 현재 사용자 PATH에 Flutter 추가
$env:Path += ";C:\flutter\bin"

# 영구 적용 (시스템 환경변수)
[Environment]::SetEnvironmentVariable(
  "Path",
  "$([Environment]::GetEnvironmentVariable('Path', 'User'));C:\flutter\bin",
  "User"
)
```

또는 GUI로:

- **[시스템 속성] > [환경 변수] > [사용자 변수] > Path > 편집**
- `C:\flutter\bin` 추가

---

## Step 3: Android Studio 설치

1. https://developer.android.com/studio 에서 다운로드
2. 설치 시 **Android Virtual Device** 체크 확인
3. 설치 후 SDK 설치:
   - **[Tools] > [SDK Manager]**
   - Android SDK 최신 버전 체크 후 Apply

### Android 라이선스 동의

```powershell
flutter doctor --android-licenses
# 모든 항목에 'y' 입력
```

---

## Step 4: VS Code 설정 (선택)

1. VS Code 설치 후 Extensions (Ctrl+Shift+X)
2. `Flutter` 검색 → Dart Code 팀의 Flutter 플러그인 설치
3. `Dart` 플러그인 자동 설치 확인

---

## Step 5: 검증

```powershell
flutter doctor -v
```

모든 항목 ✓ 확인 → 완료!

---

## Windows 자주 발생하는 에러

### ❌ "'flutter'은(는) 내부 또는 외부 명령..."

- 원인: PATH 환경 변수 미설정 또는 터미널 재시작 필요
- 해결: 터미널(PowerShell/CMD) 완전히 닫고 재시작

### ❌ "Android toolchain - No Android SDK found"

- 원인: ANDROID_HOME 환경변수 미설정
- 해결:

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("Path", "$([Environment]::GetEnvironmentVariable('Path','User'));$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```

### ❌ Windows Defender / 바이러스 백신 차단

- `C:\flutter` 폴더를 바이러스 백신 예외 목록에 추가
- Windows Defender: **[바이러스 및 위협 방지] > [제외 항목 추가]**

### ❌ Visual Studio 관련 에러 (Windows 데스크톱 개발 시)

```powershell
# Visual Studio 2022 Community 설치 후
# "Desktop development with C++" 워크로드 체크 필요
```
- `C:\flutter` 폴더를 바이러스 백신 예외 목록에 추가
- Windows Defender: **[바이러스 및 위협 방지] > [제외 항목 추가]**

### ❌ Visual Studio 관련 에러 (Windows 데스크톱 개발 시)

```powershell
# Visual Studio 2022 Community 설치 후
# "Desktop development with C++" 워크로드 체크 필요
```
