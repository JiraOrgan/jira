# 폼 & 유효성 검증 설정 가이드

> React Hook Form + Yup 기준

---

## 설치

```bash
npm install react-hook-form yup @hookform/resolvers
```

---

## 1. 기본 폼 패턴 (TypeScript)

```tsx
// src/components/forms/SignupForm.tsx
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

// ── 1. Yup 스키마 정의 ──────────────────────
const signupSchema = yup.object({
  email: yup
    .string()
    .required('이메일을 입력해주세요')
    .email('올바른 이메일 형식이 아닙니다'),
  password: yup
    .string()
    .required('비밀번호를 입력해주세요')
    .min(8, '비밀번호는 8자 이상이어야 합니다')
    .matches(
      /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])/,
      '영문, 숫자, 특수문자를 모두 포함해야 합니다'
    ),
  passwordConfirm: yup
    .string()
    .required('비밀번호 확인을 입력해주세요')
    .oneOf([yup.ref('password')], '비밀번호가 일치하지 않습니다'),
  nickname: yup
    .string()
    .required('닉네임을 입력해주세요')
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(20, '닉네임은 20자 이하여야 합니다'),
  agreeTerms: yup
    .boolean()
    .oneOf([true], '이용약관에 동의해주세요')
    .required(),
}).required();

// 스키마에서 타입 자동 추론
type SignupFormData = yup.InferType<typeof signupSchema>;

// ── 2. 폼 컴포넌트 ──────────────────────────
export default function SignupForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty, isValid },
    setError,
    reset,
    watch,
  } = useForm<SignupFormData>({
    resolver: yupResolver(signupSchema),
    mode: 'onBlur',          // 포커스 해제 시 검증
    defaultValues: {
      email: '',
      password: '',
      passwordConfirm: '',
      nickname: '',
      agreeTerms: false,
    },
  });

  const onSubmit = async (data: SignupFormData) => {
    try {
      await signupApi(data);
      reset();
    } catch (error) {
      // 서버 에러를 특정 필드에 반영
      if (error.code === 'EMAIL_EXISTS') {
        setError('email', {
          type: 'server',
          message: '이미 사용 중인 이메일입니다',
        });
      }
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      {/* 이메일 */}
      <div>
        <label htmlFor="email">이메일</label>
        <input
          id="email"
          type="email"
          {...register('email')}
          className={errors.email ? 'border-red-500' : 'border-gray-300'}
          placeholder="example@email.com"
        />
        {errors.email && (
          <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>
        )}
      </div>

      {/* 비밀번호 */}
      <div>
        <label htmlFor="password">비밀번호</label>
        <input
          id="password"
          type="password"
          {...register('password')}
          className={errors.password ? 'border-red-500' : 'border-gray-300'}
        />
        {errors.password && (
          <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
        )}
      </div>

      {/* 비밀번호 확인 */}
      <div>
        <label htmlFor="passwordConfirm">비밀번호 확인</label>
        <input
          id="passwordConfirm"
          type="password"
          {...register('passwordConfirm')}
          className={errors.passwordConfirm ? 'border-red-500' : 'border-gray-300'}
        />
        {errors.passwordConfirm && (
          <p className="text-red-500 text-sm mt-1">{errors.passwordConfirm.message}</p>
        )}
      </div>

      {/* 약관 동의 */}
      <div>
        <label>
          <input type="checkbox" {...register('agreeTerms')} />
          이용약관에 동의합니다
        </label>
        {errors.agreeTerms && (
          <p className="text-red-500 text-sm">{errors.agreeTerms.message}</p>
        )}
      </div>

      <button
        type="submit"
        disabled={isSubmitting || !isDirty || !isValid}
        className="btn-primary w-full"
      >
        {isSubmitting ? '처리 중...' : '회원가입'}
      </button>
    </form>
  );
}
```

---

## 2. JavaScript 전용 패턴

```jsx
// src/components/forms/LoginForm.jsx
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

const loginSchema = yup.object({
  email: yup.string().required('이메일을 입력해주세요').email('이메일 형식이 아닙니다'),
  password: yup.string().required('비밀번호를 입력해주세요').min(8, '8자 이상 입력해주세요'),
});

export default function LoginForm({ onSuccess }) {
  const { register, handleSubmit, formState: { errors, isSubmitting }, setError } = useForm({
    resolver: yupResolver(loginSchema),
    mode: 'onBlur',
  });

  const onSubmit = async (data) => {
    try {
      const result = await loginApi(data);
      onSuccess?.(result);
    } catch (err) {
      setError('root', { message: '이메일 또는 비밀번호가 올바르지 않습니다' });
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input type="email" placeholder="이메일" {...register('email')} />
      {errors.email && <p>{errors.email.message}</p>}

      <input type="password" placeholder="비밀번호" {...register('password')} />
      {errors.password && <p>{errors.password.message}</p>}

      {/* 폼 전체 에러 */}
      {errors.root && <p className="text-red-500">{errors.root.message}</p>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? '로그인 중...' : '로그인'}
      </button>
    </form>
  );
}
```

---

## 3. 재사용 가능한 입력 컴포넌트

```tsx
// src/components/common/FormField.tsx
import { UseFormRegisterReturn, FieldError } from 'react-hook-form';
import { cn } from '@/utils/cn';

interface FormFieldProps {
  label: string;
  registration: UseFormRegisterReturn;
  error?: FieldError;
  type?: string;
  placeholder?: string;
  required?: boolean;
}

export function FormField({
  label, registration, error,
  type = 'text', placeholder, required,
}: FormFieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        type={type}
        placeholder={placeholder}
        {...registration}
        className={cn(
          'input-base',
          error && 'border-red-500 focus:ring-red-500'
        )}
        aria-invalid={!!error}
        aria-describedby={error ? `${registration.name}-error` : undefined}
      />
      {error && (
        <p
          id={`${registration.name}-error`}
          role="alert"
          className="text-red-500 text-xs"
        >
          {error.message}
        </p>
      )}
    </div>
  );
}

// 사용 예시
<FormField
  label="이메일"
  registration={register('email')}
  error={errors.email}
  type="email"
  placeholder="example@email.com"
  required
/>
```

---

## 4. 커스텀 폼 훅 (API 연동)

```typescript
// src/hooks/useSignupForm.ts
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { signupSchema, type SignupFormData } from '@/schemas/authSchema';
import { authApi } from '@/api/auth';

export function useSignupForm() {
  const navigate = useNavigate();
  const form = useForm<SignupFormData>({
    resolver: yupResolver(signupSchema),
    mode: 'onBlur',
  });

  const mutation = useMutation({
    mutationFn: authApi.signup,
    onSuccess: () => navigate('/login', { state: { signedUp: true } }),
    onError: (error: any) => {
      const code = error.response?.data?.code;
      if (code === 'EMAIL_ALREADY_EXISTS') {
        form.setError('email', { message: '이미 사용 중인 이메일입니다' });
      } else {
        form.setError('root', { message: '회원가입에 실패했습니다. 다시 시도해주세요' });
      }
    },
  });

  const onSubmit = form.handleSubmit((data) => mutation.mutate(data));

  return {
    ...form,
    onSubmit,
    isLoading: mutation.isPending,
  };
}

// 컴포넌트에서 사용
function SignupPage() {
  const { register, formState: { errors }, onSubmit, isLoading } = useSignupForm();
  return (
    <form onSubmit={onSubmit}>
      <FormField label="이메일" registration={register('email')} error={errors.email} />
      <button disabled={isLoading}>{isLoading ? '처리 중...' : '회원가입'}</button>
    </form>
  );
}
```

---

## 5. 자주 쓰는 Yup 검증 패턴

```typescript
// src/schemas/common.ts — 공통 스키마 조각
import * as yup from 'yup';

export const emailSchema = yup
  .string()
  .required('이메일을 입력해주세요')
  .email('올바른 이메일 형식이 아닙니다');

export const passwordSchema = yup
  .string()
  .required('비밀번호를 입력해주세요')
  .min(8, '8자 이상 입력해주세요')
  .matches(/[A-Za-z]/, '영문자를 포함해야 합니다')
  .matches(/[0-9]/, '숫자를 포함해야 합니다')
  .matches(/[!@#$%^&*]/, '특수문자를 포함해야 합니다');

export const phoneSchema = yup
  .string()
  .matches(/^01[016789][0-9]{7,8}$/, '올바른 전화번호 형식이 아닙니다');

export const positiveIntSchema = yup
  .number()
  .typeError('숫자를 입력해주세요')
  .positive('0보다 큰 값을 입력해주세요')
  .integer('정수를 입력해주세요');

// 조건부 검증
export const conditionalSchema = yup.object({
  hasAddress: yup.boolean(),
  address: yup.string().when('hasAddress', {
    is: true,
    then: (schema) => schema.required('주소를 입력해주세요'),
    otherwise: (schema) => schema.optional(),
  }),
});
```

---

## 6. mode 옵션 선택 가이드

| mode | 검증 시점 | 추천 상황 |
|------|---------|---------|
| `onSubmit` | 제출 시 | 입력 중 에러 표시 불필요 시 |
| `onBlur` | 포커스 해제 시 | 일반 회원가입/로그인 (권장) |
| `onChange` | 입력할 때마다 | 실시간 피드백이 중요한 경우 |
| `all` | onBlur + onChange | 재검증도 실시간으로 |
