import { createContext } from 'react'
import type { ProjectMin } from '../types/domain'

export type ProjectsContextValue = {
  projects: ProjectMin[]
  loading: boolean
  error: string | null
  reload: () => Promise<void>
}

export const ProjectsContext = createContext<ProjectsContextValue | null>(null)
