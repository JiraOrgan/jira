import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/providers.dart';

class IssueDetailScreen extends ConsumerStatefulWidget {
  const IssueDetailScreen({super.key, required this.issueKey});

  final String issueKey;

  @override
  ConsumerState<IssueDetailScreen> createState() => _IssueDetailScreenState();
}

class _IssueDetailScreenState extends ConsumerState<IssueDetailScreen> {
  bool _unarchiveBusy = false;

  Future<void> _unarchive() async {
    setState(() => _unarchiveBusy = true);
    try {
      final detail = await ref.read(issueRepositoryProvider).update(
            widget.issueKey,
            const {'archived': false},
          );
      ref.invalidate(issueDetailProvider(widget.issueKey));
      ref.invalidate(issueListProvider(detail.projectId));
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('아카이브를 해제했습니다.')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('아카이브 해제 실패: $e')),
      );
    } finally {
      if (mounted) setState(() => _unarchiveBusy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final async = ref.watch(issueDetailProvider(widget.issueKey));

    return Scaffold(
      appBar: AppBar(title: Text(widget.issueKey)),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('조회 실패: $e')),
        data: (issue) {
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (issue.archived) ...[
                MaterialBanner(
                  content: const Text(
                    '이 이슈는 아카이브되어 있습니다. 웹에서 다시 아카이브할 수 있습니다.',
                  ),
                  actions: [
                    FilledButton(
                      onPressed: _unarchiveBusy ? null : _unarchive,
                      child: _unarchiveBusy
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('아카이브 해제'),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
              ],
              Text(
                issue.summary,
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Text('프로젝트: ${issue.projectKey}'),
              Text('상태: ${issue.status}'),
              Text('유형: ${issue.issueType}'),
              Text('우선순위: ${issue.priority}'),
              if (issue.storyPoints != null)
                Text('스토리 포인트: ${issue.storyPoints}'),
              if (issue.assigneeName != null)
                Text('담당: ${issue.assigneeName}'),
              Text('보고자: ${issue.reporterName ?? issue.reporterId}'),
              if (issue.parentKey != null)
                Text('부모 이슈: ${issue.parentKey}'),
              if (issue.securityLevel != null)
                Text('보안 레벨: ${issue.securityLevel}'),
              const SizedBox(height: 12),
              if (issue.labels.isNotEmpty) ...[
                Text(
                  '레이블',
                  style: Theme.of(context).textTheme.titleSmall,
                ),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: issue.labels
                      .map((l) => Chip(label: Text(l.name)))
                      .toList(),
                ),
                const SizedBox(height: 12),
              ],
              if (issue.components.isNotEmpty) ...[
                Text(
                  '컴포넌트',
                  style: Theme.of(context).textTheme.titleSmall,
                ),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: issue.components
                      .map((c) => Chip(label: Text(c.name)))
                      .toList(),
                ),
                const SizedBox(height: 12),
              ],
              if (issue.description != null && issue.description!.isNotEmpty) ...[
                Text(
                  '설명',
                  style: Theme.of(context).textTheme.titleSmall,
                ),
                const SizedBox(height: 4),
                Text(issue.description!),
              ],
            ],
          );
        },
      ),
    );
  }
}
