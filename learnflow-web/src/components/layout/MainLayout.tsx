import { Outlet } from 'react-router-dom'
import GNB from './GNB'

export default function MainLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <GNB />
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  )
}
