import type { ApiResponse } from '../types/api'
import type { AuditLogRow, SpringPage } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchProjectAuditLogs(
  projectId: number,
  page = 0,
  size = 30,
): Promise<SpringPage<AuditLogRow>> {
  const { data } = await api.get<ApiResponse<SpringPage<AuditLogRow>>>(
    `/api/v1/audit-logs/project/${projectId}`,
    { params: { page, size } },
  )
  return unwrapApi(data)
}
