import { Routes, Route } from 'react-router-dom'
import MainLayout from '@/components/layout/MainLayout'
import ProtectedRoute from '@/components/auth/ProtectedRoute'
import LoginPage from '@/pages/auth/LoginPage'
import SignupPage from '@/pages/auth/SignupPage'
import CourseListPage from '@/pages/course/CourseListPage'
import CourseDetailPage from '@/pages/course/CourseDetailPage'
import LessonEditPage from '@/pages/instructor/LessonEditPage'

export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<div className="container py-12 text-center"><h1 className="text-4xl font-bold mb-4">LearnFlow AI</h1><p className="text-lg text-muted-foreground">AI 기반 적응형 학습 플랫폼</p></div>} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/courses" element={<CourseListPage />} />
        <Route path="/courses/:id" element={<CourseDetailPage />} />
        <Route path="/dashboard" element={<ProtectedRoute><div className="container py-8"><h2 className="text-2xl font-bold">대시보드</h2></div></ProtectedRoute>} />
        <Route path="/my-courses" element={<ProtectedRoute><div className="container py-8"><h2 className="text-2xl font-bold">내 강의</h2></div></ProtectedRoute>} />
        <Route path="/instructor/courses" element={<ProtectedRoute requiredRole="INSTRUCTOR"><div className="container py-8"><h2 className="text-2xl font-bold">강의 관리</h2></div></ProtectedRoute>} />
        <Route path="/instructor/courses/:courseId/sections/:sectionId/lessons/:lessonId/edit" element={<ProtectedRoute requiredRole="INSTRUCTOR"><LessonEditPage /></ProtectedRoute>} />
      </Route>
    </Routes>
  )
}
