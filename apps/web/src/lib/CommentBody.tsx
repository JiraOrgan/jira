import type { ReactNode } from 'react'

const MENTION_SPLIT = /(@[^\s@]+)/g

/** 본문에서 `@토큰` 구간만 시각적으로 구분 (서버 멘션 파싱·알림은 T-405 후속). */
export function CommentBody({ text }: { text: string }): ReactNode {
  const parts = text.split(MENTION_SPLIT)
  return (
    <span className="whitespace-pre-wrap break-words">
      {parts.map((part, i) =>
        part.startsWith('@') ? (
          <span
            key={i}
            className="rounded bg-indigo-900/50 px-0.5 font-medium text-indigo-200"
          >
            {part}
          </span>
        ) : (
          <span key={i}>{part}</span>
        ),
      )}
    </span>
  )
}
