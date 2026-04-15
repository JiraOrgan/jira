import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { BacklogBoard } from '../components/backlog/BacklogBoard'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import { fetchBacklog } from '../lib/issueApi'
import { fetchSprintsByProject } from '../lib/sprintApi'
import type { IssueMin, SprintMin } from '../types/domain'

export function BacklogPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [issues, setIssues] = useState<IssueMin[]>([])
  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const assignableSprints = useMemo(
    () => sprints.filter((s) => s.status !== 'COMPLETED'),
    [sprints],
  )

  const reload = useCallback(async () => {
    if (!project) return
    setLoadError(null)
    try {
      const [b, sp] = await Promise.all([
        fetchBacklog(project.id),
        fetchSprintsByProject(project.id),
      ])
      setIssues(b)
      setSprints(sp)
    } catch (e) {
      setLoadError(errorMessage(e) || '불러오기 실패')
      setIssues([])
      setSprints([])
    } finally {
      setLoading(false)
    }
  }, [project])

  useEffect(() => {
    if (!project) {
      setLoading(false)
      return
    }
    setLoading(true)
    void reload()
  }, [project, reload])

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
          <h1 className="text-xl font-semibold text-white">백로그</h1>
          <p className="mt-1 text-sm text-slate-400">
            스프린트 미배정 이슈를 드래그해 순서를 바꾸거나 스프린트에 넣습니다.{' '}
            <span className="text-slate-500">
              (순서·배정은 스프린트 관리 권한이 필요할 수 있습니다)
            </span>
          </p>
        </div>
        <Link
          to={`/project/${project.key}/issues/new`}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
        >
          새 이슈
        </Link>
      </div>

      {loading ? (
        <p className="text-sm text-slate-500">불러오는 중…</p>
      ) : loadError ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {loadError}
        </div>
      ) : (
        <BacklogBoard
          projectId={project.id}
          issues={issues}
          assignableSprints={assignableSprints}
          onReload={reload}
        />
      )}
    </div>
  )
}
