import type { ApiResponse } from '../types/api'
import type { ReportBurndown, ReportCfd, ReportVelocity } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchBurndown(
  projectId: number,
  sprintId: number,
): Promise<ReportBurndown> {
  const { data } = await api.get<ApiResponse<ReportBurndown>>(
    `/api/v1/projects/${projectId}/reports/sprints/${sprintId}/burndown`,
  )
  return unwrapApi(data)
}

export async function fetchVelocity(
  projectId: number,
  limit = 6,
): Promise<ReportVelocity> {
  const { data } = await api.get<ApiResponse<ReportVelocity>>(
    `/api/v1/projects/${projectId}/reports/velocity`,
    { params: { limit } },
  )
  return unwrapApi(data)
}

export async function fetchCfd(
  projectId: number,
  opts?: { sprintId?: number; days?: number },
): Promise<ReportCfd> {
  const { data } = await api.get<ApiResponse<ReportCfd>>(
    `/api/v1/projects/${projectId}/reports/cfd`,
    {
      params: {
        ...(opts?.sprintId != null ? { sprintId: opts.sprintId } : {}),
        ...(opts?.days != null ? { days: opts.days } : {}),
      },
    },
  )
  return unwrapApi(data)
}
