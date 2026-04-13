import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../lib/axiosErrors'
import { fetchProjectAuditLogs } from '../lib/auditApi'
import { useProjectByKey } from '../hooks/useProjects'
import type { AuditLogRow, SpringPage } from '../types/domain'

function formatCell(v: string | null): string {
  if (v == null || v === '') return '—'
  if (v.length > 80) return `${v.slice(0, 80)}…`
  return v
}

export function ProjectAuditPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)
  const [page, setPage] = useState(0)
  const [data, setData] = useState<SpringPage<AuditLogRow> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!project) return
    setLoading(true)
    setError(null)
    try {
      const p = await fetchProjectAuditLogs(project.id, page, 30)
      setData(p)
    } catch (e) {
      setData(null)
      setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [project, page])

  useEffect(() => {
    void load()
  }, [load])

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
  }

  if (!project) {
    return (
      <p className="text-slate-400">
        프로젝트 <span className="font-mono text-white">{projectKey}</span> 을(를)
        찾을 수 없습니다.
      </p>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">감사 로그</h1>
        <p className="mt-1 text-sm text-slate-400">
          프로젝트 <span className="font-mono text-indigo-300">{project.key}</span>{' '}
          이슈 필드 변경 이력입니다. 프로젝트 ADMIN만 조회할 수 있습니다.
        </p>
      </div>

      {loading ? (
        <p className="text-slate-500">불러오는 중…</p>
      ) : error ? (
        <div className="rounded-lg border border-amber-900/60 bg-amber-950/40 px-4 py-3 text-sm text-amber-200">
          {error}
        </div>
      ) : data && data.content.length === 0 ? (
        <p className="text-slate-500">기록된 감사 로그가 없습니다.</p>
      ) : data ? (
        <>
          <div className="overflow-x-auto rounded-lg border border-slate-800">
            <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
              <thead className="bg-slate-900/80 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-3 py-2">시각</th>
                  <th className="px-3 py-2">이슈</th>
                  <th className="px-3 py-2">필드</th>
                  <th className="px-3 py-2">변경자</th>
                  <th className="px-3 py-2">이전 값</th>
                  <th className="px-3 py-2">새 값</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-200">
                {data.content.map((row) => (
                  <tr key={row.id} className="hover:bg-slate-900/40">
                    <td className="whitespace-nowrap px-3 py-2 font-mono text-xs text-slate-400">
                      {row.changedAt}
                    </td>
                    <td className="px-3 py-2">
                      {row.issueKey ? (
                        <Link
                          to={`/issue/${encodeURIComponent(row.issueKey)}`}
                          className="font-mono text-indigo-400 hover:text-indigo-300"
                        >
                          {row.issueKey}
                        </Link>
                      ) : (
                        <span className="text-slate-500">—</span>
                      )}
                    </td>
                    <td className="px-3 py-2 text-slate-300">{row.fieldName}</td>
                    <td className="px-3 py-2 text-slate-400">
                      {row.changedByName ?? row.changedById}
                    </td>
                    <td className="max-w-[200px] px-3 py-2 text-slate-500">
                      {formatCell(row.oldValue)}
                    </td>
                    <td className="max-w-[200px] px-3 py-2 text-slate-300">
                      {formatCell(row.newValue)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex items-center justify-between text-sm text-slate-400">
            <span>
              총 {data.totalElements}건 · {data.number + 1} /{' '}
              {Math.max(1, data.totalPages)} 페이지
            </span>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={data.number <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="rounded-lg border border-slate-700 px-3 py-1 text-slate-200 enabled:hover:bg-slate-800 disabled:opacity-40"
              >
                이전
              </button>
              <button
                type="button"
                disabled={data.number + 1 >= data.totalPages}
                onClick={() => setPage((p) => p + 1)}
                className="rounded-lg border border-slate-700 px-3 py-1 text-slate-200 enabled:hover:bg-slate-800 disabled:opacity-40"
              >
                다음
              </button>
            </div>
          </div>
        </>
      ) : null}
    </div>
  )
}
