class IssueMin {
  const IssueMin({
    required this.id,
    required this.issueKey,
    required this.issueType,
    required this.summary,
    required this.status,
    required this.priority,
    this.storyPoints,
    required this.backlogRank,
    this.assigneeName,
  });

  final int id;
  final String issueKey;
  final String issueType;
  final String summary;
  final String status;
  final String priority;
  final int? storyPoints;
  final int backlogRank;
  final String? assigneeName;

  factory IssueMin.fromJson(Map<String, dynamic> j) {
    return IssueMin(
      id: (j['id'] as num).toInt(),
      issueKey: j['issueKey'] as String,
      issueType: j['issueType'] as String,
      summary: j['summary'] as String,
      status: j['status'] as String,
      priority: j['priority'] as String,
      storyPoints: (j['storyPoints'] as num?)?.toInt(),
      backlogRank: (j['backlogRank'] as num?)?.toInt() ?? 0,
      assigneeName: j['assigneeName'] as String?,
    );
  }
}

class IssueLabelItem {
  const IssueLabelItem({required this.id, required this.name});
  final int id;
  final String name;
  factory IssueLabelItem.fromJson(Map<String, dynamic> j) {
    return IssueLabelItem(
      id: (j['id'] as num).toInt(),
      name: j['name'] as String,
    );
  }
}

class IssueComponentItem {
  const IssueComponentItem({required this.id, required this.name});
  final int id;
  final String name;
  factory IssueComponentItem.fromJson(Map<String, dynamic> j) {
    return IssueComponentItem(
      id: (j['id'] as num).toInt(),
      name: j['name'] as String,
    );
  }
}

class IssueDetail {
  const IssueDetail({
    required this.id,
    required this.issueKey,
    required this.projectId,
    required this.projectKey,
    required this.issueType,
    required this.summary,
    this.description,
    required this.status,
    required this.priority,
    this.storyPoints,
    this.assigneeId,
    this.assigneeName,
    required this.reporterId,
    this.reporterName,
    this.parentId,
    this.parentKey,
    this.sprintId,
    required this.backlogRank,
    this.securityLevel,
    required this.createdAt,
    required this.updatedAt,
    required this.labels,
    required this.components,
  });

  final int id;
  final String issueKey;
  final int projectId;
  final String projectKey;
  final String issueType;
  final String summary;
  final String? description;
  final String status;
  final String priority;
  final int? storyPoints;
  final int? assigneeId;
  final String? assigneeName;
  final int reporterId;
  final String? reporterName;
  final int? parentId;
  final String? parentKey;
  final int? sprintId;
  final int backlogRank;
  final String? securityLevel;
  final String createdAt;
  final String updatedAt;
  final List<IssueLabelItem> labels;
  final List<IssueComponentItem> components;

  factory IssueDetail.fromJson(Map<String, dynamic> j) {
    return IssueDetail(
      id: (j['id'] as num).toInt(),
      issueKey: j['issueKey'] as String,
      projectId: (j['projectId'] as num).toInt(),
      projectKey: j['projectKey'] as String,
      issueType: j['issueType'] as String,
      summary: j['summary'] as String,
      description: j['description'] as String?,
      status: j['status'] as String,
      priority: j['priority'] as String,
      storyPoints: (j['storyPoints'] as num?)?.toInt(),
      assigneeId: (j['assigneeId'] as num?)?.toInt(),
      assigneeName: j['assigneeName'] as String?,
      reporterId: (j['reporterId'] as num).toInt(),
      reporterName: j['reporterName'] as String?,
      parentId: (j['parentId'] as num?)?.toInt(),
      parentKey: j['parentKey'] as String?,
      sprintId: (j['sprintId'] as num?)?.toInt(),
      backlogRank: (j['backlogRank'] as num?)?.toInt() ?? 0,
      securityLevel: j['securityLevel'] as String?,
      createdAt: j['createdAt'] as String,
      updatedAt: j['updatedAt'] as String,
      labels: (j['labels'] as List<dynamic>? ?? [])
          .map((e) => IssueLabelItem.fromJson(e as Map<String, dynamic>))
          .toList(),
      components: (j['components'] as List<dynamic>? ?? [])
          .map((e) => IssueComponentItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SpringPage<T> {
  const SpringPage({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.size,
    required this.number,
  });

  final List<T> content;
  final int totalElements;
  final int totalPages;
  final int size;
  final int number;

  factory SpringPage.fromJson(
    Map<String, dynamic> j,
    T Function(Map<String, dynamic>) item,
  ) {
    final raw = j['content'] as List<dynamic>? ?? [];
    return SpringPage<T>(
      content: raw.map((e) => item(e as Map<String, dynamic>)).toList(),
      totalElements: (j['totalElements'] as num?)?.toInt() ?? 0,
      totalPages: (j['totalPages'] as num?)?.toInt() ?? 0,
      size: (j['size'] as num?)?.toInt() ?? 0,
      number: (j['number'] as num?)?.toInt() ?? 0,
    );
  }
}
