import { isAxiosError } from 'axios'
import type { ApiResponse, AuthTokenPayload } from '../types/api'
import { bareApi } from './api'

function messageFromAxios(err: unknown): string | null {
  if (!isAxiosError(err)) return null
  const body = err.response?.data
  if (body && typeof body === 'object' && 'message' in body) {
    const msg = (body as ApiResponse<unknown>).message
    return typeof msg === 'string' ? msg : null
  }
  return null
}

export async function loginRequest(
  email: string,
  password: string,
): Promise<AuthTokenPayload> {
  try {
    const { data } = await bareApi.post<ApiResponse<AuthTokenPayload>>(
      '/api/auth/login',
      { email, password },
    )
    if (!data.success || !data.data) {
      throw new Error(data.message || '로그인에 실패했습니다')
    }
    return data.data
  } catch (err) {
    const fromServer = messageFromAxios(err)
    if (fromServer) throw new Error(fromServer)
    throw err instanceof Error ? err : new Error('로그인에 실패했습니다')
  }
}
