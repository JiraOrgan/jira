import type { ApiResponse } from '../types/api'
import type { ProjectMin } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchProjects(): Promise<ProjectMin[]> {
  const { data } = await api.get<ApiResponse<ProjectMin[]>>('/api/v1/projects')
  return unwrapApi(data)
}
