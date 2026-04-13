import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/providers.dart';

class IssueDetailScreen extends ConsumerWidget {
  const IssueDetailScreen({super.key, required this.issueKey});

  final String issueKey;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(issueDetailProvider(issueKey));

    return Scaffold(
      appBar: AppBar(title: Text(issueKey)),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('조회 실패: $e')),
        data: (issue) {
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text(
                issue.summary,
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Text('상태: ${issue.status}'),
              Text('유형: ${issue.issueType}'),
              Text('우선순위: ${issue.priority}'),
              if (issue.assigneeName != null)
                Text('담당: ${issue.assigneeName}'),
              Text('보고자: ${issue.reporterName ?? issue.reporterId}'),
              const SizedBox(height: 16),
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
