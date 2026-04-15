import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import { fetchProjectRoadmapEpics } from '../lib/issueApi'
import { statusLabel } from '../lib/labels'
import type { IssueStatus, RoadmapEpicItem } from '../types/domain'

const DAY_MS = 86400000

type ZoomMode = 'full' | '90d' | '30d'

type EpicBar = {
  row: RoadmapEpicItem
  startMs: number
  endMs: number
}

/** `YYYY-MM-DD` → 로컬 자정(시작) 또는 해당일 끝(종료) ms */
function isoLocalToMs(iso: string, endOfDay: boolean): number {
  const parts = iso.split('-').map(Number)
  const y = parts[0]
  const m = parts[1]
  const d = parts[2]
  return new Date(
    y,
    m - 1,
    d,
    endOfDay ? 23 : 0,
    endOfDay ? 59 : 0,
    endOfDay ? 59 : 0,
    endOfDay ? 999 : 0,
  ).getTime()
}

function rowToBar(row: RoadmapEpicItem): EpicBar {
  return {
    row,
    startMs: isoLocalToMs(row.effectiveStart, false),
    endMs: isoLocalToMs(row.effectiveEnd, true),
  }
}

const barTone: Record<IssueStatus, string> = {
  BACKLOG: 'bg-slate-600',
  SELECTED: 'bg-sky-700',
  IN_PROGRESS: 'bg-indigo-600',
  CODE_REVIEW: 'bg-violet-600',
  QA: 'bg-amber-600',
  DONE: 'bg-emerald-700',
}

function clamp(n: number, lo: number, hi: number): number {
  return Math.min(hi, Math.max(lo, n))
}

function monthTicks(fromMs: number, toMs: number): { ms: number; label: string }[] {
  const out: { ms: number; label: string }[] = []
  const d = new Date(fromMs)
  d.setDate(1)
  d.setHours(0, 0, 0, 0)
  const end = toMs + DAY_MS
  while (d.getTime() < end) {
    const t = d.getTime()
    if (t >= fromMs - 1) {
      out.push({
        ms: t,
        label: d.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' }),
      })
    }
    d.setMonth(d.getMonth() + 1)
  }
  return out
}

function barLayout(
  startMs: number,
  endMs: number,
  viewFrom: number,
  viewTo: number,
): { left: number; width: number } {
  const total = viewTo - viewFrom
  if (total <= 0) return { left: 0, width: 0 }
  const rawLeft = ((startMs - viewFrom) / total) * 100
  const rawWidth = ((endMs - startMs) / total) * 100
  return {
    left: clamp(rawLeft, 0, 100),
    width: clamp(Math.max(rawWidth, 0.6), 0, 100),
  }
}

export function RoadmapPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [epicBars, setEpicBars] = useState<EpicBar[]>([])
  const [zoom, setZoom] = useState<ZoomMode>('full')
  const [phase, setPhase] = useState<'idle' | 'loading'>('idle')
  const [error, setError] = useState<string | null>(null)
  const [viewportNow, setViewportNow] = useState(() => Date.now())

  const load = useCallback(async () => {
    if (!project) return
    setError(null)
    setPhase('loading')
    try {
      const rows = await fetchProjectRoadmapEpics(project.id)
      setEpicBars(rows.map(rowToBar))
      setPhase('idle')
      setViewportNow(Date.now())
    } catch (e) {
      setEpicBars([])
      setError(errorMessage(e))
      setPhase('idle')
      setViewportNow(Date.now())
    }
  }, [project])

  useEffect(() => {
    if (!project) return
    const id = requestAnimationFrame(() => {
      void load()
    })
    return () => cancelAnimationFrame(id)
  }, [project, load])

  useEffect(() => {
    const id = requestAnimationFrame(() => {
      setViewportNow(Date.now())
    })
    return () => cancelAnimationFrame(id)
  }, [zoom, epicBars])

  const { viewFrom, viewTo } = useMemo(() => {
    if (epicBars.length === 0) {
      const now = viewportNow
      return { viewFrom: now - DAY_MS * 45, viewTo: now + DAY_MS * 15 }
    }
    const minStart = Math.min(...epicBars.map((b) => b.startMs))
    const maxEnd = Math.max(...epicBars.map((b) => b.endMs))
    const pad = DAY_MS * 7
    if (zoom === 'full') {
      return { viewFrom: minStart - pad, viewTo: maxEnd + pad }
    }
    const end = Math.max(viewportNow, maxEnd)
    const days = zoom === '90d' ? 90 : 30
    return { viewFrom: end - days * DAY_MS, viewTo: end }
  }, [epicBars, zoom, viewportNow])

  const ticks = useMemo(
    () => monthTicks(viewFrom, viewTo),
    [viewFrom, viewTo],
  )

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

  const loading = phase !== 'idle'

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-xl font-semibold text-white">로드맵</h1>
          <p className="mt-1 text-sm text-slate-400">
            Epic만 표시합니다. 막대는 서버가 계산한{' '}
            <span className="text-slate-300">effectiveStart ~ effectiveEnd</span>를 사용합니다
            (저장된 Epic 기간이 없으면 생성·수정일로 보강, FR-012 / SCR-009).
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          {(['full', '90d', '30d'] as const).map((z) => (
            <button
              key={z}
              type="button"
              disabled={loading}
              onClick={() => setZoom(z)}
              className={[
                'rounded-lg px-3 py-1.5 text-sm font-medium transition',
                zoom === z
                  ? 'bg-indigo-600 text-white'
                  : 'border border-slate-700 text-slate-300 hover:border-slate-600',
                loading ? 'opacity-50' : '',
              ].join(' ')}
            >
              {z === 'full' ? '전체' : z === '90d' ? '최근 90일' : '최근 30일'}
            </button>
          ))}
          <button
            type="button"
            disabled={loading}
            onClick={() => void load()}
            className="rounded-lg border border-slate-600 px-3 py-1.5 text-sm text-slate-300 hover:bg-slate-800 disabled:opacity-50"
          >
            새로고침
          </button>
        </div>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {error}
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">로드맵 데이터 불러오는 중…</p>
      ) : null}

      {!loading && epicBars.length === 0 && !error ? (
        <div className="rounded-xl border border-slate-800 bg-slate-900/40 px-6 py-10 text-center">
          <p className="text-slate-400">이 프로젝트에 Epic 이슈가 없습니다.</p>
          <Link
            to={`/project/${project.key}/issues/new`}
            className="mt-4 inline-block text-sm text-indigo-400 hover:text-indigo-300"
          >
            새 이슈에서 Epic 만들기
          </Link>
        </div>
      ) : null}

      {epicBars.length > 0 ? (
        <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900/30 pb-4">
          <div className="min-w-[720px] px-4 pt-4">
            <div className="relative mb-2 h-8 border-b border-slate-800">
              {ticks.map((t) => {
                const p = ((t.ms - viewFrom) / (viewTo - viewFrom)) * 100
                if (p < 0 || p > 100) return null
                return (
                  <span
                    key={t.ms}
                    className="absolute top-0 -translate-x-1/2 text-[10px] text-slate-500"
                    style={{ left: `${p}%` }}
                  >
                    {t.label}
                  </span>
                )
              })}
            </div>

            <div className="space-y-3">
              {epicBars.map(({ row, startMs, endMs }) => {
                const { left, width } = barLayout(
                  startMs,
                  endMs,
                  viewFrom,
                  viewTo,
                )
                const tone = barTone[row.status]
                return (
                  <div
                    key={row.id}
                    className="grid grid-cols-[160px_1fr] items-center gap-3"
                  >
                    <div className="min-w-0">
                      <Link
                        to={`/issue/${encodeURIComponent(row.issueKey)}`}
                        className="font-mono text-sm font-medium text-indigo-300 hover:underline"
                      >
                        {row.issueKey}
                      </Link>
                      <p className="line-clamp-2 text-xs text-slate-500">
                        {row.summary}
                      </p>
                      <p className="text-[10px] text-slate-600">
                        {statusLabel[row.status]}
                      </p>
                    </div>
                    <div className="relative h-9 rounded-md bg-slate-950/80 ring-1 ring-slate-800">
                      <div
                        className={`absolute top-1/2 h-5 -translate-y-1/2 rounded ${tone} opacity-90 shadow-sm ring-1 ring-white/10`}
                        style={{ left: `${left}%`, width: `${width}%` }}
                        title={`${row.effectiveStart} → ${row.effectiveEnd}`}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
