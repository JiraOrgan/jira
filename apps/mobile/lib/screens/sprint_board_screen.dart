import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/api_response.dart';
import '../core/workflow_edges.dart';
import '../models/issue.dart';
import '../models/sprint_min.dart';
import '../providers/providers.dart';
import 'issue_detail_screen.dart';

/// 스프린트 칸반 보드 — 컬럼 탭·이슈 탭 시 워크플로 전환(FR-MOBILE-002).
class SprintBoardScreen extends ConsumerStatefulWidget {
  const SprintBoardScreen({super.key, required this.projectId});

  final int projectId;

  @override
  ConsumerState<SprintBoardScreen> createState() => _SprintBoardScreenState();
}

class _SprintBoardScreenState extends ConsumerState<SprintBoardScreen> {
  int? _sprintId;

  int _defaultSprintId(List<SprintMin> list) {
    for (final s in list) {
      if (s.status == 'ACTIVE') return s.id;
    }
    return list.first.id;
  }

  Future<void> _pickTransition(
    BuildContext context,
    IssueMin issue,
    int sprintId,
  ) async {
    final targets = allowedWorkflowTargets(issue.status);
    if (targets.isEmpty) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('이 상태에서 이동할 다음 단계가 없습니다.')),
        );
      }
      return;
    }
    final picked = await showModalBottomSheet<String>(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: Text(
                '${issue.issueKey} · 상태 변경',
                style: Theme.of(ctx).textTheme.titleMedium,
              ),
            ),
            const Divider(height: 1),
            ...targets.map(
              (t) => ListTile(
                title: Text(statusLabelKo(t)),
                subtitle: Text(t, style: Theme.of(ctx).textTheme.bodySmall),
                onTap: () => Navigator.pop(ctx, t),
              ),
            ),
          ],
        ),
      ),
    );
    if (picked == null || !context.mounted) return;
    try {
      await ref.read(issueRepositoryProvider).transition(issue.issueKey, picked);
      ref.invalidate(sprintBoardProvider(sprintId));
      ref.invalidate(issueListProvider(widget.projectId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${statusLabelKo(picked)}(으)로 변경했습니다')),
        );
      }
    } on ApiException catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('전환 실패: ${e.message}')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('오류: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final sprintsAsync = ref.watch(sprintListProvider(widget.projectId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('스프린트 보드'),
        actions: [
          IconButton(
            tooltip: '새로고침',
            icon: const Icon(Icons.refresh),
            onPressed: () {
              ref.invalidate(sprintListProvider(widget.projectId));
              final sid = _sprintId;
              if (sid != null) {
                ref.invalidate(sprintBoardProvider(sid));
              }
            },
          ),
        ],
      ),
      body: sprintsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('스프린트 로드 실패: $e')),
        data: (sprints) {
          if (sprints.isEmpty) {
            return const Center(
              child: Padding(
                padding: EdgeInsets.all(24),
                child: Text(
                  '스프린트가 없습니다.\n웹에서 스프린트를 만든 뒤 다시 열어 주세요.',
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }
          final sid = _sprintId ?? _defaultSprintId(sprints);
          if (_sprintId == null) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (mounted) setState(() => _sprintId = sid);
            });
            return const Center(child: CircularProgressIndicator());
          }
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
                child: DropdownButtonFormField<int>(
                  value: sid,
                  decoration: const InputDecoration(labelText: '스프린트'),
                  items: sprints
                      .map(
                        (s) => DropdownMenuItem(
                          value: s.id,
                          child: Text('${s.name} · ${s.status}'),
                        ),
                      )
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _sprintId = v);
                  },
                ),
              ),
              const Divider(),
              Expanded(
                child: _BoardPane(
                  sprintId: sid,
                  onIssueTap: (iss) => _pickTransition(context, iss, sid),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _BoardPane extends ConsumerWidget {
  const _BoardPane({
    required this.sprintId,
    required this.onIssueTap,
  });

  final int sprintId;
  final void Function(IssueMin issue) onIssueTap;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final boardAsync = ref.watch(sprintBoardProvider(sprintId));
    return boardAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('보드 로드 실패: $e')),
      data: (board) {
        return LayoutBuilder(
          builder: (context, constraints) {
            return RefreshIndicator(
              onRefresh: () async {
                ref.invalidate(sprintBoardProvider(sprintId));
                await ref.read(sprintBoardProvider(sprintId).future);
              },
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                scrollDirection: Axis.vertical,
                child: SizedBox(
                  height: constraints.maxHeight,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: board.columns.length,
                    itemBuilder: (context, i) {
                      final col = board.columns[i];
                      final issues = col.flatIssues;
                      return SizedBox(
                        width: 272,
                        height: constraints.maxHeight,
                        child: Card(
                          margin: const EdgeInsets.fromLTRB(8, 0, 8, 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              Material(
                                color: Theme.of(context)
                                    .colorScheme
                                    .surfaceContainerHighest,
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 12,
                                    vertical: 10,
                                  ),
                                  child: Text(
                                    '${statusLabelKo(col.status)} · ${issues.length}',
                                    style:
                                        Theme.of(context).textTheme.titleSmall,
                                  ),
                                ),
                              ),
                              Expanded(
                                child: issues.isEmpty
                                    ? const Center(
                                        child: Padding(
                                          padding: EdgeInsets.all(12),
                                          child: Text(
                                            '이슈 없음',
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                      )
                                    : ListView.builder(
                                        itemCount: issues.length,
                                        itemBuilder: (context, j) {
                                          final iss = issues[j];
                                          return ListTile(
                                            dense: true,
                                            title: Text(
                                              iss.summary,
                                              maxLines: 2,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                            subtitle: Text(iss.issueKey),
                                            trailing: const Icon(
                                              Icons.swap_horiz,
                                              size: 20,
                                            ),
                                            onTap: () => onIssueTap(iss),
                                            onLongPress: () {
                                              Navigator.of(context).push<void>(
                                                MaterialPageRoute(
                                                  builder: (_) =>
                                                      IssueDetailScreen(
                                                    issueKey: iss.issueKey,
                                                  ),
                                                ),
                                              );
                                            },
                                          );
                                        },
                                      ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }
}
