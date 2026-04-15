import { useContext } from 'react'
import { ProjectsContext } from '../context/projectsContext'
import type { ProjectMin } from '../types/domain'

export function useProjects() {
  const ctx = useContext(ProjectsContext)
  if (!ctx) {
    throw new Error('useProjects는 ProjectsProvider 안에서만 사용하세요')
  }
  return ctx
}

export function useProjectByKey(projectKey: string | undefined): ProjectMin | null {
  const { projects } = useProjects()
  if (!projectKey) return null
  return projects.find((p) => p.key === projectKey) ?? null
}
