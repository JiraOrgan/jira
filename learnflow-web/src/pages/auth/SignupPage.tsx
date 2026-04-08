import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link } from 'react-router-dom'
import { signupSchema, type SignupFormData } from '@/lib/validations/auth'
import { useSignup } from '@/hooks/useAuth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'

export default function SignupPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  })
  const signup = useSignup()

  const onSubmit = (data: SignupFormData) => {
    signup.mutate({ email: data.email, password: data.password, name: data.name })
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-3.5rem)] px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">회원가입</CardTitle>
          <CardDescription>새 계정을 만들어 학습을 시작하세요</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">이름</label>
              <Input placeholder="홍길동" {...register('name')} />
              {errors.name && <p className="text-sm text-destructive">{errors.name.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">이메일</label>
              <Input type="email" placeholder="email@example.com" {...register('email')} />
              {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">비밀번호</label>
              <Input type="password" placeholder="8자 이상" {...register('password')} />
              {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">비밀번호 확인</label>
              <Input type="password" placeholder="비밀번호 확인" {...register('passwordConfirm')} />
              {errors.passwordConfirm && <p className="text-sm text-destructive">{errors.passwordConfirm.message}</p>}
            </div>
            {signup.isError && (
              <p className="text-sm text-destructive">회원가입에 실패했습니다. 다시 시도해 주세요.</p>
            )}
          </CardContent>
          <CardFooter className="flex flex-col space-y-2">
            <Button type="submit" className="w-full" disabled={signup.isPending}>
              {signup.isPending ? '가입 중...' : '회원가입'}
            </Button>
            <p className="text-sm text-muted-foreground">
              이미 계정이 있으신가요? <Link to="/login" className="text-primary hover:underline">로그인</Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
