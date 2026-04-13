class AuthToken {
  const AuthToken({
    required this.accessToken,
    required this.refreshToken,
  });

  final String accessToken;
  final String refreshToken;

  factory AuthToken.fromJson(Map<String, dynamic> j) {
    return AuthToken(
      accessToken: j['accessToken'] as String,
      refreshToken: j['refreshToken'] as String,
    );
  }
}
