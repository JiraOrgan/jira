import 'issue.dart';

class SprintBoardData {
  const SprintBoardData({
    required this.swimlane,
    required this.columns,
  });

  final String swimlane;
  final List<BoardColumn> columns;

  factory SprintBoardData.fromJson(Map<String, dynamic> j) {
    final cols = (j['columns'] as List<dynamic>? ?? [])
        .map((e) => BoardColumn.fromJson(e as Map<String, dynamic>))
        .toList();
    return SprintBoardData(
      swimlane: j['swimlane'] as String? ?? 'NONE',
      columns: cols,
    );
  }
}

class BoardColumn {
  const BoardColumn({
    required this.status,
    required this.buckets,
  });

  final String status;
  final List<BoardBucket> buckets;

  factory BoardColumn.fromJson(Map<String, dynamic> j) {
    final b = (j['buckets'] as List<dynamic>? ?? [])
        .map((e) => BoardBucket.fromJson(e as Map<String, dynamic>))
        .toList();
    return BoardColumn(
      status: j['status'] as String,
      buckets: b,
    );
  }

  /// swimlane NONE: 단일 버킷의 이슈 목록
  List<IssueMin> get flatIssues {
    final out = <IssueMin>[];
    for (final bucket in buckets) {
      out.addAll(bucket.issues);
    }
    return out;
  }
}

class BoardBucket {
  const BoardBucket({
    this.assigneeId,
    this.assigneeName,
    required this.issues,
  });

  final int? assigneeId;
  final String? assigneeName;
  final List<IssueMin> issues;

  factory BoardBucket.fromJson(Map<String, dynamic> j) {
    final raw = j['issues'] as List<dynamic>? ?? [];
    return BoardBucket(
      assigneeId: (j['assigneeId'] as num?)?.toInt(),
      assigneeName: j['assigneeName'] as String?,
      issues: raw.map((e) => IssueMin.fromJson(e as Map<String, dynamic>)).toList(),
    );
  }
}
