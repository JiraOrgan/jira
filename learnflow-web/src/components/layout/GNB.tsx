import { Link } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { useLogout } from '@/hooks/useAuth'
import { Button } from '@/components/ui/button'

export default function GNB() {
  const { user, isAuthenticated } = useAuthStore()
  const logout = useLogout()

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <Link to="/" className="mr-6 flex items-center space-x-2">
          <span className="text-xl font-bold">LearnFlow AI</span>
        </Link>
        <nav className="flex items-center space-x-6 text-sm font-medium">
          <Link to="/courses" className="transition-colors hover:text-foreground/80">강의 탐색</Link>
          {isAuthenticated() && (
            <Link to="/my-courses" className="transition-colors hover:text-foreground/80">내 강의</Link>
          )}
          {user?.role === 'INSTRUCTOR' && (
            <Link to="/instructor/courses" className="transition-colors hover:text-foreground/80">강의 관리</Link>
          )}
        </nav>
        <div className="ml-auto flex items-center space-x-2">
          {isAuthenticated() ? (
            <>
              <span className="text-sm text-muted-foreground">{user?.name}</span>
              <Button variant="ghost" size="sm" onClick={logout}>로그아웃</Button>
            </>
          ) : (
            <>
              <Link to="/login"><Button variant="ghost" size="sm">로그인</Button></Link>
              <Link to="/signup"><Button size="sm">회원가입</Button></Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
