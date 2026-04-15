import type { ApiResponse } from '../types/api'
import type {
  ReleaseNotesPayload,
  ReleaseVersionDetail,
  ReleaseVersionMin,
} from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchReleaseVersionsByProject(
  projectId: number,
): Promise<ReleaseVersionMin[]> {
  const { data } = await api.get<ApiResponse<ReleaseVersionMin[]>>(
    `/api/v1/versions/project/${projectId}`,
  )
  return unwrapApi(data)
}

export async function createReleaseVersion(payload: {
  projectId: number
  name: string
  description?: string
  releaseDate?: string
}): Promise<ReleaseVersionDetail> {
  const { data } = await api.post<ApiResponse<ReleaseVersionDetail>>(
    '/api/v1/versions',
    payload,
  )
  return unwrapApi(data)
}

export async function markVersionReleased(
  versionId: number,
): Promise<ReleaseVersionDetail> {
  const { data } = await api.post<ApiResponse<ReleaseVersionDetail>>(
    `/api/v1/versions/${versionId}/release`,
    {},
  )
  return unwrapApi(data)
}

export async function fetchReleaseNotes(
  versionId: number,
): Promise<ReleaseNotesPayload> {
  const { data } = await api.get<ApiResponse<ReleaseNotesPayload>>(
    `/api/v1/versions/${versionId}/release-notes`,
  )
  return unwrapApi(data)
}

export async function deleteReleaseVersion(versionId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/versions/${versionId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '버전을 삭제하지 못했습니다',
    )
  }
}
