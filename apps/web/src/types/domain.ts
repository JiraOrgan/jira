import type { ApiResponse } from './api'

export type IssueType = 'EPIC' | 'STORY' | 'TASK' | 'BUG' | 'SUBTASK'

export type IssueStatus =
  | 'BACKLOG'
  | 'SELECTED'
  | 'IN_PROGRESS'
  | 'CODE_REVIEW'
  | 'QA'
  | 'DONE'

export type Priority = 'HIGHEST' | 'HIGH' | 'MEDIUM' | 'LOW' | 'LOWEST'

export type SecurityLevel = 'PUBLIC' | 'INTERNAL' | 'CONFIDENTIAL'

export type SprintStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED'

export type ProjectMin = {
  id: number
  key: string
  name: string
  boardType: string
  archived: boolean
}

export type ProjectRole =
  | 'ADMIN'
  | 'DEVELOPER'
  | 'QA'
  | 'REPORTER'
  | 'VIEWER'

export type ProjectDetail = {
  id: number
  key: string
  name: string
  description: string | null
  boardType: string
  leadId: number | null
  leadName: string | null
  archived: boolean
  createdAt: string
}

export type ProjectMember = {
  id: number
  userId: number
  userName: string
  userEmail: string
  role: ProjectRole
  joinedAt: string
}

export type ProjectUpdateBody = {
  name: string
  description?: string | null
  leadId?: number
  /** 생략 시 백엔드에서 아카이브 플래그 유지 */
  archived?: boolean
}

export type VersionStatus = 'UNRELEASED' | 'RELEASED'

export type ReleaseVersionMin = {
  id: number
  name: string
  status: VersionStatus
  releaseDate: string | null
}

export type ReleaseVersionDetail = {
  id: number
  projectId: number
  name: string
  description: string | null
  status: VersionStatus
  releaseDate: string | null
  createdAt: string
}

/** `ReleaseNotesResponse.DTO` — Fix 버전 연결 이슈 기반 릴리즈 노트 초안 */
export type ReleaseNotesIssueLine = {
  issueKey: string
  summary: string
  issueType: IssueType
  status: IssueStatus
}

export type ReleaseNotesPayload = {
  versionId: number
  versionName: string
  issueCount: number
  markdown: string
  issues: ReleaseNotesIssueLine[]
}

/** `IssueResponse.MinDTO` — 백로그·목록용 */
export type IssueMin = {
  id: number
  issueKey: string
  issueType: IssueType
  summary: string
  status: IssueStatus
  priority: Priority
  storyPoints: number | null
  backlogRank: number
  assigneeName: string | null
}

export type BoardSwimlane = 'NONE' | 'ASSIGNEE'

export type SprintBoardBucket = {
  assigneeId: number | null
  assigneeName: string | null
  issues: IssueMin[]
}

export type SprintBoardColumn = {
  status: IssueStatus
  buckets: SprintBoardBucket[]
}

export type SprintBoardData = {
  swimlane: BoardSwimlane
  columns: SprintBoardColumn[]
}

export type WipLimitItem = {
  status: IssueStatus
  maxIssues: number
}

export type SpringPage<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export type SprintDetail = {
  id: number
  projectId: number
  name: string
  status: SprintStatus
  startDate: string
  endDate: string
  goalPoints: number | null
  createdAt: string
}

export type IssueLabelItem = { id: number; name: string }
export type IssueComponentItem = { id: number; name: string }

/** `CommentResponse.MentionDTO` */
export type CommentMentionUser = {
  userId: number
  userName: string | null
}

/** `CommentResponse.DetailDTO` */
export type CommentDetail = {
  id: number
  issueId: number
  authorId: number
  authorName: string | null
  body: string
  createdAt: string
  updatedAt: string
  mentionedUsers: CommentMentionUser[]
}

export type IssueDetail = {
  id: number
  issueKey: string
  projectId: number
  projectKey: string
  issueType: IssueType
  summary: string
  description: string | null
  status: IssueStatus
  priority: Priority
  storyPoints: number | null
  assigneeId: number | null
  assigneeName: string | null
  reporterId: number
  reporterName: string | null
  parentId: number | null
  parentKey: string | null
  sprintId: number | null
  backlogRank: number
  securityLevel: SecurityLevel | null
  epicStartDate?: string | null
  epicEndDate?: string | null
  createdAt: string
  updatedAt: string
  labels: IssueLabelItem[]
  components: IssueComponentItem[]
}

export type IssueSaveBody = {
  projectId: number
  issueType: IssueType
  summary: string
  description?: string
  priority: Priority
  storyPoints?: number
  assigneeId?: number
  parentId?: number
  sprintId?: number
  securityLevel?: SecurityLevel
  epicStartDate?: string
  epicEndDate?: string
}

/** `PUT /api/v1/issues/{issueKey}` — null 값은 patchEpicDates true일 때만 전송 권장 */
export type IssueUpdateBody = {
  summary?: string
  description?: string
  priority?: Priority
  storyPoints?: number
  assigneeId?: number
  sprintId?: number
  securityLevel?: SecurityLevel
  clearEpicDates?: boolean
  patchEpicDates?: boolean
  epicStartDate?: string | null
  epicEndDate?: string | null
}

export type TransitionBody = {
  toStatus: IssueStatus
  conditionNote?: string
}

export type WorkflowTransitionItem = {
  id: number
  issueId: number
  fromStatus: IssueStatus
  toStatus: IssueStatus
  changedByName: string | null
  conditionNote: string | null
  transitionedAt: string
}

export type SprintMin = {
  id: number
  name: string
  status: SprintStatus
  startDate: string
  endDate: string
}

/** `POST .../jql/search` 응답 */
export type JqlSearchResult = {
  startAt: number
  maxResults: number
  total: number
  issues: IssueMin[]
}

export type SavedJqlFilter = {
  id: number
  name: string
  jql: string
  createdAt: string
}

/** `GET .../projects/{id}/roadmap/epics` */
export type RoadmapEpicItem = {
  id: number
  issueKey: string
  summary: string
  status: IssueStatus
  epicStartDate: string | null
  epicEndDate: string | null
  effectiveStart: string
  effectiveEnd: string
}

export type DashboardGadgetRow = {
  id: number
  gadgetType: string
  position: number
  configJson: string | null
}

export type DashboardMin = {
  id: number
  name: string
  shared: boolean
  ownerName: string | null
}

export type DashboardDetail = {
  id: number
  name: string
  shared: boolean
  ownerId: number | null
  ownerName: string | null
  createdAt: string
  gadgets: DashboardGadgetRow[] | null
}

export type UserMin = {
  id: number
  email: string
  name: string
}

export type AttachmentDetail = {
  id: number
  issueId: number
  uploaderName: string | null
  fileName: string
  filePath: string
  fileSize: number
  mimeType: string
  createdAt: string
}

/** `AuditLogResponse.DetailDTO` */
export type AuditLogRow = {
  id: number
  issueId: number
  issueKey: string | null
  changedById: number
  changedByName: string | null
  fieldName: string
  oldValue: string | null
  newValue: string | null
  changedAt: string
}

/** `GET .../reports/sprints/{id}/burndown` */
export type ReportBurndownPoint = {
  date: string
  remainingStoryPoints: number
  idealRemainingPoints: number
}

export type ReportBurndown = {
  projectId: number
  sprintId: number
  sprintName: string
  startDate: string
  endDate: string
  totalScopePoints: number
  series: ReportBurndownPoint[]
}

export type ReportVelocityBar = {
  sprintId: number
  sprintName: string
  endDate: string | null
  completedStoryPoints: number
}

export type ReportVelocity = {
  projectId: number
  sprints: ReportVelocityBar[]
}

export type ReportCfdStatusCount = {
  status: IssueStatus
  count: number
}

export type ReportCfdDay = {
  date: string
  byStatus: ReportCfdStatusCount[]
}

export type ReportCfd = {
  projectId: number
  sprintId: number | null
  windowDays: number
  series: ReportCfdDay[]
}

export function unwrapApi<T>(body: ApiResponse<T>): T {
  if (!body.success || body.data === null) {
    throw new Error(body.message || '요청에 실패했습니다')
  }
  return body.data
}
