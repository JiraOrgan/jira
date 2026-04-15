import type { IssueStatus, SprintMin } from '../types/domain'
import { ISSUE_TYPES, PRIORITIES, statusLabel } from './labels'

const ISSUE_STATUSES = Object.keys(statusLabel) as IssueStatus[]

const ORDER_SNIPPETS = [
  'ORDER BY created DESC',
  'ORDER BY updated DESC',
  'ORDER BY key ASC',
  'ORDER BY priority DESC',
]

const WHERE_FIELDS = [
  'project',
  'status',
  'type',
  'assignee',
  'priority',
  'sprint',
  'text',
  'archived',
]

const KEYWORDS = ['AND', 'OR', 'IN', 'IS', 'EMPTY']

/** 커서 앞 따옴표가 홀수면 문자열 리터럴 안으로 간주 */
export function inUnterminatedString(text: string, cursor: number): boolean {
  let inStr = false
  for (let i = 0; i < cursor; i++) {
    const c = text[i]
    if (c === '"' && (i === 0 || text[i - 1] !== '\\')) {
      inStr = !inStr
    }
  }
  return inStr
}

export function lastFragment(text: string, cursor: number): string {
  const left = text.slice(0, cursor)
  const m = left.match(/[^\s]+$/u)
  return m ? m[0] : ''
}

function isOperatorSnippet(s: string): boolean {
  const t = s.trim()
  return (
    t === '=' ||
    t === '!=' ||
    t === '~' ||
    t === 'IS EMPTY' ||
    t === 'IN' ||
    t.startsWith('ORDER BY')
  )
}

export function insertSuggestion(
  text: string,
  cursor: number,
  suggestion: string,
): { text: string; cursor: number } {
  const left = text.slice(0, cursor)
  const right = text.slice(cursor)
  if (isOperatorSnippet(suggestion)) {
    const needsSpace = left.length > 0 && !/\s$/.test(left)
    let ins = (needsSpace ? ' ' : '') + suggestion
    if (!/\s$/.test(ins)) ins += ' '
    const next = left + ins + right
    return { text: next, cursor: left.length + ins.length }
  }
  const m = left.match(/[^\s]*$/u)
  const last = m?.[0] ?? ''
  const before = left.slice(0, left.length - last.length)
  const spacer =
    before.length > 0 && !/\s$/.test(before) && !/[=(,]$/.test(before.trimEnd())
      ? ' '
      : ''
  let ins = spacer + suggestion
  if (!/\s$/.test(ins) && !/[=(,]$/.test(ins)) ins += ' '
  const next = before + ins + right
  return { text: next, cursor: before.length + ins.length }
}

function sprintSuggestions(sprints: SprintMin[]): string[] {
  const out: string[] = ['sprint IS EMPTY']
  for (const s of sprints) {
    out.push(`sprint = ${s.id}`)
    const safe = s.name.replace(/"/g, '')
    out.push(`sprint = "${safe}"`)
  }
  return out
}

export function buildJqlSuggestionPool(
  projectKey: string,
  sprints: SprintMin[],
): string[] {
  const pool: string[] = [
    ...KEYWORDS,
    ...ORDER_SNIPPETS,
    ...WHERE_FIELDS,
    '=',
    '!=',
    '~',
    `project = "${projectKey}"`,
    'archived = false',
    'archived = true',
    'text ~ "검색어"',
    ...ISSUE_STATUSES,
    ...ISSUE_TYPES,
    ...PRIORITIES,
    ...sprintSuggestions(sprints),
  ]
  return [...new Set(pool)]
}

export function filterSuggestions(
  pool: string[],
  fragment: string,
  limit = 18,
): string[] {
  const f = fragment.trim().toLowerCase()
  if (!f) return pool.slice(0, limit)
  const hit = pool.filter((s) => {
    const l = s.toLowerCase()
    return l.startsWith(f) || l.includes(f)
  })
  return hit.slice(0, limit)
}
