import {
  useCallback,
  useEffect,
  useState,
  type ChangeEvent,
  type FormEvent,
} from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../lib/axiosErrors'
import {
  downloadAttachmentFile,
  fetchAttachments,
  fetchIssue,
  fetchTransitionHistory,
  transitionIssue,
  updateIssue,
  uploadAttachment,
} from '../lib/issueApi'
import { priorityLabel, statusLabel } from '../lib/labels'
import { allowedNextStatuses } from '../lib/workflow'
import type {
  AttachmentDetail,
  IssueDetail,
  IssueStatus,
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

  const [issue, setIssue] = useState<IssueDetail | null>(null)
  const [history, setHistory] = useState<WorkflowTransitionItem[]>([])
  const [attachments, setAttachments] = useState<AttachmentDetail[]>([])
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

  const reload = useCallback(async () => {
    if (!issueKey) return
    setLoadError(null)
    try {
      const [i, h, a] = await Promise.all([
        fetchIssue(issueKey),
        fetchTransitionHistory(issueKey),
        fetchAttachments(issueKey),
      ])
      setIssue(i)
      setHistory(h)
      setAttachments(a)
      setNextStatus('')
      setConditionNote('')
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
