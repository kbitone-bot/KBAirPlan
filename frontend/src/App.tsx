import { Routes, Route, Link, useLocation } from 'react-router-dom'
import { 
  LayoutDashboard, 
  Users, 
  Plane, 
  ClipboardList, 
  Calendar,
  Settings
} from 'lucide-react'
import Dashboard from './pages/Dashboard'
import Personnel from './pages/Personnel'
import Assets from './pages/Assets'
import Templates from './pages/Templates'
import DraftGenerator from './pages/DraftGenerator'
import DraftEditor from './pages/DraftEditor'
import './App.css'

function App() {
  const location = useLocation()

  const navItems = [
    { path: '/', icon: LayoutDashboard, label: '대시보드' },
    { path: '/personnel', icon: Users, label: '인원 관리' },
    { path: '/assets', icon: Plane, label: '기체 관리' },
    { path: '/templates', icon: ClipboardList, label: '임무 템플릿' },
    { path: '/drafts', icon: Calendar, label: '비행계획' },
  ]

  return (
    <div className="app">
      <nav className="sidebar">
        <div className="logo">
          <Plane size={32} />
          <span>비행계획 시스템</span>
        </div>
        <ul className="nav-list">
          {navItems.map(item => (
            <li key={item.path}>
              <Link
                to={item.path}
                className={`nav-link ${location.pathname === item.path ? 'active' : ''}`}
              >
                <item.icon size={20} />
                <span>{item.label}</span>
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/personnel" element={<Personnel />} />
          <Route path="/assets" element={<Assets />} />
          <Route path="/templates" element={<Templates />} />
          <Route path="/drafts" element={<DraftGenerator />} />
          <Route path="/drafts/:id" element={<DraftEditor />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
