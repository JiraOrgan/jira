import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { KanbanBoard } from '../components/kanban/KanbanBoard'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import { fetchAllProjectIssues } from '../lib/issueApi'
import { fetchWipLimits, replaceWipLimits } from '../lib/projectApi'
import { statusLabel } from '../lib/labels'
import { ISSUE_STATUS_ORDER } from '../lib/workflow'
import type { IssueMin, IssueStatus, WipLimitItem } from '../types/domain'

export function KanbanPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [issues, setIssues] = useState<IssueMin[]>([])
  const [wipLimits, setWipLimits] = useState<WipLimitItem[]>([])
  const [wipDraft, setWipDraft] = useState<Partial<Record<IssueStatus, string>>>(
    {},
  )
  const [loadError, setLoadError] = useState<string | null>(null)
  const [wipError, setWipError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [wipSaving, setWipSaving] = useState(false)

  const reload = useCallback(async () => {
    if (!project) return
    setLoadError(null)
    try {
      const [iss, wip] = await Promise.all([
        fetchAllProjectIssues(project.id),
        fetchWipLimits(project.id),
      ])
      setIssues(iss)
      setWipLimits(wip)
      const d: Partial<Record<IssueStatus, string>> = {}
      for (const w of wip) d[w.status] = String(w.maxIssues)
      setWipDraft(d)
    } catch (e) {
      setLoadError(errorMessage(e) || '불러오기 실패')
      setIssues([])
      setWipLimits([])
    } finally {
      setLoading(false)
    }
  }, [project])

  useEffect(() => {
    if (!project) return
    setLoading(true)
    void reload()
  }, [project, reload])

  async function saveWip() {
    if (!project || project.boardType !== 'KANBAN') return
    setWipError(null)
    setWipSaving(true)
    try {
      const limits: WipLimitItem[] = []
      for (const st of ISSUE_STATUS_ORDER) {
        const raw = wipDraft[st]?.trim()
        if (!raw) continue
        const n = Number(raw)
        if (Number.isFinite(n) && n >= 1) {
          limits.push({ status: st, maxIssues: Math.floor(n) })
        }
      }
      const next = await replaceWipLimits(project.id, limits)
      setWipLimits(next)
    } catch (e) {
      setWipError(errorMessage(e) || 'WIP 저장 실패')
    } finally {
      setWipSaving(false)
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
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-xl font-semibold text-white">칸반 보드</h1>
          <p className="mt-1 text-sm text-slate-400">
            프로젝트 전체 이슈를 상태 컬럼으로 표시합니다. WIP 제한은 칸반
            프로젝트에서만 API로 설정할 수 있습니다.
          </p>
        </div>
        <Link
          to={`/project/${project.key}/board`}
          className="text-sm text-indigo-400 hover:text-indigo-300"
        >
          스크럼 보드
        </Link>
      </div>

      {project.boardType === 'SCRUM' ? (
        <p className="rounded-lg border border-amber-900/40 bg-amber-950/20 px-4 py-2 text-sm text-amber-200/90">
          스크럼 프로젝트입니다. 스프린트 단위 보드는{' '}
          <Link
            to={`/project/${project.key}/board`}
            className="underline hover:text-white"
          >
            스크럼 보드
          </Link>
          를 이용하세요. 이 화면은 전체 이슈 칸반 뷰입니다.
        </p>
      ) : null}

      {project.boardType === 'KANBAN' ? (
        <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-4">
          <h2 className="text-sm font-medium text-slate-300">WIP 제한</h2>
          <p className="mt-1 text-xs text-slate-500">
            상태별 최대 이슈 수(1 이상). 비우면 해당 상태 제한 없음. 전부 비우고
            저장하면 제한을 모두 제거합니다.
          </p>
          <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {ISSUE_STATUS_ORDER.map((st) => (
              <label key={st} className="block text-xs text-slate-400">
                {statusLabel[st]}
                <input
                  type="number"
                  min={1}
                  value={wipDraft[st] ?? ''}
                  onChange={(e) =>
                    setWipDraft((d) => ({ ...d, [st]: e.target.value }))
                  }
                  placeholder="—"
                  className="mt-1 w-full rounded border border-slate-700 bg-slate-950 px-2 py-1 text-sm text-white"
                />
              </label>
            ))}
          </div>
          <div className="mt-3 flex items-center gap-3">
            <button
              type="button"
              disabled={wipSaving}
              onClick={() => void saveWip()}
              className="rounded-lg bg-amber-700 px-3 py-1.5 text-sm text-white hover:bg-amber-600 disabled:opacity-60"
            >
              {wipSaving ? '저장 중…' : 'WIP 저장'}
            </button>
            {wipError ? (
              <span className="text-sm text-red-300">{wipError}</span>
            ) : null}
          </div>
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">불러오는 중…</p>
      ) : loadError ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {loadError}
        </div>
      ) : (
        <KanbanBoard issues={issues} wipLimits={wipLimits} onReload={reload} />
      )}
    </div>
  )
}
