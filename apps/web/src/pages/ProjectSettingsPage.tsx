import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useProjectByKey, useProjects } from '../hooks/useProjects'
import {
  addProjectMember,
  deleteProject,
  fetchProjectById,
  fetchProjectMembers,
  removeProjectMember,
  updateProject,
} from '../lib/projectApi'
import { fetchUsers } from '../lib/userApi'
import type {
  ProjectDetail,
  ProjectMember,
  ProjectRole,
  ProjectUpdateBody,
  UserMin,
} from '../types/domain'

const ROLE_OPTIONS: { value: ProjectRole; label: string }[] = [
  { value: 'ADMIN', label: '관리자' },
  { value: 'DEVELOPER', label: '개발자' },
  { value: 'QA', label: 'QA' },
  { value: 'REPORTER', label: '리포터' },
  { value: 'VIEWER', label: '뷰어' },
]

function roleLabel(role: ProjectRole): string {
  return ROLE_OPTIONS.find((o) => o.value === role)?.label ?? role
}

export function ProjectSettingsPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)
  const { reload } = useProjects()
  const navigate = useNavigate()

  const [detail, setDetail] = useState<ProjectDetail | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [users, setUsers] = useState<UserMin[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [leadIdStr, setLeadIdStr] = useState('')
  const [saveMsg, setSaveMsg] = useState<string | null>(null)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [newUserId, setNewUserId] = useState('')
  const [newRole, setNewRole] = useState<ProjectRole>('DEVELOPER')
  const [memberError, setMemberError] = useState<string | null>(null)
  const [memberBusy, setMemberBusy] = useState(false)

  const [deleteBusy, setDeleteBusy] = useState(false)

  const load = useCallback(async () => {
    if (!project) return
    setLoading(true)
    setLoadError(null)
    try {
      const [d, m, u] = await Promise.all([
        fetchProjectById(project.id),
        fetchProjectMembers(project.id),
        fetchUsers(),
      ])
      setDetail(d)
      setMembers(m)
      setUsers(u)
      setName(d.name)
      setDescription(d.description ?? '')
      setLeadIdStr(d.leadId != null ? String(d.leadId) : '')
    } catch (e) {
      setDetail(null)
      setMembers([])
      setUsers([])
      setLoadError(
        e instanceof Error ? e.message : '설정을 불러오지 못했습니다',
      )
    } finally {
      setLoading(false)
    }
  }, [project])

  useEffect(() => {
    void load()
  }, [load])

  const memberUserIds = useMemo(
    () => new Set(members.map((m) => m.userId)),
    [members],
  )

  const addableUsers = useMemo(
    () => users.filter((u) => !memberUserIds.has(u.id)),
    [users, memberUserIds],
  )

  async function handleSaveBasic(e: React.FormEvent) {
    e.preventDefault()
    if (!project || !detail) return
    setSaveError(null)
    setSaveMsg(null)
    const trimmed = name.trim()
    if (!trimmed) {
      setSaveError('프로젝트 이름을 입력하세요.')
      return
    }
    setSaving(true)
    try {
      const body: ProjectUpdateBody = {
        name: trimmed,
        description: description.trim() === '' ? null : description.trim(),
      }
      if (leadIdStr) {
        body.leadId = Number(leadIdStr)
      }
      const updated = await updateProject(project.id, body)
      setDetail(updated)
      setName(updated.name)
      setDescription(updated.description ?? '')
      setLeadIdStr(updated.leadId != null ? String(updated.leadId) : '')
      await reload()
      setSaveMsg('저장했습니다.')
    } catch (err) {
      setSaveError(
        err instanceof Error ? err.message : '저장하지 못했습니다',
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleAddMember(e: React.FormEvent) {
    e.preventDefault()
    if (!project) return
    setMemberError(null)
    const uid = Number(newUserId)
    if (!newUserId || Number.isNaN(uid)) {
      setMemberError('추가할 사용자를 선택하세요.')
      return
    }
    setMemberBusy(true)
    try {
      const row = await addProjectMember(project.id, {
        userId: uid,
        role: newRole,
      })
      setMembers((prev) => [...prev, row].sort((a, b) => a.id - b.id))
      setNewUserId('')
      setNewRole('DEVELOPER')
    } catch (err) {
      setMemberError(
        err instanceof Error ? err.message : '멤버를 추가하지 못했습니다',
      )
    } finally {
      setMemberBusy(false)
    }
  }

  async function handleRemoveMember(member: ProjectMember) {
    if (!project) return
    if (
      !confirm(
        `${member.userName} (${member.userEmail}) 님을 프로젝트에서 제거할까요?`,
      )
    ) {
      return
    }
    setMemberError(null)
    setMemberBusy(true)
    try {
      await removeProjectMember(project.id, member.id)
      setMembers((prev) => prev.filter((m) => m.id !== member.id))
    } catch (err) {
      setMemberError(
        err instanceof Error ? err.message : '멤버를 제거하지 못했습니다',
      )
    } finally {
      setMemberBusy(false)
    }
  }

  async function handleDeleteProject() {
    if (!project || !detail) return
    if (
      !confirm(
        `프로젝트 "${detail.name}" (${detail.key})를 영구 삭제합니다. 계속할까요?`,
      )
    ) {
      return
    }
    setDeleteBusy(true)
    try {
      await deleteProject(project.id)
      await reload()
      navigate('/', { replace: true })
    } catch (err) {
      alert(
        err instanceof Error ? err.message : '프로젝트를 삭제하지 못했습니다',
      )
    } finally {
      setDeleteBusy(false)
    }
  }

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
  }

  if (!project) {
    return (
      <div className="space-y-3">
        <p className="text-slate-400">
          프로젝트 <span className="font-mono text-white">{projectKey}</span>{' '}
          을(를) 찾을 수 없거나 아카이브되었습니다.
        </p>
        <Link to="/" className="text-sm text-indigo-400 hover:text-indigo-300">
          대시보드로
        </Link>
      </div>
    )
  }

  if (loading) {
    return <p className="text-slate-400">불러오는 중…</p>
  }

  if (loadError || !detail) {
    return (
      <div className="space-y-3">
        <p className="text-red-400">{loadError ?? '데이터가 없습니다.'}</p>
        <button
          type="button"
          onClick={() => void load()}
          className="rounded-lg border border-slate-600 px-3 py-1.5 text-sm text-slate-200 hover:bg-slate-800"
        >
          다시 시도
        </button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl space-y-10">
      <div>
        <h1 className="text-xl font-semibold text-white">프로젝트 설정</h1>
        <p className="mt-1 font-mono text-sm text-indigo-300">{detail.key}</p>
        <p className="mt-2 text-sm text-slate-500">
          이름·설명·담당자를 수정하고, 멤버를 관리합니다. (멤버 추가·삭제·프로젝트
          삭제는 프로젝트 관리자만 가능합니다.)
        </p>
      </div>

      <section className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
          기본 정보
        </h2>
        <form onSubmit={handleSaveBasic} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-400">
              키 (읽기 전용)
            </label>
            <p className="mt-1 font-mono text-sm text-white">{detail.key}</p>
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400">
              보드 유형 (읽기 전용)
            </label>
            <p className="mt-1 text-sm text-slate-200">{detail.boardType}</p>
          </div>
          <div>
            <label
              htmlFor="proj-name"
              className="block text-xs font-medium text-slate-400"
            >
              이름
            </label>
            <input
              id="proj-name"
              value={name}
              onChange={(ev) => setName(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              required
            />
          </div>
          <div>
            <label
              htmlFor="proj-desc"
              className="block text-xs font-medium text-slate-400"
            >
              설명
            </label>
            <textarea
              id="proj-desc"
              value={description}
              onChange={(ev) => setDescription(ev.target.value)}
              rows={4}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            />
          </div>
          <div>
            <label
              htmlFor="proj-lead"
              className="block text-xs font-medium text-slate-400"
            >
              프로젝트 담당자
            </label>
            <select
              id="proj-lead"
              value={leadIdStr}
              onChange={(ev) => setLeadIdStr(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            >
              <option value="">— 담당자 변경 없이 저장 —</option>
              {users.map((u) => (
                <option key={u.id} value={String(u.id)}>
                  {u.name} ({u.email})
                </option>
              ))}
            </select>
            <p className="mt-1 text-xs text-slate-500">
              빈 값으로 저장하면 담당자는 그대로 둡니다. 다른 사용자를 선택하면
              담당자만 바뀝니다.
            </p>
          </div>
          {saveError ? (
            <p className="text-sm text-red-400">{saveError}</p>
          ) : null}
          {saveMsg ? (
            <p className="text-sm text-emerald-400">{saveMsg}</p>
          ) : null}
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
          >
            {saving ? '저장 중…' : '저장'}
          </button>
        </form>
      </section>

      <section className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-400">
          멤버
        </h2>
        {memberError ? (
          <p className="text-sm text-red-400">{memberError}</p>
        ) : null}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-800 text-slate-500">
                <th className="py-2 pr-4 font-medium">이름</th>
                <th className="py-2 pr-4 font-medium">이메일</th>
                <th className="py-2 pr-4 font-medium">역할</th>
                <th className="py-2 font-medium"> </th>
              </tr>
            </thead>
            <tbody>
              {members.map((m) => (
                <tr key={m.id} className="border-b border-slate-800/80">
                  <td className="py-2 pr-4 text-slate-200">{m.userName}</td>
                  <td className="py-2 pr-4 text-slate-400">{m.userEmail}</td>
                  <td className="py-2 pr-4 text-slate-300">
                    {roleLabel(m.role)}
                  </td>
                  <td className="py-2">
                    <button
                      type="button"
                      disabled={memberBusy}
                      onClick={() => void handleRemoveMember(m)}
                      className="text-xs text-red-400 hover:text-red-300 disabled:opacity-50"
                    >
                      제거
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {members.length === 0 ? (
            <p className="py-4 text-sm text-slate-500">멤버가 없습니다.</p>
          ) : null}
        </div>

        <form
          onSubmit={handleAddMember}
          className="flex flex-wrap items-end gap-3 border-t border-slate-800 pt-4"
        >
          <div className="min-w-[200px] flex-1">
            <label className="block text-xs font-medium text-slate-400">
              사용자 추가
            </label>
            <select
              value={newUserId}
              onChange={(ev) => setNewUserId(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            >
              <option value="">선택…</option>
              {addableUsers.map((u) => (
                <option key={u.id} value={String(u.id)}>
                  {u.name} ({u.email})
                </option>
              ))}
            </select>
          </div>
          <div className="w-40">
            <label className="block text-xs font-medium text-slate-400">
              역할
            </label>
            <select
              value={newRole}
              onChange={(ev) => setNewRole(ev.target.value as ProjectRole)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
            >
              {ROLE_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            disabled={memberBusy || addableUsers.length === 0}
            className="rounded-lg border border-indigo-600 px-4 py-2 text-sm font-medium text-indigo-300 hover:bg-indigo-950/50 disabled:opacity-50"
          >
            추가
          </button>
        </form>
      </section>

      <section className="space-y-4 rounded-xl border border-red-900/50 bg-red-950/20 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-red-300">
          위험 구역
        </h2>
        <p className="text-sm text-slate-400">
          프로젝트와 관련 데이터가 삭제될 수 있습니다. 관리자만 실행할 수 있습니다.
        </p>
        <button
          type="button"
          disabled={deleteBusy}
          onClick={() => void handleDeleteProject()}
          className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-500 disabled:opacity-50"
        >
          {deleteBusy ? '삭제 중…' : '프로젝트 삭제'}
        </button>
      </section>

      <Link
        to={`/project/${project.key}`}
        className="inline-block text-sm text-indigo-400 hover:text-indigo-300"
      >
        ← 프로젝트 개요
      </Link>
    </div>
  )
}
