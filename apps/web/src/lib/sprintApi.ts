import type { ApiResponse } from '../types/api'
import type { SprintDetail, SprintMin } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchSprintsByProject(
  projectId: number,
): Promise<SprintMin[]> {
  const { data } = await api.get<ApiResponse<SprintMin[]>>(
    `/api/v1/sprints/project/${projectId}`,
  )
  return unwrapApi(data)
}

export type SprintCreateBody = {
  projectId: number
  name: string
  startDate?: string
  endDate?: string
  goalPoints?: number
}

export async function createSprint(body: SprintCreateBody): Promise<SprintDetail> {
  const { data } = await api.post<ApiResponse<SprintDetail>>(
    '/api/v1/sprints',
    body,
  )
  return unwrapApi(data)
}

export async function startSprint(id: number): Promise<SprintDetail> {
  const { data } = await api.post<ApiResponse<SprintDetail>>(
    `/api/v1/sprints/${id}/start`,
  )
  return unwrapApi(data)
}

export type SprintCompleteBody = {
  disposition?: 'BACKLOG' | 'NEXT_SPRINT'
  nextSprintId?: number
}

export async function completeSprint(
  id: number,
  body?: SprintCompleteBody,
): Promise<SprintDetail> {
  const { data } = await api.post<ApiResponse<SprintDetail>>(
    `/api/v1/sprints/${id}/complete`,
    body ?? {},
  )
  return unwrapApi(data)
}

export async function deleteSprint(id: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/sprints/${id}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '스프린트를 삭제하지 못했습니다',
    )
  }
}
