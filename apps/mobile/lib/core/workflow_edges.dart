/// 백엔드 `IssueWorkflowPolicy`와 동일한 허용 전이 (FR-013/FR-014, 모바일 보드 UX용).
List<String> allowedWorkflowTargets(String fromStatus) {
  switch (fromStatus) {
    case 'BACKLOG':
      return const ['SELECTED'];
    case 'SELECTED':
      return const ['BACKLOG', 'IN_PROGRESS'];
    case 'IN_PROGRESS':
      return const ['CODE_REVIEW'];
    case 'CODE_REVIEW':
      return const ['IN_PROGRESS', 'QA'];
    case 'QA':
      return const ['IN_PROGRESS', 'DONE'];
    case 'DONE':
      return const [];
    default:
      return const [];
  }
}

String statusLabelKo(String status) {
  switch (status) {
    case 'BACKLOG':
      return '백로그';
    case 'SELECTED':
      return '스프린트 선정';
    case 'IN_PROGRESS':
      return '진행 중';
    case 'CODE_REVIEW':
      return '코드 리뷰';
    case 'QA':
      return 'QA';
    case 'DONE':
      return '완료';
    default:
      return status;
  }
}
