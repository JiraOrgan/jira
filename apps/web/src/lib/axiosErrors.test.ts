import { describe, expect, it } from 'vitest'
import { AxiosError } from 'axios'
import { errorMessage } from './axiosErrors'

describe('errorMessage', () => {
  it('uses ApiResponse.message from axios error body', () => {
    const err = new AxiosError(
      'Request failed with status code 409',
      '409',
      undefined,
      undefined,
      {
        status: 409,
        data: {
          success: false,
          status: 409,
          message: '이미 사용 중인 프로젝트 키입니다',
          data: null,
        },
      } as never,
    )
    expect(errorMessage(err)).toBe('이미 사용 중인 프로젝트 키입니다')
  })

  it('uses nested error.message when present', () => {
    const err = new AxiosError(
      'Request failed',
      '400',
      undefined,
      undefined,
      {
        status: 400,
        data: {
          success: false,
          error: { code: 'X', message: '중첩 메시지' },
        },
      } as never,
    )
    expect(errorMessage(err)).toBe('중첩 메시지')
  })
})
