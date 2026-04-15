import type { ApiResponse } from '../types/api'
import type { GithubIntegrationStatus } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchGithubOAuthAuthorizeUrl(
  projectId: number,
): Promise<string> {
  const { data } = await api.get<ApiResponse<{ authorizeUrl: string }>>(
    `/api/v1/projects/${projectId}/github/oauth/authorize-url`,
  )
  return unwrapApi(data).authorizeUrl
}

export async function fetchGithubIntegrationStatus(
  projectId: number,
): Promise<GithubIntegrationStatus> {
  const { data } = await api.get<ApiResponse<GithubIntegrationStatus>>(
    `/api/v1/projects/${projectId}/github/integration`,
  )
  return unwrapApi(data)
}

export async function connectGithubRepo(
  projectId: number,
  repoFullName: string,
): Promise<void> {
  const { data } = await api.post<ApiResponse<unknown>>(
    `/api/v1/projects/${projectId}/github/integration`,
    { repoFullName },
  )
  if (!data.success) {
    throw new Error(data.message || '저장소 웹훅 등록에 실패했습니다')
  }
}

export async function disconnectGithub(projectId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/projects/${projectId}/github/integration`,
  )
  if (!data.success) {
    throw new Error(data.message || 'GitHub 연동 해제에 실패했습니다')
  }
}
