# IDE 설정 가이드 (Android Studio / VS Code)

---

## Android Studio

### Flutter & Dart 플러그인 설치

1. **[File] > [Settings]** (Windows/Linux) 또는 **[Android Studio] > [Preferences]** (macOS)
2. **[Plugins]** → Marketplace 탭
3. **"Flutter"** 검색 → Install (Dart 플러그인 자동 포함)
4. IDE 재시작

### 새 Flutter 프로젝트 생성

- **[File] > [New] > [New Flutter Project]**
- Flutter SDK 경로 확인 후 프로젝트 설정

### 유용한 단축키 (Android Studio)

| 기능        | Windows/Linux | macOS        |
| ----------- | ------------- | ------------ |
| Hot Reload  | Ctrl+\        | Cmd+\        |
| Hot Restart | Ctrl+Shift+\  | Cmd+Shift+\  |
| Run         | Shift+F10     | Ctrl+R       |
| Debug       | Shift+F9      | Ctrl+D       |
| Format Code | Ctrl+Alt+L    | Cmd+Option+L |
| Auto Import | Alt+Enter     | Option+Enter |

### 추천 추가 플러그인

- **Flutter Intl** — 다국어(i18n) 관리
- **Dart Data Class Generator** — 데이터 클래스 자동 생성
- **Flutter Widget Snippets** — 위젯 코드 스니펫
- **Rainbow Brackets** — 괄호 색상 구분

---

## VS Code

### 필수 플러그인 설치

```
Extensions (Ctrl+Shift+X):
1. Flutter (Dart Code 팀) — 필수
2. Dart (자동 설치됨) — 필수
3. Pubspec Assist — pubspec.yaml 패키지 관리
4. Flutter Tree — 위젯 트리 시각화
5. Error Lens — 인라인 에러 표시
```

### settings.json 권장 설정

```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll": true
  },
  "[dart]": {
    "editor.formatOnSave": true,
    "editor.selectionHighlight": false,
    "editor.suggest.snippetsPreventQuickSuggestions": false,
    "editor.suggestSelection": "first",
    "editor.tabCompletion": "onlySnippets",
    "editor.wordBasedSuggestions": "off"
  },
  "dart.flutterHotReloadOnSave": "always",
  "dart.debugExternalPackageLibraries": false,
  "dart.debugSdkLibraries": false
}
```

### 유용한 단축키 (VS Code)

| 기능              | Windows/Linux | macOS        |
| ----------------- | ------------- | ------------ |
| Hot Reload        | Ctrl+F5       | Cmd+F5       |
| Hot Restart       | Ctrl+Shift+F5 | Cmd+Shift+F5 |
| Run without Debug | Ctrl+F5       | Cmd+F5       |
| Debug             | F5            | F5           |
| Widget 감싸기     | Ctrl+. → Wrap | Cmd+. → Wrap |
| 임포트 정리       | Ctrl+Shift+O  | Cmd+Shift+O  |

### launch.json 기본 설정

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Flutter Dev",
      "request": "launch",
      "type": "dart",
      "flutterMode": "debug",
      "args": ["--dart-define=ENV=dev"]
    },
    {
      "name": "Flutter Prod",
      "request": "launch",
      "type": "dart",
      "flutterMode": "release",
      "args": ["--dart-define=ENV=prod"]
    }
  ]
}
```

---

## 공통: Dart Analyzer 최적화

`analysis_options.yaml` (프로젝트 루트):

```yaml
include: package:flutter_lints/flutter.yaml

analyzer:
  errors:
    invalid_annotation_target: ignore
  exclude:
    - "**/*.g.dart"
    - "**/*.freezed.dart"

linter:
  rules:
    prefer_const_constructors: true
    prefer_const_literals_to_create_immutables: true
    avoid_print: true
    use_key_in_widget_constructors: true
```ules:
    prefer_const_constructors: true
    prefer_const_literals_to_create_immutables: true
    avoid_print: true
    use_key_in_widget_constructors: true
```
