import type { ApiResponse } from '../types/api'
import type { CommentDetail } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchCommentsByIssue(
  issueId: number,
): Promise<CommentDetail[]> {
  const { data } = await api.get<ApiResponse<CommentDetail[]>>(
    `/api/v1/comments/issue/${issueId}`,
  )
  return unwrapApi(data)
}

export async function createComment(payload: {
  issueId: number
  body: string
}): Promise<CommentDetail> {
  const { data } = await api.post<ApiResponse<CommentDetail>>(
    '/api/v1/comments',
    payload,
  )
  return unwrapApi(data)
}

export async function updateComment(
  id: number,
  body: string,
): Promise<CommentDetail> {
  const { data } = await api.put<ApiResponse<CommentDetail>>(
    `/api/v1/comments/${id}`,
    { body },
  )
  return unwrapApi(data)
}

export async function deleteComment(id: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<unknown>>(
    `/api/v1/comments/${id}`,
  )
  if (!data.success) {
    throw new Error(data.message || '댓글을 삭제하지 못했습니다')
  }
}
