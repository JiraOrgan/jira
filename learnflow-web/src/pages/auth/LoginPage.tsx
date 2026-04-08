import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link } from 'react-router-dom'
import { loginSchema, type LoginFormData } from '@/lib/validations/auth'
import { useLogin } from '@/hooks/useAuth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })
  const login = useLogin()

  const onSubmit = (data: LoginFormData) => login.mutate(data)

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-3.5rem)] px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">로그인</CardTitle>
          <CardDescription>LearnFlow AI에 오신 것을 환영합니다</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">이메일</label>
              <Input type="email" placeholder="email@example.com" {...register('email')} />
              {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">비밀번호</label>
              <Input type="password" placeholder="********" {...register('password')} />
              {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
            </div>
            {login.isError && (
              <p className="text-sm text-destructive">로그인에 실패했습니다. 이메일과 비밀번호를 확인하세요.</p>
            )}
          </CardContent>
          <CardFooter className="flex flex-col space-y-2">
            <Button type="submit" className="w-full" disabled={login.isPending}>
              {login.isPending ? '로그인 중...' : '로그인'}
            </Button>
            <p className="text-sm text-muted-foreground">
              계정이 없으신가요? <Link to="/signup" className="text-primary hover:underline">회원가입</Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
