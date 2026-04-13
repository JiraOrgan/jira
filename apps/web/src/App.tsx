import type { ReactNode } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { ProjectsProvider } from './context/ProjectsProvider'
import { AppLayout } from './layout/AppLayout'
import { BacklogPage } from './pages/BacklogPage'
import { DashboardDetailPage } from './pages/DashboardDetailPage'
import { HomePage } from './pages/HomePage'
import { IssueCreatePage } from './pages/IssueCreatePage'
import { IssueDetailPage } from './pages/IssueDetailPage'
import { JqlSearchPage } from './pages/JqlSearchPage'
import { KanbanPage } from './pages/KanbanPage'
import { LoginPage } from './pages/LoginPage'
import { ProjectHomePage } from './pages/ProjectHomePage'
import { ProjectSettingsPage } from './pages/ProjectSettingsPage'
import { RoadmapPage } from './pages/RoadmapPage'
import { ScrumBoardPage } from './pages/ScrumBoardPage'
import { SprintsPage } from './pages/SprintsPage'
import { useAuthStore } from './stores/authStore'

function RequireAuth({ children }: { children: ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken)
  const location = useLocation()

  if (!accessToken) {
    return (
      <Navigate to="/login" replace state={{ from: location.pathname }} />
    )
  }

  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <ProjectsProvider>
              <AppLayout />
            </ProjectsProvider>
          </RequireAuth>
        }
      >
        <Route index element={<HomePage />} />
        <Route
          path="dashboard/:dashboardId"
          element={<DashboardDetailPage />}
        />
        <Route
          path="project/:projectKey/board"
          element={<ScrumBoardPage />}
        />
        <Route
          path="project/:projectKey/kanban"
          element={<KanbanPage />}
        />
        <Route
          path="project/:projectKey/sprints"
          element={<SprintsPage />}
        />
        <Route
          path="project/:projectKey/backlog"
          element={<BacklogPage />}
        />
        <Route
          path="project/:projectKey/jql"
          element={<JqlSearchPage />}
        />
        <Route
          path="project/:projectKey/roadmap"
          element={<RoadmapPage />}
        />
        <Route
          path="project/:projectKey/issues/new"
          element={<IssueCreatePage />}
        />
        <Route
          path="project/:projectKey/settings"
          element={<ProjectSettingsPage />}
        />
        <Route path="project/:projectKey" element={<ProjectHomePage />} />
        <Route path="issue/:issueKey" element={<IssueDetailPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
