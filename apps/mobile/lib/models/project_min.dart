class ProjectMin {
  const ProjectMin({
    required this.id,
    required this.key,
    required this.name,
    required this.boardType,
    required this.archived,
  });

  final int id;
  final String key;
  final String name;
  final String boardType;
  final bool archived;

  factory ProjectMin.fromJson(Map<String, dynamic> j) {
    return ProjectMin(
      id: (j['id'] as num).toInt(),
      key: j['key'] as String,
      name: j['name'] as String,
      boardType: j['boardType'] as String? ?? '',
      archived: j['archived'] as bool? ?? false,
    );
  }
}
