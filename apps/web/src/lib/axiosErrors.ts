import { isAxiosError } from 'axios'

function pickApiMessage(body: unknown): string | undefined {
  if (!body || typeof body !== 'object') return undefined
  const o = body as Record<string, unknown>
  const top = o.message
  if (typeof top === 'string' && top.trim() !== '') return top
  const errObj = o.error
  if (errObj && typeof errObj === 'object') {
    const nested = (errObj as Record<string, unknown>).message
    if (typeof nested === 'string' && nested.trim() !== '') return nested
  }
  return undefined
}

/** Axios 에러에서 서버 `message`(또는 `error.message`)를 우선 사용한다. */
export function errorMessage(err: unknown): string {
  if (isAxiosError(err)) {
    const fromApi = pickApiMessage(err.response?.data)
    if (fromApi) return fromApi
  }
  if (err instanceof Error) return err.message
  return '요청에 실패했습니다'
}
