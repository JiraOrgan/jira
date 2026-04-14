/** PRD FR-017 피보나치 시퀀스 (Planning Poker 카드). */
export const PLANNING_POKER_VALUES = [1, 2, 3, 5, 8, 13] as const

export type PlanningPokerPoint = (typeof PLANNING_POKER_VALUES)[number]

const PREFIX = 'pch.planningPoker.vote.'

export function getPlanningPokerVote(issueKey: string): PlanningPokerPoint | null {
  try {
    const raw = localStorage.getItem(PREFIX + issueKey)
    if (raw == null) return null
    const n = Number(raw)
    return (PLANNING_POKER_VALUES as readonly number[]).includes(n)
      ? (n as PlanningPokerPoint)
      : null
  } catch {
    return null
  }
}

export function setPlanningPokerVote(
  issueKey: string,
  value: PlanningPokerPoint | null,
): void {
  const k = PREFIX + issueKey
  if (value == null) {
    localStorage.removeItem(k)
  } else {
    localStorage.setItem(k, String(value))
  }
}
