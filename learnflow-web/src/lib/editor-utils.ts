import { generateHTML, generateJSON } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import CodeBlock from '@tiptap/extension-code-block'
import Image from '@tiptap/extension-image'
import Link from '@tiptap/extension-link'

const extensions = [
  StarterKit.configure({ codeBlock: false }),
  CodeBlock,
  Image,
  Link,
]

export function htmlToJson(html: string) {
  return generateJSON(html, extensions)
}

export function jsonToHtml(json: Record<string, unknown>) {
  return generateHTML(json, extensions)
}
