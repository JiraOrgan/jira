import {
  useCallback,
  useEffect,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../lib/axiosErrors'
import { CommentBody } from '../lib/CommentBody'
import {
  createComment,
  deleteComment,
  fetchCommentsByIssue,
  updateComment,
} from '../lib/commentApi'
import {
  downloadAttachmentFile,
  fetchAttachments,
  fetchIssue,
  fetchTransitionHistory,
  transitionIssue,
  updateIssue,
  uploadAttachment,
} from '../lib/issueApi'
import { parseAccessTokenUserId } from '../lib/jwtSubject'
import { fetchProjectMembers } from '../lib/projectApi'
import { priorityLabel, statusLabel } from '../lib/labels'
import {
  getPlanningPokerVote,
  PLANNING_POKER_VALUES,
  setPlanningPokerVote,
} from '../lib/planningPokerStorage'
import { allowedNextStatuses } from '../lib/workflow'
import { useAuthStore } from '../stores/authStore'
import type {
  AttachmentDetail,
  CommentDetail,
  IssueDetail,
  IssueStatus,
  ProjectMember,
  WorkflowTransitionItem,
} from '../types/domain'

function formatBytes(n: number): string {
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / (1024 * 1024)).toFixed(1)} MB`
}

export function IssueDetailPage() {
  const { issueKey: rawKey } = useParams<{ issueKey: string }>()
  const issueKey = rawKey ? decodeURIComponent(rawKey) : ''
  const accessToken = useAuthStore((s) => s.accessToken)
  const currentUserId = accessToken ? parseAccessTokenUserId(accessToken) : null

  const [issue, setIssue] = useState<IssueDetail | null>(null)
  const [history, setHistory] = useState<WorkflowTransitionItem[]>([])
  const [attachments, setAttachments] = useState<AttachmentDetail[]>([])
  const [comments, setComments] = useState<CommentDetail[]>([])
  const [projectMembers, setProjectMembers] = useState<ProjectMember[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)

  const [nextStatus, setNextStatus] = useState<IssueStatus | ''>('')
  const [conditionNote, setConditionNote] = useState('')
  const [transitionError, setTransitionError] = useState<string | null>(null)
  const [transitionLoading, setTransitionLoading] = useState(false)

  const [uploadError, setUploadError] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)

  const [epicStartEdit, setEpicStartEdit] = useState('')
  const [epicEndEdit, setEpicEndEdit] = useState('')
  const [epicError, setEpicError] = useState<string | null>(null)
  const [epicSaving, setEpicSaving] = useState(false)

  const [pokerPick, setPokerPick] = useState<number | null>(null)
  const [pokerError, setPokerError] = useState<string | null>(null)
  const [pokerSaving, setPokerSaving] = useState(false)

  const [newCommentBody, setNewCommentBody] = useState('')
  const [newCommentError, setNewCommentError] = useState<string | null>(null)
  const [commentSubmitting, setCommentSubmitting] = useState(false)
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [editCommentDraft, setEditCommentDraft] = useState('')
  const [commentMutateError, setCommentMutateError] = useState<string | null>(
    null,
  )
  const [savingEditId, setSavingEditId] = useState<number | null>(null)
  const [deletingCommentId, setDeletingCommentId] = useState<number | null>(
    null,
  )

  const reload = useCallback(async () => {
    if (!issueKey) return
    setLoadError(null)
    try {
      const i = await fetchIssue(issueKey)
      const [h, a, c, members] = await Promise.all([
        fetchTransitionHistory(issueKey),
        fetchAttachments(issueKey),
        fetchCommentsByIssue(i.id),
        fetchProjectMembers(i.projectId),
      ])
      setIssue(i)
      setHistory(h)
      setAttachments(a)
      setComments(c)
      setProjectMembers(members)
      setNextStatus('')
      setConditionNote('')
      setNewCommentBody('')
      setNewCommentError(null)
      setEditingCommentId(null)
      setCommentMutateError(null)
    } catch (e) {
      setLoadError(errorMessage(e))
      setIssue(null)
    }
  }, [issueKey])

  useEffect(() => {
    void reload()
  }, [reload])

  useEffect(() => {
    if (issue?.issueType === 'EPIC') {
      setEpicStartEdit(issue.epicStartDate ?? '')
      setEpicEndEdit(issue.epicEndDate ?? '')
    }
  }, [issue])

  useEffect(() => {
    if (!issueKey) {
      setPokerPick(null)
      return
    }
    setPokerPick(getPlanningPokerVote(issueKey))
  }, [issueKey])

  async function onSaveEpicDates(e: FormEvent) {
    e.preventDefault()
    if (!issue || issue.issueType !== 'EPIC') return
    setEpicError(null)
    setEpicSaving(true)
    try {
      const updated = await updateIssue(issue.issueKey, {
        patchEpicDates: true,
        epicStartDate: epicStartEdit.trim() || null,
        epicEndDate: epicEndEdit.trim() || null,
      })
      setIssue(updated)
    } catch (err) {
      setEpicError(errorMessage(err))
    } finally {
      setEpicSaving(false)
    }
  }

  async function onClearEpicDates() {
    if (!issue || issue.issueType !== 'EPIC') return
    setEpicError(null)
    setEpicSaving(true)
    try {
      const updated = await updateIssue(issue.issueKey, {
        clearEpicDates: true,
      })
      setIssue(updated)
      setEpicStartEdit('')
      setEpicEndEdit('')
    } catch (err) {
      setEpicError(errorMessage(err))
    } finally {
      setEpicSaving(false)
    }
  }

  async function onTransition(e: FormEvent) {
    e.preventDefault()
    if (!issue || !nextStatus) return
    setTransitionError(null)
    setTransitionLoading(true)
    try {
      const note = conditionNote.trim()
      const updated = await transitionIssue(issue.issueKey, {
        toStatus: nextStatus,
        ...(note ? { conditionNote: note } : {}),
      })
      setIssue(updated)
      setHistory(await fetchTransitionHistory(issue.issueKey))
      setNextStatus('')
      setConditionNote('')
    } catch (err) {
      setTransitionError(errorMessage(err))
    } finally {
      setTransitionLoading(false)
    }
  }

  async function onApplyPokerPoints(e: FormEvent) {
    e.preventDefault()
    if (!issue || pokerPick == null) return
    setPokerError(null)
    setPokerSaving(true)
    try {
      const updated = await updateIssue(issue.issueKey, { storyPoints: pokerPick })
      setIssue(updated)
    } catch (err) {
      setPokerError(errorMessage(err))
    } finally {
      setPokerSaving(false)
    }
  }

  async function onFileChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file || !issue) return
    setUploadError(null)
    setUploading(true)
    try {
      await uploadAttachment(issue.issueKey, file)
      setAttachments(await fetchAttachments(issue.issueKey))
    } catch (err) {
      setUploadError(errorMessage(err))
    } finally {
      setUploading(false)
    }
  }

  function insertMentionToken(displayName: string) {
    const token = displayName.replace(/\s+/g, '')
    if (!token) return
    setNewCommentBody((prev) => {
      const gap = prev && !/\s$/.test(prev) ? ' ' : ''
      return `${prev}${gap}@${token} `
    })
  }

  async function onSubmitComment(e: FormEvent) {
    e.preventDefault()
    if (!issue) return
    const body = newCommentBody.trim()
    if (!body) return
    setNewCommentError(null)
    setCommentSubmitting(true)
    try {
      const created = await createComment({ issueId: issue.id, body })
      setComments((list) => [...list, created])
      setNewCommentBody('')
    } catch (err) {
      setNewCommentError(errorMessage(err))
    } finally {
      setCommentSubmitting(false)
    }
  }

  async function onSaveCommentEdit(commentId: number) {
    const body = editCommentDraft.trim()
    if (!body) return
    setCommentMutateError(null)
    setSavingEditId(commentId)
    try {
      const updated = await updateComment(commentId, body)
      setComments((list) =>
        list.map((row) => (row.id === commentId ? updated : row)),
      )
      setEditingCommentId(null)
    } catch (err) {
      setCommentMutateError(errorMessage(err))
    } finally {
      setSavingEditId(null)
    }
  }

  async function onDeleteComment(commentId: number) {
    if (!window.confirm('이 댓글을 삭제할까요?')) return
    setCommentMutateError(null)
    setDeletingCommentId(commentId)
    try {
      await deleteComment(commentId)
      setComments((list) => list.filter((row) => row.id !== commentId))
      if (editingCommentId === commentId) setEditingCommentId(null)
    } catch (err) {
      setCommentMutateError(errorMessage(err))
    } finally {
      setDeletingCommentId(null)
    }
  }

  if (!issueKey) {
    return <p className="text-slate-500">이슈 키가 없습니다.</p>
  }

  if (loadError) {
    return (
      <div className="space-y-3">
        <p className="text-red-300">{loadError}</p>
        <Link to="/" className="text-indigo-400 hover:text-indigo-300">
          대시보드
        </Link>
      </div>
    )
  }

  if (!issue) {
    return <p className="text-slate-500">불러오는 중…</p>
  }

  const options = allowedNextStatuses(issue.status)

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div>
        <p className="font-mono text-sm text-indigo-300">{issue.issueKey}</p>
        <h1 className="mt-1 text-2xl font-semibold text-white">{issue.summary}</h1>
        <div className="mt-3 flex flex-wrap gap-2 text-xs">
          <span className="rounded bg-slate-800 px-2 py-1 text-slate-300">
            {issue.issueType}
          </span>
          <span className="rounded bg-slate-800 px-2 py-1 text-slate-300">
            {statusLabel[issue.status]}
          </span>
          <span className="rounded bg-slate-800 px-2 py-1 text-slate-300">
            {priorityLabel[issue.priority]}
          </span>
          {issue.storyPoints != null ? (
            <span className="rounded bg-slate-800 px-2 py-1 text-slate-300">
              SP {issue.storyPoints}
            </span>
          ) : null}
        </div>
        <p className="mt-2 text-sm text-slate-500">
          프로젝트{' '}
          <Link
            to={`/project/${issue.projectKey}`}
            className="text-indigo-400 hover:text-indigo-300"
          >
            {issue.projectKey}
          </Link>
        </p>
      </div>

      {issue.description ? (
        <section>
          <h2 className="text-sm font-medium text-slate-400">설명</h2>
          <pre className="mt-2 whitespace-pre-wrap rounded-lg border border-slate-800 bg-slate-900/50 p-4 text-sm text-slate-200">
            {issue.description}
          </pre>
        </section>
      ) : null}

      {issue.issueType === 'EPIC' ? (
        <section className="border-t border-slate-800 pt-6">
          <h2 className="text-sm font-medium text-white">Epic 로드맵 기간</h2>
          <p className="mt-1 text-xs text-slate-500">
            「기간 저장」은 아래 두 칸 값으로 Epic 기간을 통째로 덮어씁니다(빈 칸은 저장 시
            NULL). 「기간 삭제」는 둘 다 제거합니다.
          </p>
          <form
            className="mt-4 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end"
            onSubmit={onSaveEpicDates}
          >
            <div>
              <label className="text-xs text-slate-400">시작일</label>
              <input
                type="date"
                value={epicStartEdit}
                onChange={(e) => setEpicStartEdit(e.target.value)}
                className="mt-1 block rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
              />
            </div>
            <div>
              <label className="text-xs text-slate-400">종료일</label>
              <input
                type="date"
                value={epicEndEdit}
                onChange={(e) => setEpicEndEdit(e.target.value)}
                className="mt-1 block rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
              />
            </div>
            <button
              type="submit"
              disabled={epicSaving}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {epicSaving ? '저장 중…' : '기간 저장'}
            </button>
            <button
              type="button"
              disabled={epicSaving}
              onClick={() => void onClearEpicDates()}
              className="rounded-lg border border-slate-600 px-4 py-2 text-sm text-slate-300 hover:bg-slate-800 disabled:opacity-60"
            >
              기간 삭제
            </button>
          </form>
          {epicError ? (
            <p className="mt-2 text-sm text-red-300">{epicError}</p>
          ) : null}
        </section>
      ) : null}

      <section className="grid gap-6 border-t border-slate-800 pt-6 sm:grid-cols-2">
        <div>
          <h3 className="text-xs font-medium uppercase text-slate-500">
            담당 / 보고자
          </h3>
          <p className="mt-2 text-sm text-slate-300">
            담당: {issue.assigneeName ?? '—'}
          </p>
          <p className="text-sm text-slate-300">
            보고: {issue.reporterName ?? '—'}
          </p>
          {issue.parentKey ? (
            <p className="mt-2 text-sm text-slate-400">
              부모:{' '}
              <Link
                to={`/issue/${encodeURIComponent(issue.parentKey)}`}
                className="font-mono text-indigo-400 hover:text-indigo-300"
              >
                {issue.parentKey}
              </Link>
            </p>
          ) : null}
        </div>
        <div>
          <h3 className="text-xs font-medium uppercase text-slate-500">
            레이블 / 컴포넌트
          </h3>
          <p className="mt-2 text-sm text-slate-300">
            {issue.labels.length
              ? issue.labels.map((l) => l.name).join(', ')
              : '—'}
          </p>
          <p className="text-sm text-slate-300">
            {issue.components.length
              ? issue.components.map((c) => c.name).join(', ')
              : '—'}
          </p>
        </div>
      </section>

      <section className="border-t border-slate-800 pt-6">
        <h2 className="text-sm font-medium text-white">Planning Poker</h2>
        <p className="mt-1 text-xs text-slate-500">
          FR-018 비동기 추정: 카드 선택은 이 브라우저(localStorage)에만 저장됩니다. 합의 후
          「이슈 SP로 반영」으로 서버에 기록하세요. 팀원 간 실시간 공유는 후속에서 API로
          확장할 수 있습니다.
        </p>
        <div className="mt-4 flex flex-wrap gap-2">
          {PLANNING_POKER_VALUES.map((v) => (
            <button
              key={v}
              type="button"
              onClick={() => {
                setPokerPick(v)
                setPlanningPokerVote(issue.issueKey, v)
              }}
              className={`rounded-lg border px-4 py-2 text-sm font-medium transition ${
                pokerPick === v
                  ? 'border-indigo-500 bg-indigo-600 text-white'
                  : 'border-slate-600 text-slate-300 hover:border-slate-500 hover:bg-slate-800'
              }`}
            >
              {v}
            </button>
          ))}
          <button
            type="button"
            onClick={() => {
              setPokerPick(null)
              setPlanningPokerVote(issue.issueKey, null)
            }}
            className="rounded-lg border border-slate-600 px-3 py-2 text-sm text-slate-400 hover:bg-slate-800"
          >
            로컬 지우기
          </button>
        </div>
        <form
          className="mt-4 flex flex-wrap items-end gap-3"
          onSubmit={onApplyPokerPoints}
        >
          <button
            type="submit"
            disabled={pokerSaving || pokerPick == null}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
          >
            {pokerSaving ? '반영 중…' : '선택 값을 이슈 SP로 반영'}
          </button>
          {issue.storyPoints != null ? (
            <span className="text-sm text-slate-400">
              현재 이슈 SP:{' '}
              <strong className="text-slate-200">{issue.storyPoints}</strong>
            </span>
          ) : (
            <span className="text-sm text-slate-500">이슈에 SP 없음</span>
          )}
        </form>
        {pokerError ? (
          <p className="mt-2 text-sm text-red-300">{pokerError}</p>
        ) : null}
      </section>

      <section className="border-t border-slate-800 pt-6">
        <h2 className="text-sm font-medium text-white">상태 전환</h2>
        <p className="mt-1 text-xs text-slate-500">
          허용된 다음 상태만 표시됩니다 (백엔드 워크플로와 동일).
        </p>
        {options.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500">더 이상 전환할 수 없습니다.</p>
        ) : (
          <form className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={onTransition}>
            <div className="flex-1">
              <label className="text-xs text-slate-400">다음 상태</label>
              <select
                value={nextStatus}
                onChange={(e) => setNextStatus(e.target.value as IssueStatus | '')}
                required
                className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white sm:max-w-xs"
              >
                <option value="">선택</option>
                {options.map((s) => (
                  <option key={s} value={s}>
                    {statusLabel[s]}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex-[2]">
              <label className="text-xs text-slate-400">조건 메모 (선택)</label>
              <input
                value={conditionNote}
                onChange={(e) => setConditionNote(e.target.value)}
                placeholder="예: PR 승인 완료"
                className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
              />
            </div>
            <button
              type="submit"
              disabled={transitionLoading || !nextStatus}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {transitionLoading ? '처리 중…' : '전환'}
            </button>
          </form>
        )}
        {transitionError ? (
          <p className="mt-2 text-sm text-red-300">{transitionError}</p>
        ) : null}
      </section>

      <section className="border-t border-slate-800 pt-6">
        <h2 className="text-sm font-medium text-white">첨부파일</h2>
        <div className="mt-3">
          <label className="inline-flex cursor-pointer rounded-lg border border-dashed border-slate-600 px-4 py-2 text-sm text-slate-300 hover:border-slate-500">
            {uploading ? '업로드 중…' : '파일 선택'}
            <input
              type="file"
              className="hidden"
              disabled={uploading}
              onChange={onFileChange}
            />
          </label>
        </div>
        {uploadError ? (
          <p className="mt-2 text-sm text-red-300">{uploadError}</p>
        ) : null}
        {attachments.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500">첨부 없음</p>
        ) : (
          <ul className="mt-3 divide-y divide-slate-800 rounded-lg border border-slate-800">
            {attachments.map((a) => (
              <li
                key={a.id}
                className="flex items-center justify-between gap-3 px-4 py-3 text-sm"
              >
                <div>
                  <span className="text-slate-200">{a.fileName}</span>
                  <span className="ml-2 text-xs text-slate-500">
                    {formatBytes(a.fileSize)} · {a.uploaderName ?? '—'}
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => void downloadAttachmentFile(a.id, a.fileName)}
                  className="shrink-0 text-indigo-400 hover:text-indigo-300"
                >
                  다운로드
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="border-t border-slate-800 pt-6">
        <h2 className="text-sm font-medium text-white">댓글</h2>
        <p className="mt-1 text-xs text-slate-500">
          <code className="text-slate-400">@이름</code> 형태는 강조만 됩니다. 멘션
          알림·파싱 저장은 T-405 이후 백엔드와 연동할 수 있습니다.
        </p>
        {projectMembers.length > 0 ? (
          <div className="mt-3">
            <p className="text-xs text-slate-500">멘션 삽입 (공백 제거 토큰)</p>
            <div className="mt-2 flex flex-wrap gap-1">
              {projectMembers.map((m) => (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => insertMentionToken(m.userName)}
                  className="rounded border border-slate-700 bg-slate-900 px-2 py-1 text-xs text-slate-300 hover:border-slate-500 hover:text-white"
                >
                  @{m.userName.replace(/\s+/g, '')}
                </button>
              ))}
            </div>
          </div>
        ) : null}
        <form className="mt-4 space-y-2" onSubmit={onSubmitComment}>
          <textarea
            value={newCommentBody}
            onChange={(e) => setNewCommentBody(e.target.value)}
            rows={3}
            placeholder="댓글을 입력하세요…"
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white placeholder:text-slate-600"
          />
          <div className="flex items-center gap-2">
            <button
              type="submit"
              disabled={commentSubmitting || !newCommentBody.trim()}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {commentSubmitting ? '등록 중…' : '댓글 등록'}
            </button>
          </div>
          {newCommentError ? (
            <p className="text-sm text-red-300">{newCommentError}</p>
          ) : null}
        </form>
        {commentMutateError ? (
          <p className="mt-3 text-sm text-red-300">{commentMutateError}</p>
        ) : null}
        {comments.length === 0 ? (
          <p className="mt-4 text-sm text-slate-500">댓글 없음</p>
        ) : (
          <ul className="mt-4 space-y-3">
            {comments.map((row) => {
              const isMine =
                currentUserId != null && row.authorId === currentUserId
              const isEditing = editingCommentId === row.id
              return (
                <li
                  key={row.id}
                  className="rounded-lg border border-slate-800 bg-slate-900/40 p-3 text-sm"
                >
                  <div className="flex flex-wrap items-baseline justify-between gap-2 text-xs text-slate-500">
                    <span className="font-medium text-slate-300">
                      {row.authorName ?? '—'}
                    </span>
                    <span>
                      {row.createdAt.replace('T', ' ').slice(0, 19)}
                      {row.updatedAt !== row.createdAt ? ' · 수정됨' : ''}
                    </span>
                  </div>
                  {isEditing ? (
                    <div className="mt-2 space-y-2">
                      <textarea
                        value={editCommentDraft}
                        onChange={(e) => setEditCommentDraft(e.target.value)}
                        rows={3}
                        className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
                      />
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          disabled={
                            savingEditId === row.id || !editCommentDraft.trim()
                          }
                          onClick={() => void onSaveCommentEdit(row.id)}
                          className="rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
                        >
                          {savingEditId === row.id ? '저장 중…' : '저장'}
                        </button>
                        <button
                          type="button"
                          disabled={savingEditId === row.id}
                          onClick={() => setEditingCommentId(null)}
                          className="rounded-lg border border-slate-600 px-3 py-1.5 text-xs text-slate-300 hover:bg-slate-800 disabled:opacity-60"
                        >
                          취소
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="mt-2 text-slate-200">
                        <CommentBody text={row.body} />
                      </div>
                      {isMine ? (
                        <div className="mt-2 flex flex-wrap gap-2">
                          <button
                            type="button"
                            disabled={deletingCommentId === row.id}
                            onClick={() => {
                              setCommentMutateError(null)
                              setEditingCommentId(row.id)
                              setEditCommentDraft(row.body)
                            }}
                            className="text-xs text-indigo-400 hover:text-indigo-300 disabled:opacity-60"
                          >
                            편집
                          </button>
                          <button
                            type="button"
                            disabled={deletingCommentId === row.id}
                            onClick={() => void onDeleteComment(row.id)}
                            className="text-xs text-red-400 hover:text-red-300 disabled:opacity-60"
                          >
                            {deletingCommentId === row.id
                              ? '삭제 중…'
                              : '삭제'}
                          </button>
                        </div>
                      ) : null}
                    </>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </section>

      <section className="border-t border-slate-800 pt-6">
        <h2 className="text-sm font-medium text-white">전환 이력</h2>
        {history.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500">이력 없음</p>
        ) : (
          <ul className="mt-3 space-y-2 text-sm text-slate-400">
            {history.map((row) => (
              <li key={row.id} className="rounded-lg bg-slate-900/50 px-3 py-2">
                <span className="text-slate-300">
                  {statusLabel[row.fromStatus]} → {statusLabel[row.toStatus]}
                </span>
                <span className="mx-2 text-slate-600">·</span>
                {row.changedByName ?? '—'}
                <span className="mx-2 text-slate-600">·</span>
                {row.transitionedAt.replace('T', ' ').slice(0, 19)}
                {row.conditionNote ? (
                  <span className="mt-1 block text-xs text-slate-500">
                    {row.conditionNote}
                  </span>
                ) : null}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
