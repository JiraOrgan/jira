import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useProjects } from '../hooks/useProjects'
import { createDashboard, fetchDashboards } from '../lib/dashboardApi'
import type { DashboardMin } from '../types/domain'

export function HomePage() {
  const navigate = useNavigate()
  const { projects, loading, error, reload } = useProjects()

  const [dashboards, setDashboards] = useState<DashboardMin[]>([])
  const [dashLoading, setDashLoading] = useState(true)
  const [dashError, setDashError] = useState<string | null>(null)

  const [newName, setNewName] = useState('')
  const [newShared, setNewShared] = useState(false)
  const [createBusy, setCreateBusy] = useState(false)
  const [createErr, setCreateErr] = useState<string | null>(null)

  const reloadDashboards = useCallback(async () => {
    setDashLoading(true)
    setDashError(null)
    try {
      const list = await fetchDashboards()
      setDashboards(list)
    } catch (e) {
      setDashError(
        e instanceof Error ? e.message : '대시보드 목록을 불러오지 못했습니다',
      )
      setDashboards([])
    } finally {
      setDashLoading(false)
    }
  }, [])

  useEffect(() => {
    void reloadDashboards()
  }, [reloadDashboards])

  async function handleCreateDashboard(e: React.FormEvent) {
    e.preventDefault()
    const name = newName.trim()
    if (!name) {
      setCreateErr('대시보드 이름을 입력하세요.')
      return
    }
    setCreateErr(null)
    setCreateBusy(true)
    try {
      const d = await createDashboard({ name, shared: newShared })
      setNewName('')
      setNewShared(false)
      await reloadDashboards()
      navigate(`/dashboard/${d.id}`)
    } catch (err) {
      setCreateErr(
        err instanceof Error ? err.message : '대시보드를 만들지 못했습니다',
      )
    } finally {
      setCreateBusy(false)
    }
  }

  return (
    <div className="space-y-10">
      <div>
        <h1 className="text-xl font-semibold text-white">대시보드</h1>
        <p className="mt-1 text-sm text-slate-400">
          맞춤 대시보드와 가젯으로 요약을 보고, 아래에서 프로젝트로 바로
          이동합니다.
        </p>
      </div>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          맞춤 대시보드
        </h2>
        {dashLoading ? (
          <p className="text-sm text-slate-500">대시보드 불러오는 중…</p>
        ) : dashError ? (
          <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
            {dashError}
            <button
              type="button"
              onClick={() => void reloadDashboards()}
              className="ml-3 text-indigo-400 hover:text-indigo-300"
            >
              다시 시도
            </button>
          </div>
        ) : dashboards.length === 0 ? (
          <p className="text-sm text-slate-500">
            아직 대시보드가 없습니다. 아래에서 새로 만드세요.
          </p>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-800">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-800 bg-slate-900/80 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">이름</th>
                  <th className="px-4 py-3 font-medium">소유자</th>
                  <th className="px-4 py-3 font-medium">공유</th>
                  <th className="px-4 py-3 font-medium text-right"> </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {dashboards.map((d) => (
                  <tr key={d.id} className="hover:bg-slate-900/40">
                    <td className="px-4 py-3 text-slate-200">{d.name}</td>
                    <td className="px-4 py-3 text-slate-500">
                      {d.ownerName ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {d.shared ? '예' : '아니오'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        to={`/dashboard/${d.id}`}
                        className="text-indigo-400 hover:text-indigo-300"
                      >
                        열기
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <form
          onSubmit={handleCreateDashboard}
          className="flex flex-wrap items-end gap-3 rounded-xl border border-slate-800 bg-slate-900/40 p-4"
        >
          <div className="min-w-[200px] flex-1">
            <label
              htmlFor="new-dash-name"
              className="block text-xs font-medium text-slate-400"
            >
              새 대시보드
            </label>
            <input
              id="new-dash-name"
              value={newName}
              onChange={(ev) => setNewName(ev.target.value)}
              placeholder="이름"
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            />
          </div>
          <label className="flex items-center gap-2 pb-2 text-sm text-slate-400">
            <input
              type="checkbox"
              checked={newShared}
              onChange={(ev) => setNewShared(ev.target.checked)}
              className="rounded border-slate-600"
            />
            공유
          </label>
          <button
            type="submit"
            disabled={createBusy}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
          >
            {createBusy ? '만드는 중…' : '만들기'}
          </button>
          {createErr ? (
            <p className="w-full text-sm text-red-400">{createErr}</p>
          ) : null}
        </form>
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          프로젝트
        </h2>
        <p className="text-sm text-slate-400">
          참여 중인 프로젝트입니다. 키를 눌러 개요 또는 새 이슈로 이동합니다.
        </p>

        {loading ? (
          <p className="text-sm text-slate-500">불러오는 중…</p>
        ) : error ? (
          <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
            {error}
            <button
              type="button"
              onClick={() => void reload()}
              className="ml-3 text-indigo-400 hover:text-indigo-300"
            >
              다시 시도
            </button>
          </div>
        ) : projects.length === 0 ? (
          <p className="text-sm text-slate-500">
            프로젝트가 없습니다. API로 프로젝트를 먼저 생성하세요.
          </p>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-800">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-800 bg-slate-900/80 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">키</th>
                  <th className="px-4 py-3 font-medium">이름</th>
                  <th className="px-4 py-3 font-medium">보드</th>
                  <th className="px-4 py-3 font-medium text-right">동작</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {projects.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-900/40">
                    <td className="px-4 py-3 font-mono text-indigo-300">
                      {p.key}
                    </td>
                    <td className="px-4 py-3 text-slate-200">{p.name}</td>
                    <td className="px-4 py-3 text-slate-500">{p.boardType}</td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        to={`/project/${p.key}`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        개요
                      </Link>
                      <Link
                        to={`/project/${p.key}/board`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        보드
                      </Link>
                      <Link
                        to={`/project/${p.key}/kanban`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        칸반
                      </Link>
                      <Link
                        to={`/project/${p.key}/sprints`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        스프린트
                      </Link>
                      <Link
                        to={`/project/${p.key}/backlog`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        백로그
                      </Link>
                      <Link
                        to={`/project/${p.key}/jql`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        JQL
                      </Link>
                      <Link
                        to={`/project/${p.key}/roadmap`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        로드맵
                      </Link>
                      <Link
                        to={`/project/${p.key}/releases`}
                        className="mr-3 text-indigo-400 hover:text-indigo-300"
                      >
                        릴리즈
                      </Link>
                      <Link
                        to={`/project/${p.key}/issues/new`}
                        className="text-indigo-400 hover:text-indigo-300"
                      >
                        새 이슈
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
