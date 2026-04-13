import { isAxiosError } from 'axios'
import type { ApiResponse } from '../types/api'

export function errorMessage(err: unknown): string {
  if (isAxiosError(err)) {
    const body = err.response?.data
    if (body && typeof body === 'object' && 'message' in body) {
      const msg = (body as ApiResponse<unknown>).message
      if (typeof msg === 'string') return msg
    }
  }
  if (err instanceof Error) return err.message
  return '요청에 실패했습니다'
}
