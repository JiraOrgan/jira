import { Link, useParams } from 'react-router-dom'
import { cn } from '@/lib/utils'
import type { SectionSummary } from '@/types/course'

interface Props {
  sections: SectionSummary[]
  courseId: number
}

export default function Sidebar({ sections, courseId }: Props) {
  const { lessonId } = useParams()

  return (
    <aside className="w-64 shrink-0 border-r bg-muted/40 p-4 overflow-y-auto">
      <h3 className="mb-4 text-sm font-semibold">커리큘럼</h3>
      {sections.map((section) => (
        <div key={section.id} className="mb-4">
          <h4 className="mb-2 text-xs font-medium text-muted-foreground uppercase">{section.title}</h4>
          <ul className="space-y-1">
            {section.lessons.map((lesson) => (
              <li key={lesson.id}>
                <Link
                  to={`/courses/${courseId}/learn/${lesson.id}`}
                  className={cn(
                    "block rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent",
                    String(lesson.id) === lessonId && "bg-accent font-medium"
                  )}
                >
                  {lesson.title}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </aside>
  )
}
