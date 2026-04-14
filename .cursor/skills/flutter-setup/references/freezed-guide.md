# Freezed 불변 모델 & Union 타입 가이드

> Dart에서 불변(immutable) 데이터 클래스, sealed union, JSON 직렬화를 코드 생성으로 처리하는 패키지

---

## 설치

```yaml
# pubspec.yaml
dependencies:
  freezed_annotation: ^2.4.4
  json_annotation: ^4.9.0 # JSON 직렬화 필요 시

dev_dependencies:
  freezed: ^2.5.2
  json_serializable: ^6.8.0 # JSON 직렬화 필요 시
  build_runner: ^2.4.0
```

```bash
dart pub get
```

---

## 1. 기본 데이터 클래스

```dart
// domain/model/user.dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'user.freezed.dart';
part 'user.g.dart';  // JSON 직렬화 사용 시

@freezed
class User with _$User {
  const factory User({
    required String id,
    required String name,
    @Default('') String email,
    @Default(false) bool isActive,
  }) = _User;

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
}
```

### 자동 생성되는 기능

- `copyWith()` — 일부 필드만 변경한 새 인스턴스 생성
- `==` / `hashCode` — 값 기반 동등성 비교
- `toString()` — 읽기 좋은 문자열 표현
- `fromJson()` / `toJson()` — JSON 직렬화 (선언 시)

```dart
final user = User(id: '1', name: '홍길동', email: 'hong@test.com');

// copyWith
final updated = user.copyWith(name: '김철수');

// 동등성 비교
User(id: '1', name: '홍길동') == User(id: '1', name: '홍길동'); // true

// JSON
final json = user.toJson();           // Map<String, dynamic>
final fromApi = User.fromJson(json);  // User 인스턴스
```

---

## 2. Union 타입 (Sealed Class 패턴)

API 응답, 상태 관리 등에서 여러 상태를 타입 안전하게 표현할 때 사용한다.

```dart
// domain/model/result.dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'result.freezed.dart';

@freezed
sealed class Result<T> with _$Result<T> {
  const factory Result.success(T data) = Success<T>;
  const factory Result.failure(String message, [Object? error]) = Failure<T>;
  const factory Result.loading() = Loading<T>;
}
```

### 패턴 매칭으로 분기 처리

```dart
// Dart 3 switch expression (권장)
Widget buildWidget(Result<List<Product>> result) {
  return switch (result) {
    Success(:final data) => ProductList(products: data),
    Failure(:final message) => ErrorView(message: message),
    Loading() => const CircularProgressIndicator(),
  };
}

// when 메서드 (모든 케이스 필수 처리)
final message = result.when(
  success: (data) => '${data.length}건 로드 완료',
  failure: (msg, _) => '에러: $msg',
  loading: () => '로딩 중...',
);

// maybeWhen (일부만 처리, orElse 필수)
result.maybeWhen(
  success: (data) => print('성공!'),
  orElse: () => print('아직 로딩 중이거나 에러'),
);
```

---

## 3. 커스텀 메서드 / getter 추가

```dart
@freezed
class Product with _$Product {
  const Product._();  // private 생성자 필수 (커스텀 메서드 추가 시)

  const factory Product({
    required String id,
    required String name,
    required double price,
    @Default(0) double discountRate,
  }) = _Product;

  // 커스텀 getter
  double get finalPrice => price * (1 - discountRate);

  // 커스텀 메서드
  bool get isOnSale => discountRate > 0;

  factory Product.fromJson(Map<String, dynamic> json) =>
      _$ProductFromJson(json);
}
```

---

## 4. @JsonKey 활용

```dart
@freezed
class ApiResponse with _$ApiResponse {
  const factory ApiResponse({
    @JsonKey(name: 'result_code') required int resultCode,
    @JsonKey(name: 'result_message') required String resultMessage,
    @JsonKey(name: 'data_list', defaultValue: []) required List<dynamic> dataList,
    @JsonKey(includeToJson: false) String? internalNote,  // toJson 제외
  }) = _ApiResponse;

  factory ApiResponse.fromJson(Map<String, dynamic> json) =>
      _$ApiResponseFromJson(json);
}
```

---

## 5. 상태관리 연동 패턴

### Riverpod + freezed

```dart
// 상태 정의
@freezed
class ProductListState with _$ProductListState {
  const factory ProductListState({
    @Default([]) List<Product> products,
    @Default(false) bool isLoading,
    String? errorMessage,
  }) = _ProductListState;
}

// Provider
@riverpod
class ProductList extends _$ProductList {
  @override
  ProductListState build() => const ProductListState();

  Future<void> loadProducts() async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final products = await ref.read(apiServiceProvider).getProducts();
      state = state.copyWith(products: products, isLoading: false);
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }
}
```

### Bloc + freezed

```dart
// events
@freezed
sealed class UserEvent with _$UserEvent {
  const factory UserEvent.load() = LoadUsers;
  const factory UserEvent.delete(String id) = DeleteUser;
  const factory UserEvent.refresh() = RefreshUsers;
}

// states
@freezed
sealed class UserState with _$UserState {
  const factory UserState.initial() = UserInitial;
  const factory UserState.loading() = UserLoading;
  const factory UserState.loaded(List<User> users) = UserLoaded;
  const factory UserState.error(String message) = UserError;
}
```

---

## 6. 코드 생성

```bash
# 일회성 생성
dart run build_runner build --delete-conflicting-outputs

# 파일 변경 감지 자동 생성 (개발 중 권장)
dart run build_runner watch --delete-conflicting-outputs
```

생성 파일:

- `*.freezed.dart` — copyWith, ==, toString, when/map 등
- `*.g.dart` — fromJson/toJson (json_serializable 사용 시)

---

## 자주 발생하는 에러

### "Missing concrete implementation of..."

```
→ dart run build_runner build 실행 필요 (.freezed.dart 미생성 상태)
```

### "The method 'copyWith' isn't defined..."

```
→ part 'filename.freezed.dart'; 선언 확인
→ build_runner 재실행
```

### "Could not generate fromJson/toJson"

```
→ part 'filename.g.dart'; 선언 확인
→ json_serializable 의존성 확인
→ factory X.fromJson(...) 팩토리 선언 확인
```

### freezed + json_serializable 함께 쓸 때

- `@JsonSerializable()` 어노테이션 **불필요** — freezed가 자동 처리
- `factory X.fromJson(Map<String, dynamic> json) => _$XFromJson(json);`만 선언하면 됨

### freezed + json_serializable 함께 쓸 때

- `@JsonSerializable()` 어노테이션 **불필요** — freezed가 자동 처리
- `factory X.fromJson(Map<String, dynamic> json) => _$XFromJson(json);`만 선언하면 됨
