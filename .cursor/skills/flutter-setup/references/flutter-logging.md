# Flutter 로깅 & 디버깅 가이드

---

## 1. logger 패키지 (권장)

### 설치

```yaml
dependencies:
  logger: ^2.4.0
```

### 기본 설정

```dart
// core/utils/app_logger.dart
import 'package:logger/logger.dart';

class AppLogger {
  static final Logger _logger = Logger(
    printer: PrettyPrinter(
      methodCount: 2,        // 스택 트레이스 줄 수
      errorMethodCount: 8,   // 에러 시 스택 트레이스 줄 수
      lineLength: 120,
      colors: true,
      printEmojis: true,
      dateTimeFormat: DateTimeFormat.onlyTimeAndSinceStart,
    ),
    // 릴리즈 빌드에서 로그 비활성화
    level: kReleaseMode ? Level.off : Level.trace,
  );

  static void trace(String message) => _logger.t(message);
  static void debug(String message) => _logger.d(message);
  static void info(String message) => _logger.i(message);
  static void warning(String message) => _logger.w(message);
  static void error(String message, [Object? error, StackTrace? stackTrace]) =>
      _logger.e(message, error: error, stackTrace: stackTrace);
  static void fatal(String message, [Object? error, StackTrace? stackTrace]) =>
      _logger.f(message, error: error, stackTrace: stackTrace);
}
```

### 사용 예시

```dart
import 'core/utils/app_logger.dart';

// 레벨별 사용
AppLogger.debug('API 호출 시작: /users');
AppLogger.info('사용자 로드 완료: 25명');
AppLogger.warning('토큰 만료 임박: 5분 남음');
AppLogger.error('API 호출 실패', error, stackTrace);
```

---

## 2. 릴리즈 빌드 로그 제거 전략

### 방법 A: kReleaseMode 조건부 로그

```dart
import 'package:flutter/foundation.dart';

void appLog(String message) {
  if (kDebugMode) {
    print(message);
  }
}
```

### 방법 B: logger 레벨 제어 (권장)

```dart
// main.dart
void main() {
  if (kReleaseMode) {
    Logger.level = Level.off;  // 릴리즈에서 전체 비활성화
  }
  runApp(const MyApp());
}
```

### 방법 C: dart define으로 환경별 제어

```bash
# 개발 빌드
flutter run --dart-define=LOG_LEVEL=debug

# 스테이징
flutter run --dart-define=LOG_LEVEL=warning

# 프로덕션
flutter build apk --dart-define=LOG_LEVEL=off
```

```dart
// 코드에서 사용
const logLevel = String.fromEnvironment('LOG_LEVEL', defaultValue: 'debug');
```

---

## 3. Dio HTTP 클라이언트 로깅

```yaml
dependencies:
  dio: ^5.7.0
  pretty_dio_logger: ^1.4.0
```

```dart
// core/network/dio_client.dart
import 'package:dio/dio.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:flutter/foundation.dart';

class DioClient {
  static Dio create() {
    final dio = Dio(BaseOptions(
      baseUrl: 'https://api.example.com',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));

    // 개발 환경에서만 로그 인터셉터 추가
    if (kDebugMode) {
      dio.interceptors.add(
        PrettyDioLogger(
          requestHeader: true,
          requestBody: true,
          responseBody: true,
          responseHeader: false,
          error: true,
          compact: true,
        ),
      );
    }

    return dio;
  }
}
```

---

## 4. Bloc 이벤트 로깅

```dart
// core/bloc/app_bloc_observer.dart
import 'package:flutter_bloc/flutter_bloc.dart';
import '../utils/app_logger.dart';

class AppBlocObserver extends BlocObserver {
  @override
  void onCreate(BlocBase bloc) {
    super.onCreate(bloc);
    AppLogger.debug('[Bloc Created] ${bloc.runtimeType}');
  }

  @override
  void onEvent(Bloc bloc, Object? event) {
    super.onEvent(bloc, event);
    AppLogger.debug('[Bloc Event] ${bloc.runtimeType}: $event');
  }

  @override
  void onChange(BlocBase bloc, Change change) {
    super.onChange(bloc, change);
    AppLogger.debug('[Bloc Change] ${bloc.runtimeType}\n  current: ${change.currentState}\n  next: ${change.nextState}');
  }

  @override
  void onError(BlocBase bloc, Object error, StackTrace stackTrace) {
    AppLogger.error('[Bloc Error] ${bloc.runtimeType}', error, stackTrace);
    super.onError(bloc, error, stackTrace);
  }

  @override
  void onClose(BlocBase bloc) {
    super.onClose(bloc);
    AppLogger.debug('[Bloc Closed] ${bloc.runtimeType}');
  }
}

// main.dart에 등록
void main() {
  Bloc.observer = AppBlocObserver();
  runApp(const MyApp());
}
```

---

## 5. Flutter DevTools 활용

```bash
# DevTools 실행
flutter pub global activate devtools
flutter pub global run devtools

# 앱 실행 시 자동 연결
flutter run --debug
# 콘솔에 출력되는 DevTools URL 클릭
```

### 주요 탭

- **Flutter Inspector** — 위젯 트리 시각화, 레이아웃 문제 파악
- **Performance** — 프레임 드롭 분석, Jank 감지
- **Memory** — 메모리 누수 추적
- **Network** — HTTP 요청/응답 모니터링
- **Logging** — 앱 로그 필터링 및 검색

---

## 6. 크래시 리포팅 (Firebase Crashlytics)

```yaml
dependencies:
  firebase_core: ^3.6.0
  firebase_crashlytics: ^4.1.3
```

```dart
// main.dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();

  // Flutter 프레임워크 에러를 Crashlytics로 전송
  FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;

  // 비동기 에러 처리
  PlatformDispatcher.instance.onError = (error, stack) {
    FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
    return true;
  };

  runApp(const MyApp());
}

// 커스텀 로그 추가
FirebaseCrashlytics.instance.log('결제 프로세스 시작');
```

---

## 디버깅 팁

```dart
// 위젯 리빌드 추적
import 'package:flutter/rendering.dart';

void main() {
  debugPrintMarkNeedsLayoutStacks = true;  // 레이아웃 재계산 추적
  debugPrintRebuildDirtyWidgets = true;    // 리빌드 위젯 출력
  runApp(const MyApp());
}

// 특정 위젯 성능 측정
Timeline.startSync('MyWidget.build');
// ... 빌드 로직
Timeline.finishSync();
```unApp(const MyApp());
}

// 특정 위젯 성능 측정
Timeline.startSync('MyWidget.build');
// ... 빌드 로직
Timeline.finishSync();
```
