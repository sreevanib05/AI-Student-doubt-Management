import {
  BarChart3,
  CheckCircle2,
  History,
  Inbox,
  LayoutDashboard,
  LogOut,
  Send,
  UserRound,
  Users,
} from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../services/AuthContext.jsx';

const navigation = {
  STUDENT: [
    { to: '/student', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/student/submit', label: 'Submit Doubt', icon: Send },
    { to: '/student/doubts', label: 'My Doubts', icon: History },
  ],
  MENTOR: [
    { to: '/mentor', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/mentor/assigned', label: 'Assigned Doubts', icon: Inbox },
  ],
  ADMIN: [
    { to: '/admin', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/admin/analytics', label: 'Teacher Analytics', icon: BarChart3 },
    { to: '/admin/mentors', label: 'Mentors', icon: Users },
  ],
};

export default function DashboardLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const links = navigation[user.role] || [];

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="flex min-h-screen bg-slate-50">
      <aside className="flex w-72 shrink-0 flex-col bg-sidebar px-4 py-5 text-white">
        <div className="mb-8 flex items-center gap-3 px-2">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-white text-sidebar">
            <CheckCircle2 size={22} />
          </div>
          <div>
            <p className="text-lg font-bold leading-tight">DoubtFlow AI</p>
            <p className="text-xs text-blue-100">{user.role.toLowerCase()} workspace</p>
          </div>
        </div>

        <nav className="space-y-1">
          {links.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to.split('/').length === 2}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                    isActive ? 'bg-white text-sidebar' : 'text-blue-50 hover:bg-blue-800'
                  }`
                }
              >
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="mt-auto rounded-lg bg-blue-950/35 p-4">
          <div className="mb-3 flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-white/15">
              <UserRound size={18} />
            </div>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">{user.name}</p>
              <p className="truncate text-xs text-blue-100">{user.email}</p>
            </div>
          </div>
          <button type="button" onClick={handleLogout} className="secondary-button w-full border-white/15 bg-white/10 text-white hover:bg-white hover:text-sidebar">
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1">
        <div className="mx-auto max-w-7xl px-8 py-7">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
