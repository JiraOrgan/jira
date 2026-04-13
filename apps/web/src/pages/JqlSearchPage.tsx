import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useProjectByKey } from '../hooks/useProjects'
import { errorMessage } from '../lib/axiosErrors'
import {
  deleteJqlFilter,
  listSavedJqlFilters,
  saveJqlFilter,
  searchJql,
} from '../lib/jqlApi'
import {
  buildJqlSuggestionPool,
  filterSuggestions,
  inUnterminatedString,
  insertSuggestion,
  lastFragment,
} from '../lib/jqlHints'
import {
  issueTypeLabel,
  priorityLabel,
  statusLabel,
} from '../lib/labels'
import { fetchSprintsByProject } from '../lib/sprintApi'
import type { JqlSearchResult, SavedJqlFilter, SprintMin } from '../types/domain'

const PAGE_SIZE = 25

export function JqlSearchPage() {
  const { projectKey } = useParams<{ projectKey: string }>()
  const project = useProjectByKey(projectKey)
  const taRef = useRef<HTMLTextAreaElement>(null)

  const [jql, setJql] = useState('')
  const [cursor, setCursor] = useState(0)
  const [sprints, setSprints] = useState<SprintMin[]>([])
  const [saved, setSaved] = useState<SavedJqlFilter[]>([])
  const [filterName, setFilterName] = useState('')
  const [result, setResult] = useState<JqlSearchResult | null>(null)
  const [startAt, setStartAt] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loadMeta, setLoadMeta] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const suggestionPool = useMemo(
    () =>
      project
        ? buildJqlSuggestionPool(project.key, sprints)
        : [],
    [project, sprints],
  )

  const fragment = useMemo(
    () => (inUnterminatedString(jql, cursor) ? '' : lastFragment(jql, cursor)),
    [jql, cursor],
  )

  const suggestions = useMemo(
    () => filterSuggestions(suggestionPool, fragment),
    [suggestionPool, fragment],
  )

  const reloadMeta = useCallback(async () => {
    if (!project) return
    setLoadMeta(true)
    setError(null)
    try {
      const [sp, filters] = await Promise.all([
        fetchSprintsByProject(project.id),
        listSavedJqlFilters(project.id),
      ])
      setSprints(sp)
      setSaved(filters)
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setLoadMeta(false)
    }
  }, [project])

  useEffect(() => {
    if (!project) {
      setJql('')
      setSprints([])
      setSaved([])
      return
    }
    setJql(`project = "${project.key}" AND type = TASK`)
    void reloadMeta()
  }, [project, reloadMeta])

  async function runSearch(nextStart = 0) {
    if (!project) return
    const q = jql.trim()
    if (!q) {
      setError('JQL을 입력하세요.')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const res = await searchJql(project.id, {
        jql: q,
        startAt: nextStart,
        maxResults: PAGE_SIZE,
      })
      setResult(res)
      setStartAt(nextStart)
    } catch (e) {
      setResult(null)
      setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function onSaveFilter() {
    if (!project) return
    const name = filterName.trim()
    const q = jql.trim()
    if (!name || !q) {
      setError('필터 이름과 JQL을 모두 채워 주세요.')
      return
    }
    setError(null)
    try {
      await saveJqlFilter(project.id, name, q)
      setFilterName('')
      await reloadMeta()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function onDeleteFilter(id: number) {
    if (!project) return
    if (!confirm('이 저장 필터를 삭제할까요?')) return
    setError(null)
    try {
      await deleteJqlFilter(project.id, id)
      await reloadMeta()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  function applySuggestion(s: string) {
    const el = taRef.current
    const pos = el?.selectionStart ?? cursor
    const { text, cursor: next } = insertSuggestion(jql, pos, s)
    setJql(text)
    setCursor(next)
    requestAnimationFrame(() => {
      if (!taRef.current) return
      taRef.current.focus()
      taRef.current.setSelectionRange(next, next)
    })
  }

  function loadSavedFilter(f: SavedJqlFilter) {
    setJql(f.jql)
    setResult(null)
    setStartAt(0)
  }

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

  const hasNext =
    result &&
    startAt + result.issues.length < result.total

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">JQL 검색</h1>
        <p className="mt-1 text-sm text-slate-400">
          PCH JQL로 이슈를 찾고, 저장 필터로 다시 불러올 수 있습니다. 입력창
          아래 제안을 누르면 커서 위치에 삽입됩니다.
        </p>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-900/50 bg-red-950/30 px-4 py-3 text-sm text-red-300">
          {error}
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-[1fr_280px]">
        <div className="space-y-3">
          <label className="block text-sm font-medium text-slate-300">
            JQL
            <textarea
              ref={taRef}
              value={jql}
              onChange={(e) => {
                setJql(e.target.value)
                setCursor(e.target.selectionStart)
              }}
              onSelect={(e) =>
                setCursor(e.currentTarget.selectionStart ?? cursor)
              }
              onKeyUp={(e) =>
                setCursor(e.currentTarget.selectionStart ?? cursor)
              }
              rows={5}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 font-mono text-sm text-slate-100 placeholder:text-slate-600 focus:border-indigo-500 focus:outline-none"
              placeholder='예: type = TASK AND text ~ "버그"'
              spellCheck={false}
            />
          </label>

          <div>
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              자동완성 제안
            </p>
            <div className="mt-2 flex max-h-40 flex-wrap gap-1.5 overflow-y-auto">
              {suggestions.map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => applySuggestion(s)}
                  className="rounded-md border border-slate-700 bg-slate-900/80 px-2 py-1 font-mono text-xs text-indigo-200 hover:border-indigo-600 hover:text-white"
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              disabled={loading}
              onClick={() => void runSearch(0)}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
            >
              {loading ? '검색 중…' : '검색'}
            </button>
            <span className="text-xs text-slate-500">
              페이지당 {PAGE_SIZE}건 · 서버 상한 적용
            </span>
          </div>

          {result ? (
            <div className="space-y-3">
              <p className="text-sm text-slate-400">
                총 <span className="text-slate-200">{result.total}</span>건
                중 {result.startAt + 1}–
                {result.startAt + result.issues.length}
              </p>
              <div className="overflow-hidden rounded-xl border border-slate-800">
                <table className="w-full text-left text-sm">
                  <thead className="border-b border-slate-800 bg-slate-900/80 text-xs uppercase text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">키</th>
                      <th className="px-4 py-3 font-medium">유형</th>
                      <th className="px-4 py-3 font-medium">요약</th>
                      <th className="px-4 py-3 font-medium">상태</th>
                      <th className="px-4 py-3 font-medium">우선순위</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800">
                    {result.issues.map((issue) => (
                      <tr key={issue.id} className="hover:bg-slate-900/40">
                        <td className="px-4 py-3 font-mono text-indigo-300">
                          <Link
                            to={`/issue/${encodeURIComponent(issue.issueKey)}`}
                            className="hover:underline"
                          >
                            {issue.issueKey}
                          </Link>
                        </td>
                        <td className="px-4 py-3 text-slate-400">
                          {issueTypeLabel[issue.issueType]}
                        </td>
                        <td className="px-4 py-3 text-slate-200">
                          {issue.summary}
                        </td>
                        <td className="px-4 py-3 text-slate-400">
                          {statusLabel[issue.status]}
                        </td>
                        <td className="px-4 py-3 text-slate-500">
                          {priorityLabel[issue.priority]}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={loading || startAt <= 0}
                  onClick={() => void runSearch(Math.max(0, startAt - PAGE_SIZE))}
                  className="rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-300 hover:border-slate-600 disabled:opacity-40"
                >
                  이전
                </button>
                <button
                  type="button"
                  disabled={loading || !hasNext}
                  onClick={() => void runSearch(startAt + PAGE_SIZE)}
                  className="rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-300 hover:border-slate-600 disabled:opacity-40"
                >
                  다음
                </button>
              </div>
            </div>
          ) : null}
        </div>

        <aside className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/40 p-4">
          <div>
            <h2 className="text-sm font-semibold text-white">저장 필터</h2>
            {loadMeta ? (
              <p className="mt-2 text-xs text-slate-500">불러오는 중…</p>
            ) : saved.length === 0 ? (
              <p className="mt-2 text-xs text-slate-500">저장된 필터가 없습니다.</p>
            ) : (
              <ul className="mt-2 space-y-2">
                {saved.map((f) => (
                  <li
                    key={f.id}
                    className="rounded-lg border border-slate-800 bg-slate-950/50 p-2"
                  >
                    <button
                      type="button"
                      onClick={() => loadSavedFilter(f)}
                      className="w-full text-left text-sm font-medium text-indigo-300 hover:text-indigo-200"
                    >
                      {f.name}
                    </button>
                    <p className="mt-1 line-clamp-2 font-mono text-[10px] text-slate-500">
                      {f.jql}
                    </p>
                    <button
                      type="button"
                      onClick={() => void onDeleteFilter(f.id)}
                      className="mt-1 text-xs text-red-400 hover:text-red-300"
                    >
                      삭제
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="border-t border-slate-800 pt-4">
            <h2 className="text-sm font-semibold text-white">현재 JQL 저장</h2>
            <input
              type="text"
              value={filterName}
              onChange={(e) => setFilterName(e.target.value)}
              placeholder="필터 이름"
              maxLength={200}
              className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-indigo-500 focus:outline-none"
            />
            <button
              type="button"
              onClick={() => void onSaveFilter()}
              className="mt-2 w-full rounded-lg border border-indigo-600 py-2 text-sm font-medium text-indigo-300 hover:bg-indigo-950/40"
            >
              저장
            </button>
          </div>
        </aside>
      </div>
    </div>
  )
}
