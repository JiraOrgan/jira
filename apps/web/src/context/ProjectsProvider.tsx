import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { errorMessage } from '../lib/axiosErrors'
import { fetchProjects } from '../lib/projectApi'
import type { ProjectMin } from '../types/domain'
import { ProjectsContext } from './projectsContext'

export function ProjectsProvider({ children }: { children: ReactNode }) {
  const [projects, setProjects] = useState<ProjectMin[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const reload = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const list = await fetchProjects()
      setProjects(list.filter((p) => !p.archived))
    } catch (e) {
      setError(errorMessage(e) || '프로젝트를 불러오지 못했습니다')
      setProjects([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void reload()
  }, [reload])

  const value = useMemo(
    () => ({ projects, loading, error, reload }),
    [projects, loading, error, reload],
  )

  return (
    <ProjectsContext.Provider value={value}>{children}</ProjectsContext.Provider>
  )
}
