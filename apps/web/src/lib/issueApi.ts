import type { ApiResponse } from '../types/api'
import type {
  AttachmentDetail,
  IssueDetail,
  IssueMin,
  IssueSaveBody,
  IssueUpdateBody,
  RoadmapEpicItem,
  SpringPage,
  TransitionBody,
  WorkflowTransitionItem,
} from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function downloadAttachmentFile(
  id: number,
  fileName: string,
): Promise<void> {
  const res = await api.get(`/api/v1/attachments/${id}/file`, {
    responseType: 'blob',
  })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export async function fetchAllProjectIssues(
  projectId: number,
  pageSize = 100,
): Promise<IssueMin[]> {
  const all: IssueMin[] = []
  let page = 0
  while (true) {
    const { data } = await api.get<ApiResponse<SpringPage<IssueMin>>>(
      `/api/v1/issues/project/${projectId}`,
      { params: { page, size: pageSize, sort: 'updatedAt,desc' } },
    )
    const body = unwrapApi(data)
    all.push(...body.content)
    if (page + 1 >= body.totalPages || body.content.length === 0) break
    page += 1
  }
  return all
}

export async function fetchBacklog(projectId: number): Promise<IssueMin[]> {
  const { data } = await api.get<ApiResponse<IssueMin[]>>(
    `/api/v1/issues/project/${projectId}/backlog`,
  )
  return unwrapApi(data)
}

export async function reorderBacklog(
  projectId: number,
  orderedIssueIds: number[],
): Promise<IssueMin[]> {
  const { data } = await api.put<ApiResponse<IssueMin[]>>(
    `/api/v1/issues/project/${projectId}/backlog/order`,
    { orderedIssueIds },
  )
  return unwrapApi(data)
}

export async function assignSprintToIssues(
  projectId: number,
  payload: { sprintId: number | null; issueIds: number[] },
): Promise<void> {
  const { data } = await api.post<ApiResponse<unknown>>(
    `/api/v1/issues/project/${projectId}/sprint-assignment`,
    payload,
  )
  if (!data.success) {
    throw new Error(data.message || '스프린트 배정에 실패했습니다')
  }
}

export async function createIssue(body: IssueSaveBody): Promise<IssueDetail> {
  const { data } = await api.post<ApiResponse<IssueDetail>>(
    '/api/v1/issues',
    body,
  )
  return unwrapApi(data)
}

export async function fetchIssue(issueKey: string): Promise<IssueDetail> {
  const { data } = await api.get<ApiResponse<IssueDetail>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}`,
  )
  return unwrapApi(data)
}

export async function updateIssue(
  issueKey: string,
  body: IssueUpdateBody,
): Promise<IssueDetail> {
  const { data } = await api.put<ApiResponse<IssueDetail>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}`,
    body,
  )
  return unwrapApi(data)
}

export async function fetchProjectRoadmapEpics(
  projectId: number,
): Promise<RoadmapEpicItem[]> {
  const { data } = await api.get<ApiResponse<RoadmapEpicItem[]>>(
    `/api/v1/projects/${projectId}/roadmap/epics`,
  )
  return unwrapApi(data)
}

export async function transitionIssue(
  issueKey: string,
  body: TransitionBody,
): Promise<IssueDetail> {
  const { data } = await api.post<ApiResponse<IssueDetail>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}/transitions`,
    body,
  )
  return unwrapApi(data)
}

export async function fetchTransitionHistory(
  issueKey: string,
): Promise<WorkflowTransitionItem[]> {
  const { data } = await api.get<ApiResponse<WorkflowTransitionItem[]>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}/transitions`,
  )
  return unwrapApi(data)
}

export async function fetchAttachments(
  issueKey: string,
): Promise<AttachmentDetail[]> {
  const { data } = await api.get<ApiResponse<AttachmentDetail[]>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}/attachments`,
  )
  return unwrapApi(data)
}

export async function uploadAttachment(
  issueKey: string,
  file: File,
): Promise<AttachmentDetail> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<ApiResponse<AttachmentDetail>>(
    `/api/v1/issues/${encodeURIComponent(issueKey)}/attachments`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  )
  return unwrapApi(data)
}
