import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { fetchBurndown, fetchCfd, fetchVelocity } from '../lib/reportApi'
import { fetchSprintsByProject } from '../lib/sprintApi'
import type {
  IssueStatus,
  ReportBurndown,
  ReportBurndownPoint,
  ReportCfd,
  ReportVelocity,
  SprintMin,
} from '../types/domain'

type Tab = 'burndown' | 'velocity' | 'cfd'

const CFD_COLORS: Record<IssueStatus, string> = {
  BACKLOG: '#64748b',
  SELECTED: '#818cf8',
  IN_PROGRESS: '#38bdf8',
  CODE_REVIEW: '#a78bfa',
  QA: '#fbbf24',
  DONE: '#34d399',
}

function BurndownSvg({ series }: { series: ReportBurndownPoint[] }) {
  const w = 640
  const h = 260
  const pad = { t: 16, r: 24, b: 36, l: 48 }
  const innerW = w - pad.l - pad.r
  const innerH = h - pad.t - pad.b

  const maxY = useMemo(() => {
    let m = 1
    for (const p of series) {
      m = Math.max(
        m,
        p.remainingStoryPoints,
        Math.ceil(p.idealRemainingPoints),
      )
    }
    return m
  }, [series])

  const n = Math.max(series.length - 1, 1)
  const xAt = (i: number) => pad.l + (innerW * i) / n
  const yAt = (v: number) => pad.t + innerH - (innerH * v) / maxY

  const actualPath = series
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${xAt(i)} ${yAt(p.remainingStoryPoints)}`)
    .join(' ')
  const idealPath = series
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${xAt(i)} ${yAt(p.idealRemainingPoints)}`)
    .join(' ')

  const ticks = [0, Math.round(maxY / 2), maxY]

  return (
    <svg
      className="max-w-full text-slate-200"
      viewBox={`0 0 ${w} ${h}`}
      role="img"
      aria-label="번다운 차트"
    >
      <rect
        x={pad.l}
        y={pad.t}
        width={innerW}
        height={innerH}
        fill="none"
        stroke="#334155"
        strokeWidth={1}
      />
      {ticks.map((t) => (
        <g key={t}>
          <line
            x1={pad.l}
            x2={pad.l + innerW}
            y1={yAt(t)}
            y2={yAt(t)}
            stroke="#1e293b"
            strokeDasharray="4 4"
          />
          <text
            x={pad.l - 8}
            y={yAt(t) + 4}
            textAnchor="end"
            className="fill-slate-500 text-[10px]"
          >
            {t}
          </text>
        </g>
      ))}
      <path
        d={idealPath}
        fill="none"
        stroke="#475569"
        strokeWidth={2}
        strokeDasharray="6 4"
      />
      <path d={actualPath} fill="none" stroke="#818cf8" strokeWidth={2.5} />
      {series.map((p, i) => (
        <g key={p.date}>
          <text
            x={xAt(i)}
            y={h - 8}
            textAnchor="middle"
            className="fill-slate-500 text-[9px]"
          >
            {p.date.slice(5)}
          </text>
        </g>
      ))}
      <text x={pad.l + innerW - 4} y={pad.t + 14} textAnchor="end" className="fill-slate-500 text-[10px]">
        보라: 실제 잔량 / 점선: 이상선
      </text>
    </svg>
  )
}

function VelocityBars({ data }: { data: ReportVelocity }) {
  const max = Math.max(
    1,
    ...data.sprints.map((s) => Number(s.completedStoryPoints)),
  )
  return (
    <div className="space-y-3">
      {data.sprints.length === 0 ? (
        <p className="text-sm text-slate-500">완료된 스프린트가 없습니다.</p>
      ) : (
        data.sprints.map((s) => {
          const pct = (Number(s.completedStoryPoints) / max) * 100
          return (
            <div key={s.sprintId}>
              <div className="flex justify-between text-xs text-slate-400">
                <span className="truncate pr-2 font-medium text-slate-300">
                  {s.sprintName}
                </span>
                <span className="shrink-0 font-mono text-indigo-300">
                  {s.completedStoryPoints} pt
                </span>
              </div>
              <div className="mt-1 h-2 overflow-hidden rounded-full bg-slate-800">
                <div
                  className="h-full rounded-full bg-indigo-500"
                  style={{ width: `${pct}%` }}
                />
              </div>
              {s.endDate ? (
                <p className="mt-0.5 text-[10px] text-slate-600">{s.endDate}</p>
              ) : null}
            </div>
          )
        })
      )}
    </div>
  )
}

function CfdSvg({ data }: { data: ReportCfd }) {
  const statuses: IssueStatus[] = [
    'BACKLOG',
    'SELECTED',
    'IN_PROGRESS',
    'CODE_REVIEW',
    'QA',
    'DONE',
  ]
  const w = 640
  const h = 280
  const pad = { t: 16, r: 24, b: 36, l: 48 }
  const innerW = w - pad.l - pad.r
  const innerH = h - pad.t - pad.b
  const series = data.series
  const n = Math.max(series.length - 1, 1)

  let maxY = 1
  for (const day of series) {
    let sum = 0
    for (const row of day.byStatus) sum += row.count
    maxY = Math.max(maxY, sum)
  }

  const xAt = (i: number) => pad.l + (innerW * i) / n
  const yAt = (v: number) => pad.t + innerH - (innerH * v) / maxY

  const paths: { status: IssueStatus; d: string }[] = []
  for (const st of statuses) {
    const d = series
      .map((day, i) => {
        const c =
          day.byStatus.find((x) => x.status === st)?.count ?? 0
        const y = yAt(c)
        return `${i === 0 ? 'M' : 'L'} ${xAt(i)} ${y}`
      })
      .join(' ')
    paths.push({ status: st, d })
  }

  return (
    <div>
      <svg
        className="max-w-full"
        viewBox={`0 0 ${w} ${h}`}
        role="img"
        aria-label="누적 흐름 차트"
      >
        <rect
          x={pad.l}
          y={pad.t}
          width={innerW}
          height={innerH}
          fill="none"
          stroke="#334155"
          strokeWidth={1}
        />
        {paths.map(({ status, d }) => (
          <path
            key={status}
            d={d}
            fill="none"
            stroke={CFD_COLORS[status]}
            strokeWidth={2}
          />
        ))}
        {series.map((day, i) => (
          <text
            key={day.date}
            x={xAt(i)}
            y={h - 8}
            textAnchor="middle"
            className="fill-slate-500 text-[9px]"
          >
            {day.date.slice(5)}
          </text>
        ))}
      </svg>
      <div className="mt-3 flex flex-wrap gap-3 text-[10px] text-slate-400">
        {statuses.map((st) => (
          <span key={st} className="flex items-center gap-1">
            <span
              className="inline-block h-2 w-2 rounded-full"
              style={{ background: CFD_COLORS[st] }}
            />
            {st}
          </span>
        ))}
      </div>
    </div>
  )
}

export function ReportsPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)
  const [tab, setTab] = useState<Tab>('burndown')

  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [sprintId, setSprintId] = useState<number | ''>('')
  const [burndown, setBurndown] = useState<ReportBurndown | null>(null)
  const [velocity, setVelocity] = useState<ReportVelocity | null>(null)
  const [cfd, setCfd] = useState<ReportCfd | null>(null)
  const [cfdSprint, setCfdSprint] = useState<number | ''>('')
  const [cfdDays, setCfdDays] = useState(30)
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const pid = project?.id

  useEffect(() => {
    if (!pid) return
    void (async () => {
      try {
        const list = await fetchSprintsByProject(pid)
        setSprints(list)
        if (list.length > 0) {
          setSprintId((prev) => {
            if (prev !== '') return prev
            const active = list.find((s) => s.status === 'ACTIVE')
            return (active ?? list[0]).id
          })
        }
      } catch {
        setSprints([])
      }
    })()
  }, [pid])

  const loadBurndown = useCallback(async () => {
    if (!pid || sprintId === '') return
    setLoading(true)
    setErr(null)
    try {
      const b = await fetchBurndown(pid, Number(sprintId))
      setBurndown(b)
    } catch (e) {
      setBurndown(null)
      setErr(e instanceof Error ? e.message : '번다운을 불러오지 못했습니다')
    } finally {
      setLoading(false)
    }
  }, [pid, sprintId])

  const loadVelocity = useCallback(async () => {
    if (!pid) return
    setLoading(true)
    setErr(null)
    try {
      const v = await fetchVelocity(pid, 8)
      setVelocity(v)
    } catch (e) {
      setVelocity(null)
      setErr(e instanceof Error ? e.message : '속도 차트를 불러오지 못했습니다')
    } finally {
      setLoading(false)
    }
  }, [pid])

  const loadCfd = useCallback(async () => {
    if (!pid) return
    setLoading(true)
    setErr(null)
    try {
      const c = await fetchCfd(pid, {
        days: cfdDays,
        ...(cfdSprint === '' ? {} : { sprintId: Number(cfdSprint) }),
      })
      setCfd(c)
    } catch (e) {
      setCfd(null)
      setErr(e instanceof Error ? e.message : 'CFD를 불러오지 못했습니다')
    } finally {
      setLoading(false)
    }
  }, [pid, cfdDays, cfdSprint])

  useEffect(() => {
    if (tab !== 'burndown' || !pid || sprintId === '') return
    void loadBurndown()
  }, [tab, pid, sprintId, loadBurndown])

  useEffect(() => {
    if (tab !== 'velocity' || !pid) return
    void loadVelocity()
  }, [tab, pid, loadVelocity])

  useEffect(() => {
    if (tab !== 'cfd' || !pid) return
    void loadCfd()
  }, [tab, pid, loadCfd])

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
  }

  if (!project) {
    return (
      <p className="text-slate-400">
        프로젝트를 찾을 수 없습니다.{' '}
        <Link to="/" className="text-indigo-400 hover:text-indigo-300">
          홈으로
        </Link>
      </p>
    )
  }

  const tabBtn = (id: Tab, label: string) => (
    <button
      type="button"
      onClick={() => setTab(id)}
      className={[
        'rounded-lg px-4 py-2 text-sm font-medium transition',
        tab === id
          ? 'bg-indigo-600 text-white'
          : 'bg-slate-800/80 text-slate-400 hover:text-slate-200',
      ].join(' ')}
    >
      {label}
    </button>
  )

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div>
        <p className="text-xs text-slate-500">
          <Link to="/" className="text-indigo-400 hover:text-indigo-300">
            홈
          </Link>
          <span className="mx-2 text-slate-600">/</span>
          <Link
            to={`/project/${projectKey}`}
            className="text-indigo-400 hover:text-indigo-300"
          >
            {project.key}
          </Link>
          <span className="mx-2 text-slate-600">/</span>
          <span className="text-slate-400">리포트</span>
        </p>
        <h1 className="mt-2 text-xl font-semibold text-white">리포트</h1>
        <p className="mt-1 text-sm text-slate-400">
          번다운·속도·CFD (FR-022). 동일 스프린트·프로젝트 기준 백엔드 집계와
          맞춥니다.
        </p>
      </div>

      <div className="flex flex-wrap gap-2">
        {tabBtn('burndown', '번다운')}
        {tabBtn('velocity', '속도')}
        {tabBtn('cfd', '누적 흐름 (CFD)')}
      </div>

      {err ? (
        <p className="rounded-lg border border-red-900/50 bg-red-950/40 px-4 py-3 text-sm text-red-300">
          {err}
        </p>
      ) : null}

      {tab === 'burndown' ? (
        <section className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-6">
          <div className="flex flex-wrap items-end gap-4">
            <div>
              <label className="block text-xs text-slate-500">스프린트</label>
              <select
                value={sprintId === '' ? '' : String(sprintId)}
                onChange={(e) =>
                  setSprintId(e.target.value ? Number(e.target.value) : '')
                }
                className="mt-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              >
                {sprints.length === 0 ? (
                  <option value="">스프린트 없음</option>
                ) : (
                  sprints.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.status})
                    </option>
                  ))
                )}
              </select>
            </div>
            <button
              type="button"
              disabled={loading || sprintId === ''}
              onClick={() => void loadBurndown()}
              className="rounded-lg border border-slate-600 px-3 py-2 text-sm text-slate-200 hover:bg-slate-800 disabled:opacity-50"
            >
              새로고침
            </button>
          </div>
          {burndown && burndown.series.length > 0 ? (
            <>
              <p className="text-sm text-slate-400">
                <span className="font-medium text-slate-200">
                  {burndown.sprintName}
                </span>
                {' · '}
                범위 {burndown.startDate} ~ {burndown.endDate} · 총 범위{' '}
                <span className="font-mono text-indigo-300">
                  {burndown.totalScopePoints}
                </span>{' '}
                pt
              </p>
              <BurndownSvg series={burndown.series} />
            </>
          ) : (
            <p className="text-sm text-slate-500">
              스프린트를 선택하면 번다운 데이터가 표시됩니다.
            </p>
          )}
        </section>
      ) : null}

      {tab === 'velocity' ? (
        <section className="rounded-xl border border-slate-800 bg-slate-900/40 p-6">
          <div className="mb-4 flex items-center justify-between gap-4">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
              완료 스프린트별 DONE 스토리 포인트
            </h2>
            <button
              type="button"
              disabled={loading}
              onClick={() => void loadVelocity()}
              className="text-xs text-indigo-400 hover:text-indigo-300 disabled:opacity-50"
            >
              새로고침
            </button>
          </div>
          {velocity ? <VelocityBars data={velocity} /> : null}
        </section>
      ) : null}

      {tab === 'cfd' ? (
        <section className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-6">
          <div className="flex flex-wrap items-end gap-4">
            <div>
              <label className="block text-xs text-slate-500">범위 (일)</label>
              <select
                value={cfdDays}
                onChange={(e) => setCfdDays(Number(e.target.value))}
                className="mt-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              >
                {[7, 14, 30, 60, 90].map((d) => (
                  <option key={d} value={d}>
                    {d}일
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs text-slate-500">스프린트 (선택)</label>
              <select
                value={cfdSprint === '' ? '' : String(cfdSprint)}
                onChange={(e) =>
                  setCfdSprint(e.target.value ? Number(e.target.value) : '')
                }
                className="mt-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              >
                <option value="">프로젝트 전체</option>
                {sprints.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="button"
              disabled={loading}
              onClick={() => void loadCfd()}
              className="rounded-lg border border-slate-600 px-3 py-2 text-sm text-slate-200 hover:bg-slate-800 disabled:opacity-50"
            >
              적용
            </button>
          </div>
          {cfd && cfd.series.length > 0 ? <CfdSvg data={cfd} /> : null}
        </section>
      ) : null}
    </div>
  )
}
