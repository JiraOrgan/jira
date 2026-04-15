/** JWT access token `sub` — 백엔드 `JwtTokenProvider`와 동일하게 사용자 ID(숫자 문자열). */
export function parseAccessTokenUserId(accessToken: string): number | null {
  try {
    const parts = accessToken.split('.')
    if (parts.length < 2) return null
    let payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = payload.length % 4
    if (pad) payload += '='.repeat(4 - pad)
    const json = JSON.parse(atob(payload)) as { sub?: string }
    const id = Number(json.sub)
    return Number.isFinite(id) ? id : null
  } catch {
    return null
  }
}
