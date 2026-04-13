import type { ApiResponse } from '../types/api'
import type { ProjectMin, WipLimitItem } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchProjects(): Promise<ProjectMin[]> {
  const { data } = await api.get<ApiResponse<ProjectMin[]>>('/api/v1/projects')
  return unwrapApi(data)
}

export async function fetchWipLimits(
  projectId: number,
): Promise<WipLimitItem[]> {
  const { data } = await api.get<ApiResponse<WipLimitItem[]>>(
    `/api/v1/projects/${projectId}/wip-limits`,
  )
  return unwrapApi(data)
}

export async function replaceWipLimits(
  projectId: number,
  limits: WipLimitItem[],
): Promise<WipLimitItem[]> {
  const { data } = await api.put<ApiResponse<WipLimitItem[]>>(
    `/api/v1/projects/${projectId}/wip-limits`,
    { limits },
  )
  return unwrapApi(data)
}
