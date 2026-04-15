import type { ApiResponse } from '../types/api'
import type { JqlSearchResult, SavedJqlFilter } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export type JqlSearchBody = {
  jql: string
  startAt?: number
  maxResults?: number
}

export async function searchJql(
  projectId: number,
  body: JqlSearchBody,
): Promise<JqlSearchResult> {
  const { data } = await api.post<ApiResponse<JqlSearchResult>>(
    `/api/v1/projects/${projectId}/jql/search`,
    {
      jql: body.jql,
      startAt: body.startAt ?? 0,
      maxResults: body.maxResults ?? 25,
    },
  )
  return unwrapApi(data)
}

export async function listSavedJqlFilters(
  projectId: number,
): Promise<SavedJqlFilter[]> {
  const { data } = await api.get<ApiResponse<SavedJqlFilter[]>>(
    `/api/v1/projects/${projectId}/jql/filters`,
  )
  return unwrapApi(data)
}

export async function saveJqlFilter(
  projectId: number,
  name: string,
  jql: string,
): Promise<SavedJqlFilter> {
  const { data } = await api.post<ApiResponse<SavedJqlFilter>>(
    `/api/v1/projects/${projectId}/jql/filters`,
    { name, jql },
  )
  return unwrapApi(data)
}

export async function deleteJqlFilter(
  projectId: number,
  filterId: number,
): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/projects/${projectId}/jql/filters/${filterId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '저장 필터를 삭제하지 못했습니다',
    )
  }
}
