import 'package:dio/dio.dart';

import '../core/api_response.dart';
import '../models/project_min.dart';

class ProjectRepository {
  ProjectRepository(this._dio);

  final Dio _dio;

  Future<List<ProjectMin>> listMine() async {
    final res = await _dio.get<Map<String, dynamic>>('/api/v1/projects');
    final body = ApiResponse.fromJson(
      res.data!,
      (d) {
        final list = d as List<dynamic>;
        return list
            .map((e) => ProjectMin.fromJson(e as Map<String, dynamic>))
            .toList();
      },
    );
    return unwrapData(body);
  }
}
