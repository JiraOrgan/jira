import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/project_min.dart';
import '../providers/providers.dart';
import 'issue_create_screen.dart';
import 'issue_detail_screen.dart';
import 'sprint_board_screen.dart';

class IssueListScreen extends ConsumerWidget {
  const IssueListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen<AsyncValue<List<ProjectMin>>>(projectListProvider, (prev, next) {
      next.whenData((list) {
        if (list.isEmpty) return;
        final sel = ref.read(selectedProjectProvider);
        final valid = sel != null && list.any((p) => p.id == sel.id);
        if (!valid) {
          ref.read(selectedProjectProvider.notifier).state = list.first;
        }
      });
    });

    final projectsAsync = ref.watch(projectListProvider);
    final selected = ref.watch(selectedProjectProvider);

    return projectsAsync.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (e, _) => Scaffold(
        appBar: AppBar(title: const Text('이슈')),
        body: Center(child: Text('프로젝트 로드 실패: $e')),
      ),
      data: (projects) {
        if (projects.isEmpty) {
          return Scaffold(
            appBar: AppBar(
              title: const Text('이슈'),
              actions: [
                IconButton(
                  icon: const Icon(Icons.logout),
                  onPressed: () =>
                      ref.read(authNotifierProvider.notifier).logout(),
                ),
              ],
            ),
            body: const Center(child: Text('참여 중인 프로젝트가 없습니다.')),
          );
        }

        final current = selected != null &&
                projects.any((p) => p.id == selected.id)
            ? selected
            : projects.first;

        final issuesAsync = ref.watch(issueListProvider(current.id));

        return Scaffold(
          appBar: AppBar(
            title: const Text('이슈'),
            actions: [
              IconButton(
                icon: const Icon(Icons.view_kanban_outlined),
                tooltip: '스프린트 보드',
                onPressed: () {
                  Navigator.of(context).push<void>(
                    MaterialPageRoute(
                      builder: (_) =>
                          SprintBoardScreen(projectId: current.id),
                    ),
                  );
                },
              ),
              IconButton(
                icon: const Icon(Icons.logout),
                tooltip: '로그아웃',
                onPressed: () =>
                    ref.read(authNotifierProvider.notifier).logout(),
              ),
            ],
          ),
          body: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
                child: DropdownButtonFormField<ProjectMin>(
                  initialValue: current,
                  decoration: const InputDecoration(labelText: '프로젝트'),
                  items: projects
                      .map(
                        (p) => DropdownMenuItem(
                          value: p,
                          child: Text('${p.key} · ${p.name}'),
                        ),
                      )
                      .toList(),
                  onChanged: (p) {
                    if (p != null) {
                      ref.read(selectedProjectProvider.notifier).state = p;
                    }
                  },
                ),
              ),
              const Divider(),
              Expanded(
                child: issuesAsync.when(
                  loading: () =>
                      const Center(child: CircularProgressIndicator()),
                  error: (e, _) => Center(child: Text('이슈 로드 실패: $e')),
                  data: (issues) {
                    if (issues.isEmpty) {
                      return const Center(child: Text('이슈가 없습니다.'));
                    }
                    return RefreshIndicator(
                      onRefresh: () async {
                        ref.invalidate(issueListProvider(current.id));
                        await ref.read(issueListProvider(current.id).future);
                      },
                      child: ListView.builder(
                        itemCount: issues.length,
                        itemBuilder: (context, i) {
                          final it = issues[i];
                          return ListTile(
                            title: Text(
                              it.summary,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            subtitle: Text(
                              '${it.issueKey} · ${it.status} · ${it.issueType}'
                              '${it.archived ? ' · 아카이브' : ''}',
                            ),
                            onTap: () {
                              Navigator.of(context).push<void>(
                                MaterialPageRoute(
                                  builder: (_) =>
                                      IssueDetailScreen(issueKey: it.issueKey),
                                ),
                              );
                            },
                          );
                        },
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton.extended(
            onPressed: () async {
              final key = await Navigator.of(context).push<String>(
                MaterialPageRoute(
                  builder: (_) => IssueCreateScreen(projectId: current.id),
                ),
              );
              if (key != null && context.mounted) {
                ref.invalidate(issueListProvider(current.id));
              }
            },
            icon: const Icon(Icons.add),
            label: const Text('이슈'),
          ),
        );
      },
    );
  }
}
