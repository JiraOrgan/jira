import type { ApiResponse } from '../types/api'
import type {
  ProjectCreateBody,
  ProjectDetail,
  ProjectMember,
  ProjectMin,
  ProjectUpdateBody,
  WipLimitItem,
} from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchProjects(): Promise<ProjectMin[]> {
  const { data } = await api.get<ApiResponse<ProjectMin[]>>('/api/v1/projects')
  return unwrapApi(data)
}

export async function createProject(
  body: ProjectCreateBody,
): Promise<ProjectDetail> {
  const { data } = await api.post<ApiResponse<ProjectDetail>>(
    '/api/v1/projects',
    body,
  )
  return unwrapApi(data)
}

export async function fetchProjectById(
  projectId: number,
): Promise<ProjectDetail> {
  const { data } = await api.get<ApiResponse<ProjectDetail>>(
    `/api/v1/projects/${projectId}`,
  )
  return unwrapApi(data)
}

/** 멤버만 조회. 아카이브 프로젝트 포함(설정 URL 직접 진입). */
export async function fetchProjectByKey(
  projectKey: string,
): Promise<ProjectDetail> {
  const { data } = await api.get<ApiResponse<ProjectDetail>>(
    `/api/v1/projects/by-key/${encodeURIComponent(projectKey)}`,
  )
  return unwrapApi(data)
}

export async function updateProject(
  projectId: number,
  body: ProjectUpdateBody,
): Promise<ProjectDetail> {
  const { data } = await api.put<ApiResponse<ProjectDetail>>(
    `/api/v1/projects/${projectId}`,
    body,
  )
  return unwrapApi(data)
}

/** 관리자: 설정된 일수 기준으로 DONE 이슈 일괄 아카이브. 반환값은 갱신된 건수. */
export async function runProjectAutoArchiveDone(
  projectId: number,
): Promise<number> {
  const { data } = await api.post<ApiResponse<number>>(
    `/api/v1/projects/${projectId}/issues/auto-archive-done`,
  )
  return unwrapApi(data)
}

export async function deleteProject(projectId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/projects/${projectId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '프로젝트를 삭제하지 못했습니다',
    )
  }
}

export async function fetchProjectMembers(
  projectId: number,
): Promise<ProjectMember[]> {
  const { data } = await api.get<ApiResponse<ProjectMember[]>>(
    `/api/v1/projects/${projectId}/members`,
  )
  return unwrapApi(data)
}

export async function addProjectMember(
  projectId: number,
  payload: { userId: number; role: ProjectMember['role'] },
): Promise<ProjectMember> {
  const { data } = await api.post<ApiResponse<ProjectMember>>(
    `/api/v1/projects/${projectId}/members`,
    payload,
  )
  return unwrapApi(data)
}

export async function removeProjectMember(
  projectId: number,
  memberId: number,
): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/projects/${projectId}/members/${memberId}`,
  )
  if (data && typeof data === 'object' && 'success' in data && !data.success) {
    throw new Error(
      (data as ApiResponse<unknown>).message ||
        '멤버를 제거하지 못했습니다',
    )
  }
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
