export type ApiResponse<T> = {
  success: boolean
  status: number
  message: string
  data: T | null
}

export type AuthTokenPayload = {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}
