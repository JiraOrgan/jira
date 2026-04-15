import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import {
  createReleaseVersion,
  deleteReleaseVersion,
  fetchReleaseNotes,
  fetchReleaseVersionsByProject,
  markVersionReleased,
} from '../lib/releaseApi'
import type {
  ReleaseNotesPayload,
  ReleaseVersionMin,
  VersionStatus,
} from '../types/domain'

function statusLabel(s: VersionStatus): string {
  return s === 'RELEASED' ? '릴리즈됨' : '미배포'
}

export function ReleasesPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [rows, setRows] = useState<ReleaseVersionMin[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [newName, setNewName] = useState('')
  const [newDesc, setNewDesc] = useState('')
  const [newDate, setNewDate] = useState('')
  const [createBusy, setCreateBusy] = useState(false)
  const [createErr, setCreateErr] = useState<string | null>(null)

  const [actionErr, setActionErr] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  const [notesFor, setNotesFor] = useState<ReleaseVersionMin | null>(null)
  const [notesPayload, setNotesPayload] = useState<ReleaseNotesPayload | null>(
    null,
  )
  const [notesBusyForId, setNotesBusyForId] = useState<number | null>(null)
  const [notesErr, setNotesErr] = useState<string | null>(null)
  const notesRequestSeq = useRef(0)

  const load = useCallback(async () => {
    if (!project) return
    setLoading(true)
    setLoadError(null)
    try {
      const list = await fetchReleaseVersionsByProject(project.id)
      setRows(list)
    } catch (e) {
      setRows([])
      setLoadError(
        errorMessage(e) || '버전 목록을 불러오지 못했습니다',
      )
    } finally {
      setLoading(false)
    }
  }, [project])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    if (!project) return
    const name = newName.trim()
    if (!name) {
      setCreateErr('버전 이름을 입력하세요.')
      return
    }
    setCreateErr(null)
    setActionErr(null)
    setCreateBusy(true)
    try {
      const body: {
        projectId: number
        name: string
        description?: string
        releaseDate?: string
      } = { projectId: project.id, name }
      if (newDesc.trim()) body.description = newDesc.trim()
      if (newDate.trim()) body.releaseDate = newDate.trim()
      await createReleaseVersion(body)
      setNewName('')
      setNewDesc('')
      setNewDate('')
      await load()
    } catch (err) {
      setCreateErr(
        errorMessage(err) || '버전을 만들지 못했습니다',
      )
    } finally {
      setCreateBusy(false)
    }
  }

  async function handleRelease(v: ReleaseVersionMin) {
    if (v.status === 'RELEASED') return
    if (!confirm(`"${v.name}" 을(를) 릴리즈 처리할까요?`)) return
    setActionErr(null)
    setBusyId(v.id)
    try {
      await markVersionReleased(v.id)
      await load()
    } catch (err) {
      setActionErr(
        errorMessage(err) || '릴리즈 처리하지 못했습니다',
      )
    } finally {
      setBusyId(null)
    }
  }

  function closeNotesModal() {
    setNotesFor(null)
    setNotesPayload(null)
    setNotesErr(null)
  }

  async function openReleaseNotes(v: ReleaseVersionMin) {
    const seq = ++notesRequestSeq.current
    setNotesFor(v)
    setNotesPayload(null)
    setNotesErr(null)
    setNotesBusyForId(v.id)
    try {
      const data = await fetchReleaseNotes(v.id)
      if (notesRequestSeq.current !== seq) return
      setNotesPayload(data)
    } catch (err) {
      if (notesRequestSeq.current !== seq) return
      setNotesErr(
        errorMessage(err) || '릴리즈 노트를 불러오지 못했습니다',
      )
    } finally {
      if (notesRequestSeq.current === seq) {
        setNotesBusyForId(null)
      }
    }
  }

  async function copyNotesMarkdown() {
    if (!notesPayload?.markdown) return
    try {
      await navigator.clipboard.writeText(notesPayload.markdown)
    } catch {
      setNotesErr('클립보드에 복사하지 못했습니다.')
    }
  }

  async function handleDelete(v: ReleaseVersionMin) {
    if (!confirm(`"${v.name}" 을(를) 삭제할까요?`)) return
    setActionErr(null)
    setBusyId(v.id)
    try {
      await deleteReleaseVersion(v.id)
      await load()
    } catch (err) {
      setActionErr(
        errorMessage(err) || '삭제하지 못했습니다',
      )
    } finally {
      setBusyId(null)
    }
  }

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
  }

  if (!project) {
    return (
      <div className="space-y-3">
        <p className="text-slate-400">
          프로젝트 <span className="font-mono text-white">{projectKey}</span>{' '}
          을(를) 찾을 수 없거나 아카이브되었습니다.
        </p>
        <Link to="/" className="text-sm text-indigo-400 hover:text-indigo-300">
          대시보드로
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <p className="text-xs text-slate-500">
          <Link
            to={`/project/${project.key}`}
            className="text-indigo-400 hover:text-indigo-300"
          >
            {project.name}
          </Link>
          <span className="mx-2 text-slate-600">/</span>
          <span className="text-slate-400">릴리즈</span>
        </p>
        <h1 className="mt-2 text-xl font-semibold text-white">Fix 버전 · 릴리즈</h1>
        <p className="mt-1 text-sm text-slate-400">
          프로젝트 단위 버전을 등록하고 릴리즈 상태로 전환합니다. 생성·릴리즈·삭제는
          프로젝트 관리 권한이 필요할 수 있습니다. Fix 버전에 연결된 이슈로 릴리즈
          노트 초안을 자동 생성할 수 있습니다.
        </p>
      </div>

      <section className="rounded-xl border border-slate-800 bg-slate-900/40 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
          새 버전
        </h2>
        <form onSubmit={handleCreate} className="mt-4 space-y-4">
          <div>
            <label
              htmlFor="ver-name"
              className="block text-xs font-medium text-slate-400"
            >
              이름
            </label>
            <input
              id="ver-name"
              value={newName}
              onChange={(ev) => setNewName(ev.target.value)}
              placeholder="예: 1.2.0"
              className="mt-1 w-full max-w-md rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            />
          </div>
          <div>
            <label
              htmlFor="ver-desc"
              className="block text-xs font-medium text-slate-400"
            >
              설명 (선택)
            </label>
            <textarea
              id="ver-desc"
              value={newDesc}
              onChange={(ev) => setNewDesc(ev.target.value)}
              rows={2}
              className="mt-1 w-full max-w-xl rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            />
          </div>
          <div>
            <label
              htmlFor="ver-date"
              className="block text-xs font-medium text-slate-400"
            >
              목표 릴리즈일 (선택)
            </label>
            <input
              id="ver-date"
              type="date"
              value={newDate}
              onChange={(ev) => setNewDate(ev.target.value)}
              className="mt-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            />
          </div>
          {createErr ? (
            <p className="text-sm text-red-400">{createErr}</p>
          ) : null}
          <button
            type="submit"
            disabled={createBusy}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
          >
            {createBusy ? '등록 중…' : '버전 등록'}
          </button>
        </form>
      </section>

      <section className="space-y-3">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
          버전 목록
        </h2>
        {actionErr ? (
          <p className="text-sm text-red-400">{actionErr}</p>
        ) : null}
        {loading ? (
          <p className="text-sm text-slate-500">불러오는 중…</p>
        ) : loadError ? (
          <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
            {loadError}
            <button
              type="button"
              onClick={() => void load()}
              className="ml-3 text-indigo-400 hover:text-indigo-300"
            >
              다시 시도
            </button>
          </div>
        ) : rows.length === 0 ? (
          <p className="text-sm text-slate-500">등록된 버전이 없습니다.</p>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-800">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-800 bg-slate-900/80 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">이름</th>
                  <th className="px-4 py-3 font-medium">상태</th>
                  <th className="px-4 py-3 font-medium">릴리즈일</th>
                  <th className="px-4 py-3 font-medium text-right">동작</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {rows.map((v) => (
                  <tr key={v.id} className="hover:bg-slate-900/40">
                    <td className="px-4 py-3 font-medium text-slate-200">
                      {v.name}
                    </td>
                    <td className="px-4 py-3 text-slate-400">
                      <span
                        className={
                          v.status === 'RELEASED'
                            ? 'text-emerald-400'
                            : 'text-amber-400'
                        }
                      >
                        {statusLabel(v.status)}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {v.releaseDate ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        disabled={busyId === v.id || notesBusyForId === v.id}
                        onClick={() => void openReleaseNotes(v)}
                        className="mr-3 text-sm text-slate-300 hover:text-white disabled:opacity-50"
                      >
                        노트
                      </button>
                      {v.status === 'UNRELEASED' ? (
                        <button
                          type="button"
                          disabled={busyId === v.id}
                          onClick={() => void handleRelease(v)}
                          className="mr-3 text-sm text-indigo-400 hover:text-indigo-300 disabled:opacity-50"
                        >
                          릴리즈
                        </button>
                      ) : null}
                      <button
                        type="button"
                        disabled={busyId === v.id}
                        onClick={() => void handleDelete(v)}
                        className="text-sm text-red-400 hover:text-red-300 disabled:opacity-50"
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {notesFor ? (
        <div
          className="fixed inset-0 z-40 flex items-center justify-center bg-black/70 p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="release-notes-title"
        >
          <div className="max-h-[85vh] w-full max-w-2xl overflow-hidden rounded-xl border border-slate-700 bg-slate-950 shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-800 px-4 py-3">
              <h2
                id="release-notes-title"
                className="text-sm font-semibold text-white"
              >
                릴리즈 노트 — {notesFor.name}
              </h2>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={!notesPayload?.markdown}
                  onClick={() => void copyNotesMarkdown()}
                  className="rounded-lg border border-slate-600 px-3 py-1 text-xs text-slate-200 hover:bg-slate-800 disabled:opacity-40"
                >
                  마크다운 복사
                </button>
                <button
                  type="button"
                  onClick={closeNotesModal}
                  className="rounded-lg px-3 py-1 text-xs text-slate-400 hover:bg-slate-800 hover:text-white"
                >
                  닫기
                </button>
              </div>
            </div>
            <div className="max-h-[calc(85vh-3.5rem)] overflow-auto p-4">
              {notesBusyForId !== null ? (
                <p className="text-sm text-slate-500">불러오는 중…</p>
              ) : notesErr ? (
                <p className="text-sm text-red-400">{notesErr}</p>
              ) : notesPayload ? (
                <>
                  <p className="mb-3 text-xs text-slate-500">
                    연결 이슈 {notesPayload.issueCount}건 (타입별 마크다운 초안)
                  </p>
                  <pre className="whitespace-pre-wrap break-words font-mono text-xs leading-relaxed text-slate-300">
                    {notesPayload.markdown}
                  </pre>
                </>
              ) : null}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
