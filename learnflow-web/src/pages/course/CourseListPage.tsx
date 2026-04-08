import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useCourseList } from '@/hooks/useCourses'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

export default function CourseListPage() {
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const { data, isLoading } = useCourseList(search, page)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearch(keyword)
    setPage(0)
  }

  return (
    <div className="container py-8">
      <h1 className="text-3xl font-bold mb-6">강의 탐색</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-8 max-w-md">
        <Input
          placeholder="강의 검색..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <Button type="submit">검색</Button>
      </form>

      {isLoading ? (
        <div className="text-center py-12 text-muted-foreground">로딩 중...</div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {data?.content.map((course) => (
              <Link key={course.id} to={`/courses/${course.id}`}>
                <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
                  <CardHeader>
                    <CardTitle className="text-lg line-clamp-2">{course.title}</CardTitle>
                    <CardDescription>
                      {course.instructorName && <span>{course.instructorName}</span>}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className="text-xs text-muted-foreground">
                      {new Date(course.createdAt).toLocaleDateString('ko-KR')}
                    </p>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
              <span className="flex items-center text-sm text-muted-foreground">{page + 1} / {data.totalPages}</span>
              <Button variant="outline" size="sm" disabled={data.last} onClick={() => setPage(page + 1)}>다음</Button>
            </div>
          )}

          {data?.content.length === 0 && (
            <div className="text-center py-12 text-muted-foreground">검색 결과가 없습니다.</div>
          )}
        </>
      )}
    </div>
  )
}
