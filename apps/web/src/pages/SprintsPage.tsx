import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import {
  completeSprint,
  createSprint,
  deleteSprint,
  fetchSprintsByProject,
  startSprint,
} from '../lib/sprintApi'
import type { SprintMin } from '../types/domain'

export function SprintsPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<number | null>(null)

  const [name, setName] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [goalPoints, setGoalPoints] = useState('')
  const [creating, setCreating] = useState(false)

  const reload = useCallback(async () => {
    if (!project) return
    setLoadError(null)
    try {
      const list = await fetchSprintsByProject(project.id)
      setSprints(list)
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : '목록을 불러오지 못했습니다')
      setSprints([])
    } finally {
      setLoading(false)
    }
  }, [project])

  useEffect(() => {
    if (!project) return
    setLoading(true)
    void reload()
  }, [project, reload])

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    if (!project || !name.trim()) return
    setActionError(null)
    setCreating(true)
    try {
      await createSprint({
        projectId: project.id,
        name: name.trim(),
        ...(startDate ? { startDate } : {}),
        ...(endDate ? { endDate } : {}),
        ...(goalPoints ? { goalPoints: Number(goalPoints) } : {}),
      })
      setName('')
      setStartDate('')
      setEndDate('')
      setGoalPoints('')
      await reload()
    } catch (err) {
      setActionError(errorMessage(err))
    } finally {
      setCreating(false)
    }
  }

  async function run(id: number, fn: () => Promise<unknown>) {
    setActionError(null)
    setBusyId(id)
    try {
      await fn()
      await reload()
    } catch (err) {
      setActionError(errorMessage(err))
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
        <p className="text-slate-400">프로젝트를 찾을 수 없습니다.</p>
        <Link to="/" className="text-indigo-400 hover:text-indigo-300">
          대시보드
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-xl font-semibold text-white">스프린트 관리</h1>
        <p className="mt-1 text-sm text-slate-400">
          스프린트 생성·시작·완료·삭제(제약 조건은 서버에서 검증)입니다.
        </p>
        <Link
          to={`/project/${project.key}/board`}
          className="mt-2 inline-block text-sm text-indigo-400 hover:text-indigo-300"
        >
          스크럼 보드로
        </Link>
      </div>

      <form
        onSubmit={onCreate}
        className="rounded-xl border border-slate-800 bg-slate-900/40 p-4"
      >
        <h2 className="text-sm font-medium text-slate-300">새 스프린트</h2>
        <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <label className="block text-xs text-slate-400">
            이름 *
            <input
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 w-full rounded border border-slate-700 bg-slate-950 px-2 py-1.5 text-sm text-white"
            />
          </label>
          <label className="block text-xs text-slate-400">
            시작일
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="mt-1 w-full rounded border border-slate-700 bg-slate-950 px-2 py-1.5 text-sm text-white"
            />
          </label>
          <label className="block text-xs text-slate-400">
            종료일
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="mt-1 w-full rounded border border-slate-700 bg-slate-950 px-2 py-1.5 text-sm text-white"
            />
          </label>
          <label className="block text-xs text-slate-400">
            목표 포인트
            <input
              type="number"
              min={0}
              value={goalPoints}
              onChange={(e) => setGoalPoints(e.target.value)}
              className="mt-1 w-full rounded border border-slate-700 bg-slate-950 px-2 py-1.5 text-sm text-white"
            />
          </label>
        </div>
        <button
          type="submit"
          disabled={creating}
          className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
        >
          {creating ? '생성 중…' : '생성'}
        </button>
      </form>

      {actionError ? (
        <p className="text-sm text-red-300">{actionError}</p>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">불러오는 중…</p>
      ) : loadError ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {loadError}
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-800">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-800 bg-slate-900/80 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">이름</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">기간</th>
                <th className="px-4 py-3 text-right">동작</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {sprints.map((s) => (
                <tr key={s.id} className="hover:bg-slate-900/40">
                  <td className="px-4 py-3 text-slate-200">{s.name}</td>
                  <td className="px-4 py-3 text-slate-400">{s.status}</td>
                  <td className="px-4 py-3 text-xs text-slate-500">
                    {s.startDate ?? '—'} ~ {s.endDate ?? '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      disabled={
                        busyId === s.id || s.status !== 'PLANNING'
                      }
                      onClick={() =>
                        void run(s.id, () => startSprint(s.id))
                      }
                      className="mr-2 text-xs text-indigo-400 hover:text-indigo-300 disabled:opacity-40"
                    >
                      시작
                    </button>
                    <button
                      type="button"
                      disabled={
                        busyId === s.id || s.status !== 'ACTIVE'
                      }
                      onClick={() => {
                        if (
                          confirm(
                            '스프린트를 완료 처리할까요?',
                          )
                        ) {
                          void run(s.id, () => completeSprint(s.id))
                        }
                      }}
                      className="mr-2 text-xs text-amber-400/90 hover:text-amber-300 disabled:opacity-40"
                    >
                      완료
                    </button>
                    <button
                      type="button"
                      disabled={busyId === s.id || s.status === 'ACTIVE'}
                      onClick={() => {
                        if (
                          confirm(
                            '스프린트를 삭제할까요? (이슈가 있거나 ACTIVE이면 실패할 수 있습니다.)',
                          )
                        ) {
                          void run(s.id, () => deleteSprint(s.id))
                        }
                      }}
                      className="text-xs text-red-400/90 hover:text-red-300 disabled:opacity-40"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {sprints.length === 0 ? (
            <p className="px-4 py-8 text-center text-sm text-slate-500">
              스프린트가 없습니다.
            </p>
          ) : null}
        </div>
      )}
    </div>
  )
}
