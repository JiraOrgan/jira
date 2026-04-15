# Linux (Ubuntu) Flutter 설치 가이드

> Ubuntu 22.04 LTS / 24.04 LTS 기준

## 사전 요구사항

```bash
# 필수 패키지 설치
sudo apt update
sudo apt install -y curl git unzip xz-utils zip libglu1-mesa \
  clang cmake ninja-build pkg-config libgtk-3-dev \
  liblzma-dev libstdc++-12-dev
```

---

## Step 1: Flutter SDK 설치

### 방법 A: snap (가장 간단)

```bash
sudo snap install flutter --classic
flutter sdk-path
```

### 방법 B: FVM 사용 (프로젝트 버전 관리, 권장)

```bash
# FVM 설치
dart pub global activate fvm

# PATH 추가 (~/.bashrc 또는 ~/.zshrc)
echo 'export PATH="$PATH:$HOME/.pub-cache/bin"' >> ~/.bashrc
source ~/.bashrc

# Flutter stable 설치
fvm install stable
fvm global stable
```

### 방법 C: 수동 설치

```bash
# 설치 디렉토리 생성
mkdir -p ~/development && cd ~/development

# SDK 다운로드 (최신 버전 확인: https://docs.flutter.dev/release/archive)
wget https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_3.24.0-stable.tar.xz

# 압축 해제
tar xf flutter_linux_*.tar.xz

# PATH 설정
echo 'export PATH="$PATH:$HOME/development/flutter/bin"' >> ~/.bashrc
source ~/.bashrc
```

---

## Step 2: Android SDK 설치

### Android Studio 설치 (권장)

```bash
# snap으로 설치
sudo snap install android-studio --classic

# 또는 공식 사이트에서 .tar.gz 다운로드 후 수동 설치
sudo tar -xzf android-studio-*.tar.gz -C /opt/
/opt/android-studio/bin/studio.sh
```

### 환경변수 설정

```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools/bin' >> ~/.bashrc
source ~/.bashrc
```

### Android 라이선스 동의

```bash
flutter doctor --android-licenses
# 모든 항목 'y' 입력
```

---

## Step 3: KVM 설치 (에뮬레이터 가속)

```bash
# KVM 설치
sudo apt install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils

# 현재 사용자를 kvm 그룹에 추가
sudo usermod -aG kvm $USER
sudo usermod -aG libvirt $USER

# 로그아웃 후 재로그인 또는:
newgrp kvm

# KVM 동작 확인
kvm-ok
# 결과: "KVM acceleration can be used" 이면 정상
```

---

## Step 4: VS Code 설치 (선택)

```bash
# snap으로 설치
sudo snap install code --classic

# 또는 apt
wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > packages.microsoft.gpg
sudo install -o root -g root -m 644 packages.microsoft.gpg /etc/apt/trusted.gpg.d/
sudo sh -c 'echo "deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main" > /etc/apt/sources.list.d/vscode.list'
sudo apt update && sudo apt install code
```

---

## Step 5: 검증

```bash
flutter doctor -v
```

---

## Linux 자주 발생하는 에러

### ❌ "flutter: command not found"

```bash
# PATH 확인
echo $PATH | grep flutter

# .bashrc 재적용
source ~/.bashrc

# snap 설치 시 경로 확인
which flutter
# /snap/bin/flutter 이면 정상
```

### ❌ "Android toolchain - No Android SDK"

```bash
# ANDROID_HOME 확인
echo $ANDROID_HOME
ls $ANDROID_HOME

# Android Studio에서 SDK 위치 확인
# [File] > [Settings] > [Appearance & Behavior] > [System Settings] > [Android SDK]
```

### ❌ "Unable to locate Android SDK" (SDK Manager 경로 문제)

```bash
# cmdline-tools 수동 설치
mkdir -p $ANDROID_HOME/cmdline-tools
cd $ANDROID_HOME/cmdline-tools
# Android Studio > SDK Manager > SDK Tools > Android SDK Command-line Tools 설치
```

### ❌ "libGL error" / OpenGL 에러 (에뮬레이터)

```bash
# Mesa 라이브러리 설치
sudo apt install -y mesa-utils libgl1-mesa-glx

# 에뮬레이터 소프트웨어 렌더링으로 실행
emulator -avd [AVD_NAME] -gpu swiftshader_indirect
```

### ❌ Permission denied - /dev/kvm

```bash
sudo chmod 666 /dev/kvm
# 또는 그룹 추가 후 재로그인
sudo usermod -aG kvm $USER
```

### ❌ snap Flutter와 Android Studio 경로 충돌

```bash
# snap Flutter에 Android SDK 경로 수동 지정
flutter config --android-sdk $HOME/Android/Sdk
```
```bash
sudo chmod 666 /dev/kvm
# 또는 그룹 추가 후 재로그인
sudo usermod -aG kvm $USER
```

### ❌ snap Flutter와 Android Studio 경로 충돌

```bash
# snap Flutter에 Android SDK 경로 수동 지정
flutter config --android-sdk $HOME/Android/Sdk
```
