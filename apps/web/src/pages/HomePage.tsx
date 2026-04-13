import { Link } from 'react-router-dom'
import { useProjects } from '../hooks/useProjects'

export function HomePage() {
  const { projects, loading, error, reload } = useProjects()

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">대시보드</h1>
        <p className="mt-1 text-sm text-slate-400">
          참여 중인 프로젝트입니다. 키를 눌러 개요 또는 새 이슈로 이동합니다.
        </p>
      </div>

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
                  <td className="px-4 py-3 font-mono text-indigo-300">{p.key}</td>
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
    </div>
  )
}
