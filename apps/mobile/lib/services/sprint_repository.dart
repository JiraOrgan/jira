import 'package:dio/dio.dart';

import '../core/api_response.dart';
import '../models/sprint_board.dart';
import '../models/sprint_min.dart';

class SprintRepository {
  SprintRepository(this._dio);

  final Dio _dio;

  Future<List<SprintMin>> listByProject(int projectId) async {
    final res = await _dio.get<Map<String, dynamic>>(
      '/api/v1/sprints/project/$projectId',
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => (d as List<dynamic>)
          .map((e) => SprintMin.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return unwrapData(body);
  }

  Future<SprintBoardData> fetchBoard(int sprintId, {String swimlane = 'NONE'}) async {
    final res = await _dio.get<Map<String, dynamic>>(
      '/api/v1/sprints/$sprintId/board',
      queryParameters: {'swimlane': swimlane},
    );
    final body = ApiResponse.fromJson(
      res.data!,
      (d) => SprintBoardData.fromJson(d! as Map<String, dynamic>),
    );
    return unwrapData(body);
  }
}
