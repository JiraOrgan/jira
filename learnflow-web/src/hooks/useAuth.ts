import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import apiClient from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import type { LoginRequest, SignupRequest } from '@/types/auth'
import type { ApiResponse } from '@/types/api'

export function useLogin() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  return useMutation({
    mutationFn: async (data: LoginRequest) => {
      const res = await apiClient.post<ApiResponse<{ accessToken: string; refreshToken: string; user: { id: number; email: string; name: string; role: 'LEARNER' | 'INSTRUCTOR' | 'ADMIN' } }>>('/auth/login', data)
      return res.data.data
    },
    onSuccess: (data) => {
      setAuth(data.user, data.accessToken, data.refreshToken)
      navigate('/dashboard')
    },
  })
}

export function useSignup() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  return useMutation({
    mutationFn: async (data: SignupRequest) => {
      const res = await apiClient.post<ApiResponse<{ accessToken: string; refreshToken: string; user: { id: number; email: string; name: string; role: 'LEARNER' | 'INSTRUCTOR' | 'ADMIN' } }>>('/auth/signup', data)
      return res.data.data
    },
    onSuccess: (data) => {
      setAuth(data.user, data.accessToken, data.refreshToken)
      navigate('/dashboard')
    },
  })
}

export function useLogout() {
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  return () => {
    apiClient.post('/auth/logout').catch(() => {})
    logout()
    navigate('/')
  }
}
