import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/api_client.dart';
import '../models/auth_token.dart';
import '../models/issue.dart';
import '../models/project_min.dart';
import '../models/sprint_board.dart';
import '../models/sprint_min.dart';
import '../services/auth_repository.dart';
import '../services/issue_repository.dart';
import '../services/project_repository.dart';
import '../services/sprint_repository.dart';

final bareDioProvider = Provider<Dio>((ref) => createBaseDio());

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(ref.watch(bareDioProvider)),
);

class AuthNotifier extends AsyncNotifier<AuthToken?> {
  @override
  Future<AuthToken?> build() {
    return ref.read(authRepositoryProvider).loadStored();
  }

  Future<void> login(String email, String password) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final t = await ref.read(authRepositoryProvider).login(email, password);
      await ref.read(authRepositoryProvider).persist(t);
      return t;
    });
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).clear();
    state = const AsyncData(null);
  }
}

final authNotifierProvider =
    AsyncNotifierProvider<AuthNotifier, AuthToken?>(AuthNotifier.new);

final apiDioProvider = Provider<Dio>((ref) {
  final auth = ref.watch(authNotifierProvider);
  final dio = createBaseDio();
  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) {
        final token = auth.valueOrNull;
        if (token != null) {
          options.headers['Authorization'] = 'Bearer ${token.accessToken}';
        }
        return handler.next(options);
      },
      onError: (e, handler) async {
        if (e.response?.statusCode == 401) {
          await ref.read(authNotifierProvider.notifier).logout();
        }
        return handler.next(e);
      },
    ),
  );
  return dio;
});

final projectRepositoryProvider = Provider<ProjectRepository>(
  (ref) => ProjectRepository(ref.watch(apiDioProvider)),
);

final issueRepositoryProvider = Provider<IssueRepository>(
  (ref) => IssueRepository(ref.watch(apiDioProvider)),
);

final sprintRepositoryProvider = Provider<SprintRepository>(
  (ref) => SprintRepository(ref.watch(apiDioProvider)),
);

final sprintListProvider =
    FutureProvider.family<List<SprintMin>, int>((ref, projectId) async {
  return ref.watch(sprintRepositoryProvider).listByProject(projectId);
});

final sprintBoardProvider =
    FutureProvider.family<SprintBoardData, int>((ref, sprintId) async {
  return ref.watch(sprintRepositoryProvider).fetchBoard(sprintId);
});

final selectedProjectProvider = StateProvider<ProjectMin?>((ref) => null);

final projectListProvider = FutureProvider<List<ProjectMin>>((ref) async {
  final auth = ref.watch(authNotifierProvider);
  if (!auth.hasValue || auth.valueOrNull == null) {
    return [];
  }
  return ref.watch(projectRepositoryProvider).listMine();
});

final issueListProvider =
    FutureProvider.family<List<IssueMin>, int>((ref, projectId) async {
  return ref.watch(issueRepositoryProvider).listByProject(projectId);
});

final issueDetailProvider =
    FutureProvider.family<IssueDetail, String>((ref, issueKey) async {
  return ref.watch(issueRepositoryProvider).byKey(issueKey);
});
