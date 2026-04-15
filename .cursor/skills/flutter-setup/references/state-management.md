# Flutter 상태관리 설정 가이드

---

## 상태관리 라이브러리 선택 가이드

| 라이브러리   | 적합한 규모 | 학습 난이도 | 특징                                   |
| ------------ | ----------- | ----------- | -------------------------------------- |
| **Riverpod** | 중~대형     | 중간        | 타입 안전, 코드 생성, 현재 가장 권장   |
| **Bloc**     | 대형        | 높음        | 이벤트 기반, 테스트 용이, 엔터프라이즈 |
| **Provider** | 소~중형     | 낮음        | 간단하나 규모 커지면 한계              |
| **GetX**     | 소~중형     | 낮음        | 올인원이나 규모 커지면 관리 어려움     |

---

## 1. Riverpod (권장)

> ⚠️ **버전 기준**: 이 프로젝트는 아래 버전 조합을 표준으로 사용합니다.

### 패키지 구성

| 패키지                | 역할                           | 버전       |
| --------------------- | ------------------------------ | ---------- |
| `flutter_riverpod`    | 상태 관리 라이브러리           | `^3.1.0`   |
| `riverpod_annotation` | 코드 생성용 어노테이션         | `^4.0.0`   |
| `riverpod_generator`  | 어노테이션 기반 코드 자동 생성 | `^4.0.0+1` |
| `build_runner`        | 코드 생성 실행 도구            | `^2.4.0`   |
| `riverpod_lint`       | Riverpod 린트 규칙             | `^3.1.0`   |

### 설치

```yaml
# pubspec.yaml
dependencies:
  flutter_riverpod: ^3.1.0
  riverpod_annotation: ^4.0.0

dev_dependencies:
  build_runner: ^2.4.0
  riverpod_generator: ^4.0.0+1
  riverpod_lint: ^3.1.0
  custom_lint: ^0.6.4
```

```bash
flutter pub get
```

### analysis_options.yaml 설정

```yaml
# riverpod_lint 플러그인 활성화 필수
analyzer:
  plugins:
    - custom_lint
```

### 기본 설정 (main.dart)

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  runApp(
    const ProviderScope(  // 앱 전체를 감싸기 필수
      child: MyApp(),
    ),
  );
}
```

### Provider 작성

```dart
// providers/product_provider.dart
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'product_provider.g.dart';  // 코드 생성 파일 선언 필수

@riverpod
class ProductNotifier extends _$ProductNotifier {
  @override
  List<Product> build() {
    return [];  // 초기 상태
  }

  void addProduct(Product product) {
    state = [...state, product];
  }
}
```

### 코드 생성

```bash
# 코드 생성 실행 (.g.dart 파일 생성)
dart run build_runner build

# 파일 변경 감지 자동 생성 (개발 중 권장)
dart run build_runner watch

# 충돌 파일 삭제 후 재생성
dart run build_runner build --delete-conflicting-outputs
```

> ⚠️ **주의**: 코드 수정 후 반드시 `build_runner`를 실행해야 `.g.dart` 파일이 갱신됩니다.

### Widget에서 Provider 사용

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

class ProductScreen extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Provider 읽기 (상태 변경 시 리빌드)
    final products = ref.watch(productNotifierProvider);

    return ElevatedButton(
      onPressed: () {
        // Provider 수정 (리빌드 없이 메서드 호출)
        ref.read(productNotifierProvider.notifier).addProduct(newProduct);
      },
      child: Text('상품 추가'),
    );
  }
}
```

### 프로젝트 구조 권장

```
lib/
├── main.dart
├── features/
│   └── product/
│       ├── data/
│       │   └── product_repository.dart
│       ├── domain/
│       │   └── product.dart
│       └── presentation/
│           ├── providers/
│           │   ├── product_provider.dart
│           │   └── product_provider.g.dart   ← build_runner 자동 생성
│           └── screens/
│               └── product_screen.dart
```

---

## 2. Bloc

### 설치

```yaml
dependencies:
  flutter_bloc: ^8.1.6
  equatable: ^2.0.5

dev_dependencies:
  bloc_test: ^9.1.7
```

### 기본 구조

```dart
// events
abstract class UserEvent extends Equatable {}
class LoadUsers extends UserEvent {
  @override List<Object> get props => [];
}

// states
abstract class UserState extends Equatable {}
class UserInitial extends UserState { @override List<Object> get props => []; }
class UserLoading extends UserState { @override List<Object> get props => []; }
class UserLoaded extends UserState {
  final List<User> users;
  const UserLoaded(this.users);
  @override List<Object> get props => [users];
}
class UserError extends UserState {
  final String message;
  const UserError(this.message);
  @override List<Object> get props => [message];
}

// bloc
class UserBloc extends Bloc<UserEvent, UserState> {
  final UserRepository _repository;

  UserBloc(this._repository) : super(UserInitial()) {
    on<LoadUsers>(_onLoadUsers);
  }

  Future<void> _onLoadUsers(LoadUsers event, Emitter<UserState> emit) async {
    emit(UserLoading());
    try {
      final users = await _repository.getUsers();
      emit(UserLoaded(users));
    } catch (e) {
      emit(UserError(e.toString()));
    }
  }
}
```

### BlocProvider 설정 (main.dart)

```dart
void main() {
  runApp(
    MultiRepositoryProvider(
      providers: [
        RepositoryProvider(create: (_) => UserRepository()),
      ],
      child: MultiBlocProvider(
        providers: [
          BlocProvider(create: (ctx) => UserBloc(ctx.read<UserRepository>())),
        ],
        child: const MyApp(),
      ),
    ),
  );
}
```

### Bloc Observer (로깅 연동)

```dart
// bloc_observer.dart
class AppBlocObserver extends BlocObserver {
  @override
  void onEvent(Bloc bloc, Object? event) {
    super.onEvent(bloc, event);
    print('[Bloc] ${bloc.runtimeType} → $event');
  }

  @override
  void onError(BlocBase bloc, Object error, StackTrace stackTrace) {
    super.onError(bloc, error, stackTrace);
    print('[Bloc ERROR] ${bloc.runtimeType}: $error');
  }
}

// main.dart
void main() {
  Bloc.observer = AppBlocObserver();
  runApp(const MyApp());
}
```

---

## 3. Provider (간단한 프로젝트)

### 설치

```yaml
dependencies:
  provider: ^6.1.2
```

### 기본 설정

```dart
// main.dart
void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => UserProvider()),
      ],
      child: const MyApp(),
    ),
  );
}

// providers/user_provider.dart
class UserProvider extends ChangeNotifier {
  List<User> _users = [];
  List<User> get users => _users;

  Future<void> loadUsers() async {
    _users = await UserRepository().getUsers();
    notifyListeners();
  }
}
```

---

## 상태관리 공통 팁

### freezed로 불변 모델 클래스 생성

> 상세 가이드는 `references/freezed-guide.md` 참조 (설치, Union 타입, 패턴 매칭, 상태관리 연동 패턴 포함)

## 상태관리 공통 팁

### freezed로 불변 모델 클래스 생성

> 상세 가이드는 `references/freezed-guide.md` 참조 (설치, Union 타입, 패턴 매칭, 상태관리 연동 패턴 포함)
