import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios'
import { useAuthStore } from '../stores/authStore'
import type { ApiResponse, AuthTokenPayload } from '../types/api'

const baseURL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''

/** 인터셉터 없음 — 로그인·리프레시 전용 */
export const bareApi = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

type QueueItem = {
  resolve: (value: unknown) => void
  reject: (reason?: unknown) => void
  config: InternalAxiosRequestConfig
}

let isRefreshing = false
let failedQueue: QueueItem[] = []

function flushQueue(error: unknown, token: string | null) {
  failedQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error)
      return
    }
    if (!token) {
      reject(new Error('No access token after refresh'))
      return
    }
    config.headers.Authorization = `Bearer ${token}`
    resolve(api(config))
  })
  failedQueue = []
}

function setAuthHeader(
  config: InternalAxiosRequestConfig,
  token: string,
) {
  config.headers.Authorization = `Bearer ${token}`
}

api.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState()
  if (accessToken) {
    setAuthHeader(config, accessToken)
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig | undefined
    const status = error.response?.status

    if (status !== 401 || !original) {
      return Promise.reject(error)
    }

    const url = original.url ?? ''
    if (
      url.includes('/api/auth/login') ||
      url.includes('/api/auth/register') ||
      url.includes('/api/auth/refresh')
    ) {
      return Promise.reject(error)
    }

    if (original._retry) {
      useAuthStore.getState().clear()
      window.location.assign('/login')
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config: original })
      })
    }

    original._retry = true
    isRefreshing = true

    const { refreshToken } = useAuthStore.getState()
    if (!refreshToken) {
      isRefreshing = false
      useAuthStore.getState().clear()
      window.location.assign('/login')
      return Promise.reject(error)
    }

    try {
      const { data } = await bareApi.post<ApiResponse<AuthTokenPayload>>(
        '/api/auth/refresh',
        { refreshToken },
      )
      if (!data.success || !data.data) {
        throw new Error(data.message || '토큰 갱신 실패')
      }
      const { accessToken, refreshToken: nextRefresh } = data.data
      useAuthStore.getState().setTokens(accessToken, nextRefresh)
      flushQueue(null, accessToken)
      setAuthHeader(original, accessToken)
      return api(original)
    } catch (refreshErr) {
      flushQueue(refreshErr, null)
      useAuthStore.getState().clear()
      window.location.assign('/login')
      return Promise.reject(refreshErr)
    } finally {
      isRefreshing = false
    }
  },
)
