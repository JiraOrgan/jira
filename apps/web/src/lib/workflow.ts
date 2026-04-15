import type { IssueStatus } from '../types/domain'

/** 백엔드 `IssueWorkflowPolicy` 와 동일한 허용 전이 */
const EDGES: ReadonlyArray<readonly [IssueStatus, IssueStatus]> = [
  ['BACKLOG', 'SELECTED'],
  ['SELECTED', 'BACKLOG'],
  ['SELECTED', 'IN_PROGRESS'],
  ['IN_PROGRESS', 'CODE_REVIEW'],
  ['CODE_REVIEW', 'IN_PROGRESS'],
  ['CODE_REVIEW', 'QA'],
  ['QA', 'IN_PROGRESS'],
  ['QA', 'DONE'],
]

export function allowedNextStatuses(from: IssueStatus): IssueStatus[] {
  return EDGES.filter(([a]) => a === from).map(([, b]) => b)
}

export const ISSUE_STATUS_ORDER: IssueStatus[] = [
  'BACKLOG',
  'SELECTED',
  'IN_PROGRESS',
  'CODE_REVIEW',
  'QA',
  'DONE',
]
