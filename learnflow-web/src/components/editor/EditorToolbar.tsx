import { type Editor } from '@tiptap/react'
import { Button } from '@/components/ui/button'

interface Props {
  editor: Editor | null
}

export default function EditorToolbar({ editor }: Props) {
  if (!editor) return null

  return (
    <div className="flex flex-wrap gap-1 border-b p-2">
      <Button
        type="button" variant={editor.isActive('bold') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleBold().run()}
      >B</Button>
      <Button
        type="button" variant={editor.isActive('italic') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleItalic().run()}
      ><em>I</em></Button>
      <div className="w-px bg-border mx-1" />
      <Button
        type="button" variant={editor.isActive('heading', { level: 1 }) ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
      >H1</Button>
      <Button
        type="button" variant={editor.isActive('heading', { level: 2 }) ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
      >H2</Button>
      <Button
        type="button" variant={editor.isActive('heading', { level: 3 }) ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
      >H3</Button>
      <div className="w-px bg-border mx-1" />
      <Button
        type="button" variant={editor.isActive('bulletList') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleBulletList().run()}
      >UL</Button>
      <Button
        type="button" variant={editor.isActive('orderedList') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleOrderedList().run()}
      >OL</Button>
      <div className="w-px bg-border mx-1" />
      <Button
        type="button" variant={editor.isActive('codeBlock') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => editor.chain().focus().toggleCodeBlock().run()}
      >Code</Button>
      <Button
        type="button" variant="ghost" size="sm"
        onClick={() => {
          const url = window.prompt('이미지 URL:')
          if (url) editor.chain().focus().setImage({ src: url }).run()
        }}
      >IMG</Button>
      <Button
        type="button" variant={editor.isActive('link') ? 'secondary' : 'ghost'} size="sm"
        onClick={() => {
          const url = window.prompt('링크 URL:')
          if (url) editor.chain().focus().setLink({ href: url }).run()
          else editor.chain().focus().unsetLink().run()
        }}
      >Link</Button>
    </div>
  )
}
