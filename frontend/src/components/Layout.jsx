import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { formatRole } from '../utils/roles';
import iconLight from '../assets/icon-light.png';
import iconDark from '../assets/icon-dark.png';

const studentLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/chatbot', label: 'AI Chatbot' },
  { to: '/cv-guidance', label: 'CV Guidance' },
  { to: '/resources', label: 'Resources' },
  { to: '/interview-questions', label: 'Interview Prep' },
  { to: '/career-pathways', label: 'Career Pathways' },
  { to: '/reminders', label: 'Reminders' },
  { to: '/feedback', label: 'Feedback' },
  { to: '/settings', label: 'Settings' },
];

const staffLinks = [
  { to: '/admin/resources', label: 'Manage Resources' },
  { to: '/admin/interview-questions', label: 'Manage Interview Qs' },
  { to: '/admin/career-pathways', label: 'Manage Pathways' },
  { to: '/admin/feedback', label: 'Review Feedback' },
  { to: '/admin/analytics', label: 'Analytics' },
  { to: '/settings', label: 'Settings' },
];

const adminLinks = [
  ...staffLinks.filter((link) => link.to !== '/settings'),
  { to: '/admin/staff-accounts', label: 'Staff Accounts' },
  { to: '/settings', label: 'Settings' },
];

export default function Layout() {
  const { user, isAdmin, isStaff, logout } = useAuth();
  const links = isAdmin ? adminLinks : isStaff ? staffLinks : studentLinks;

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-brand">
          <img src={iconLight} alt="" className="app-brand-logo app-brand-logo-light" />
          <img src={iconDark} alt="" className="app-brand-logo app-brand-logo-dark" />
          <span className="app-brand-text">Student Support &amp; Career Advisor</span>
        </div>
        <div className="app-user">
          <span>
            {user?.fullName} <span className="role-badge">{formatRole(user?.role)}</span>
          </span>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Log out
          </button>
        </div>
      </header>
      <div className="app-body">
        <nav className="app-nav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => `app-nav-link${isActive ? ' active' : ''}`}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
