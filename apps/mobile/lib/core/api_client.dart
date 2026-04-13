import 'package:dio/dio.dart';

/// 백엔드 Base URL — `flutter run --dart-define=API_BASE_URL=http://...`
Dio createBaseDio() {
  const base = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
  return Dio(
    BaseOptions(
      baseUrl: base.replaceAll(RegExp(r'/$'), ''),
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 30),
      headers: const {'Content-Type': 'application/json'},
    ),
  );
}
