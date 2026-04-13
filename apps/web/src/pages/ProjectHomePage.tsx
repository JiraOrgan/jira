import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'

export function ProjectHomePage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
  }

  if (!project) {
    return (
      <div className="space-y-3">
        <p className="text-slate-400">
          프로젝트 <span className="font-mono text-white">{projectKey}</span> 을(를)
          찾을 수 없거나 아카이브되었습니다.
        </p>
        <Link to="/" className="text-sm text-indigo-400 hover:text-indigo-300">
          대시보드로
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">{project.name}</h1>
        <p className="mt-1 font-mono text-sm text-indigo-300">{project.key}</p>
        <p className="mt-2 text-sm text-slate-400">
          보드 유형: {project.boardType}
        </p>
      </div>
      <div className="flex flex-wrap gap-3">
        <Link
          to={`/project/${project.key}/board`}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
        >
          스크럼 보드
        </Link>
        <Link
          to={`/project/${project.key}/kanban`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          칸반
        </Link>
        <Link
          to={`/project/${project.key}/sprints`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          스프린트
        </Link>
        <Link
          to={`/project/${project.key}/backlog`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          백로그
        </Link>
        <Link
          to={`/project/${project.key}/jql`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          JQL 검색
        </Link>
        <Link
          to={`/project/${project.key}/roadmap`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          로드맵
        </Link>
        <Link
          to={`/project/${project.key}/settings`}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:border-slate-600"
        >
          설정
        </Link>
        <Link
          to={`/project/${project.key}/issues/new`}
          className="rounded-lg border border-indigo-600 px-4 py-2 text-sm font-medium text-indigo-300 hover:bg-indigo-950/50"
        >
          새 이슈 만들기
        </Link>
        <Link
          to="/"
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm text-slate-300 hover:border-slate-600"
        >
          대시보드
        </Link>
      </div>
    </div>
  )
}
