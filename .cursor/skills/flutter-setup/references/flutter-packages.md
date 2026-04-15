# Flutter 필수 패키지 설정 가이드

## 목차

- [전체 pubspec.yaml 한눈에 보기](#전체-pubspecyaml-한눈에-보기)
- [1️⃣ 네트워크 / API 통신](#1️⃣-네트워크--api-통신) — Dio, Retrofit, Pretty Logger
- [2️⃣ JSON / 모델 변환](#2️⃣-json--모델-변환) — json_serializable, build_runner
- [3️⃣ 로컬 저장소](#3️⃣-로컬-저장소) — SharedPreferences, Hive
- [4️⃣ 라우팅 (GoRouter)](#4️⃣-라우팅-gorouter) — 선언적 라우팅, Riverpod 연동
- [5️⃣ UI / 디자인](#5️⃣-ui--디자인) — ScreenUtil, CachedNetworkImage, Flutter SVG
- [6️⃣ 유틸리티](#6️⃣-유틸리티) — intl, uuid, collection
- [전체 코드 생성 한 번에 실행](#전체-코드-생성-한-번에-실행)

---

## 전체 pubspec.yaml 한눈에 보기

```yaml
dependencies:
  flutter:
    sdk: flutter

  # 네트워크
  dio: ^5.9.2
  retrofit: ^4.4.1
  pretty_dio_logger: ^1.4.0

  # JSON 직렬화
  json_annotation: ^4.9.0

  # 로컬 저장소
  shared_preferences: ^2.3.3
  hive: ^2.2.3
  hive_flutter: ^1.1.0

  # 라우팅
  go_router: ^17.2.0

  # UI
  flutter_screenutil: ^5.9.3
  cached_network_image: ^3.4.1
  flutter_svg: ^2.0.10+1

  # 유틸
  intl: ^0.19.0
  uuid: ^4.5.1
  collection: ^1.18.0

dev_dependencies:
  build_runner: ^2.4.0
  json_serializable: ^6.8.0
  retrofit_generator: ^9.1.5+1
  hive_generator: ^2.0.1
```

---

## 1️⃣ 네트워크 / API 통신

### Dio 설정

```dart
// core/network/dio_client.dart
import 'package:dio/dio.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:flutter/foundation.dart';

class DioClient {
  static Dio create({required String baseUrl}) {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
        headers: {'Content-Type': 'application/json'},
      ),
    );

    // 토큰 자동 주입 인터셉터
    dio.interceptors.add(_AuthInterceptor());

    // 개발 환경에서만 로그 출력
    if (kDebugMode) {
      dio.interceptors.add(
        PrettyDioLogger(
          requestHeader: true,
          requestBody: true,
          responseBody: true,
          error: true,
          compact: true,
        ),
      );
    }

    return dio;
  }
}

class _AuthInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    // SharedPreferences 등에서 토큰 가져와 헤더에 추가
    const token = ''; // TODO: 토큰 로드 로직
    if (token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      // 토큰 만료 처리 (리프레시 또는 로그아웃)
    }
    handler.next(err);
  }
}
```

### Retrofit.dart 설정

```dart
// data/remote/api_service.dart
import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';
import '../model/product.dart';

part 'api_service.g.dart';  // 코드 생성 필수

@RestApi()
abstract class ApiService {
  factory ApiService(Dio dio, {String baseUrl}) = _ApiService;

  @GET('/products')
  Future<List<Product>> getProducts();

  @GET('/products/{id}')
  Future<Product> getProduct(@Path('id') int id);

  @POST('/products')
  Future<Product> createProduct(@Body() Product product);

  @PUT('/products/{id}')
  Future<Product> updateProduct(
    @Path('id') int id,
    @Body() Product product,
  );

  @DELETE('/products/{id}')
  Future<void> deleteProduct(@Path('id') int id);

  @POST('/upload')
  @MultiPart()
  Future<String> uploadFile(@Part() File file);
}
```

```bash
# 코드 생성 (api_service.g.dart 생성)
dart run build_runner build --delete-conflicting-outputs
```

### Riverpod Provider 등록

```dart
// providers/network_provider.dart
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'network_provider.g.dart';

@riverpod
Dio dio(DioRef ref) => DioClient.create(baseUrl: 'https://api.example.com');

@riverpod
ApiService apiService(ApiServiceRef ref) =>
    ApiService(ref.watch(dioProvider));
```

---

## 2️⃣ JSON / 모델 변환

### json_serializable 설정

```dart
// domain/model/product.dart
import 'package:json_annotation/json_annotation.dart';

part 'product.g.dart';  // 코드 생성 필수

@JsonSerializable()
class Product {
  final int id;
  final String name;
  final double price;

  @JsonKey(name: 'image_url')   // 키 이름이 다를 때
  final String imageUrl;

  @JsonKey(defaultValue: false)  // 기본값 지정
  final bool isActive;

  const Product({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
    required this.isActive,
  });

  factory Product.fromJson(Map<String, dynamic> json) =>
      _$ProductFromJson(json);

  Map<String, dynamic> toJson() => _$ProductToJson(this);
}
```

```bash
dart run build_runner build --delete-conflicting-outputs
```

> **freezed + json_serializable 함께 쓸 때**: freezed가 `fromJson/toJson`도 생성하므로 별도 `@JsonSerializable` 불필요. `@freezed` + `factory X.fromJson(...)` 패턴 사용.

---

## 3️⃣ 로컬 저장소

### Shared Preferences (간단한 키-값 저장)

```dart
// core/storage/preference_storage.dart
import 'package:shared_preferences/shared_preferences.dart';

class PreferenceStorage {
  static late SharedPreferences _prefs;

  static Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  // 토큰 저장/조회/삭제
  static Future<void> saveToken(String token) =>
      _prefs.setString('access_token', token);

  static String? getToken() => _prefs.getString('access_token');

  static Future<void> removeToken() => _prefs.remove('access_token');

  // 테마/언어 등 설정값
  static Future<void> setDarkMode(bool value) =>
      _prefs.setBool('dark_mode', value);

  static bool isDarkMode() => _prefs.getBool('dark_mode') ?? false;
}

// main.dart에서 초기화
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await PreferenceStorage.init();
  runApp(const ProviderScope(child: MyApp()));
}
```

### Hive (고성능 로컬 DB)

```dart
// domain/model/cart_item.dart
import 'package:hive/hive.dart';

part 'cart_item.g.dart';  // 코드 생성 필수

@HiveType(typeId: 0)  // typeId는 앱 전체에서 고유해야 함
class CartItem extends HiveObject {
  @HiveField(0)
  late String productId;

  @HiveField(1)
  late String name;

  @HiveField(2)
  late int quantity;

  @HiveField(3)
  late double price;
}
```

```dart
// main.dart에서 Hive 초기화
import 'package:hive_flutter/hive_flutter.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Hive.initFlutter();

  // 어댑터 등록 (코드 생성 후 사용 가능)
  Hive.registerAdapter(CartItemAdapter());

  // Box 열기
  await Hive.openBox<CartItem>('cart');

  runApp(const ProviderScope(child: MyApp()));
}
```

```dart
// 사용 예시
final cartBox = Hive.box<CartItem>('cart');

// 추가
await cartBox.add(CartItem()
  ..productId = '001'
  ..name = '상품명'
  ..quantity = 1
  ..price = 10000.0);

// 조회
final items = cartBox.values.toList();

// 삭제
await cartBox.delete(key);

// Riverpod Provider 연동
@riverpod
Box<CartItem> cartBox(CartBoxRef ref) => Hive.box<CartItem>('cart');
```

```bash
dart run build_runner build --delete-conflicting-outputs
```

---

## 4️⃣ 라우팅 (GoRouter)

```dart
// core/router/app_router.dart
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'app_router.g.dart';

@riverpod
GoRouter appRouter(AppRouterRef ref) {
  return GoRouter(
    initialLocation: '/',
    debugLogDiagnostics: true,    // 라우팅 로그 (개발 중)
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const HomeScreen(),
        routes: [
          GoRoute(
            path: 'products',
            builder: (context, state) => const ProductListScreen(),
          ),
          GoRoute(
            path: 'products/:id',
            builder: (context, state) {
              final id = state.pathParameters['id']!;
              return ProductDetailScreen(id: id);
            },
          ),
        ],
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
    ],
    // 인증 리다이렉트
    redirect: (context, state) {
      final isLoggedIn = PreferenceStorage.getToken() != null;
      final isLoginPage = state.matchedLocation == '/login';
      if (!isLoggedIn && !isLoginPage) return '/login';
      if (isLoggedIn && isLoginPage) return '/';
      return null;
    },
  );
}

// main.dart에서 적용
class MyApp extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);
    return MaterialApp.router(routerConfig: router);
  }
}
```

```dart
// 화면 이동
context.go('/products');                    // 이동 (히스토리 교체)
context.push('/products/123');              // 이동 (히스토리 쌓기)
context.pop();                              // 뒤로
context.go('/products', extra: product);    // 데이터 전달
```

---

## 5️⃣ UI / 디자인

### Flutter ScreenUtil (반응형 크기)

```dart
// main.dart 설정
void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ScreenUtilInit(
      designSize: const Size(390, 844),  // 디자인 기준 해상도 (iPhone 14 기준)
      minTextAdapt: true,
      builder: (context, child) {
        return MaterialApp.router(
          routerConfig: ref.watch(appRouterProvider),
        );
      },
    );
  }
}
```

```dart
// 사용법
Container(
  width: 200.w,       // 너비 (기준 해상도 기반 비율)
  height: 100.h,      // 높이
  padding: EdgeInsets.all(16.r),  // 반응형 패딩
  child: Text(
    '상품명',
    style: TextStyle(fontSize: 16.sp),  // 반응형 폰트
  ),
)
```

### Cached Network Image (이미지 캐싱)

```dart
CachedNetworkImage(
  imageUrl: product.imageUrl,
  width: 120.w,
  height: 120.w,
  fit: BoxFit.cover,
  placeholder: (context, url) => const CircularProgressIndicator(),
  errorWidget: (context, url, error) => const Icon(Icons.broken_image),
  // 캐시 설정
  cacheKey: product.id.toString(),  // 고유 키
  memCacheWidth: 300,               // 메모리 캐시 크기 제한
)
```

### Flutter SVG

```dart
// assets/icons/ 에 SVG 파일 배치 후 pubspec.yaml에 등록
// flutter:
//   assets:
//     - assets/icons/

// Asset SVG
SvgPicture.asset(
  'assets/icons/home.svg',
  width: 24.r,
  height: 24.r,
  colorFilter: ColorFilter.mode(Colors.black, BlendMode.srcIn),
)

// 네트워크 SVG
SvgPicture.network(
  'https://example.com/logo.svg',
  width: 80.w,
)
```

---

## 6️⃣ 유틸리티

### intl (날짜 포맷)

```dart
import 'package:intl/intl.dart';

// 날짜 포맷
final formatter = DateFormat('yyyy.MM.dd');
final result = formatter.format(DateTime.now());  // "2025.03.14"

// 날짜 + 시간
DateFormat('yyyy.MM.dd HH:mm').format(dateTime);

// 상대적 시간 (직접 구현 or timeago 패키지 별도 추가)
// 숫자 포맷
NumberFormat('#,###').format(10000);         // "10,000"
NumberFormat('#,###원').format(10000);       // "10,000원"
NumberFormat.currency(symbol: '₩').format(10000);

// 로케일 초기화 (main.dart)
import 'package:intl/date_symbol_data_local.dart';

void main() async {
  await initializeDateFormatting('ko_KR');
  runApp(...);
}
```

### uuid (UUID 생성)

```dart
import 'package:uuid/uuid.dart';

const uuid = Uuid();

final id = uuid.v4();           // "110e8400-e29b-41d4-a716-446655440000"
final shortId = uuid.v4().replaceAll('-', '').substring(0, 8);  // 짧은 ID
```

### collection (컬렉션 유틸)

```dart
import 'package:collection/collection.dart';

final products = [Product(id: 1, price: 5000), Product(id: 2, price: 3000)];

// 첫 번째 일치 항목 (null 반환 가능)
final found = products.firstWhereOrNull((p) => p.id == 99);  // null

// 그룹핑
final grouped = products.groupBy((p) => p.category);
// { 'food': [...], 'drink': [...] }

// 최대/최솟값
final mostExpensive = products.maxBy((p) => p.price);
final cheapest = products.minBy((p) => p.price);

// 평균
final avgPrice = products.map((p) => p.price).average;

// 중복 제거 (id 기준)
final unique = products.distinctBy((p) => p.id).toList();
```

---

## 전체 코드 생성 한 번에 실행

```bash
# 모든 .g.dart 파일 일괄 재생성
dart run build_runner build --delete-conflicting-outputs
```

생성 대상:

- `*.g.dart` — json_serializable, Retrofit, Hive, Riverpod Generator
- GoRouter, SharedPreferences, Dio는 코드 생성 불필요ive, Riverpod Generator
- GoRouter, SharedPreferences, Dio는 코드 생성 불필요
