import type { ApiResponse } from '../types/api'
import type { UserMin } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchUsers(): Promise<UserMin[]> {
  const { data } = await api.get<ApiResponse<UserMin[]>>('/api/v1/users')
  return unwrapApi(data)
}
