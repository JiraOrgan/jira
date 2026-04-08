export interface User {
  id: number
  email: string
  name: string
  role: 'LEARNER' | 'INSTRUCTOR' | 'ADMIN'
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface SignupRequest {
  email: string
  password: string
  name: string
}
