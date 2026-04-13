import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ScrumBoard } from '../components/scrum/ScrumBoard'
import { useProjectByKey } from '../hooks/useProjects'
import { fetchSprintBoard } from '../lib/boardApi'
import { fetchSprintsByProject } from '../lib/sprintApi'
import type { BoardSwimlane, SprintBoardData, SprintMin } from '../types/domain'

function defaultSprintId(sprints: SprintMin[]): number | null {
  const active = sprints.find((s) => s.status === 'ACTIVE')
  if (active) return active.id
  const plan = sprints.find((s) => s.status === 'PLANNING')
  if (plan) return plan.id
  return sprints[0]?.id ?? null
}

export function ScrumBoardPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)

  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [sprintId, setSprintId] = useState<number | null>(null)
  const [swimlane, setSwimlane] = useState<BoardSwimlane>('NONE')
  const [board, setBoard] = useState<SprintBoardData | null>(null)
  const [listError, setListError] = useState<string | null>(null)
  const [boardError, setBoardError] = useState<string | null>(null)
  const [sprintsLoading, setSprintsLoading] = useState(true)
  const [boardLoading, setBoardLoading] = useState(false)

  const reloadSprints = useCallback(async () => {
    if (!project) return
    setListError(null)
    setSprintsLoading(true)
    try {
      const sp = await fetchSprintsByProject(project.id)
      setSprints(sp)
      setSprintId((prev) => {
        if (prev != null && sp.some((s) => s.id === prev)) return prev
        return defaultSprintId(sp)
      })
    } catch (e) {
      setListError(
        e instanceof Error ? e.message : '스프린트 목록을 불러오지 못했습니다',
      )
      setSprints([])
      setSprintId(null)
    } finally {
      setSprintsLoading(false)
    }
  }, [project])

  useEffect(() => {
    if (!project) return
    void reloadSprints()
  }, [project, reloadSprints])

  useEffect(() => {
    if (sprintId == null) {
      setBoard(null)
      return
    }
    let cancelled = false
    setBoardLoading(true)
    setBoardError(null)
    void (async () => {
      try {
        const b = await fetchSprintBoard(sprintId, swimlane)
        if (!cancelled) setBoard(b)
      } catch (e) {
        if (!cancelled) {
          setBoardError(
            e instanceof Error ? e.message : '보드를 불러오지 못했습니다',
          )
          setBoard(null)
        }
      } finally {
        if (!cancelled) setBoardLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [sprintId, swimlane])

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
          <h1 className="text-xl font-semibold text-white">스크럼 보드</h1>
          <p className="mt-1 text-sm text-slate-400">
            스프린트 이슈를 컬럼(상태) 사이로 드래그하면 워크플로 전환이
            적용됩니다.
          </p>
          {project.boardType === 'KANBAN' ? (
            <p className="mt-2 text-xs text-amber-400/90">
              이 프로젝트는 칸반 유형입니다. 스프린트가 있으면 동일 보드 API로
              조회합니다.
            </p>
          ) : null}
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <Link
            to={`/project/${project.key}/backlog`}
            className="text-sm text-indigo-400 hover:text-indigo-300"
          >
            백로그
          </Link>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <label className="flex items-center gap-2 text-sm text-slate-300">
          스프린트
          <select
            value={sprintId ?? ''}
            onChange={(e) => {
              const v = e.target.value
              setSprintId(v ? Number(v) : null)
            }}
            disabled={sprintsLoading || sprints.length === 0}
            className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-1.5 text-sm text-white"
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
        </label>
        <label className="flex items-center gap-2 text-sm text-slate-300">
          스윔레인
          <select
            value={swimlane}
            onChange={(e) => setSwimlane(e.target.value as BoardSwimlane)}
            disabled={boardLoading}
            className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-1.5 text-sm text-white"
          >
            <option value="NONE">없음</option>
            <option value="ASSIGNEE">담당자</option>
          </select>
        </label>
        <button
          type="button"
          onClick={() => void reloadSprints()}
          className="text-sm text-slate-400 hover:text-white"
        >
          새로고침
        </button>
      </div>

      {listError ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {listError}
        </div>
      ) : null}

      {sprintsLoading ? (
        <p className="text-sm text-slate-500">스프린트 불러오는 중…</p>
      ) : sprints.length === 0 ? (
        <p className="text-sm text-slate-500">
          스프린트가 없습니다. 스프린트를 만든 뒤 이슈를 배정하세요.
        </p>
      ) : boardLoading ? (
        <p className="text-sm text-slate-500">보드 불러오는 중…</p>
      ) : boardError ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {boardError}
        </div>
      ) : board && sprintId != null ? (
        <ScrumBoard
          sprintId={sprintId}
          swimlane={swimlane}
          board={board}
          onBoardChange={setBoard}
        />
      ) : null}
    </div>
  )
}
