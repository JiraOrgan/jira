import { useParams, useNavigate } from 'react-router-dom'
import { useCourseDetail, useEnroll } from '@/hooks/useCourses'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export default function CourseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: course, isLoading } = useCourseDetail(Number(id))
  const enroll = useEnroll()
  const { isAuthenticated } = useAuthStore()

  if (isLoading) {
    return <div className="container py-12 text-center text-muted-foreground">로딩 중...</div>
  }

  if (!course) {
    return <div className="container py-12 text-center text-muted-foreground">강의를 찾을 수 없습니다.</div>
  }

  const handleEnroll = () => {
    if (!isAuthenticated()) {
      navigate('/login')
      return
    }
    enroll.mutate(course.id, {
      onSuccess: () => navigate(`/courses/${course.id}/learn`),
    })
  }

  const totalLessons = course.sections.reduce((sum, s) => sum + s.lessons.length, 0)
  const totalMinutes = course.sections.reduce(
    (sum, s) => sum + s.lessons.reduce((lSum, l) => lSum + l.durationMinutes, 0), 0
  )

  return (
    <div className="container py-8 max-w-4xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">{course.title}</h1>
        <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
          {course.instructorName && <span>강사: {course.instructorName}</span>}
          <span>수강생 {course.enrollmentCount}명</span>
          <span>{totalLessons}개 레슨</span>
          {totalMinutes > 0 && <span>총 {Math.floor(totalMinutes / 60)}시간 {totalMinutes % 60}분</span>}
        </div>
        {course.description && (
          <p className="text-muted-foreground leading-relaxed">{course.description}</p>
        )}
        <Button className="mt-6" size="lg" onClick={handleEnroll} disabled={enroll.isPending}>
          {enroll.isPending ? '신청 중...' : '수강 신청'}
        </Button>
      </div>

      <h2 className="text-xl font-semibold mb-4">커리큘럼</h2>
      <div className="space-y-3">
        {course.sections.map((section) => (
          <Card key={section.id}>
            <CardHeader className="py-3">
              <CardTitle className="text-base flex justify-between">
                <span>{section.title}</span>
                <span className="text-sm font-normal text-muted-foreground">{section.lessons.length}개 레슨</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="py-0 pb-3">
              <ul className="space-y-1">
                {section.lessons.map((lesson) => (
                  <li key={lesson.id} className="flex justify-between items-center py-1.5 px-2 rounded text-sm hover:bg-muted">
                    <span>{lesson.title}</span>
                    {lesson.durationMinutes > 0 && (
                      <span className="text-xs text-muted-foreground">{lesson.durationMinutes}분</span>
                    )}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
