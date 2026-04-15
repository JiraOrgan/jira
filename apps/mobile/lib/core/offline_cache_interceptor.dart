import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// GET `/api/v1/**` 성공 응답을 SharedPreferences에 캐시하고, 네트워크 실패 시 스테일 데이터를 반환한다.
class OfflineResponseCacheInterceptor extends Interceptor {
  OfflineResponseCacheInterceptor(this._prefs);

  final SharedPreferences _prefs;

  static String _cacheKey(Uri uri) => 'pch_http_cache_v1_${uri.toString()}';

  @override
  void onResponse(Response<dynamic> response, ResponseInterceptorHandler handler) {
    final ro = response.requestOptions;
    if (ro.method == 'GET' &&
        response.statusCode == 200 &&
        ro.uri.path.startsWith('/api/v1/')) {
      try {
        final encoded = jsonEncode(response.data);
        _prefs.setString(_cacheKey(ro.uri), encoded);
      } catch (_) {}
    }
    handler.next(response);
  }

  @override
  Future<void> onError(DioException err, ErrorInterceptorHandler handler) async {
    final ro = err.requestOptions;
    if (ro.method == 'GET' && ro.uri.path.startsWith('/api/v1/')) {
      final t = err.type;
      if (t == DioExceptionType.connectionError ||
          t == DioExceptionType.connectionTimeout ||
          t == DioExceptionType.receiveTimeout) {
        final raw = _prefs.getString(_cacheKey(ro.uri));
        if (raw != null) {
          try {
            final data = jsonDecode(raw) as dynamic;
            return handler.resolve(
              Response<dynamic>(
                requestOptions: ro,
                data: data,
                statusCode: 200,
              ),
            );
          } catch (_) {}
        }
      }
    }
    handler.next(err);
  }
}
