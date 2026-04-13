import {
  NavLink,
  Outlet,
  useMatch,
  useNavigate,
  useParams,
} from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

const navCls = ({ isActive }: { isActive: boolean }) =>
  [
    'block rounded-lg px-3 py-2 text-sm transition',
    isActive
      ? 'bg-slate-800 text-white'
      : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200',
  ].join(' ')

export function AppLayout() {
  const navigate = useNavigate()
  const clear = useAuthStore((s) => s.clear)
  const { projectKey } = useParams<{ projectKey?: string }>()
  const issueMatch = useMatch('/issue/:issueKey')
  const headerIssueKey = issueMatch?.params.issueKey
    ? decodeURIComponent(issueMatch.params.issueKey)
    : null

  function logout() {
    clear()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100">
      <aside className="flex w-56 shrink-0 flex-col border-r border-slate-800 bg-slate-900/50">
        <div className="border-b border-slate-800 px-4 py-4">
          <NavLink to="/" className="text-base font-semibold text-white">
            PCH
          </NavLink>
          <p className="mt-0.5 text-xs text-slate-500">Project Control Hub</p>
        </div>
        <nav className="flex flex-1 flex-col gap-1 p-3">
          <NavLink to="/" end className={navCls}>
            대시보드
          </NavLink>
          {projectKey ? (
            <>
              <div className="my-2 border-t border-slate-800" />
              <p className="px-3 text-[10px] font-medium uppercase tracking-wider text-slate-500">
                프로젝트 {projectKey}
              </p>
              <NavLink
                to={`/project/${projectKey}`}
                className={navCls}
              >
                개요
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/settings`}
                className={navCls}
              >
                설정
              </NavLink>
              <NavLink to={`/project/${projectKey}/board`} className={navCls}>
                스크럼 보드
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/kanban`}
                className={navCls}
              >
                칸반
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/sprints`}
                className={navCls}
              >
                스프린트
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/backlog`}
                className={navCls}
              >
                백로그
              </NavLink>
              <NavLink to={`/project/${projectKey}/jql`} className={navCls}>
                JQL
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/roadmap`}
                className={navCls}
              >
                로드맵
              </NavLink>
              <NavLink
                to={`/project/${projectKey}/issues/new`}
                className={navCls}
              >
                새 이슈
              </NavLink>
            </>
          ) : null}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-14 shrink-0 items-center justify-between border-b border-slate-800 px-6">
          <div className="text-sm text-slate-400">
            {headerIssueKey ? (
              <span>
                <span className="text-slate-500">이슈</span>{' '}
                <span className="font-mono font-medium text-slate-200">
                  {headerIssueKey}
                </span>
              </span>
            ) : projectKey ? (
              <span>
                <span className="text-slate-500">프로젝트</span>{' '}
                <span className="font-medium text-slate-200">{projectKey}</span>
              </span>
            ) : (
              <span className="text-slate-500">프로젝트를 선택하세요</span>
            )}
          </div>
          <button
            type="button"
            onClick={logout}
            className="text-sm text-slate-400 hover:text-white"
          >
            로그아웃
          </button>
        </header>
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
