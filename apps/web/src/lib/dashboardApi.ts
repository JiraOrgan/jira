import type { ApiResponse } from '../types/api'
import type { DashboardDetail, DashboardMin } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchDashboards(): Promise<DashboardMin[]> {
  const { data } = await api.get<ApiResponse<DashboardMin[]>>(
    '/api/v1/dashboards',
  )
  return unwrapApi(data)
}

export async function fetchDashboardById(
  dashboardId: number,
): Promise<DashboardDetail> {
  const { data } = await api.get<ApiResponse<DashboardDetail>>(
    `/api/v1/dashboards/${dashboardId}`,
  )
  return unwrapApi(data)
}

export async function createDashboard(payload: {
  name: string
  shared: boolean
}): Promise<DashboardDetail> {
  const { data } = await api.post<ApiResponse<DashboardDetail>>(
    '/api/v1/dashboards',
    payload,
  )
  return unwrapApi(data)
}

export async function updateDashboard(
  dashboardId: number,
  payload: { name?: string; shared?: boolean },
): Promise<DashboardDetail> {
  const { data } = await api.put<ApiResponse<DashboardDetail>>(
    `/api/v1/dashboards/${dashboardId}`,
    payload,
  )
  return unwrapApi(data)
}

export async function deleteDashboard(dashboardId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/dashboards/${dashboardId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '대시보드를 삭제하지 못했습니다',
    )
  }
}

export async function addDashboardGadget(
  dashboardId: number,
  payload: { gadgetType: string; position: number; configJson?: string },
): Promise<DashboardDetail> {
  const { data } = await api.post<ApiResponse<DashboardDetail>>(
    `/api/v1/dashboards/${dashboardId}/gadgets`,
    payload,
  )
  return unwrapApi(data)
}

export async function removeDashboardGadget(
  dashboardId: number,
  gadgetId: number,
): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/dashboards/${dashboardId}/gadgets/${gadgetId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '가젯을 제거하지 못했습니다',
    )
  }
}
