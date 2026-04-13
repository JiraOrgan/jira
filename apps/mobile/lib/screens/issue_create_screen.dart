import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/providers.dart';

const _types = ['STORY', 'TASK', 'BUG', 'EPIC', 'SUBTASK'];
const _priorities = [
  'HIGHEST',
  'HIGH',
  'MEDIUM',
  'LOW',
  'LOWEST',
];

class IssueCreateScreen extends ConsumerStatefulWidget {
  const IssueCreateScreen({super.key, required this.projectId});

  final int projectId;

  @override
  ConsumerState<IssueCreateScreen> createState() => _IssueCreateScreenState();
}

class _IssueCreateScreenState extends ConsumerState<IssueCreateScreen> {
  final _summary = TextEditingController();
  final _description = TextEditingController();
  String _type = 'TASK';
  String _priority = 'MEDIUM';
  bool _saving = false;

  @override
  void dispose() {
    _summary.dispose();
    _description.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('이슈 생성')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          DropdownButtonFormField<String>(
            value: _type,
            decoration: const InputDecoration(labelText: '유형'),
            items: _types
                .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                .toList(),
            onChanged: (v) => setState(() => _type = v ?? _type),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            value: _priority,
            decoration: const InputDecoration(labelText: '우선순위'),
            items: _priorities
                .map((p) => DropdownMenuItem(value: p, child: Text(p)))
                .toList(),
            onChanged: (v) => setState(() => _priority = v ?? _priority),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _summary,
            decoration: const InputDecoration(labelText: '요약'),
            maxLines: 2,
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _description,
            decoration: const InputDecoration(labelText: '설명 (선택)'),
            maxLines: 4,
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _saving
                ? null
                : () async {
                    final s = _summary.text.trim();
                    if (s.isEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('요약을 입력하세요')),
                      );
                      return;
                    }
                    setState(() => _saving = true);
                    try {
                      final body = <String, dynamic>{
                        'projectId': widget.projectId,
                        'issueType': _type,
                        'summary': s,
                        'priority': _priority,
                      };
                      final desc = _description.text.trim();
                      if (desc.isNotEmpty) body['description'] = desc;

                      final created = await ref
                          .read(issueRepositoryProvider)
                          .create(body);
                      if (context.mounted) {
                        Navigator.of(context).pop(created.issueKey);
                      }
                    } catch (e) {
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(e.toString())),
                        );
                      }
                    } finally {
                      if (mounted) setState(() => _saving = false);
                    }
                  },
            child: _saving
                ? const SizedBox(
                    height: 22,
                    width: 22,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('생성'),
          ),
        ],
      ),
    );
  }
}
