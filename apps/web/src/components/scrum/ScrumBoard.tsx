import {
  DndContext,
  type DragEndEvent,
  type DragStartEvent,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  closestCorners,
  useDroppable,
  useDraggable,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../../lib/axiosErrors'
import { fetchSprintBoard } from '../../lib/boardApi'
import { transitionIssue } from '../../lib/issueApi'
import { issueTypeLabel, priorityLabel, statusLabel } from '../../lib/labels'
import { ISSUE_STATUS_ORDER, allowedNextStatuses } from '../../lib/workflow'
import type {
  BoardSwimlane,
  IssueMin,
  IssueStatus,
  SprintBoardData,
} from '../../types/domain'

const BOARD_ISSUE_PREFIX = 'board-issue-'

function boardIssueDndId(issueKey: string) {
  return `${BOARD_ISSUE_PREFIX}${issueKey}`
}

function parseBoardIssueDndId(raw: string | number): string | null {
  const s = String(raw)
  if (!s.startsWith(BOARD_ISSUE_PREFIX)) return null
  return s.slice(BOARD_ISSUE_PREFIX.length) || null
}

const COL_PREFIX = 'board-col-'

function columnDroppableId(status: IssueStatus) {
  return `${COL_PREFIX}${status}`
}

function parseColumnDroppableId(raw: string | number): IssueStatus | null {
  const s = String(raw)
  if (!s.startsWith(COL_PREFIX)) return null
  const st = s.slice(COL_PREFIX.length) as IssueStatus
  return ISSUE_STATUS_ORDER.includes(st) ? st : null
}

function findIssueInBoard(
  board: SprintBoardData,
  issueKey: string,
): { issue: IssueMin; status: IssueStatus } | null {
  for (const col of board.columns) {
    for (const b of col.buckets) {
      const hit = b.issues.find((i) => i.issueKey === issueKey)
      if (hit) return { issue: hit, status: col.status }
    }
  }
  return null
}

function BoardIssueCard({
  issue,
  disabled,
}: {
  issue: IssueMin
  disabled: boolean
}) {
  const cardDisabled = disabled || issue.archived
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({
      id: boardIssueDndId(issue.issueKey),
      disabled: cardDisabled,
    })

  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
      }
    : undefined

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={[
        'rounded-lg border border-slate-800 bg-slate-900/80 p-2 shadow-sm',
        issue.archived ? 'opacity-80' : '',
        isDragging ? 'opacity-50 ring-2 ring-indigo-500/30' : '',
      ].join(' ')}
    >
      <div className="flex gap-2">
        <button
          type="button"
          className="shrink-0 cursor-grab touch-none text-slate-500 hover:text-slate-300 active:cursor-grabbing disabled:cursor-not-allowed disabled:opacity-40"
          aria-label="보드로 이동"
          disabled={cardDisabled}
          {...listeners}
          {...attributes}
        >
          ⋮⋮
        </button>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-1">
            <Link
              to={`/issue/${encodeURIComponent(issue.issueKey)}`}
              className="font-mono text-xs text-indigo-400 hover:text-indigo-300"
              onClick={(e) => e.stopPropagation()}
            >
              {issue.issueKey}
            </Link>
            <span className="rounded bg-slate-800 px-1 py-0.5 text-[9px] uppercase text-slate-500">
              {issueTypeLabel[issue.issueType]}
            </span>
            {issue.archived ? (
              <span className="rounded border border-amber-800/50 bg-amber-950/40 px-1 py-0.5 text-[9px] text-amber-200">
                아카이브
              </span>
            ) : null}
          </div>
          <p className="mt-1 line-clamp-2 text-xs text-slate-200">
            {issue.summary}
          </p>
          <p className="mt-1 text-[10px] text-slate-500">
            {priorityLabel[issue.priority]}
            {issue.assigneeName ? ` · ${issue.assigneeName}` : ''}
          </p>
        </div>
      </div>
    </div>
  )
}

function CardPreview({ issue }: { issue: IssueMin }) {
  return (
    <div className="w-64 rounded-lg border border-indigo-500/40 bg-slate-900 p-2 shadow-xl">
      <div className="flex flex-wrap items-center gap-1">
        <span className="font-mono text-xs text-indigo-300">{issue.issueKey}</span>
        {issue.archived ? (
          <span className="rounded border border-amber-800/50 bg-amber-950/40 px-1 py-0.5 text-[9px] text-amber-200">
            아카이브
          </span>
        ) : null}
      </div>
      <p className="mt-1 line-clamp-2 text-xs text-slate-200">{issue.summary}</p>
    </div>
  )
}

function BoardColumn({
  status,
  issues,
  dragSourceStatus,
  disabled,
}: {
  status: IssueStatus
  issues: IssueMin[]
  dragSourceStatus: IssueStatus | null
  disabled: boolean
}) {
  const droppableId = columnDroppableId(status)
  const allowed =
    dragSourceStatus === null
      ? true
      : dragSourceStatus === status ||
        allowedNextStatuses(dragSourceStatus).includes(status)

  const { setNodeRef, isOver } = useDroppable({
    id: droppableId,
    disabled: disabled || (dragSourceStatus !== null && !allowed),
  })

  return (
    <div className="flex w-72 shrink-0 flex-col">
      <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
        {statusLabel[status]}
        <span className="ml-2 font-normal text-slate-600">({issues.length})</span>
      </h3>
      <div
        ref={setNodeRef}
        className={[
          'flex min-h-[200px] flex-1 flex-col gap-2 rounded-xl border-2 border-dashed p-2 transition',
          disabled ? 'border-slate-800 bg-slate-950/30' : 'border-slate-800 bg-slate-950/50',
          isOver && allowed && !disabled
            ? 'border-indigo-500 bg-indigo-950/20'
            : '',
          dragSourceStatus !== null && !allowed && !disabled
            ? 'opacity-40'
            : '',
        ].join(' ')}
      >
        {issues.length === 0 ? (
          <p className="py-6 text-center text-[11px] text-slate-600">빈 칸</p>
        ) : (
          issues.map((issue) => (
            <BoardIssueCard key={issue.issueKey} issue={issue} disabled={disabled} />
          ))
        )}
      </div>
    </div>
  )
}

type ScrumBoardProps = {
  sprintId: number
  swimlane: BoardSwimlane
  board: SprintBoardData
  onBoardChange: (next: SprintBoardData) => void
}

export function ScrumBoard({
  sprintId,
  swimlane,
  board,
  onBoardChange,
}: ScrumBoardProps) {
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [activeIssue, setActiveIssue] = useState<IssueMin | null>(null)
  const [dragSourceStatus, setDragSourceStatus] = useState<IssueStatus | null>(
    null,
  )

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  )

  const columnIssues = new Map<IssueStatus, IssueMin[]>()
  for (const st of ISSUE_STATUS_ORDER) {
    columnIssues.set(st, [])
  }
  for (const col of board.columns) {
    const merged: IssueMin[] = []
    for (const b of col.buckets) {
      merged.push(...b.issues)
    }
    columnIssues.set(col.status, merged)
  }

  function onDragStart(event: DragStartEvent) {
    setActionError(null)
    const key = parseBoardIssueDndId(event.active.id)
    if (!key) return
    const found = findIssueInBoard(board, key)
    if (found) {
      setActiveIssue(found.issue)
      setDragSourceStatus(found.status)
    }
  }

  function resetDragVisual() {
    setActiveIssue(null)
    setDragSourceStatus(null)
  }

  async function onDragEnd(event: DragEndEvent) {
    const { over } = event
    const key = parseBoardIssueDndId(event.active.id)
    resetDragVisual()
    if (!key || !over || busy) return

    const targetStatus = parseColumnDroppableId(over.id)
    if (!targetStatus) return

    const from = findIssueInBoard(board, key)?.status
    if (from == null || from === targetStatus) return

    if (!allowedNextStatuses(from).includes(targetStatus)) {
      setActionError('허용되지 않는 상태 전환입니다')
      return
    }

    setBusy(true)
    setActionError(null)
    try {
      await transitionIssue(key, { toStatus: targetStatus })
      const next = await fetchSprintBoard(sprintId, swimlane)
      onBoardChange(next)
    } catch (e) {
      setActionError(errorMessage(e))
      try {
        const next = await fetchSprintBoard(sprintId, swimlane)
        onBoardChange(next)
      } catch {
        /* ignore */
      }
    } finally {
      setBusy(false)
    }
  }

  function onDragCancel() {
    resetDragVisual()
  }

  return (
    <div className="space-y-4">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={onDragStart}
        onDragEnd={onDragEnd}
        onDragCancel={onDragCancel}
      >
        <div className="flex gap-4 overflow-x-auto pb-4">
          {ISSUE_STATUS_ORDER.map((status) => (
            <BoardColumn
              key={status}
              status={status}
              issues={columnIssues.get(status) ?? []}
              dragSourceStatus={dragSourceStatus}
              disabled={busy}
            />
          ))}
        </div>
        <DragOverlay dropAnimation={null}>
          {activeIssue ? <CardPreview issue={activeIssue} /> : null}
        </DragOverlay>
      </DndContext>

      {actionError ? (
        <p className="text-sm text-red-300">{actionError}</p>
      ) : null}
      {busy ? <p className="text-xs text-slate-500">저장 중…</p> : null}
    </div>
  )
}
