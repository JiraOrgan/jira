import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  addDashboardGadget,
  deleteDashboard,
  fetchDashboardById,
  removeDashboardGadget,
  updateDashboard,
} from '../lib/dashboardApi'
import { getUserIdFromAccessToken } from '../lib/jwtPayload'
import { useAuthStore } from '../stores/authStore'
import type { DashboardDetail, DashboardGadgetRow } from '../types/domain'

const GADGET_PRESETS: { value: string; label: string; defaultConfig: string }[] =
  [
    {
      value: 'TEXT_NOTE',
      label: '메모',
      defaultConfig: JSON.stringify({ text: '메모 내용을 입력하세요' }),
    },
    {
      value: 'PROJECT_LINKS',
      label: '프로젝트 바로가기',
      defaultConfig: JSON.stringify({ keys: ['DEMO'] }),
    },
    {
      value: 'PLACEHOLDER',
      label: '플레이스홀더',
      defaultConfig: '{}',
    },
  ]

function parseProjectKeys(configJson: string | null): string[] {
  if (!configJson?.trim()) return []
  try {
    const o = JSON.parse(configJson) as unknown
    if (Array.isArray(o)) {
      return o.filter((x): x is string => typeof x === 'string')
    }
    if (
      o &&
      typeof o === 'object' &&
      'keys' in o &&
      Array.isArray((o as { keys: unknown }).keys)
    ) {
      return (o as { keys: unknown[] }).keys.filter(
        (x): x is string => typeof x === 'string',
      )
    }
  } catch {
    /* ignore */
  }
  return []
}

function parseNoteText(configJson: string | null): string | null {
  if (!configJson?.trim()) return null
  try {
    const o = JSON.parse(configJson) as unknown
    if (o && typeof o === 'object' && 'text' in o && typeof (o as { text: unknown }).text === 'string') {
      return (o as { text: string }).text
    }
  } catch {
    /* fall through */
  }
  return configJson
}

function GadgetCard({ gadget }: { gadget: DashboardGadgetRow }) {
  const keys = parseProjectKeys(gadget.configJson)

  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/50 p-4">
      <div className="flex items-start justify-between gap-2">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          {gadget.gadgetType}
        </p>
        <span className="text-[10px] text-slate-600">#{gadget.position}</span>
      </div>
      <div className="mt-3 text-sm text-slate-200">
        {gadget.gadgetType === 'TEXT_NOTE' ? (
          <p className="whitespace-pre-wrap text-slate-300">
            {parseNoteText(gadget.configJson) ?? '(내용 없음)'}
          </p>
        ) : gadget.gadgetType === 'PROJECT_LINKS' ? (
          keys.length > 0 ? (
            <ul className="space-y-1">
              {keys.map((k) => (
                <li key={k}>
                  <Link
                    to={`/project/${encodeURIComponent(k)}`}
                    className="font-mono text-indigo-400 hover:text-indigo-300"
                  >
                    {k}
                  </Link>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-slate-500">
              <code className="text-xs">configJson</code>에{' '}
              <code className="text-xs">{`{"keys":["KEY1"]}`}</code> 형식으로
              프로젝트 키를 넣으세요.
            </p>
          )
        ) : (
          <pre className="max-h-40 overflow-auto whitespace-pre-wrap break-all text-xs text-slate-400">
            {gadget.configJson?.trim()
              ? (() => {
                  try {
                    return JSON.stringify(
                      JSON.parse(gadget.configJson) as unknown,
                      null,
                      2,
                    )
                  } catch {
                    return gadget.configJson
                  }
                })()
              : '(설정 없음)'}
          </pre>
        )}
      </div>
    </div>
  )
}

export function DashboardDetailPage() {
  const { dashboardId: idParam } = useParams<{ dashboardId: string }>()
  const navigate = useNavigate()
  const accessToken = useAuthStore((s) => s.accessToken)
  const currentUserId = useMemo(
    () => getUserIdFromAccessToken(accessToken),
    [accessToken],
  )

  const dashboardId = idParam ? Number(idParam) : NaN

  const [detail, setDetail] = useState<DashboardDetail | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [editName, setEditName] = useState('')
  const [editShared, setEditShared] = useState(false)
  const [settingsMsg, setSettingsMsg] = useState<string | null>(null)
  const [settingsErr, setSettingsErr] = useState<string | null>(null)
  const [settingsBusy, setSettingsBusy] = useState(false)

  const [gadgetType, setGadgetType] = useState(GADGET_PRESETS[0].value)
  const [gadgetConfig, setGadgetConfig] = useState(GADGET_PRESETS[0].defaultConfig)
  const [gadgetErr, setGadgetErr] = useState<string | null>(null)
  const [gadgetBusy, setGadgetBusy] = useState(false)

  const [deleteBusy, setDeleteBusy] = useState(false)

  const load = useCallback(async () => {
    if (!Number.isFinite(dashboardId)) return
    setLoading(true)
    setLoadError(null)
    try {
      const d = await fetchDashboardById(dashboardId)
      setDetail(d)
      setEditName(d.name)
      setEditShared(d.shared)
    } catch (e) {
      setDetail(null)
      setLoadError(
        e instanceof Error ? e.message : '대시보드를 불러오지 못했습니다',
      )
    } finally {
      setLoading(false)
    }
  }, [dashboardId])

  useEffect(() => {
    void load()
  }, [load])

  const canWrite =
    detail != null &&
    currentUserId != null &&
    detail.ownerId != null &&
    detail.ownerId === currentUserId

  const sortedGadgets = useMemo(() => {
    const g = detail?.gadgets ?? []
    return [...g].sort((a, b) => a.position - b.position)
  }, [detail?.gadgets])

  function onGadgetTypeChange(next: string) {
    setGadgetType(next)
    const preset = GADGET_PRESETS.find((p) => p.value === next)
    if (preset) setGadgetConfig(preset.defaultConfig)
  }

  async function handleSaveSettings(e: React.FormEvent) {
    e.preventDefault()
    if (!detail || !canWrite) return
    const name = editName.trim()
    if (!name) {
      setSettingsErr('이름을 입력하세요.')
      return
    }
    setSettingsErr(null)
    setSettingsMsg(null)
    setSettingsBusy(true)
    try {
      const updated = await updateDashboard(detail.id, {
        name,
        shared: editShared,
      })
      setDetail(updated)
      setSettingsMsg('저장했습니다.')
    } catch (err) {
      setSettingsErr(
        err instanceof Error ? err.message : '저장하지 못했습니다',
      )
    } finally {
      setSettingsBusy(false)
    }
  }

  async function handleAddGadget(e: React.FormEvent) {
    e.preventDefault()
    if (!detail || !canWrite) return
    setGadgetErr(null)
    let parsed: unknown
    try {
      parsed = JSON.parse(gadgetConfig.trim() || '{}')
    } catch {
      setGadgetErr('설정 JSON 형식이 올바르지 않습니다.')
      return
    }
    const nextPos =
      sortedGadgets.length > 0
        ? Math.max(...sortedGadgets.map((g) => g.position)) + 1
        : 0
    setGadgetBusy(true)
    try {
      const updated = await addDashboardGadget(detail.id, {
        gadgetType,
        position: nextPos,
        configJson: JSON.stringify(parsed),
      })
      setDetail(updated)
    } catch (err) {
      setGadgetErr(
        err instanceof Error ? err.message : '가젯을 추가하지 못했습니다',
      )
    } finally {
      setGadgetBusy(false)
    }
  }

  async function handleRemoveGadget(g: DashboardGadgetRow) {
    if (!detail || !canWrite) return
    if (!confirm('이 가젯을 제거할까요?')) return
    setGadgetErr(null)
    setGadgetBusy(true)
    try {
      await removeDashboardGadget(detail.id, g.id)
      await load()
    } catch (err) {
      setGadgetErr(
        err instanceof Error ? err.message : '가젯을 제거하지 못했습니다',
      )
    } finally {
      setGadgetBusy(false)
    }
  }

  async function handleDeleteDashboard() {
    if (!detail || !canWrite) return
    if (
      !confirm(
        `대시보드 "${detail.name}"을(를) 삭제합니다. 계속할까요?`,
      )
    ) {
      return
    }
    setDeleteBusy(true)
    try {
      await deleteDashboard(detail.id)
      navigate('/', { replace: true })
    } catch (err) {
      alert(
        err instanceof Error
          ? err.message
          : '대시보드를 삭제하지 못했습니다',
      )
    } finally {
      setDeleteBusy(false)
    }
  }

  if (!Number.isFinite(dashboardId)) {
    return <p className="text-slate-500">잘못된 대시보드 ID입니다.</p>
  }

  if (loading) {
    return <p className="text-slate-400">불러오는 중…</p>
  }

  if (loadError || !detail) {
    return (
      <div className="space-y-3">
        <p className="text-red-400">{loadError ?? '데이터가 없습니다.'}</p>
        <Link to="/" className="text-sm text-indigo-400 hover:text-indigo-300">
          ← 홈으로
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <p className="text-xs text-slate-500">
          <Link to="/" className="text-indigo-400 hover:text-indigo-300">
            홈
          </Link>
          <span className="mx-2 text-slate-600">/</span>
          <span className="text-slate-400">{detail.name}</span>
        </p>
        <h1 className="mt-2 text-xl font-semibold text-white">{detail.name}</h1>
        <p className="mt-1 text-sm text-slate-400">
          소유자 {detail.ownerName ?? '—'}
          {detail.shared ? (
            <span className="ml-2 rounded bg-slate-800 px-2 py-0.5 text-xs text-slate-300">
              공유
            </span>
          ) : null}
        </p>
      </div>

      {!canWrite ? (
        <p className="rounded-lg border border-slate-800 bg-slate-900/40 px-4 py-3 text-sm text-slate-400">
          공유로 열람 중인 대시보드입니다. 이름·가젯 수정은 소유자만 할 수
          있습니다.
        </p>
      ) : (
        <section className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
            설정
          </h2>
          <form onSubmit={handleSaveSettings} className="space-y-4">
            <div>
              <label
                htmlFor="dash-name"
                className="block text-xs font-medium text-slate-400"
              >
                이름
              </label>
              <input
                id="dash-name"
                value={editName}
                onChange={(ev) => setEditName(ev.target.value)}
                className="mt-1 w-full max-w-md rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              />
            </div>
            <label className="flex items-center gap-2 text-sm text-slate-300">
              <input
                type="checkbox"
                checked={editShared}
                onChange={(ev) => setEditShared(ev.target.checked)}
                className="rounded border-slate-600"
              />
              팀에 공유 (다른 사용자가 볼 수 있음)
            </label>
            {settingsErr ? (
              <p className="text-sm text-red-400">{settingsErr}</p>
            ) : null}
            {settingsMsg ? (
              <p className="text-sm text-emerald-400">{settingsMsg}</p>
            ) : null}
            <button
              type="submit"
              disabled={settingsBusy}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
            >
              {settingsBusy ? '저장 중…' : '설정 저장'}
            </button>
          </form>

          <div className="border-t border-slate-800 pt-6">
            <h3 className="text-sm font-medium text-red-300">위험 구역</h3>
            <button
              type="button"
              disabled={deleteBusy}
              onClick={() => void handleDeleteDashboard()}
              className="mt-3 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-500 disabled:opacity-50"
            >
              {deleteBusy ? '삭제 중…' : '대시보드 삭제'}
            </button>
          </div>
        </section>
      )}

      <section className="space-y-4">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
            가젯
          </h2>
        </div>

        {canWrite ? (
          <form
            onSubmit={handleAddGadget}
            className="rounded-xl border border-slate-800 bg-slate-900/40 p-4"
          >
            <p className="text-xs font-medium text-slate-500">가젯 추가</p>
            <div className="mt-3 flex flex-wrap gap-3">
              <div>
                <label className="block text-xs text-slate-500">유형</label>
                <select
                  value={gadgetType}
                  onChange={(ev) => onGadgetTypeChange(ev.target.value)}
                  className="mt-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
                >
                  {GADGET_PRESETS.map((p) => (
                    <option key={p.value} value={p.value}>
                      {p.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="min-w-[200px] flex-1">
                <label className="block text-xs text-slate-500">
                  설정 (JSON)
                </label>
                <textarea
                  value={gadgetConfig}
                  onChange={(ev) => setGadgetConfig(ev.target.value)}
                  rows={3}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 font-mono text-xs text-slate-200 outline-none focus:border-indigo-500"
                />
              </div>
            </div>
            {gadgetErr ? (
              <p className="mt-2 text-sm text-red-400">{gadgetErr}</p>
            ) : null}
            <button
              type="submit"
              disabled={gadgetBusy}
              className="mt-3 rounded-lg border border-indigo-600 px-4 py-2 text-sm font-medium text-indigo-300 hover:bg-indigo-950/50 disabled:opacity-50"
            >
              {gadgetBusy ? '추가 중…' : '가젯 추가'}
            </button>
          </form>
        ) : null}

        <div className="grid gap-4 sm:grid-cols-2">
          {sortedGadgets.map((g) => (
            <div key={g.id} className="relative">
              <GadgetCard gadget={g} />
              {canWrite ? (
                <button
                  type="button"
                  disabled={gadgetBusy}
                  onClick={() => void handleRemoveGadget(g)}
                  className="absolute right-2 top-2 text-xs text-red-400 hover:text-red-300 disabled:opacity-50"
                >
                  제거
                </button>
              ) : null}
            </div>
          ))}
        </div>
        {sortedGadgets.length === 0 ? (
          <p className="text-sm text-slate-500">
            가젯이 없습니다. 소유자라면 위에서 유형을 고르고 추가할 수 있습니다.
          </p>
        ) : null}
      </section>
    </div>
  )
}
