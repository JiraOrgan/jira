import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/api_response.dart';
import '../models/auth_token.dart';

const _kAccess = 'pch_access_token';
const _kRefresh = 'pch_refresh_token';

class AuthRepository {
  AuthRepository(this._dio);

  final Dio _dio;

  Future<AuthToken?> loadStored() async {
    final p = await SharedPreferences.getInstance();
    final a = p.getString(_kAccess);
    final r = p.getString(_kRefresh);
    if (a == null || r == null) return null;
    return AuthToken(accessToken: a, refreshToken: r);
  }

  Future<void> persist(AuthToken t) async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_kAccess, t.accessToken);
    await p.setString(_kRefresh, t.refreshToken);
  }

  Future<void> clear() async {
    final p = await SharedPreferences.getInstance();
    await p.remove(_kAccess);
    await p.remove(_kRefresh);
  }

  Future<AuthToken> login(String email, String password) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/auth/login',
      data: {'email': email, 'password': password},
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => AuthToken.fromJson(d! as Map<String, dynamic>),
    );
    return unwrapData(body);
  }

  Future<AuthToken> refresh(String refreshToken) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/auth/refresh',
      data: {'refreshToken': refreshToken},
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => AuthToken.fromJson(d! as Map<String, dynamic>),
    );
    return unwrapData(body);
  }
}
