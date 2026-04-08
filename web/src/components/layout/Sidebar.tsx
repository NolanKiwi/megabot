import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';

const navItems = [
  { path: '/', label: 'Dashboard', icon: '~' },
  { path: '/scripts', label: 'Scripts', icon: '{ }' },
  { path: '/devices', label: 'Devices', icon: '#' },
  { path: '/logs', label: 'Logs', icon: '>' },
  { path: '/messages', label: 'Messages', icon: '@' },
  { path: '/settings', label: 'Settings', icon: '*' },
];

export default function Sidebar() {
  const { user, logout } = useAuthStore();

  return (
    <aside className="w-64 bg-slate-800 border-r border-slate-700 flex flex-col min-h-screen">
      <div className="p-6 border-b border-slate-700">
        <h1 className="text-xl font-bold text-white">_nolanbot</h1>
        <p className="text-sm text-slate-400 mt-1">Bot Dashboard</p>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'text-slate-300 hover:bg-slate-700 hover:text-white'
              }`
            }
          >
            <span className="font-mono text-xs w-6 text-center">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-slate-700">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-white">{user?.displayName}</p>
            <p className="text-xs text-slate-400">{user?.email}</p>
          </div>
          <button
            onClick={logout}
            className="text-xs text-slate-400 hover:text-red-400 transition-colors"
          >
            Logout
          </button>
        </div>
      </div>
    </aside>
  );
}
