import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import apiClient from '@/lib/api-client'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import LessonEditor from '@/components/editor/LessonEditor'
import type { ApiResponse } from '@/types/api'

interface LessonDetail {
  id: number
  sectionId: number
  title: string
  content: string | null
  contentType: string
  videoUrl: string | null
  orderIndex: number
  durationMinutes: number
}

export default function LessonEditPage() {
  const { courseId, sectionId, lessonId } = useParams()
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [hasChanges, setHasChanges] = useState(false)

  const { data: lesson } = useQuery({
    queryKey: ['lesson', lessonId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<LessonDetail>>(
        `/courses/${courseId}/sections/${sectionId}/lessons/${lessonId}`
      )
      return res.data.data
    },
    enabled: !!lessonId,
  })

  useEffect(() => {
    if (lesson) {
      setTitle(lesson.title)
      setContent(lesson.content || '')
    }
  }, [lesson])

  const handleBeforeUnload = useCallback((e: BeforeUnloadEvent) => {
    if (hasChanges) {
      e.preventDefault()
      e.returnValue = ''
    }
  }, [hasChanges])

  useEffect(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => window.removeEventListener('beforeunload', handleBeforeUnload)
  }, [handleBeforeUnload])

  const save = useMutation({
    mutationFn: async () => {
      await apiClient.put(
        `/courses/${courseId}/sections/${sectionId}/lessons/${lessonId}`,
        { title, content, contentType: 'TEXT', orderIndex: lesson?.orderIndex ?? 0, durationMinutes: lesson?.durationMinutes ?? 0 }
      )
    },
    onSuccess: () => {
      setHasChanges(false)
      navigate(`/instructor/courses/${courseId}/edit`)
    },
  })

  return (
    <div className="container max-w-4xl py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">레슨 편집</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate(-1)}>취소</Button>
          <Button onClick={() => save.mutate()} disabled={save.isPending}>
            {save.isPending ? '저장 중...' : '저장'}
          </Button>
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <label className="text-sm font-medium">레슨 제목</label>
          <Input
            value={title}
            onChange={(e) => { setTitle(e.target.value); setHasChanges(true) }}
            placeholder="레슨 제목을 입력하세요"
          />
        </div>
        <div>
          <label className="text-sm font-medium mb-2 block">레슨 내용</label>
          <LessonEditor
            content={content}
            onChange={(html) => { setContent(html); setHasChanges(true) }}
          />
        </div>
      </div>

      {hasChanges && (
        <p className="mt-4 text-sm text-amber-600">저장하지 않은 변경사항이 있습니다.</p>
      )}
    </div>
  )
}
