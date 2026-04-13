import type { ApiResponse } from '../types/api'
import type { SprintMin } from '../types/domain'
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
