import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { subjectsApi, subjectRolesApi, rolesApi } from '../api/entities';
import StatusBadge from '../components/StatusBadge';
import AssociationSection from '../components/AssociationSection';

export default function SubjectDetailPage() {
  const { id } = useParams();
  const [subject, setSubject] = useState(null);
  const [roles, setRoles] = useState([]);
  const [allRoles, setAllRoles] = useState([]);
  const [roleAssignments, setRoleAssignments] = useState([]);
  const [error, setError] = useState('');

  async function load() {
    try {
      const [s, assignments, all] = await Promise.all([
        subjectsApi.get(id),
        subjectRolesApi.assignments(id),
        rolesApi.list(),
      ]);
      setSubject(s);
      setRoleAssignments(assignments || []);
      setAllRoles(all);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleAssign(roleId) {
    await subjectRolesApi.assign(id, roleId);
    load();
  }

  async function handleToggle(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await subjectRolesApi.disable(item.assignmentId);
    else await subjectRolesApi.enable(item.assignmentId);
    load();
  }

  if (!subject) return <p>Loading...</p>;

  return (
    <div className="page">
      <h2>
        {subject.displayName} <StatusBadge status={subject.status} />
      </h2>
      <p className="muted">
        {subject.name} — {subject.subjectType}
      </p>
      <p>{subject.description}</p>

      {error && <div className="error-banner">{error}</div>}

      <AssociationSection
        title="Manage Assigned Roles"
        items={roleAssignments}
        renderLabel={(assignment) => allRoles.find((role) => role.id === assignment.roleId)?.name || assignment.roleId}
        optionsList={allRoles}
        selectLabel="Assign Role"
        onAssign={handleAssign}
        onToggle={handleToggle}
      />

    </div>
  );
}
