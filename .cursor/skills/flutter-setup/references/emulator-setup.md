# 에뮬레이터 / 시뮬레이터 설정 가이드

---

## Android 에뮬레이터 (AVD Manager)

### Android Studio에서 생성

1. **[Tools] > [Device Manager]**
2. **Create Device** 클릭
3. 기기 선택 (Pixel 7 권장)
4. System Image 선택 (최신 API 레벨, x86_64 또는 arm64)
5. 완료 후 ▷ 버튼으로 실행

### 터미널에서 관리

```bash
# 설치된 에뮬레이터 목록
flutter emulators

# 에뮬레이터 생성
flutter emulators --create --name pixel_7_api34

# 에뮬레이터 실행
flutter emulators --launch pixel_7_api34

# 실행 중인 기기 확인
flutter devices

# 특정 기기로 앱 실행
flutter run -d emulator-5554
```

### AVD 성능 최적화

**Hardware Acceleration 활성화:**

- **Windows**: Intel HAXM 또는 Windows Hypervisor Platform (WHPX) 사용
  ```powershell
  # Windows 기능에서 활성화
  # [Windows 기능 켜기/끄기] > [Hyper-V] 또는 [Windows Hypervisor Platform]
  ```
- **macOS**: 자동으로 Hypervisor.framework 사용
- **Linux**: KVM 설치
  ```bash
  sudo apt install qemu-kvm libvirt-daemon-system
  sudo usermod -aG kvm $USER
  ```

**AVD 설정 권장값:**

- RAM: 2048 MB 이상
- Internal Storage: 4096 MB 이상
- Graphics: Hardware - GLES 2.0

---

## Linux (Ubuntu) KVM 에뮬레이터 설정

```bash
# KVM 설치 및 사용자 그룹 추가
sudo apt install -y qemu-kvm libvirt-daemon-system
sudo usermod -aG kvm $USER
newgrp kvm   # 재로그인 전 즉시 적용

# KVM 확인
kvm-ok
# "KVM acceleration can be used" → 정상
```

---

## 물리 기기 연결

### Android 기기

1. **개발자 옵션** 활성화: **[설정] > [휴대폰 정보] > [빌드 번호]** 7번 탭
2. **USB 디버깅** 활성화
3. USB 연결 후:

```bash
adb devices
# 기기가 "unauthorized" 상태면 기기에서 승인

flutter devices
flutter run
```

---

## 자주 발생하는 에뮬레이터 에러

### ❌ "HAXM is not installed" (Windows)

```
Android Studio > SDK Manager > SDK Tools > Intel x86 Emulator Accelerator (HAXM) 설치
```

또는 WHPX 사용:

```powershell
# 관리자 권한 PowerShell
dism /online /enable-feature /featurename:HypervisorPlatform /all
```

### ❌ "emulator: ERROR: x86 emulation currently requires hardware acceleration"

- BIOS에서 Intel VT-x 또는 AMD-V 활성화 필요
- BIOS 진입 후 Virtualization Technology 옵션 Enable

### ❌ 에뮬레이터가 매우 느림

```bash
# x86_64 System Image 사용 (arm64 이미지보다 빠름)
# AVD 설정에서 Graphics → Hardware 변경
# Cold Boot 대신 Quick Boot 사용
```

### ❌ Linux: "libGL error" / OpenGL 에러

```bash
sudo apt install -y mesa-utils libgl1-mesa-glx
# 소프트웨어 렌더링으로 실행
emulator -avd [AVD_NAME] -gpu swiftshader_indirect
```

### ❌ Linux: Permission denied - /dev/kvm

```bash
sudo chmod 666 /dev/kvm
# 또는 그룹 추가 후 재로그인
sudo usermod -aG kvm $USER
```

---

## iOS 시뮬레이터 (macOS 전용)

> iOS 앱 개발에는 macOS + Xcode가 필수입니다.

### 시뮬레이터 실행

```bash
# Xcode 커맨드라인 도구 설치 (미설치 시)
xcode-select --install

# 사용 가능한 시뮬레이터 목록
xcrun simctl list devices

# 시뮬레이터 부팅
open -a Simulator

# 특정 기기로 부팅
xcrun simctl boot "iPhone 16 Pro"

# Flutter에서 iOS 시뮬레이터로 실행
flutter run -d "iPhone 16 Pro"
```

### 시뮬레이터 관리

```bash
# 새 시뮬레이터 생성
xcrun simctl create "My iPhone" "iPhone 16" "iOS-18-0"

# 시뮬레이터 초기화 (데이터 삭제)
xcrun simctl erase "iPhone 16 Pro"

# 모든 시뮬레이터 종료
xcrun simctl shutdown all

# 사용 불가능한 시뮬레이터 정리
xcrun simctl delete unavailable
```

### 자주 발생하는 iOS 시뮬레이터 에러

#### ❌ "Unable to boot the Simulator"

```bash
# Xcode 라이선스 동의
sudo xcodebuild -license accept

# DerivedData 정리
rm -rf ~/Library/Developer/Xcode/DerivedData
```

#### ❌ "No supported devices found"

```bash
# Xcode에서 추가 플랫폼 다운로드
# Xcode > Settings > Platforms > [+] > iOS xx
# 또는 커맨드라인:
xcodebuild -downloadPlatform iOS
```

#### ❌ CocoaPods 관련 에러

```bash
# CocoaPods 설치/업데이트
sudo gem install cocoapods
# 또는 Homebrew로:
brew install cocoapods

# Pod 재설치
cd ios && pod deintegrate && pod install --repo-update
```

#### ❌ "Building for iOS, but linking in dylib built for macOS"

```bash
# ios 폴더 정리 후 재빌드
cd ios && rm -rf Pods Podfile.lock && pod install
cd .. && flutter clean && flutter run
```
