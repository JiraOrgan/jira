class SprintMin {
  const SprintMin({
    required this.id,
    required this.name,
    required this.status,
    this.startDate,
    this.endDate,
  });

  final int id;
  final String name;
  final String status;
  final String? startDate;
  final String? endDate;

  factory SprintMin.fromJson(Map<String, dynamic> j) {
    return SprintMin(
      id: (j['id'] as num).toInt(),
      name: j['name'] as String,
      status: j['status'] as String,
      startDate: j['startDate'] as String?,
      endDate: j['endDate'] as String?,
    );
  }
}
