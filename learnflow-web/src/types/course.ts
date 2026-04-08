export interface Course {
  id: number
  title: string
  instructorName: string | null
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  createdAt: string
}

export interface CourseDetail {
  id: number
  title: string
  description: string | null
  thumbnailUrl: string | null
  instructorName: string | null
  status: string
  enrollmentCount: number
  sections: SectionSummary[]
  createdAt: string
}

export interface SectionSummary {
  id: number
  title: string
  orderIndex: number
  lessons: LessonSummary[]
}

export interface LessonSummary {
  id: number
  title: string
  orderIndex: number
  durationMinutes: number
}

export interface Enrollment {
  id: number
  courseId: number
  courseTitle: string
  progress: number
  status: 'ACTIVE' | 'COMPLETED' | 'DROPPED'
  enrolledAt: string
}
