import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useProjects } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import {
  connectGithubRepo,
  disconnectGithub,
  fetchGithubIntegrationStatus,
  fetchGithubOAuthAuthorizeUrl,
} from '../lib/githubIntegrationApi'
import {
  addProjectMember,
  deleteProject,
  fetchProjectByKey,
  fetchProjectMembers,
  removeProjectMember,
  runProjectAutoArchiveDone,
  updateProject,
} from '../lib/projectApi'
import { fetchUsers } from '../lib/userApi'
import type {
  GithubIntegrationStatus,
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
  const { projectKey: rawProjectKey } = useParams<{ projectKey: string }>()
  const projectKey = rawProjectKey ? decodeURIComponent(rawProjectKey) : ''
  const [searchParams, setSearchParams] = useSearchParams()
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

  const [archived, setArchived] = useState(false)
  const [autoArchiveDaysStr, setAutoArchiveDaysStr] = useState('')
  const [archiveRunBusy, setArchiveRunBusy] = useState(false)
  const [archiveRunMsg, setArchiveRunMsg] = useState<string | null>(null)

  const [githubStatus, setGithubStatus] = useState<GithubIntegrationStatus | null>(
    null,
  )
  const [githubStatusError, setGithubStatusError] = useState<string | null>(null)
  const [githubRepoInput, setGithubRepoInput] = useState('')
  const [githubBusy, setGithubBusy] = useState(false)
  const [githubMsg, setGithubMsg] = useState<string | null>(null)
  const [githubErr, setGithubErr] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!projectKey) return
    setLoading(true)
    setLoadError(null)
    try {
      const d = await fetchProjectByKey(projectKey)
      const [m, u] = await Promise.all([
        fetchProjectMembers(d.id),
        fetchUsers(),
      ])
      setDetail(d)
      setMembers(m)
      setUsers(u)
      setName(d.name)
      setDescription(d.description ?? '')
      setLeadIdStr(d.leadId != null ? String(d.leadId) : '')
      setArchived(d.archived)
      setAutoArchiveDaysStr(
        d.autoArchiveDoneAfterDays != null ? String(d.autoArchiveDoneAfterDays) : '',
      )
      setArchiveRunMsg(null)
      setGithubMsg(null)
      setGithubErr(null)
      setGithubStatusError(null)
      try {
        const gh = await fetchGithubIntegrationStatus(d.id)
        setGithubStatus(gh)
      } catch (e) {
        setGithubStatus(null)
        setGithubStatusError(errorMessage(e))
      }
    } catch (e) {
      setDetail(null)
      setMembers([])
      setUsers([])
      setLoadError(errorMessage(e) || '설정을 불러오지 못했습니다')
    } finally {
      setLoading(false)
    }
  }, [projectKey])

  useEffect(() => {
    void load()
  }, [load])

  useEffect(() => {
    const oauth = searchParams.get('github_oauth')
    if (oauth === 'ok') {
      setGithubErr(null)
      setGithubMsg('GitHub OAuth 연결이 완료되었습니다. 저장소 이름을 등록하면 웹훅이 생성됩니다.')
      searchParams.delete('github_oauth')
      setSearchParams(searchParams, { replace: true })
    }
  }, [searchParams, setSearchParams])

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
    if (!detail) return
    setSaveError(null)
    setSaveMsg(null)
    const trimmed = name.trim()
    if (!trimmed) {
      setSaveError('프로젝트 이름을 입력하세요.')
      return
    }
    const advTrim = autoArchiveDaysStr.trim()
    let autoArchiveDoneAfterDays = 0
    if (advTrim !== '') {
      autoArchiveDoneAfterDays = parseInt(advTrim, 10)
      if (!Number.isFinite(autoArchiveDoneAfterDays) || autoArchiveDoneAfterDays < 0) {
        setSaveError('DONE 자동 아카이브 일수는 0 이상 정수이거나 비워 비활성만 가능합니다.')
        return
      }
    }
    setSaving(true)
    try {
      const body: ProjectUpdateBody = {
        name: trimmed,
        description: description.trim() === '' ? null : description.trim(),
        archived,
        autoArchiveDoneAfterDays,
      }
      if (leadIdStr) {
        body.leadId = Number(leadIdStr)
      }
      const updated = await updateProject(detail.id, body)
      setDetail(updated)
      setName(updated.name)
      setDescription(updated.description ?? '')
      setLeadIdStr(updated.leadId != null ? String(updated.leadId) : '')
      setArchived(updated.archived)
      setAutoArchiveDaysStr(
        updated.autoArchiveDoneAfterDays != null
          ? String(updated.autoArchiveDoneAfterDays)
          : '',
      )
      await reload()
      setSaveMsg('저장했습니다.')
    } catch (err) {
      setSaveError(
        errorMessage(err) || '저장하지 못했습니다',
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleRunAutoArchiveDone() {
    if (!detail) return
    setArchiveRunMsg(null)
    setArchiveRunBusy(true)
    try {
      const n = await runProjectAutoArchiveDone(detail.id)
      setArchiveRunMsg(`아카이브 처리된 DONE 이슈: ${n}건`)
    } catch (err) {
      setArchiveRunMsg(
        errorMessage(err) || '자동 아카이브 실행에 실패했습니다',
      )
    } finally {
      setArchiveRunBusy(false)
    }
  }

  async function handleAddMember(e: React.FormEvent) {
    e.preventDefault()
    if (!detail) return
    setMemberError(null)
    const uid = Number(newUserId)
    if (!newUserId || Number.isNaN(uid)) {
      setMemberError('추가할 사용자를 선택하세요.')
      return
    }
    setMemberBusy(true)
    try {
      const row = await addProjectMember(detail.id, {
        userId: uid,
        role: newRole,
      })
      setMembers((prev) => [...prev, row].sort((a, b) => a.id - b.id))
      setNewUserId('')
      setNewRole('DEVELOPER')
    } catch (err) {
      setMemberError(
        errorMessage(err) || '멤버를 추가하지 못했습니다',
      )
    } finally {
      setMemberBusy(false)
    }
  }

  async function handleRemoveMember(member: ProjectMember) {
    if (!detail) return
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
      await removeProjectMember(detail.id, member.id)
      setMembers((prev) => prev.filter((m) => m.id !== member.id))
    } catch (err) {
      setMemberError(
        errorMessage(err) || '멤버를 제거하지 못했습니다',
      )
    } finally {
      setMemberBusy(false)
    }
  }

  async function handleGithubOAuthStart() {
    if (!detail) return
    setGithubMsg(null)
    setGithubErr(null)
    setGithubBusy(true)
    try {
      const url = await fetchGithubOAuthAuthorizeUrl(detail.id)
      window.location.assign(url)
    } catch (e) {
      setGithubErr(errorMessage(e))
    } finally {
      setGithubBusy(false)
    }
  }

  async function handleGithubConnectRepo(e: React.FormEvent) {
    e.preventDefault()
    if (!detail) return
    const name = githubRepoInput.trim()
    if (!name) {
      setGithubErr('owner/repo 형식으로 입력하세요.')
      return
    }
    setGithubMsg(null)
    setGithubErr(null)
    setGithubBusy(true)
    try {
      await connectGithubRepo(detail.id, name)
      const st = await fetchGithubIntegrationStatus(detail.id)
      setGithubStatus(st)
      setGithubRepoInput('')
      setGithubMsg('GitHub 저장소와 웹훅을 등록했습니다.')
    } catch (e) {
      setGithubErr(errorMessage(e))
    } finally {
      setGithubBusy(false)
    }
  }

  async function handleGithubDisconnect() {
    if (!detail) return
    if (!confirm('GitHub 연동과 웹훅을 제거할까요?')) return
    setGithubMsg(null)
    setGithubErr(null)
    setGithubBusy(true)
    try {
      await disconnectGithub(detail.id)
      setGithubStatus({
        oauthComplete: false,
        githubRepoFullName: null,
        githubWebhookId: null,
      })
      setGithubMsg('연동을 해제했습니다.')
    } catch (e) {
      setGithubErr(errorMessage(e))
    } finally {
      setGithubBusy(false)
    }
  }

  async function handleDeleteProject() {
    if (!detail) return
    if (
      !confirm(
        `프로젝트 "${detail.name}" (${detail.key})를 영구 삭제합니다. 계속할까요?`,
      )
    ) {
      return
    }
    setDeleteBusy(true)
    try {
      await deleteProject(detail.id)
      await reload()
      navigate('/', { replace: true })
    } catch (err) {
      alert(
        errorMessage(err) || '프로젝트를 삭제하지 못했습니다',
      )
    } finally {
      setDeleteBusy(false)
    }
  }

  if (!projectKey) {
    return <p className="text-slate-500">잘못된 경로입니다.</p>
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
          이름·설명·담당자·아카이브·DONE 자동 아카이브 규칙을 수정하고, 멤버를 관리합니다.
          (멤버 추가·삭제·프로젝트 삭제는 프로젝트 관리자만 가능합니다.)
        </p>
        {detail.archived ? (
          <p className="mt-3 rounded-lg border border-amber-800/60 bg-amber-950/30 px-3 py-2 text-sm text-amber-200">
            아카이브된 프로젝트입니다. 사이드바 목록에는 나타나지 않으며, URL로 이 설정
            화면에만 진입할 수 있습니다.
          </p>
        ) : null}
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
          <div className="flex items-start gap-3 rounded-lg border border-slate-800 bg-slate-950/50 p-3">
            <input
              id="proj-archived"
              type="checkbox"
              checked={archived}
              onChange={(ev) => setArchived(ev.target.checked)}
              className="mt-1 h-4 w-4 rounded border-slate-600 bg-slate-950 text-indigo-600"
            />
            <div>
              <label
                htmlFor="proj-archived"
                className="text-sm font-medium text-slate-200"
              >
                프로젝트 아카이브
              </label>
              <p className="mt-1 text-xs text-slate-500">
                체크 후 저장하면 대시보드·사이드바 비아카이브 목록에서 숨깁니다. 해제 후
                저장으로 다시 표시할 수 있습니다.
              </p>
            </div>
          </div>
          <div className="space-y-3 rounded-lg border border-slate-800 bg-slate-950/50 p-3">
            <div>
              <label
                htmlFor="proj-auto-arch-days"
                className="text-sm font-medium text-slate-200"
              >
                DONE 이슈 자동 아카이브 (일)
              </label>
              <p className="mt-1 text-xs text-slate-500">
                DONE 상태이며 마지막 갱신일로부터 이 일수가 지난 이슈를 아카이브합니다.
                비우면 비활성입니다. 저장한 뒤 아래 &quot;지금 실행&quot;으로 즉시 일괄
                처리할 수 있습니다.
              </p>
              <input
                id="proj-auto-arch-days"
                type="number"
                min={0}
                step={1}
                value={autoArchiveDaysStr}
                onChange={(ev) => setAutoArchiveDaysStr(ev.target.value)}
                placeholder="비활성"
                className="mt-2 w-40 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white outline-none focus:border-indigo-500"
              />
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <button
                type="button"
                disabled={archiveRunBusy}
                onClick={() => void handleRunAutoArchiveDone()}
                className="rounded-lg border border-slate-600 px-3 py-1.5 text-sm text-slate-200 hover:bg-slate-800 disabled:opacity-50"
              >
                {archiveRunBusy ? '실행 중…' : 'DONE 자동 아카이브 지금 실행'}
              </button>
              {archiveRunMsg ? (
                <p className="text-sm text-slate-400">{archiveRunMsg}</p>
              ) : null}
            </div>
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
          외부 연동 (GitHub)
        </h2>
        <p className="text-sm text-slate-500">
          프로젝트 관리자만 설정할 수 있습니다. OAuth로 토큰을 받은 뒤 저장소
          (owner/repo)를 등록하면 해당 저장소에 웹훅이 생성되고, 커밋·PR 메시지에
          이슈 키가 포함되면 이슈 상세에 VCS 링크가 자동으로 쌓입니다.
        </p>
        {githubStatusError ? (
          <p className="text-sm text-amber-300/90">
            연동 상태를 불러오지 못했습니다 (관리자만 조회 가능할 수 있습니다).{' '}
            <span className="text-slate-500">{githubStatusError}</span>
          </p>
        ) : null}
        {githubStatus ? (
          <div className="space-y-3 text-sm text-slate-300">
            <p>
              OAuth:{' '}
              <span className="font-medium text-white">
                {githubStatus.oauthComplete ? '연결됨' : '미연결'}
              </span>
            </p>
            {githubStatus.githubRepoFullName ? (
              <p>
                저장소:{' '}
                <span className="font-mono text-indigo-300">
                  {githubStatus.githubRepoFullName}
                </span>
              </p>
            ) : null}
            {githubErr ? (
              <p className="text-sm text-red-400">{githubErr}</p>
            ) : null}
            {githubMsg ? (
              <p className="text-sm text-emerald-400/90">{githubMsg}</p>
            ) : null}
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                disabled={githubBusy}
                onClick={() => void handleGithubOAuthStart()}
                className="rounded-lg border border-slate-600 px-3 py-1.5 text-sm text-slate-200 hover:bg-slate-800 disabled:opacity-50"
              >
                GitHub OAuth 시작
              </button>
              {githubStatus.oauthComplete && githubStatus.githubRepoFullName ? (
                <button
                  type="button"
                  disabled={githubBusy}
                  onClick={() => void handleGithubDisconnect()}
                  className="rounded-lg border border-red-800/60 px-3 py-1.5 text-sm text-red-300 hover:bg-red-950/30 disabled:opacity-50"
                >
                  연동 해제
                </button>
              ) : null}
            </div>
            <form
              onSubmit={handleGithubConnectRepo}
              className="flex flex-col gap-2 border-t border-slate-800 pt-4 sm:flex-row sm:items-end"
            >
              <div className="min-w-[200px] flex-1">
                <label className="block text-xs font-medium text-slate-400">
                  GitHub 저장소 (owner/repo)
                </label>
                <input
                  value={githubRepoInput}
                  onChange={(ev) => setGithubRepoInput(ev.target.value)}
                  placeholder="예: octocat/Hello-World"
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 font-mono text-sm text-white outline-none focus:border-indigo-500"
                />
              </div>
              <button
                type="submit"
                disabled={githubBusy || !githubStatus.oauthComplete}
                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
              >
                웹훅 등록
              </button>
            </form>
          </div>
        ) : null}
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
        to={`/project/${detail.key}`}
        className="inline-block text-sm text-indigo-400 hover:text-indigo-300"
      >
        ← 프로젝트 개요
      </Link>
    </div>
  )
}
