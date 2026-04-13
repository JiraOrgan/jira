class ApiResponse<T> {
  ApiResponse({
    required this.success,
    required this.status,
    this.message,
    this.data,
  });

  final bool success;
  final int status;
  final String? message;
  final T? data;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Object? json)? parseData,
  ) {
    return ApiResponse<T>(
      success: json['success'] as bool? ?? false,
      status: (json['status'] as num?)?.toInt() ?? 0,
      message: json['message'] as String?,
      data: parseData != null && json.containsKey('data')
          ? parseData(json['data'])
          : json['data'] as T?,
    );
  }
}

T unwrapData<T>(ApiResponse<T> r) {
  if (!r.success || r.data == null) {
    throw ApiException(r.status, r.message ?? '요청이 실패했습니다');
  }
  return r.data as T;
}

class ApiException implements Exception {
  ApiException(this.status, this.message);
  final int status;
  final String message;

  @override
  String toString() => 'ApiException($status): $message';
}
