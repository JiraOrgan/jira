import { type FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import { createIssue } from '../lib/issueApi'
import {
  ISSUE_TYPES,
  PRIORITIES,
  SECURITY_LEVELS,
  STORY_POINT_OPTIONS,
  issueTypeLabel,
  priorityLabel,
  securityLabel,
} from '../lib/labels'
import { fetchSprintsByProject } from '../lib/sprintApi'
import { fetchUsers } from '../lib/userApi'
import type {
  IssueSaveBody,
  IssueType,
  Priority,
  SecurityLevel,
  SprintMin,
  UserMin,
} from '../types/domain'

export function IssueCreatePage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)
  const navigate = useNavigate()

  const [users, setUsers] = useState<UserMin[]>([])
  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [issueType, setIssueType] = useState<IssueType>('TASK')
  const [summary, setSummary] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<Priority>('MEDIUM')
  const [storyPoints, setStoryPoints] = useState<string>('')
  const [assigneeId, setAssigneeId] = useState<string>('')
  const [parentId, setParentId] = useState<string>('')
  const [sprintId, setSprintId] = useState<string>('')
  const [securityLevel, setSecurityLevel] = useState<string>('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const u = await fetchUsers()
        if (cancelled) return
        setUsers(u)
        if (project?.id) {
          const sp = await fetchSprintsByProject(project.id)
          if (cancelled) return
          setSprints(
            sp.filter(
              (x) => x.status === 'PLANNING' || x.status === 'ACTIVE',
            ),
          )
        } else {
          setSprints([])
        }
      } catch {
        if (!cancelled) setUsers([])
      }
    })()
    return () => {
      cancelled = true
    }
  }, [project?.id])

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

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!project) return
    setError(null)
    setLoading(true)
    try {
      const body: IssueSaveBody = {
        projectId: project.id,
        issueType,
        summary: summary.trim(),
        priority,
      }
      const desc = description.trim()
      if (desc) body.description = desc
      if (storyPoints) {
        const n = Number(storyPoints)
        if (!Number.isNaN(n)) body.storyPoints = n
      }
      if (assigneeId) body.assigneeId = Number(assigneeId)
      if (parentId) body.parentId = Number(parentId)
      if (sprintId) body.sprintId = Number(sprintId)
      if (securityLevel) body.securityLevel = securityLevel as SecurityLevel

      const created = await createIssue(body)
      navigate(`/issue/${encodeURIComponent(created.issueKey)}`, {
        replace: true,
      })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">새 이슈</h1>
        <p className="mt-1 text-sm text-slate-400">
          프로젝트{' '}
          <span className="font-mono text-indigo-300">{project.key}</span>
        </p>
      </div>

      <form className="space-y-5" onSubmit={onSubmit}>
        <div>
          <label className="text-xs font-medium text-slate-400">유형</label>
          <select
            value={issueType}
            onChange={(e) => setIssueType(e.target.value as IssueType)}
            className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          >
            {ISSUE_TYPES.map((t) => (
              <option key={t} value={t}>
                {issueTypeLabel[t]}
              </option>
            ))}
          </select>
          {issueType === 'BUG' ? (
            <p className="mt-1 text-xs text-amber-400/90">
              Bug는 설명에 재현 절차·기대/실제 결과·환경을 적어 두면 좋습니다.
            </p>
          ) : null}
        </div>

        <div>
          <label className="text-xs font-medium text-slate-400">요약 *</label>
          <input
            required
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          />
        </div>

        <div>
          <label className="text-xs font-medium text-slate-400">설명</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-xs font-medium text-slate-400">우선순위</label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as Priority)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            >
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>
                  {priorityLabel[p]}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-slate-400">
              스토리 포인트
            </label>
            <select
              value={storyPoints}
              onChange={(e) => setStoryPoints(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            >
              <option value="">—</option>
              {STORY_POINT_OPTIONS.map((n) => (
                <option key={n} value={String(n)}>
                  {n}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-xs font-medium text-slate-400">담당자</label>
            <select
              value={assigneeId}
              onChange={(e) => setAssigneeId(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            >
              <option value="">—</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} ({u.email})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-slate-400">스프린트</label>
            <select
              value={sprintId}
              onChange={(e) => setSprintId(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            >
              <option value="">백로그(미배정)</option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.status})
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-xs font-medium text-slate-400">
              부모 이슈 ID
            </label>
            <input
              type="number"
              min={1}
              value={parentId}
              onChange={(e) => setParentId(e.target.value)}
              placeholder="선택"
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            />
          </div>
          <div>
            <label className="text-xs font-medium text-slate-400">
              보안 레벨
            </label>
            <select
              value={securityLevel}
              onChange={(e) => setSecurityLevel(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            >
              <option value="">기본</option>
              {SECURITY_LEVELS.map((s) => (
                <option key={s} value={s}>
                  {securityLabel[s]}
                </option>
              ))}
            </select>
          </div>
        </div>

        {error ? (
          <p className="rounded-lg bg-red-950/50 px-3 py-2 text-sm text-red-300">
            {error}
          </p>
        ) : null}

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={loading}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
          >
            {loading ? '생성 중…' : '이슈 생성'}
          </button>
          <Link
            to={`/project/${project.key}`}
            className="rounded-lg border border-slate-700 px-4 py-2 text-sm text-slate-300 hover:border-slate-600"
          >
            취소
          </Link>
        </div>
      </form>
    </div>
  )
}
