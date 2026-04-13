import 'package:dio/dio.dart';

import '../core/api_response.dart';
import '../models/issue.dart';

class IssueRepository {
  IssueRepository(this._dio);

  final Dio _dio;

  Future<List<IssueMin>> listByProject(int projectId, {int size = 50}) async {
    final all = <IssueMin>[];
    var page = 0;
    while (true) {
      final res = await _dio.get<Map<String, dynamic>>(
        '/api/v1/issues/project/$projectId',
        queryParameters: {
          'page': page,
          'size': size,
          'sort': 'updatedAt,desc',
        },
      );
      final body = ApiResponse.fromJson(
        res.data!,
        (d) => SpringPage.fromJson(
          d! as Map<String, dynamic>,
          IssueMin.fromJson,
        ),
      );
      final p = unwrapData(body);
      all.addAll(p.content);
      if (page + 1 >= p.totalPages || p.content.isEmpty) break;
      page += 1;
    }
    return all;
  }

  Future<IssueDetail> byKey(String issueKey) async {
    final res = await _dio.get<Map<String, dynamic>>(
      '/api/v1/issues/${Uri.encodeComponent(issueKey)}',
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => IssueDetail.fromJson(d! as Map<String, dynamic>),
    );
    return unwrapData(body);
  }

  Future<IssueDetail> create(Map<String, dynamic> payload) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/v1/issues',
      data: payload,
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => IssueDetail.fromJson(d! as Map<String, dynamic>),
    );
    return unwrapData(body);
  }
}
