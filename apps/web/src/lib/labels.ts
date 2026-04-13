import type { IssueStatus, IssueType, Priority, SecurityLevel } from '../types/domain'

export const issueTypeLabel: Record<IssueType, string> = {
  EPIC: 'Epic',
  STORY: 'Story',
  TASK: 'Task',
  BUG: 'Bug',
  SUBTASK: 'Sub-task',
}

export const priorityLabel: Record<Priority, string> = {
  HIGHEST: 'Highest',
  HIGH: 'High',
  MEDIUM: 'Medium',
  LOW: 'Low',
  LOWEST: 'Lowest',
}

export const statusLabel: Record<IssueStatus, string> = {
  BACKLOG: 'Backlog',
  SELECTED: 'Selected for Sprint',
  IN_PROGRESS: 'In Progress',
  CODE_REVIEW: 'Code Review',
  QA: 'QA',
  DONE: 'Done',
}

export const securityLabel: Record<SecurityLevel, string> = {
  PUBLIC: 'Public',
  INTERNAL: 'Internal',
  CONFIDENTIAL: 'Confidential',
}

export const ISSUE_TYPES: IssueType[] = [
  'EPIC',
  'STORY',
  'TASK',
  'BUG',
  'SUBTASK',
]

export const PRIORITIES: Priority[] = [
  'HIGHEST',
  'HIGH',
  'MEDIUM',
  'LOW',
  'LOWEST',
]

export const SECURITY_LEVELS: SecurityLevel[] = [
  'PUBLIC',
  'INTERNAL',
  'CONFIDENTIAL',
]

export const STORY_POINT_OPTIONS = [1, 2, 3, 5, 8, 13] as const
