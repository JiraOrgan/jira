import {
  DndContext,
  type DragEndEvent,
  type DragStartEvent,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  useDroppable,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../../lib/axiosErrors'
import { assignSprintToIssues, reorderBacklog } from '../../lib/issueApi'
import { issueTypeLabel, priorityLabel } from '../../lib/labels'
import type { IssueMin, SprintMin } from '../../types/domain'

function issueDndId(id: number) {
  return `issue-${id}`
}

function sprintDropId(id: number) {
  return `assign-sprint-${id}`
}

function parseIssueDndId(raw: string | number): number | null {
  const s = String(raw)
  if (!s.startsWith('issue-')) return null
  const n = Number(s.slice('issue-'.length))
  return Number.isFinite(n) ? n : null
}

function parseSprintDropId(raw: string | number): number | null {
  const s = String(raw)
  if (!s.startsWith('assign-sprint-')) return null
  const n = Number(s.slice('assign-sprint-'.length))
  return Number.isFinite(n) ? n : null
}

function SortableIssueRow({
  issue,
  disabled,
}: {
  issue: IssueMin
  disabled: boolean
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: issueDndId(issue.id),
    disabled,
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={[
        'flex items-start gap-3 rounded-lg border border-slate-800 bg-slate-900/60 p-3',
        isDragging ? 'z-10 opacity-60 shadow-lg ring-2 ring-indigo-500/40' : '',
      ].join(' ')}
    >
      <button
        type="button"
        className="mt-0.5 cursor-grab touch-none text-slate-500 hover:text-slate-300 active:cursor-grabbing disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="순서 변경"
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        ⋮⋮
      </button>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <Link
            to={`/issue/${encodeURIComponent(issue.issueKey)}`}
            className="font-mono text-sm text-indigo-400 hover:text-indigo-300"
          >
            {issue.issueKey}
          </Link>
          <span className="rounded bg-slate-800 px-1.5 py-0.5 text-[10px] uppercase text-slate-400">
            {issueTypeLabel[issue.issueType]}
          </span>
          <span className="text-[10px] text-slate-500">
            {priorityLabel[issue.priority]}
          </span>
          {issue.storyPoints != null ? (
            <span className="text-[10px] text-slate-500">SP {issue.storyPoints}</span>
          ) : null}
        </div>
        <p className="mt-1 text-sm text-slate-200">{issue.summary}</p>
        <p className="mt-1 text-xs text-slate-500">
          담당: {issue.assigneeName ?? '—'}
        </p>
      </div>
    </div>
  )
}

function IssueRowPreview({ issue }: { issue: IssueMin }) {
  return (
    <div className="flex items-start gap-3 rounded-lg border border-indigo-500/50 bg-slate-900 p-3 shadow-xl">
      <span className="mt-0.5 text-slate-500">⋮⋮</span>
      <div className="min-w-0 flex-1">
        <span className="font-mono text-sm text-indigo-300">{issue.issueKey}</span>
        <p className="mt-1 text-sm text-slate-200">{issue.summary}</p>
      </div>
    </div>
  )
}

function SprintDropZone({
  sprint,
  disabled,
}: {
  sprint: SprintMin
  disabled: boolean
}) {
  const { setNodeRef, isOver } = useDroppable({
    id: sprintDropId(sprint.id),
    disabled,
  })

  return (
    <div
      ref={setNodeRef}
      className={[
        'rounded-lg border-2 border-dashed px-4 py-6 text-center text-sm transition',
        disabled ? 'border-slate-800 text-slate-600' : 'border-slate-700 text-slate-400',
        isOver && !disabled
          ? 'border-indigo-500 bg-indigo-950/30 text-indigo-200'
          : '',
      ].join(' ')}
    >
      <p className="font-medium text-slate-200">{sprint.name}</p>
      <p className="mt-1 text-xs text-slate-500">{sprint.status}</p>
      <p className="mt-3 text-xs text-slate-500">
        이슈를 여기로 끌어다 놓으면 스프린트에 배정됩니다
      </p>
    </div>
  )
}

type BacklogBoardProps = {
  projectId: number
  issues: IssueMin[]
  assignableSprints: SprintMin[]
  onReload: () => Promise<void>
}

export function BacklogBoard({
  projectId,
  issues,
  assignableSprints,
  onReload,
}: BacklogBoardProps) {
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [activeIssue, setActiveIssue] = useState<IssueMin | null>(null)

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 8 },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  )

  const sortableIds = issues.map((i) => issueDndId(i.id))

  function onDragStart(event: DragStartEvent) {
    setActionError(null)
    const id = parseIssueDndId(event.active.id)
    if (id == null) return
    const row = issues.find((i) => i.id === id)
    setActiveIssue(row ?? null)
  }

  function onDragCancel() {
    setActiveIssue(null)
  }

  async function onDragEnd(event: DragEndEvent) {
    const { active, over } = event
    setActiveIssue(null)
    if (!over || busy) return

    const activeIssueId = parseIssueDndId(active.id)
    if (activeIssueId == null) return

    const overSprintId = parseSprintDropId(over.id)
    if (overSprintId != null) {
      setBusy(true)
      setActionError(null)
      try {
        await assignSprintToIssues(projectId, {
          sprintId: overSprintId,
          issueIds: [activeIssueId],
        })
        await onReload()
      } catch (e) {
        setActionError(errorMessage(e))
      } finally {
        setBusy(false)
      }
      return
    }

    const overIssueId = parseIssueDndId(over.id)
    if (overIssueId == null) return
    if (activeIssueId === overIssueId) return

    const oldIndex = issues.findIndex((i) => i.id === activeIssueId)
    const newIndex = issues.findIndex((i) => i.id === overIssueId)
    if (oldIndex < 0 || newIndex < 0) return

    const reordered = arrayMove(issues, oldIndex, newIndex)
    const orderedIds = reordered.map((i) => i.id)

    setBusy(true)
    setActionError(null)
    try {
      await reorderBacklog(projectId, orderedIds)
      await onReload()
    } catch (e) {
      setActionError(errorMessage(e))
      await onReload()
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-6">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={onDragStart}
        onDragEnd={onDragEnd}
        onDragCancel={onDragCancel}
      >
        <div className="grid gap-8 lg:grid-cols-5">
          <div className="lg:col-span-3">
            {issues.length === 0 ? (
              <p className="rounded-lg border border-dashed border-slate-800 py-12 text-center text-sm text-slate-500">
                백로그에 이슈가 없습니다.{' '}
                <span className="text-slate-400">새 이슈를 만들어 보세요.</span>
              </p>
            ) : (
              <SortableContext
                items={sortableIds}
                strategy={verticalListSortingStrategy}
              >
                <ul className="flex flex-col gap-2">
                  {issues.map((issue) => (
                    <li key={issue.id}>
                      <SortableIssueRow issue={issue} disabled={busy} />
                    </li>
                  ))}
                </ul>
              </SortableContext>
            )}
          </div>

          <div className="lg:col-span-2">
            <h3 className="text-sm font-medium text-slate-300">스프린트 배정</h3>
            <p className="mt-1 text-xs text-slate-500">
              이슈를 드래그해 PLANNING · ACTIVE 스프린트 영역에 놓으면 배정됩니다.
            </p>
            <div className="mt-4 flex flex-col gap-3">
              {assignableSprints.length === 0 ? (
                <p className="text-sm text-slate-500">
                  배정 가능한 스프린트가 없습니다.
                </p>
              ) : (
                assignableSprints.map((s) => (
                  <SprintDropZone key={s.id} sprint={s} disabled={busy} />
                ))
              )}
            </div>
          </div>
        </div>

        <DragOverlay dropAnimation={null}>
          {activeIssue ? <IssueRowPreview issue={activeIssue} /> : null}
        </DragOverlay>
      </DndContext>

      {actionError ? (
        <div className="rounded-lg bg-red-950/40 px-4 py-2 text-sm text-red-300">
          {actionError}
        </div>
      ) : null}
      {busy ? <p className="text-xs text-slate-500">저장 중…</p> : null}
    </div>
  )
}
