import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ping } from '../api/auth';

const links = [
  { to: '/authorization-check', label: 'Authorization Check', highlight: true },
  { to: '/entities/subjects', label: 'Subjects' },
  { to: '/entities/roles', label: 'Roles' },
  { to: '/entities/groups', label: 'Groups' },
  { to: '/entities/permissions', label: 'Permissions' },
  { to: '/entities/actions', label: 'Actions' },
  { to: '/entities/resources', label: 'Resources' },
];

export default function DashboardPage() {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    ping().then(setStatus).catch(() => {});
  }, []);

  return (
    <div className="page">
      <h2>Dashboard</h2>
      {status && <p className="muted">Signed in as {status.authenticatedAs}</p>}
      <div className="nav-grid">
        {links.map((l) => (
          <Link key={l.to} to={l.to} className={`nav-card ${l.highlight ? 'nav-card-highlight' : ''}`}>
            {l.label}
          </Link>
        ))}
      </div>
    </div>
  );
}
