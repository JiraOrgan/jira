import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import apiClient from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { Course, CourseDetail } from '@/types/course'

export function useCourseList(keyword?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['courses', keyword, page, size],
    queryFn: async () => {
      const params = new URLSearchParams()
      if (keyword) params.set('keyword', keyword)
      params.set('page', String(page))
      params.set('size', String(size))
      const res = await apiClient.get<ApiResponse<PageResponse<Course>>>(`/courses?${params}`)
      return res.data.data
    },
  })
}

export function useCourseDetail(id: number) {
  return useQuery({
    queryKey: ['course', id],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<CourseDetail>>(`/courses/${id}`)
      return res.data.data
    },
    enabled: !!id,
  })
}

export function useEnroll() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (courseId: number) => {
      const res = await apiClient.post(`/courses/${courseId}/enroll`)
      return res.data.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['enrollments'] })
    },
  })
}
