import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  FiHome, FiBookOpen, FiClipboard, FiUsers, FiActivity,
  FiCheckSquare, FiBarChart2, FiLogOut, FiMenu, FiX
} from 'react-icons/fi';
import { useAuthStore } from '../store/authStore';

const ADMIN_NAV = [
  { to: '/admin', label: 'Dashboard', icon: FiHome, end: true },
  { to: '/admin/catalog', label: 'Catalog', icon: FiBookOpen },
  { to: '/admin/assessments', label: 'Assessments', icon: FiClipboard },
  { to: '/admin/onboarding', label: 'Onboarding', icon: FiUsers },
  { to: '/admin/monitoring', label: 'Monitoring', icon: FiActivity },
  { to: '/admin/evaluation', label: 'Evaluation', icon: FiCheckSquare },
  { to: '/admin/reports', label: 'Reports', icon: FiBarChart2 },
];

const CANDIDATE_NAV = [
  { to: '/candidate', label: 'My Assessments', icon: FiHome, end: true },
];

const getInitials = (label) => {
  if (!label) return 'U';
  return label
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((s) => s[0])
    .join('')
    .toUpperCase();
};

const Layout = ({ children, role }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const navItems = role === 'ADMIN' ? ADMIN_NAV : CANDIDATE_NAV;
  const displayName = user?.name || user?.email || 'User';

  const isActive = (item) =>
    item.end ? location.pathname === item.to : location.pathname.startsWith(item.to);

  const handleLogout = () => {
    logout();
    navigate(role === 'ADMIN' ? '/login' : '/candidate-login');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-white/95 backdrop-blur border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-center justify-between h-16">
            <Link to={role === 'ADMIN' ? '/admin' : '/candidate'} className="flex items-center gap-2.5 shrink-0">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                LA
              </div>
              <div className="hidden sm:block leading-tight">
                <p className="font-bold text-gray-900 text-sm tracking-tight">Language Assessment</p>
                <p className="text-[11px] text-gray-500 -mt-0.5">
                  {role === 'ADMIN' ? 'Admin Console' : 'Candidate Portal'}
                </p>
              </div>
            </Link>

            {navItems.length > 1 && (
              <nav className="hidden md:flex items-center gap-1">
                {navItems.map((item) => {
                  const Icon = item.icon;
                  const active = isActive(item);
                  return (
                    <Link
                      key={item.to}
                      to={item.to}
                      className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                        active
                          ? 'bg-indigo-50 text-indigo-700'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                    >
                      <Icon size={16} />
                      {item.label}
                    </Link>
                  );
                })}
              </nav>
            )}

            <div className="flex items-center gap-1">
              <div className="relative hidden sm:block">
                <button
                  onClick={() => setMenuOpen((o) => !o)}
                  className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-full hover:bg-gray-100 transition-colors"
                >
                  <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center text-xs font-semibold shrink-0">
                    {getInitials(displayName)}
                  </div>
                  <span className="text-sm font-medium text-gray-700 max-w-[140px] truncate">
                    {displayName}
                  </span>
                </button>

                {menuOpen && (
                  <>
                    <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
                    <div className="absolute right-0 mt-2 w-52 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-20 overflow-hidden">
                      <div className="px-4 py-3 border-b border-gray-100">
                        <p className="text-sm font-semibold text-gray-900 truncate">{displayName}</p>
                        {user?.email && (
                          <p className="text-xs text-gray-500 truncate mt-0.5">{user.email}</p>
                        )}
                      </div>
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
                      >
                        <FiLogOut size={16} />
                        Logout
                      </button>
                    </div>
                  </>
                )}
              </div>

              <button
                onClick={handleLogout}
                className="sm:hidden p-2 text-gray-500 hover:text-red-600"
                title="Logout"
              >
                <FiLogOut size={20} />
              </button>

              {navItems.length > 1 && (
                <button
                  onClick={() => setMobileOpen((o) => !o)}
                  className="md:hidden p-2 text-gray-500 hover:text-gray-900"
                  aria-label="Toggle navigation"
                >
                  {mobileOpen ? <FiX size={22} /> : <FiMenu size={22} />}
                </button>
              )}
            </div>
          </div>

          {mobileOpen && navItems.length > 1 && (
            <nav className="md:hidden pb-3 flex flex-col gap-1">
              {navItems.map((item) => {
                const Icon = item.icon;
                const active = isActive(item);
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    onClick={() => setMobileOpen(false)}
                    className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium ${
                      active ? 'bg-indigo-50 text-indigo-700' : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    <Icon size={16} />
                    {item.label}
                  </Link>
                );
              })}
            </nav>
          )}
        </div>
      </header>

      <main className="flex-1">{children}</main>
    </div>
  );
};

export default Layout;
