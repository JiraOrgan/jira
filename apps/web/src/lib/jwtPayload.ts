/**
 * 액세스 토큰 JWT 페이로드를 디코드합니다. 서명은 검증하지 않으며,
 * UI에서 소유자 여부 판별 등에만 사용합니다.
 */
export function getUserIdFromAccessToken(token: string | null): number | null {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4)
    const json = atob(padded)
    const payload = JSON.parse(json) as { sub?: string }
    if (payload.sub === undefined) return null
    const n = Number(payload.sub)
    return Number.isFinite(n) ? n : null
  } catch {
    return null
  }
}
